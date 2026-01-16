package si.ornik.dicedontlie.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing die roll data in the local database.
 */
@Entity(
    tableName = "die_rolls",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gameId"])]
)
data class DieRollEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: String,
    val redDie: Int, // Value of the red die (1-6)
    val yellowDie: Int, // Value of the yellow die (1-6)
    val eventDie: EventDie? = null, // Value of the Cities and Knights event die
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Extension function to convert DieRollEntity to DieRoll domain model.
 */
fun DieRollEntity.toDieRoll(): DieRoll {
    return DieRoll(
        redDie = this.redDie,
        yellowDie = this.yellowDie,
        eventDie = this.eventDie,
        timestamp = this.timestamp
    )
}

/**
 * Extension function to convert DieRoll domain model to DieRollEntity.
 */
fun DieRoll.toEntity(gameId: String): DieRollEntity {
    return DieRollEntity(
        gameId = gameId,
        redDie = this.redDie,
        yellowDie = this.yellowDie,
        eventDie = this.eventDie,
        timestamp = this.timestamp
    )
}
