package codes.towel.survivalSprint.event

import codes.towel.survivalSprint.Event
import kotlin.reflect.KClass

enum class EventEnum(val clazz: KClass<out Event>) {
}