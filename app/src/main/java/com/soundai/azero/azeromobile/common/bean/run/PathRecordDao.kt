package com.soundai.azero.azeromobile.common.bean.run

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PathRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg recommendations: PathRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg recommendations: PathRecord): Int

    @Delete
    fun delete(vararg recommendations: PathRecord)

    @Query("SELECT * FROM PathRecord WHERE date_tag == (:dateTag)")
    fun getPathRecordByDate(dateTag:String):PathRecord
}