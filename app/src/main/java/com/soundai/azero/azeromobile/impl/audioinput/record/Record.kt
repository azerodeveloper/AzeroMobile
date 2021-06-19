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

package com.soundai.azero.azeromobile.impl.audioinput.record

/**
 * 负责为识别模块提供数据
 */
abstract class Record {
    protected var listener: Listener? = null
    /**
     * 开始读取数据
     */
    abstract fun start()

    /**
     * 停止读取数据
     */
    abstract fun stop()

    /**
     * 判断是否正在录音
     */
    abstract fun isRecording():Boolean

    /**
     * 判断是否正在录音
     */
    abstract fun release()

    fun setDataListener(listener: Listener?) {
        this.listener = listener
    }

    /**
     * 数据回调
     */
    interface Listener {
        fun onData(data: ByteArray, size: Int)
        fun onData(data: ShortArray, size: Int)
    }
}
