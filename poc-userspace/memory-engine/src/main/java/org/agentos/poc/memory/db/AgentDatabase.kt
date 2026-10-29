package org.agentos.poc.memory.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        EpisodicSnapshotEntity::class,
        EpisodicFtsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AgentDatabase : RoomDatabase() {

    abstract fun episodicDao(): EpisodicDao

    companion object {
        @Volatile
        private var INSTANCE: AgentDatabase? = null

        fun getInstance(context: Context): AgentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgentDatabase::class.java,
                    "agent_memory.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

