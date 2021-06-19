package com.soundai.azero.azeromobile.common.db

import androidx.room.TypeConverter
import com.amap.api.maps2d.model.LatLng
import com.google.gson.Gson

object Converts {
    @TypeConverter
    @JvmStatic
    fun fromLatLng(latLng: LatLng): String {
        return "${latLng.latitude}:${latLng.longitude}"
    }

    @TypeConverter
    @JvmStatic
    fun stringToLatLng(latlng: String): LatLng {
        val split = latlng.split(":")
        return LatLng(split[0].toDouble(), split[1].toDouble())
    }

    @TypeConverter
    @JvmStatic
    fun fromLatngLists(latLngList: List<List<LatLng>>?): String? {
        return if (latLngList == null) null else Gson().toJson(LatLngList(latLngList))
    }

    @TypeConverter
    @JvmStatic
    fun stringListsToLatLngLists(lLString: String?): List<List<LatLng>>? {
        return if (lLString == null) null else Gson().fromJson(
            lLString,
            LatLngList::class.java
        ).mPathLinePoints
    }

    private class LatLngList(val mPathLinePoints: List<List<LatLng>>)
}