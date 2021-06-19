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

package com.soundai.azero.azeromobile.impl.phonecallcontroller

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.ContactIngestion.ContactInputSourceType
import com.azero.sdk.impl.ContactIngestion.ContactPojos.AddressDetails
import com.azero.sdk.impl.ContactIngestion.ContactPojos.Contact
import com.azero.sdk.impl.ContactIngestion.ContactPojos.ContactSourceType
import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler
import com.azero.sdk.impl.PhoneCallController.AbstractPhoneCallDispatcher
import com.azero.sdk.impl.PhoneCallController.PhoneCallControllerHandler
import com.azero.sdk.util.log
import com.google.gson.JsonParseException
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.common.bean.ContactsBean
import com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.PhoneConnectionStateManagerHelper
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.system.TaAudioManager
import com.soundai.azero.azeromobile.utils.ContactProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 * @Author: sjy
 * @Date: 2019/9/3
 */
class PhoneCallControllerImpl(final val context: Context) {

    private var mPhoneCallControllerHandler: PhoneCallControllerHandler? = null
    private lateinit var contactCache: MutableList<ContactsBean>
    private val ams = context.getSystemService(AUDIO_SERVICE) as AudioManager

    companion object {
        var isRecording: Boolean = true
    }

    init {
        if (mPhoneCallControllerHandler == null) {
            mPhoneCallControllerHandler =
                AzeroManager.getInstance()
                    .getHandler(AzeroManager.PHONECALL_HANDLER) as PhoneCallControllerHandler
        }
        TaAudioManager.addAudioStateListener(object : TaAudioManager.AudioStateListener {
            override fun onFucusGain() {
                TaApp.isInNoneedToNotifyEngineState = false
                //重新进入蓝牙模式
                if (TaAudioManager.isBTHeadsetConnected()) {
                    log.e("=====EarMode 连接蓝牙")
//                    BluetoothUtil.openSco()
                }
                isRecording = true
            }

            override fun onFucusLoss() {
                TaApp.isInNoneedToNotifyEngineState = true
                if (!phoneIsinUse(context)) {
                    //微信通话
                    log.e(" voip电话 停止录音！")
//                    AudioInputManager.getInstance().stopAudioInput()
//                    isRecording = false;
                }
            }

        })
        register()
        CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
            contactCache = ContactProvider.getContactData(context)
            //需要修改Native代码 ContactUploaderEngineImpl 获取账户信息
            upLoadContacts(contactCache)
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL)
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        context.registerReceiver(LocalPhoneListener(), intentFilter)
    }

    /**
     * 上传通讯录
     */
    private fun upLoadContacts(contactBeans: MutableList<ContactsBean>) {

        //上传前先清空一次云端的通讯录
        val contactUploaderHandler = AzeroManager.getInstance()
            .getHandler(AzeroManager.CONTACT_UPLOADER_HANDLER) as ContactUploaderHandler

        PhoneConnectionStateManagerHelper.getInstance()
            .cancelUploadingContacts(contactUploaderHandler)
        val contactLists = mutableListOf<Contact>()
        contactBeans.forEach {
            val addressDetailsList = ArrayList<AddressDetails>()
            val phoneNumberList = it.numberList
            for (phoneNumber in phoneNumberList) {
                addressDetailsList.add(
                    AddressDetails(
                        ContactSourceType.PHONENUMBER,
                        phoneNumber,
                        "phone"
                    )
                )
            }

            val contact = Contact(it.contactId, "", it.name, "", "", addressDetailsList)
            contactLists.add(contact)
        }
        PhoneConnectionStateManagerHelper.getInstance().startUploadingContacts(
            contactLists,
            ContactInputSourceType.PHONE_CONTACT,
            contactUploaderHandler
        )
    }

    private fun register() {
        mPhoneCallControllerHandler!!.registerPhoneCallControllerDispatchedListener(object :
            AbstractPhoneCallDispatcher() {
            override fun onDial(payload: String): Boolean {
                log.d(payload)
                try {
                    val json = JSONObject(payload)
                    val callee = json.getJSONObject("callee")
                    val detailsString = callee.getString("details")
                    val details = JSONObject(detailsString)
                    val defaultContactAddress = callee.getJSONObject("defaultContactAddress")
                    // 新ta来了通话技能中查询联系人位于技能，全部下发"phone_number"进行通话
                    when (details.getString("type")) {
                        "contact_id" -> {//拼音拨号
                            val pinyin =
                                defaultContactAddress.getString("format").replace(" ".toRegex(), "")
                            val pinyinList = defaultContactAddress.getString("format").split(" ")
                            log.d("pinyin:${pinyin}")
                            CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                                // var result = ContactProvider.findDataByEN(pinyin, contactCache)
                                // if (result.isEmpty()) result = ContactProvider.findDataByEN(
                                //     firstLetter.toString(),
                                //     contactCache
                                // )
                                var pinyinSearch =
                                    pinyinList.joinToString(separator = "") { it[0].toString() }
                                var result: ContactsBean? = null
                                while (pinyinSearch.isNotEmpty()) {
                                    val resultList = ContactProvider.findDataByEN(
                                        pinyinSearch,
                                        contactCache
                                    )
                                    val map = HashMap<String, ContactsBean>()
                                    resultList.forEach { map[it.namePinYin!!] = it }
                                    result =
                                        ContactProvider.queryByInitialConsonant(map, pinyinList)
                                    if (result == null) {
                                        pinyinSearch = pinyinSearch.dropLast(1)
                                        continue
                                    }
                                    break
                                }
                                if (result != null) {
                                    //展示列表
                                    ContactProvider.callPhone(context, result.numberList[0])
                                    log.d("call num:${result.numberList[0]}")
                                }
                            }
                        }
                        "phone_number" -> {//电话号
                            val phoneNum = defaultContactAddress.getString("value")
                            ContactProvider.callPhone(context, phoneNum)
                        }
                    }
                } catch (e: JsonParseException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                return super.onDial(payload)
            }

            override fun onReDial(payload: String): Boolean {
                log.d(payload)
                return super.onReDial(payload)
            }

            override fun onAnswer(payload: String) {
                log.d(payload)
                super.onAnswer(payload)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ANSWER_PHONE_CALLS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val telecomManager =
                            context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                        telecomManager.acceptRingingCall()
                    }
                } else {
                    val telephonyManager =
                        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    val answerRingCallMethod = telephonyManager.javaClass.getDeclaredMethod(
                        "answerRingingCall"
                    )
                    answerRingCallMethod.isAccessible = true
                    answerRingCallMethod.invoke(telephonyManager)
                }
            }

            override fun onStop(payload: String) {
                log.d(payload)
                super.onStop(payload)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ANSWER_PHONE_CALLS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val telecomManager =
                            context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                        telecomManager.endCall()
                    }
                } else {
                    val telephonyManager =
                        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    val endCallMethod = telephonyManager.javaClass.getDeclaredMethod(
                        "endCall"
                    )
                    endCallMethod.isAccessible = true
                    endCallMethod.invoke(telephonyManager)
                }
            }

            override fun onSendDTMF(payload: String) {
                log.d(payload)
                super.onSendDTMF(payload)
            }
        })
    }

    private var isChangeToPause: Boolean = false

    fun phoneIsinUse(context: Context): Boolean {
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) == PERMISSION_GRANTED
        ) {
            return tm.isInCall
        } else {
            return false
        }
    }

    class LocalPhoneListener : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            } else {
                // 如果是来电
                val tManager = p0?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
                //电话的状态
                when (tManager.callState) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        if (!isRecording) {
                            isRecording = true
//                            AudioInputManager.getInstance().startAudioInput()
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                    }
                }
            }
        }

    }


}

