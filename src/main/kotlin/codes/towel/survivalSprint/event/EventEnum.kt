package codes.towel.survivalSprint.event

import codes.towel.survivalSprint.Effect
import kotlin.reflect.KClass

enum class EventEnum(val clazz: KClass<out Effect>) {
}