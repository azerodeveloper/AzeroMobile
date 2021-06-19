package com.soundai.azero.lib_todayrunrecord.bean;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.amap.api.maps2d.model.LatLng;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity
public class RunRecord implements Parcelable {
    public RunRecord() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        dateTag = simpleDateFormat.format(date);
        startTime = System.currentTimeMillis();
        id = System.currentTimeMillis();
    }

    //主键
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "ID")
    private long id;

    //运动轨迹
    @ColumnInfo(name = "path_line_points")
    @NonNull
    private List<Track> tracks = new ArrayList<>();

    //运动距离 单位m
    @ColumnInfo(name = "distance")
    private double distance = 0.0;

    //运动时长 单位s
    @ColumnInfo(name = "duration")
    private long duration = 0L;

    //运动开始时间
    @ColumnInfo(name = "start_time")
    private long startTime = 0L;

    //运动结束时间
    @ColumnInfo(name = "end_time")
    private long endTime = 0L;

    //消耗卡路里
    @ColumnInfo(name = "calorie")
    private double calorie = 0.0;

    //平均配速(分钟/公里)
    @ColumnInfo(name = "distribution")
    private double distribution = 0.0;

    //日期标记
    @ColumnInfo(name = "date_tag")
    private String dateTag;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(@NotNull List<Track> tracks) {
        this.tracks = tracks;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getCalorie() {
        return calorie;
    }

    public void setCalorie(double calorie) {
        this.calorie = calorie;
    }

    public double getDistribution() {
        return distribution;
    }

    public void setDistribution(double distribution) {
        this.distribution = distribution;
    }

    public String getDateTag() {
        return dateTag;
    }

    public void setDateTag(String dateTag) {
        this.dateTag = dateTag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeList(this.tracks);
        dest.writeDouble(this.distance);
        dest.writeLong(this.duration);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeDouble(this.calorie);
        dest.writeDouble(this.distribution);
        dest.writeString(this.dateTag);
    }

    protected RunRecord(Parcel in) {
        this.id = in.readLong();
        this.tracks = new ArrayList<>();
        in.readList(this.tracks, Track.class.getClassLoader());
        this.distance = in.readDouble();
        this.duration = in.readLong();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.calorie = in.readDouble();
        this.distribution = in.readDouble();
        this.dateTag = in.readString();
    }

    public static final Creator<RunRecord> CREATOR = new Creator<RunRecord>() {
        @Override
        public RunRecord createFromParcel(Parcel source) {
            return new RunRecord(source);
        }

        @Override
        public RunRecord[] newArray(int size) {
            return new RunRecord[size];
        }
    };


    public static class Track implements Parcelable {
        private List<LatLng> trackLinePoints = new ArrayList<>();

        public List<LatLng> getTrackLinePoints() {
            return trackLinePoints;
        }

        public void setTrackLinePoints(List<LatLng> trackLinePoints) {
            this.trackLinePoints = trackLinePoints;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(this.trackLinePoints);
        }

        public Track() {
        }

        protected Track(Parcel in) {
            this.trackLinePoints = in.createTypedArrayList(LatLng.CREATOR);
        }

        public static final Creator<Track> CREATOR = new Creator<Track>() {
            @Override
            public Track createFromParcel(Parcel source) {
                return new Track(source);
            }

            @Override
            public Track[] newArray(int size) {
                return new Track[size];
            }
        };
    }
}
