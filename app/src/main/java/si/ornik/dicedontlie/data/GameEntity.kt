package si.ornik.dicedontlie.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.UUID

/**
 * Room entity for storing game data in the local database.
 */
@Entity(tableName = "games")
@TypeConverters(Converters::class)
data class GameEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val players: List<String>, // 1-6 players
    val eventDieEnabled: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    var endTime: Long? = null
)

/**
 * Extension function to convert GameEntity to Game domain model.
 */
fun GameEntity.toGame(rolls: List<DieRoll>): Game {
    return Game(
        id = UUID.fromString(this.id),
        name = this.name,
        players = this.players,
        eventDieEnabled = this.eventDieEnabled,
        startTime = this.startTime,
        endTime = this.endTime,
        rolls = rolls
    )
}
