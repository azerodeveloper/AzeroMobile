/*
 * Copyright (c) 2019 SoundAI. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.soundai.azero.azeromobile.utils

import android.content.Context
import android.content.res.AssetManager
import android.os.Handler
import android.os.Looper
import android.util.Log

import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 复制配置文件到data目录的Task
 */
class CopyConfigTask(context: Context, private val assetsDirName: String) {

    private val assetManager: AssetManager
    private val configPath: String
    private var configListener: ConfigListener? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        assetManager = context.assets
        configPath = context.filesDir.toString() + File.separator + CONFIG_DIR_NAME
    }

    /**
     * Task完成后从此回调反馈
     *
     * @param listener 回调接口
     * @return
     */
    fun setConfigListener(listener: ConfigListener): CopyConfigTask {
        this.configListener = listener
        return this
    }

    fun execute() {
        this.execute(configPath, true)
    }

    fun execute(isOverride: Boolean) {
        execute(configPath, isOverride)
    }

    @JvmOverloads
    fun execute(destPath: String, isOverride: Boolean = true) {
        CopyThread(destPath, isOverride).start()
    }

    /**
     * 复制文件线程
     */
    private inner class CopyThread internal constructor(
        private val destPath: String,
        private val isOverride: Boolean
    ) : Thread() {

        override fun run() {
            Log.i(TAG, "CopyConfigTask start.")
            try {
                val destDir = File(destPath)
                if (!destDir.exists()) {
                    if (!destDir.mkdirs()) {
                        handleError("mkdirs 'sai_config' failed.")
                    }
                }
                val fileNames = assetManager.list(assetsDirName)
                if (fileNames!!.size > 0) {
                    for (fileName in fileNames) {
                        copyFilesFromAssets(
                            isOverride,
                            destDir.absolutePath + File.separator + fileName,
                            assetsDirName + File.separator + fileName
                        )
                    }
                }
                if (configListener != null) {
                    handler.post { configListener!!.onSuccess(destPath) }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                handleError(e.message!!)
            }

        }

        /**
         * 从Assets复制配置文件
         *
         * @param isOverride    是否覆盖
         * @param destPath      目标地址
         * @param assetFilename 文件名
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun copyFilesFromAssets(
            isOverride: Boolean,
            destPath: String,
            assetFilename: String
        ) {
            val outFile = File(destPath)
            if (outFile.exists()) {
                if (isOverride) {
                    outFile.delete()
                    Log.i(TAG, "overriding file $destPath")
                } else {
                    Log.i(TAG, "file $destPath already exists. No isOverride.\n")
                    return
                }
            }
            val inputStream = assetManager.open(assetFilename)
            val fos = FileOutputStream(destPath)
            try {
                val buffer = ByteArray(BUFFER_SIZE)
                var readBytes: Int = inputStream.read(buffer)
                while (readBytes != -1) {
                    fos.write(buffer, 0, readBytes)
                    readBytes = inputStream.read(buffer)
                }
                fos.flush()
            } finally {
                closeStream(fos, inputStream)
            }
        }

        /**
         * 错误捕捉
         *
         * @param errorMsg 错误信息
         */
        private fun handleError(errorMsg: String) {
            if (configListener != null) {
                handler.post { configListener!!.onFailed(errorMsg) }
            }
        }
    }

    /**
     * 结果回调
     */
    interface ConfigListener {
        /**
         * Task Finish
         *
         * @param configPath 存储路径
         */
        fun onSuccess(configPath: String)

        /**
         * 任务执行失败
         *
         * @param errorMsg 错误信息
         */
        fun onFailed(errorMsg: String)
    }

    companion object {

        private val TAG = "SoundAI"

        private val CONFIG_DIR_NAME = "sai_config"

        private val BUFFER_SIZE = 8192

        fun closeStream(vararg closeables: Closeable) {
            for (c in closeables) {
                try {
                    c?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}
