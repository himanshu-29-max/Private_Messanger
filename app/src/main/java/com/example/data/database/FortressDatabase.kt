package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalProfile::class,
        RoomEntity::class,
        RoomMemberEntity::class,
        MessageEntity::class,
        ContactEntity::class,
        CallLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FortressDatabase : RoomDatabase() {

    abstract fun fortressDao(): FortressDao

    companion object {
        @Volatile
        private var INSTANCE: FortressDatabase? = null

        fun getDatabase(context: Context): FortressDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FortressDatabase::class.java,
                    "fortress_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
