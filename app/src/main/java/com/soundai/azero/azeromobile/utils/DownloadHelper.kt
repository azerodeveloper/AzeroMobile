package com.soundai.azero.azeromobile.utils

import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

object DownloadHelper {

    private var okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private const val tag = "DownloadHelper"

    fun download(url: String, dir: String, filename: String, listener: DownloadListener) {
        if (url.isBlank()) {
            return
        }
        val tmp = File(dir, filename)
        if (tmp.exists()) {
            Log.d(tag, "file is exist, dir is $dir filename is $filename")
            listener.onDownloadSuccess(tmp)
            return
        }

        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onDownloadFailed(e)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                var inputStream: InputStream? = null
                val buffer = ByteArray(1024)
                var len:Int
                var fileOutputStream: FileOutputStream? = null
                val file = File(dir)

                try {
                    if (!file.exists()) {
                        if (!file.mkdir()) {
                            Log.d(tag, "onResponse: 文件创建失败")
                        } else {
                            Log.d(tag, "onResponse: 文件创建成功")
                        }
                    } else {
                        Log.d(tag, "onResponse: 文件存在dir: $dir, filename: $filename")
                    }
                    inputStream = response.body()!!.byteStream()
                    val total = response.body()!!.contentLength()
                    val file1 = File(file, filename)
                    fileOutputStream = FileOutputStream(file1)
                    var sum: Long = 0

                    while (inputStream.read(buffer).also { len = it } != -1) {
                        fileOutputStream.write(buffer, 0, len)
                        sum += len.toLong()
                        val progress = sum * 0.01f / (total * 0.01f)
                        Log.d(tag, "downloading")
                        listener.onDownloading(progress)
                    }
                    fileOutputStream.flush()
                    Log.d(tag, "download success file1: ${file1.path}")
                    listener.onDownloadSuccess(file1)
                } catch (e: Exception) {
                    e.printStackTrace()
                    listener.onDownloadFailed(e)
                } finally {
                    inputStream?.close()
                    fileOutputStream?.close()
                }
            }
        })
    }

    interface DownloadListener {
        fun onDownloadSuccess(file: File)
        fun onDownloading(progress: Float)
        fun onDownloadFailed(exception: Exception)
    }

}