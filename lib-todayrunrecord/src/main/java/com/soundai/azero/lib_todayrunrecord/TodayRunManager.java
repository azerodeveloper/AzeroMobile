package com.soundai.azero.lib_todayrunrecord;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import androidx.core.content.ContextCompat;

/**
 * 计步SDK初始化方法
 */

public class TodayRunManager {

    private static final String TAG = "TodayRunManager";

    public static void startTodayRunService(Application application) {
        try {
            Intent intent = new Intent(application, TodayRunService.class);
            application.startService(intent);
//            ContextCompat.startForegroundService(application, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean bindService(Context context, ServiceConnection conn) {
        try {
            Intent intent = new Intent(context, TodayRunService.class);
            return context.bindService(intent, conn, Activity.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
