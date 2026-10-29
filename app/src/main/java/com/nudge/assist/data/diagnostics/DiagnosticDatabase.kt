package com.nudge.assist.data.diagnostics

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Embedded Room Database for AssistX local diagnostics.
 */
@Database(entities = [DiagnosticEventEntity::class], version = 1, exportSchema = false)
abstract class DiagnosticDatabase : RoomDatabase() {

    abstract fun diagnosticDao(): DiagnosticDao

    companion object {
        @Volatile
        private var INSTANCE: DiagnosticDatabase? = null

        fun getInstance(context: Context): DiagnosticDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiagnosticDatabase::class.java,
                    "assistx_diagnostics.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

