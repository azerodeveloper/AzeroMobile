package com.soundai.azero.azeromobile.utils

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    @SuppressLint("SimpleDateFormat")
    fun convertDateFormat(
        date: String,
        newFormat: String,
        oldFormat: String = "yyyy-MM-dd"
    ): String {
        val oldSimpleDateFormat = SimpleDateFormat(oldFormat)
        val newSimpleDateFormat = SimpleDateFormat(newFormat)
        return newSimpleDateFormat.format(oldSimpleDateFormat.parse(date))
    }

    @SuppressLint("SimpleDateFormat")
    fun getTodayDate(format: String = "yyyy-MM-dd"): String {
        return SimpleDateFormat(format).format(Date())
    }

    fun isToday(date: String): Boolean {
        return date == getTodayDate()
    }

    @SuppressLint("SimpleDateFormat")
    fun getWeek(daytime: String, format: String = "yyyy-MM-dd"): String {
        val simpleDateFormat = SimpleDateFormat(format)
        val calendar = Calendar.getInstance()
        try {
            calendar.timeInMillis = simpleDateFormat.parse(daytime).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.CHINA)
    }
}