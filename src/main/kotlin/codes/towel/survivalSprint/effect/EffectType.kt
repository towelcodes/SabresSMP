package codes.towel.survivalSprint.effect

import kotlin.reflect.KClass

enum class EffectType(val clazz: KClass<out Effect>) {
    TotemCooldown(TotemCooldownEffect::class)
}