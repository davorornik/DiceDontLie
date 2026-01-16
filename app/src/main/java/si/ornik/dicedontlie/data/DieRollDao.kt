package si.ornik.dicedontlie.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for DieRoll entities.
 */
@Dao
interface DieRollDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDieRoll(dieRoll: DieRollEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDieRolls(dieRolls: List<DieRollEntity>)

    @Delete
    suspend fun deleteDieRoll(dieRoll: DieRollEntity)

    @Query("SELECT * FROM die_rolls WHERE gameId = :gameId ORDER BY timestamp ASC")
    fun getRollsForGame(gameId: String): Flow<List<DieRollEntity>>

    @Query("SELECT * FROM die_rolls WHERE gameId = :gameId ORDER BY timestamp ASC")
    suspend fun getRollsForGameSync(gameId: String): List<DieRollEntity>

    @Query("SELECT * FROM die_rolls WHERE gameId = :gameId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRollForGame(gameId: String): DieRollEntity?

    @Query("SELECT COUNT(*) FROM die_rolls WHERE gameId = :gameId")
    suspend fun getRollCountForGame(gameId: String): Int

    @Query("SELECT COUNT(*) FROM die_rolls WHERE gameId = :gameId")
    fun getRollCountForGameFlow(gameId: String): Flow<Int>

    @Query("DELETE FROM die_rolls WHERE gameId = :gameId")
    suspend fun deleteRollsForGame(gameId: String)

    @Query("DELETE FROM die_rolls WHERE id = :id")
    suspend fun deleteDieRollById(id: Long)

    @Update
    suspend fun updateDieRoll(dieRoll: DieRollEntity)

    @Query("SELECT * FROM die_rolls WHERE id = :id")
    suspend fun getRollById(id: Long): DieRollEntity?

    @Query("SELECT * FROM die_rolls ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRolls(limit: Int): Flow<List<DieRollEntity>>

    @Query("SELECT SUM(redDie + yellowDie) FROM die_rolls WHERE gameId = :gameId")
    suspend fun getTotalSumForGame(gameId: String): Int?

    /**
     * Get roll distribution counts directly from database.
     * Returns a list of 11 counts for sums 2 through 12.
     * Much faster than loading all rolls and aggregating in memory.
     */
    @Query("""
        SELECT COUNT(*) FROM die_rolls WHERE gameId = :gameId AND (redDie + yellowDie) = :sum
    """)
    suspend fun getRollCountForSum(gameId: String, sum: Int): Int

    /**
     * Get total roll count for a game.
     */
    @Query("SELECT COUNT(*) FROM die_rolls WHERE gameId = :gameId")
    suspend fun getRollCountForGameSync(gameId: String): Int

    /**
     * Get roll distribution aggregated by sum and event die type.
     * Returns counts grouped by (redDie + yellowDie) and eventDie.
     * Much faster than loading all rolls and aggregating in memory.
     */
    @Query("""
        SELECT (redDie + yellowDie) as sum, eventDie, COUNT(*) as count
        FROM die_rolls
        WHERE gameId = :gameId
        GROUP BY (redDie + yellowDie), eventDie
        ORDER BY sum ASC
    """)
    fun getRollDistributionCitiesAndKnights(gameId: String): Flow<List<RollDistribution>>

    /**
     * Get roll distribution aggregated by sum only (without event die).
     * Used for regular games without an event die.
     * Returns counts grouped by (redDie + yellowDie).
     */
    @Query("""
        SELECT (redDie + yellowDie) as sum, COUNT(*) as count
        FROM die_rolls
        WHERE gameId = :gameId
        GROUP BY (redDie + yellowDie)
        ORDER BY sum ASC
    """)
    fun getRollDistribution(gameId: String): Flow<List<RollDistributionSimple>>

    /**
     * Get roll distribution with actual percentages for fairness analysis.
     * Returns the count and calculated percentage for each sum.
     */
    @Query("""
        SELECT 
            (redDie + yellowDie) as sum,
            COUNT(*) as count,
            CAST(COUNT(*) AS FLOAT) * 100 / (
                SELECT CAST(COUNT(*) AS FLOAT) FROM die_rolls WHERE gameId = :gameId
            ) as percentage
        FROM die_rolls
        WHERE gameId = :gameId
        GROUP BY (redDie + yellowDie)
        ORDER BY sum ASC
    """)
    fun getRollDistributionWithPercentage(gameId: String): Flow<List<RollDistributionWithPercentage>>

    /**
     * Get the most frequent rolls (hot numbers) - returns counts ordered by frequency descending.
     */
    @Query("""
        SELECT 
            (redDie + yellowDie) as sum,
            COUNT(*) as count,
            CAST(COUNT(*) AS FLOAT) * 100 / (
                SELECT CAST(COUNT(*) AS FLOAT) FROM die_rolls WHERE gameId = :gameId
            ) as percentage
        FROM die_rolls
        WHERE gameId = :gameId
        GROUP BY (redDie + yellowDie)
        ORDER BY count DESC
        LIMIT 1
    """)
    suspend fun getHotNumber(gameId: String): NumberFrequency?

    /**
     * Get the least frequent rolls (cold numbers) - returns counts ordered by frequency ascending.
     */
    @Query("""
        SELECT 
            (redDie + yellowDie) as sum,
            COUNT(*) as count,
            CAST(COUNT(*) AS FLOAT) * 100 / (
                SELECT CAST(COUNT(*) AS FLOAT) FROM die_rolls WHERE gameId = :gameId
            ) as percentage
        FROM die_rolls
        WHERE gameId = :gameId
        GROUP BY (redDie + yellowDie)
        ORDER BY count ASC
        LIMIT 1
    """)
    suspend fun getColdNumber(gameId: String): NumberFrequency?

    /**
     * Get the top 3 most frequent rolls (hot numbers).
     */
    @Query("""
        SELECT 
            (redDie + yellowDie) as sum,
            COUNT(*) as count,
            CAST(COUNT(*) AS FLOAT) * 100 / (
                SELECT CAST(COUNT(*) AS FLOAT) FROM die_rolls WHERE gameId = :gameId
            ) as percentage
        FROM die_rolls
        WHERE gameId = :gameId
        GROUP BY (redDie + yellowDie)
        ORDER BY count DESC
        LIMIT 3
    """)
    suspend fun getHotNumbers(gameId: String): List<NumberFrequency>

    /**
     * Get the top 3 least frequent rolls (cold numbers).
     */
    @Query("""
        SELECT 
            (redDie + yellowDie) as sum,
            COUNT(*) as count,
            CAST(COUNT(*) AS FLOAT) * 100 / (
                SELECT CAST(COUNT(*) AS FLOAT) FROM die_rolls WHERE gameId = :gameId
            ) as percentage
        FROM die_rolls
        WHERE gameId = :gameId
        GROUP BY (redDie + yellowDie)
        ORDER BY count ASC
        LIMIT 3
    """)
    suspend fun getColdNumbers(gameId: String): List<NumberFrequency>

    /**
     * Get the longest streak of consecutive rolls with the same sum.
     * Returns a Pair of (sum, streakLength).
     */
    @Query("""
        WITH RankedRolls AS (
            SELECT
                (redDie + yellowDie) as sum,
                timestamp,
                LAG((redDie + yellowDie), 1) OVER (ORDER BY timestamp) as prev_sum,
                LAG(timestamp, 1) OVER (ORDER BY timestamp) as prev_timestamp
            FROM die_rolls
            WHERE gameId = :gameId
        ),
        Streaks AS (
            SELECT
                sum,
                CASE WHEN sum = prev_sum THEN 0 ELSE ROW_NUMBER() OVER (ORDER BY timestamp) END as streak_group
            FROM RankedRolls
        )
        SELECT
            sum,
            COUNT(*) as streak_length
        FROM Streaks
        GROUP BY sum, streak_group
        ORDER BY streak_length DESC
        LIMIT 1
    """)
    suspend fun getLongestStreak(gameId: String): LongestStreak?

    /**
     * Calculate the average time between rolls in milliseconds.
     */
    @Query("""
        SELECT
            AVG(timestamp - prev_timestamp) as avg_time_diff
        FROM (
            SELECT
                timestamp,
                LAG(timestamp, 1) OVER (ORDER BY timestamp) as prev_timestamp
            FROM die_rolls
            WHERE gameId = :gameId
        )
        WHERE prev_timestamp IS NOT NULL
    """)
    suspend fun getAverageTimeBetweenRolls(gameId: String): Double?

    /**
     * Data class for longest streak result.
     */
    data class LongestStreak(
        val sum: Int,
        val streak_length: Int
    )
}
