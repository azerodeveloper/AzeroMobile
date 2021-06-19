package com.soundai.azero.azeromobile.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.TypedValue
import androidx.appcompat.widget.TintContextWrapper
import androidx.core.app.ActivityCompat
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object Utils {
    fun dp2px(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        Resources.getSystem().displayMetrics
    )

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    fun sp2px(spValue: Float): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spValue,
            Resources.getSystem().displayMetrics
        )

    @SuppressLint("HardwareIds")
    fun getimei(context: Context): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        var imei: String? = null
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.imei
                } else {
                    imei = telephonyManager.deviceId
                }
                imei
            } catch (e: Exception) {
                e.printStackTrace()
                imei
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            imei = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).toString()
            imei
        } else {
            try {
                imei = telephonyManager.imei
                imei
            } catch (e: Exception) {
                e.printStackTrace()
                imei
            }
        }
    }

    fun isApplicationForeground(): Boolean {
        return ActivityLifecycleManager.getInstance().isAppForeground
    }

    fun restartApp() {
        val application = TaApp.application!!
        application.packageManager.getLaunchIntentForPackage(application.packageName)?.let {
            it.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
            application.startActivity(it)
        }
    }

    fun findActivity(context: Context?): Activity? {
        if (context == null) {
            return null
        }
        if (context is Activity) {
            return context
        } else if (context is TintContextWrapper) {
            return findActivity(context.baseContext)
        } else if (context is ContextWrapper) {
            return findActivity(context.baseContext)
        }
        return null
    }

    /**
     * 判断当前日期是星期几
     *
     * @param  pTime     设置的需要判断的时间  //格式如2012-09-08
     *
     *
     * @return dayForWeek 判断结果
     * @Exception 发生异常
     */
//  String pTime = "2012-03-12";
    @SuppressLint("SimpleDateFormat")
    fun getWeek(pTime: String): String? {
        var Week = ""
        val format = SimpleDateFormat("yyyy-MM-dd")
        val c: Calendar = Calendar.getInstance()
        try {
            c.time = format.parse(pTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            Week = "星期天"
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            Week = "星期一"
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            Week = "星期二"
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            Week = "星期三"
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            Week = "星期四"
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            Week = "星期五"
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            Week = "星期六"
        }
        return Week
    }

    public fun getDate(pTime: String): String = SimpleDateFormat("yyyy-MM-dd").parse(pTime).run {
            SimpleDateFormat("MM/dd").format(this)
        }

    fun isLandscape(context: Context):Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun getVersion(context: Context?): String? {
        var mVersionName = ""
        val packageManager: PackageManager? = context?.getPackageManager()
        try {
            val packageInfo: PackageInfo = packageManager!!.getPackageInfo(context!!.getPackageName(), 0)
            mVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            mVersionName = ""
        }
        return mVersionName
    }

}