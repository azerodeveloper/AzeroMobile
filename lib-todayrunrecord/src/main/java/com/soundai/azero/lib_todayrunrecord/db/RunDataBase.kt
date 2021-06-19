package com.soundai.azero.lib_todayrunrecord.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.soundai.azero.lib_todayrunrecord.bean.RunRecord
import com.soundai.azero.lib_todayrunrecord.bean.RunRecordDao

@Database(entities = [RunRecord::class], version = 1, exportSchema = false)
@TypeConverters(Converts::class)
abstract class RunDataBase : RoomDatabase() {
    abstract fun runRecordDao(): RunRecordDao

    companion object {
        private var INSTANCE: RunDataBase? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RunDataBase::class.java,
                    "azero_root_runner.db"
                )
                    .build().also { INSTANCE = it }
            }
    }
}