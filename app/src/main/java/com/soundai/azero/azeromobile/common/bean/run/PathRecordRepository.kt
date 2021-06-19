package com.soundai.azero.azeromobile.common.bean.run

import android.app.Application
import com.soundai.azero.azeromobile.common.db.RunDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PathRecordRepository(application: Application) {
    private var db = RunDataBase.getInstance(application)
    private var pathRecordDao = db.pathRecordDao()

    //xxxx年xx月xx日
    public fun getPathRecord(data: String): PathRecord {
        return pathRecordDao.getPathRecordByDate(data)
    }

    public fun save(pathRecord: PathRecord) {
        CoroutineScope(Dispatchers.IO).launch {
            pathRecordDao.insert(pathRecord)
        }
    }
}