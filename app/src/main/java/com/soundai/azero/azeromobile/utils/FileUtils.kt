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

import android.os.Environment

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object FileUtils {

    val externalStorageDirectory: String
        get() = Environment.getExternalStorageDirectory().path

    /**
     * @param buffer   data
     * @param filePath destination file path
     * @param isAppend
     */
    fun writeFile(buffer: ByteArray, filePath: String, isAppend: Boolean) {
        val file = File(filePath)
        var fos: FileOutputStream? = null
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            fos = FileOutputStream(file, isAppend)
            fos.write(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (fos != null) {
                    fos.flush()
                    fos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun writeFile(buffer: ShortArray, filePath: String, isAppend: Boolean) {
        val file = File(filePath)
        var fos: FileOutputStream? = null
        try {
            if (!file.exists()) {
                file.createNewFile()
            }
            fos = FileOutputStream(file, isAppend)
            var byteArr:ByteArray = shortArr2byteArr(buffer,buffer.size)
            fos.write(byteArr)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (fos != null) {
                    fos.flush()
                    fos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }


    private fun shortArr2byteArr(
        shortArr: ShortArray,
        shortArrLen: Int
    ): ByteArray {
        val byteArr = ByteArray(shortArrLen * 2)
        ByteBuffer.wrap(byteArr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            .put(shortArr)
        return byteArr
    }
}
