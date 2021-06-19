package com.soundai.azero.azeromobile.common.bean.run

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.amap.api.maps2d.model.LatLng
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity
@Parcelize
class PathRecord : Parcelable {
    //主键
    @PrimaryKey
    var id: Long? = null
    //运动轨迹
    @ColumnInfo(name = "path_line_points")
    var mPathLinePoints = mutableListOf<MutableList<LatLng>>()
    //运动距离 单位m
    @ColumnInfo(name = "distance")
    var mDistance: Double = 0.0
    //运动时长 单位s
    @ColumnInfo(name = "duration")
    var mDuration: Long = 0L
    //运动开始时间
    @ColumnInfo(name = "start_time")
    var mStartTime: Long = 0L
    //运动结束时间
    @ColumnInfo(name = "end_time")
    var mEndTime: Long = 0L
    //消耗卡路里
    @ColumnInfo(name = "calorie")
    var mCalorie: Double = 0.0
    //平均时速(公里/小时)
    @Ignore
    var mSpeed: Double? = null
    //平均配速(分钟/公里)
    @ColumnInfo(name = "distribution")
    var mDistribution: Double = 0.0
    //日期标记
    @ColumnInfo(name = "date_tag")
    var mDateTag: String = ""

    init {
        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        val date = Date(System.currentTimeMillis())
        mDateTag = simpleDateFormat.format(date)
    }
}