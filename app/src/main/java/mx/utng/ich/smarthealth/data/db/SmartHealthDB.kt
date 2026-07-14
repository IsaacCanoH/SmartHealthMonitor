package mx.utng.ich.smarthealth.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [LecturaFC::class],
    version = 2,
    exportSchema = false
)
abstract class SmartHealthDB : RoomDatabase() {

    abstract fun lecturaDao(): LecturaFCDao

    companion object {
        @Volatile
        private var INSTANCE: SmartHealthDB? = null

        fun getDatabase(context: Context): SmartHealthDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartHealthDB::class.java,
                    "smarthealthmonitor_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE lecturas_fc ADD COLUMN estado TEXT NOT NULL DEFAULT 'Normal'")
                db.execSQL("ALTER TABLE lecturas_fc ADD COLUMN dispositivo TEXT NOT NULL DEFAULT 'app'")
                db.execSQL("ALTER TABLE lecturas_fc ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE lecturas_fc ADD COLUMN neonId INTEGER")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_lecturas_fc_neonId " +
                        "ON lecturas_fc(neonId)"
                )
            }
        }
    }
}
