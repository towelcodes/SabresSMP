package codes.towel.survivalSprint

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class CustomItemType {
    SMOKE_SABRE,
    SHADE_SABRE,
    SNOW_SABRE,
    STORM_SABRE;

    fun getItem() : ItemStack {
        return map[this]!!
    }
}

@Suppress("UnstableApiUsage")
val map = mapOf(
    CustomItemType.SMOKE_SABRE to ItemStack(Material.NETHERITE_SWORD).apply {
        itemMeta.apply {
            setCustomModelDataComponent(customModelDataComponent.apply {
                strings = listOf("smoke_sabre")
            })
            displayName(Component.text("Smoke Sabre"))
            setRarity(ItemRarity.RARE)
            setEnchantmentGlintOverride(true)
            addEnchantment(Enchantment.FIRE_ASPECT, 2)
            addAttributeModifier(
                Attribute.ATTACK_DAMAGE,
                AttributeModifier(NamespacedKey("ss", "smoke_sabre"), 4.0, AttributeModifier.Operation.ADD_NUMBER)
            )
            addAttributeModifier(
                Attribute.ATTACK_SPEED,
                AttributeModifier(NamespacedKey("ss", "smoke_sabre"), -2.4, AttributeModifier.Operation.ADD_NUMBER)
            )
            lore(listOf(
                Component.text("skibidi sigmaj"),
            ))
            val effects = listOf("FireChargeInteract")
            persistentDataContainer.set(NamespacedKey("ss", "equip_effects"), PersistentDataType.LIST.strings(), effects)
            persistentDataContainer.set(NamespacedKey("ss", "cooldown_time"), PersistentDataType.INTEGER, 20)
        }
    }
)