package com.soundai.azero.lib_todayrunrecord

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps2d.AMapUtils
import com.amap.api.maps2d.model.LatLng
import com.soundai.azero.lib_todayrunrecord.bean.RunRecord
import com.soundai.azero.lib_todayrunrecord.bean.RunRecordRepository
import kotlinx.coroutines.*

class TodayRunService : Service() {
    companion object {
        const val RUN_STATE_IDLE = "RUN_STATE_IDLE"
        const val RUN_STATE_PAUSE = "RUN_STATE_PAUSE"
        const val RUN_STATE_RUNNING = "RUN_STATE_RUNNING"

        private const val TAG = "TodayRunService"

        /**
         * 跑步数据通知ID
         */
        private const val NOTIFY_ID = 2000

        /**
         * 保存数据库频率 单位ms
         */
        private const val DB_SAVE_FREQUENCY = 5 * 1000L
        /**
         * 定位间隔，3s一次
         */
        private const val REFRESH_NOTIFY_STEP_DURATION = 3 * 1000L

        /**
         * 如果走路如果停止，10秒钟后保存数据库
         */
        private const val LAST_SAVE_STEP_DURATION = 10 * 1000
    }

    private val dbLock by lazy { Any() }

    /**
     * 当前运动数据记录
     */
    private var mRunRecord: RunRecord? = null

    private var lastRunRecord: RunRecord? = null

    private var runState = RUN_STATE_IDLE

    private val remoteCallbacks by lazy { RemoteCallbackList<IRunStateCallback>() }

    /**
     * 是否在跑步
     */
    private var isStartUp = false
    private var jobTime: Job? = null
    private var jobSaveData: Job? = null
    private val mLocationClient by lazy {
        AMapLocationClient(this).apply {
            setLocationOption(mLocationOption)
            setLocationListener(aMapLocationListener)
        }
    }
    private val mLocationOption by lazy {
        AMapLocationClientOption().apply {
            locationMode =
                AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
            isGpsFirst = false //可选，设置是否gps优先，只在高精度模式下有效。默认关闭
            httpTimeOut = 30000 //可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
            interval = REFRESH_NOTIFY_STEP_DURATION //可选，设置定位间隔。默认为2秒
            isNeedAddress = false //可选，设置是否返回逆地理地址信息。默认是true
            isOnceLocation = false //可选，设置是否单次定位。默认是false
            isOnceLocationLatest = false //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
            isSensorEnable = false //可选，设置是否使用传感器。默认是false
            isWifiScan = true //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
            isLocationCacheEnable = true //可选，设置是否使用缓存定位，默认为true
            geoLanguage =
                AMapLocationClientOption.GeoLanguage.ZH //可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）

        }
    }

