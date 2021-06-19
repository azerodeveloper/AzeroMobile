package com.soundai.azero.azeromobile

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.fastjson.JSONObject
import com.azero.sdk.AzeroManager
import com.azero.sdk.util.Utils
import com.soundai.azero.azeromobile.common.bean.walk.StepData
import com.soundai.azero.azeromobile.ui.activity.launcher.item.IGridItem
import com.soundai.azero.lib_todayrunrecord.bean.RunRecord
import java.util.*

/**
 * Create by xingw on 2019/11/1
 */
fun calculateTotalSpan(data: MutableList<IGridItem>): Pair<Int, MutableList<Int>> {
    var totalSpan = 0
    val spanList = mutableListOf<Int>()
    for (item in data) {
        totalSpan += item.getSpanSize()
        spanList.add(totalSpan)
    }
    return Pair(totalSpan, spanList)
}

fun stringForTime(timeMs: Int): String {
    val totalSeconds = timeMs / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600
    val mStringBuilder = StringBuilder()
    val mFormatter = Formatter(mStringBuilder, Locale.CHINA)
    mStringBuilder.setLength(0)
    return if (hours > 0) {
        mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    } else {
        mFormatter.format("%02d:%02d", minutes, seconds).toString()
    }
}

fun sendSensorDataEvent(runRecord: RunRecord?, stepData: StepData?) {
    val json = JSONObject()
    json.put("event", JSONObject().also { event ->
        event.put("header", JSONObject().also { header ->
            header["namespace"] = "AzeroExpress"
            header["name"] = "SensorData"
            header["messageId"] = Utils.getUuid()
        })
        event.put("payload", JSONObject().also { payload ->
            runRecord?.let { runRecord ->
                payload.put("run", JSONObject().also { run ->
                    run["Id"] = runRecord.id
                    run["Calorie"] = runRecord.calorie.toInt()
                    run["DataTag"] = runRecord.dateTag
                    run["Distance"] = runRecord.distance
                    run["Duration"] = runRecord.duration
                    run["StartTime"] = runRecord.startTime
                    run["EndTime"] = runRecord.endTime
                })
            }
            stepData?.let { stepData ->
                payload.put("walk", JSONObject().also { walk ->
                    walk["Calorie"] = stepData.calorie
                    walk["DataTag"] = stepData.dataTag
                    walk["Distance"] = stepData.distance
                    walk["StepCount"] = stepData.stepCount
                })
            }

        })
    })
    AzeroManager.getInstance().customAgent?.sendEvent(json.toJSONString())
}

fun sendStopRunningEvent() {
    val json = JSONObject()
    json.put("event", JSONObject().also { event ->
        event.put("header", JSONObject().also { header ->
            header["namespace"] = "AzeroExpress"
            header["name"] = "StopRunning"
            header["messageId"] = Utils.getUuid()
        })
        event.put("payload", JSONObject())
    })
    AzeroManager.getInstance().customAgent?.sendEvent(json.toJSONString())
}

fun NavController.safeNavigate(actionId: Int, sourceId: Int) {
    if (currentDestination?.id == sourceId) {
        navigate(actionId)
    }
}

@AnyThread
inline fun <reified T> MutableLiveData<T>.postNext(map: (T) -> T) {
    postValue(map(verifyLiveDataNotEmpty()))
}

@MainThread
inline fun <reified T> MutableLiveData<T>.setNext(map: (T) -> T) {
    value = map(verifyLiveDataNotEmpty())
}

@AnyThread
inline fun <reified T> LiveData<T>.verifyLiveDataNotEmpty(): T {
    return value
        ?: throw NullPointerException("MutableLiveData<${T::class.java}> not contain value.")
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

//ViewPager2 指定时间的页面切换
fun ViewPager2.setCurrentItem(
    item: Int,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    pagePxWidth: Int = width // 使用viewpager2.getWidth()获取
) {
    val pxToDrag: Int = pagePxWidth * (item - currentItem)
    val animator = ValueAnimator.ofInt(0, pxToDrag)
    var previousValue = 0
    animator.addUpdateListener { valueAnimator ->
        val currentValue = valueAnimator.animatedValue as Int
        val currentPxToDrag = (currentValue - previousValue).toFloat()
        fakeDragBy(-currentPxToDrag)
        previousValue = currentValue
    }
    animator.doOnStart { beginFakeDrag() }
    animator.doOnEnd { endFakeDrag() }
    animator.interpolator = interpolator
    animator.duration = duration
    animator.start()
}