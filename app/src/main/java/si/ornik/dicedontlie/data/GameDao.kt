package si.ornik.dicedontlie.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Game entities.
 */
@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: String): GameEntity?

    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameByIdFlow(gameId: String): Flow<GameEntity?>

    @Query("SELECT * FROM games ORDER BY startTime DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games ORDER BY startTime DESC LIMIT :limit")
    fun getRecentGames(limit: Int): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveGame(): GameEntity?

    @Query("SELECT * FROM games WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveGameFlow(): Flow<GameEntity?>

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGameById(gameId: String)

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int
}
