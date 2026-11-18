package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        WeaponEntity::class,
        CamoEntity::class,
        ChallengeEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class GrindDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun weaponDao(): WeaponDao
    abstract fun camoDao(): CamoDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: GrindDatabase? = null

        fun getInstance(context: Context): GrindDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): GrindDatabase {
            return Room.databaseBuilder(
                context,
                GrindDatabase::class.java,
                "codm_grind_tracker.db",
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.categoryDao()?.insertAll(defaultCategories())
                        }
                    }
                })
                .build()
        }

        fun defaultCategories(now: Long = System.currentTimeMillis()): List<CategoryEntity> = listOf(
            CategoryEntity("cat_ar", "Assault Rifles", "assault", false, 0, now, now),
            CategoryEntity("cat_smg", "SMGs", "smg", false, 1, now, now),
            CategoryEntity("cat_sniper", "Sniper Rifles", "sniper", false, 2, now, now),
            CategoryEntity("cat_lmg", "LMGs", "lmg", false, 3, now, now),
            CategoryEntity("cat_shotgun", "Shotguns", "shotgun", false, 4, now, now),
            CategoryEntity("cat_marksman", "Marksman", "marksman", false, 5, now, now),
            CategoryEntity("cat_pistol", "Pistols", "pistol", false, 6, now, now),
            CategoryEntity("cat_launcher", "Launchers", "launcher", false, 7, now, now),
            CategoryEntity("cat_melee", "Melee", "melee", false, 8, now, now),
        )
    }
}
