package si.ornik.dicedontlie.data

import androidx.room.TypeConverter

/**
 * Type converters for Room database to handle complex types.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split("|||")
    }

    @TypeConverter
    fun fromEventDie(eventDie: EventDie?): String? {
        return eventDie?.name
    }

    @TypeConverter
    fun toEventDie(value: String?): EventDie? {
        if (value == null) return null
        // Handle backward compatibility with old color-based names
        return when (value.uppercase()) {
            "BLUE" -> EventDie.POLITICS
            "GREEN" -> EventDie.SCIENCE
            "YELLOW" -> EventDie.TRADE
            "BLACK" -> EventDie.PIRATES
            else -> EventDie.valueOf(value)
        }
    }
}