    /**
     * 定位结果回调
     *
     * @param aMapLocation 位置信息类
     */
    private val aMapLocationListener by lazy {
        AMapLocationListener { aMapLocation: AMapLocation? ->
            if (null == aMapLocation) return@AMapLocationListener
            if (aMapLocation.errorCode == 0) { //先暂时获得经纬度信息，并将其记录在List中
                //定位成功
                updateLocation(aMapLocation)
            } else {
                Log.w(TAG, aMapLocation.errorInfo)
            }
        }
    }
    private val restTimer: CountDownTimer = object : CountDownTimer(90 * 60 * 1000, 60 * 1000) {
        override fun onFinish() {
            jobTime?.cancel()
            jobTime = null
            jobSaveData?.cancel()
            jobSaveData = null
            mRunRecord = null
            isStartUp = false
            saveRecord()
            notifyCallback("restTimeOut")
        }

        override fun onTick(p0: Long) {
            Log.d("TodayRunService", "Has stop running for ${90 - p0 / (1000 * 60)} minutes")
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mIBinder.asBinder()
    }

//    override fun onCreate() {
//        super.onCreate()
//        initNotification()
//    }

    @Synchronized
    private fun initNotification() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("跑步服务")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(NOTIFY_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onDestroy() {
        super.onDestroy()
        releaseLocation()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFY_ID)
    }

    private fun saveRecord() {
        synchronized(dbLock) {
            mRunRecord?.let {
                //运动距离过短不保存
                if (it.distance > 100)
                    RunRecordRepository(applicationContext).save(it)
            }
        }
    }

    /**
     * 开始定位。
     */
    private fun startLocation() {
        mLocationClient.startLocation()
    }

    private fun stopLocation() {
        mLocationClient.stopLocation()
    }

    private fun releaseLocation() {
        mLocationClient.run {
            stopLocation()
            unRegisterLocationListener(aMapLocationListener)
            onDestroy()
        }
    }

    private fun updateLocation(aMapLocation: AMapLocation) { //原始轨迹
        mRunRecord?.let { record ->
            val newLatLng = LatLng(aMapLocation.latitude, aMapLocation.longitude)
            if (record.tracks.last().trackLinePoints.isEmpty()) {
                record.tracks.last().trackLinePoints.add(newLatLng)
            } else {
                val distance = AMapUtils.calculateLineDistance(
                    record.tracks.last().trackLinePoints.last(),
                    newLatLng
                )
                if (distance > 2) {
                    record.tracks.last().trackLinePoints.add(newLatLng)
                    record.distance += distance
                } else return
            }
            //计算配速
            val seconds = record.duration
            val sportMile: Double = record.distance / 1000.0
            //运动距离大于0.2公里再计算配速
            if (seconds > 0 && sportMile >= 0.01) {
                val distribution = seconds.toDouble() / 60 / sportMile //  分/公里
                record.distribution = distribution
                record.calorie = 80 * sportMile * 1.036
                Log.d(
                    TAG,
                    "distribution:${distribution},calorie:${record.calorie},mile:${sportMile}"
                )
            }
        }
    }



    private val mIBinder = object : IRunRecorderInterface.Stub() {
        override fun finish() {
            if (mRunRecord == null) return
            isStartUp = false
            stopLocation()
            releaseJobs()
            saveRecord()
            restTimer.cancel()
            lastRunRecord = mRunRecord
            mRunRecord = null
            notifyCallback("finish")
            runState = RUN_STATE_IDLE
        }

        override fun pause() {
            if (mRunRecord == null) return
            isStartUp = false
            stopLocation()
            restTimer.start()
            releaseJobs()
            saveRecord()
            notifyCallback("pause")
            runState = RUN_STATE_PAUSE
        }

        override fun start() {
            if (mRunRecord == null) {
                mRunRecord = RunRecord()
                mRunRecord?.tracks?.add(RunRecord.Track())
                isStartUp = true
                notifyCallback("start")
                startLocation()
                startJobs()
                runState = RUN_STATE_RUNNING
            } else {
                notifyCallback("alreadyStart")
            }
        }

        override fun resume() {
            if (mRunRecord == null) return
            isStartUp = true
            startLocation()
            restTimer.cancel()
            startJobs()
            mRunRecord?.let { runRecord ->
                if (runRecord.tracks.last().trackLinePoints.size > 1) {
                    runRecord.tracks.add(RunRecord.Track())
                }
            }
            notifyCallback("resume")
            runState = RUN_STATE_RUNNING
        }


        override fun getLastRunData(): RunRecord? {
            return lastRunRecord
        }

        override fun getCurrentRunState(): String {
            return runState
        }

        override fun registerCallback(cb: IRunStateCallback?) {
            if (cb != null) {
                remoteCallbacks.register(cb)
            }
        }

        override fun unRegisterCallback(cb: IRunStateCallback?) {
            if (cb != null) {
                remoteCallbacks.unregister(cb)
            }
        }

        override fun getCurrentRunData(): RunRecord? {
            return mRunRecord
        }
    }

    private fun startTimerJob() {
        jobTime = if (jobTime == null) {
            CoroutineScope(Dispatchers.Default).launch {
                while (this.isActive) {
                    val record = mRunRecord
                    if (isStartUp && record != null) {
                        record.duration++
                        notifyCallback("update")
                    }
                    delay(1000)
                }
            }
        } else {
            return
        }
    }

    private fun startSaveDateJob() {
        jobSaveData = if (jobSaveData == null) {
            CoroutineScope(Dispatchers.Default).launch(coroutineExceptionHandler) {
                while (this.isActive) {
                    delay(DB_SAVE_FREQUENCY)
                    if (isStartUp) {
                        saveRecord()
                    }
                }
            }
        } else {
            return
        }
    }

    private fun startJobs() {
        startTimerJob()
        startSaveDateJob()
    }

    private fun releaseJobs() {
        jobTime?.run {
            jobTime = null
            cancel()
        }
        jobSaveData?.run {
            jobSaveData = null
            cancel()
        }
    }

    private fun notifyCallback(state: String) =
        synchronized(remoteCallbacks) {
            val size = remoteCallbacks.beginBroadcast()
            for (i in 0 until size) {
                val callback = remoteCallbacks.getBroadcastItem(i)
                when (state) {
                    "start" -> callback.onStart()
                    "pause" -> callback.onPause()
                    "finish" -> callback.onFinish()
                    "resume" -> callback.onResume()
                    "alreadyStart" -> callback.onError("alreadyStart")
                    "restTimeOut" -> callback.onError("restTimeOut")
                    "update" -> callback.onUpdate(mRunRecord)
                }
                remoteCallbacks.finishBroadcast()
            }
        }
}