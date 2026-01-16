package si.ornik.dicedontlie.data

import java.util.UUID

data class Game(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val players: List<String>, // 1-6 players
    val eventDieEnabled: Boolean = false,
    val startTime: Long = System.currentTimeMillis(), // Timestamp for when the game started
    var endTime: Long? = null, // Timestamp for when the game ended, nullable
    val rolls: List<DieRoll> // List of DieRolls (all rolls in the game)
)