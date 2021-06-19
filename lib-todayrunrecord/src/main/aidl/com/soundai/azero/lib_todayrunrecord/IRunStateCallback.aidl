// IRunStateCallback.aidl
package com.soundai.azero.lib_todayrunrecord;

import com.soundai.azero.lib_todayrunrecord.bean.RunRecord;

// Declare any non-default types here with import statements

interface IRunStateCallback {
    void onStart();
    void onPause();
    void onResume();
    void onFinish();
    void onError(String reason);
    void onUpdate(in RunRecord record);
}
