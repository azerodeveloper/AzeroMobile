// IRunRecorderInterface.aidl
package com.soundai.azero.lib_todayrunrecord;

// Declare any non-default types here with import statements
import com.soundai.azero.lib_todayrunrecord.bean.RunRecord;
import com.soundai.azero.lib_todayrunrecord.IRunStateCallback;
interface IRunRecorderInterface {
    RunRecord getCurrentRunData();
    RunRecord getLastRunData();
    String getCurrentRunState();
    void start();
    void pause();
    void resume();
    void finish();
    void registerCallback(IRunStateCallback cb);
    void unRegisterCallback(IRunStateCallback cb);
}
