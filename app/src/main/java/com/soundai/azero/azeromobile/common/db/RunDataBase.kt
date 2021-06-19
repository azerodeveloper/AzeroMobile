package com.soundai.azero.azeromobile.common.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.soundai.azero.azeromobile.common.bean.run.PathRecord
import com.soundai.azero.azeromobile.common.bean.run.PathRecordDao

@Database(entities = [PathRecord::class], version = 1, exportSchema = false)
@TypeConverters(Converts::class)
abstract class RunDataBase : RoomDatabase() {
    abstract fun pathRecordDao(): PathRecordDao

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