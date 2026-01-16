package si.ornik.dicedontlie.data

import android.content.Context

/**
 * Database provider for accessing Room database and DAOs.
 * This is a simple dependency injection container.
 */
object DatabaseProvider {

    private var database: DiceDontLieDatabase? = null

    /**
     * Initialize the database provider with application context.
     * Should be called from Application.onCreate()
     */
    fun initialize(context: Context) {
        database = DiceDontLieDatabase.getInstance(context)
    }

    /**
     * Get the database instance.
     * @throws IllegalStateException if initialize() hasn't been called
     */
    fun getDatabase(): DiceDontLieDatabase {
        return database ?: throw IllegalStateException(
            "DatabaseProvider has not been initialized. Call initialize() in Application.onCreate()"
        )
    }

    /**
     * Get the GameDao.
     */
    fun getGameDao(): GameDao {
        return getDatabase().gameDao()
    }

    /**
     * Get the DieRollDao.
     */
    fun getDieRollDao(): DieRollDao {
        return getDatabase().dieRollDao()
    }
}
