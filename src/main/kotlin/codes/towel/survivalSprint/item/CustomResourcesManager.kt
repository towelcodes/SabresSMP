package codes.towel.survivalSprint.item

import codes.towel.survivalSprint.SurvivalSprint
import codes.towel.survivalSprint.effect.GlobalEffectManager
import java.io.File

/**
 * we don't always want to load custom resources
 * so this handles everything to do with that
 */
class CustomResourcesManager(val plugin: SurvivalSprint) {
    private val logger = plugin.logger
    val effectManager: GlobalEffectManager

    init {
        // initialize the effect manager
        val effectDataFile = File(plugin.dataFolder, "effect.yml")
        effectDataFile.createNewFile()
        effectDataFile.reader().use { logger.info(it.readText()) }
        effectManager = GlobalEffectManager(plugin, effectDataFile)
        plugin.server.pluginManager.registerEvents(effectManager, plugin)
    }

    fun disable() {
        effectManager.save()
    }
}