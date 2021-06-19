package com.soundai.azero.lib_todayrunrecord.bean

import android.content.Context
import com.soundai.azero.lib_todayrunrecord.coroutineExceptionHandler
import com.soundai.azero.lib_todayrunrecord.db.RunDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RunRecordRepository(application: Context) {
    private var db = RunDataBase.getInstance(application)
    private var runRecordDao = db.runRecordDao()

    //xxxx年xx月xx日
    public fun getRunRecord(data: String): List<RunRecord> {
        return runRecordDao.getPathRecordByDate(data)
    }

    public fun save(runRecord: RunRecord) {
        CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
            runRecordDao.insert(runRecord)
        }
    }
}