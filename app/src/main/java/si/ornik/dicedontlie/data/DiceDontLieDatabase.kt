package si.ornik.dicedontlie.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for Dice Don't Lie application.
 */
@Database(
    entities = [GameEntity::class, DieRollEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DiceDontLieDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun dieRollDao(): DieRollDao

    companion object {
        private const val DATABASE_NAME = "dice_dont_lie_database"

        @Volatile
        private var INSTANCE: DiceDontLieDatabase? = null

        fun getInstance(context: Context): DiceDontLieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiceDontLieDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
