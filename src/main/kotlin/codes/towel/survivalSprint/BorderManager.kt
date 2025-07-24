package codes.towel.survivalSprint

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.WorldBorder
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

class BorderManager(private val plugin: SurvivalSprint, private val config: ServerConfiguration, private val state: ServerState, private val lang: FileConfiguration) {
    private val logger = LoggerFactory.getLogger("SS.BorderManager")
    private var worldBorder: WorldBorder
    private var netherBorder: WorldBorder
    private var updateTask: BukkitTask? = null

    init {
        // will throw an error if either worlds don't exist
        val world = Bukkit.getWorld(config.primaryWorld)!!
        val nether = Bukkit.getWorld(config.netherWorld)!!

        worldBorder = world.worldBorder
        netherBorder = nether.worldBorder

        worldBorder.apply {
            size = state.borderSize
            damageAmount = 2.0
            damageBuffer = 2.0
            warningTime = 0
            warningDistance = config.warningDistance
        }
        netherBorder.apply {
            size = state.borderSize / 4 // TODO test
            damageAmount = 2.0
            damageBuffer = 2.0
            warningTime = 0
            warningDistance = config.warningDistance
        }

        // set up the border movement
        if (state.borderMoving) {
            enableBorderMovement()
        }
    }

    fun enableBorderMovement() {
        logger.info("Starting border movement")
        updateTask = object : BukkitRunnable() {
            override fun run() {
                update()
            }
        }.runTaskTimer(plugin, 20L, 20L)
        state.borderMoving = true
    }

    fun stopBorderMovement() {
        logger.info("Stopping border movement")
        updateTask?.cancel()
        state.borderMoving = false
    }

    /**
     * should only be called on 0 seconds
     * assumes the Instant passed is the current time
     * return true if the day has changed
     */
    fun calcDay(date: ZonedDateTime): Boolean {
        val totalDays = config.totalDays
        val now = System.currentTimeMillis() / 1000
        val actualDay = (now - config.startTime) / 24000

        if (date.hour != config.dayChangeoverHour) {
            return false
        }

        if (state.day >= actualDay) {
            return false
        }

        if (state.day >= totalDays) {
            return false
        }

        logger.info("Started day ${state.day}")
        state.day++
        changeDay(state.day)

        return true
    }

    /**
     * Inform players of day change
     */
    private fun changeDay(day: Int) {
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(lang.getString("border.new_day")?: "Day %day% has started.".replace("%day%", day.toString())))
        for (player in Bukkit.getOnlinePlayers()) {
            player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f)
        }

        if (day == config.totalDays) {
            logger.info("Final day reached, stopping automatic border movement")
            state.borderMoving = false
        }
    }

    fun setBorderTarget(target: Int) {
        logger.info("setting border target to $target")
        state.borderTarget = target
    }

    private var lastHour: Int? = null
    /**
     * should be called each second (20 server ticks)
     */
    private fun update() {
        logger.info("updating border")
        if (state.borderMoving) {
            logger.info("moving border")
            val perDay = config.borderShrink
            val perSecond = perDay / (24.0 * 60.0 * 60.0)
            logger.info(perDay.toString())
            logger.info(perSecond.toString())
            worldBorder.size -= perSecond
            netherBorder.size -= (perSecond * 8.0)
            state.borderSize = worldBorder.size
            logger.info(worldBorder.size.toString())
        }

        if (state.borderTarget > state.borderSize) {
            logger.info("target reached, stopping")
            stopBorderMovement()
        }

        if (lastHour != null && lastHour != ZonedDateTime.now().hour) {
            logger.info("changing day")
            calcDay(ZonedDateTime.now())
        }
        lastHour = ZonedDateTime.now().hour
    }
}