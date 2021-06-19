package com.soundai.azero.lib_todayrunrecord.bean

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RunRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg runRecord: RunRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg runRecord: RunRecord): Int

    @Delete
    fun delete(vararg runRecord: RunRecord)

    @Query("SELECT * FROM RunRecord WHERE date_tag == (:dateTag)")
    fun getPathRecordByDate(dateTag: String): List<RunRecord>
}