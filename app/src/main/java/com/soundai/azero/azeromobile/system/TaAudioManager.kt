package com.soundai.azero.azeromobile.system

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.*
import android.media.AudioManager.GET_DEVICES_INPUTS
import android.media.AudioManager.STREAM_VOICE_CALL
import android.os.Build
import android.os.Handler
import android.os.Message
import androidx.annotation.RequiresApi
import com.azero.sdk.AzeroManager
import com.azero.sdk.event.Command
import com.azero.sdk.impl.MediaPlayer.MediaPlayerHandler
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.impl.audioinput.AudioInputManager
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.utils.DeviceUtils
import com.soundai.azero.azeromobile.utils.headsetstate.HeadsetStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 音频管理类
 * 包括音频路由管理、焦点管理等
 */
object TaAudioManager {
    private var mAudioFocused = false
    private var mIsBluetoothHeadsetConnected = false
    private var mIsBluetoothHeadsetScoConnected = false
    private var mIsChangeToPauseFromFocusLoss: Boolean = false
    private var mIsChangeToPauseFromBtDisconnected: Boolean = false
    private var mIsSupportSco : Boolean = true
    private var mCurrentMicSource: MicSource  = MicSource.BUILTIN
    private var mIsRecordFailed:Boolean = true
    private var mIsRetrying :Boolean = false

    private var mContext: Context? = null
    private var mAudioManager: AudioManager? = null
    private var mBluetoothHeadset: BluetoothHeadset? = null
    private var mBluetoothStateFilter: IntentFilter? = null
    private var mFocusRequest: AudioFocusRequest? = null
    private var mHeadsetProfileListener: BluetoothHeadset? = null
    private val mBluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val bluetoothStateListeners by lazy { ArrayList<BluetoothStateListener>() }
    private val audioStateListeners by lazy { ArrayList<AudioStateListener>() }

    enum class MicSource{
        SCO,//蓝牙耳机mic
        WIRED,//有线耳机mic
        BUILTIN//内置耳机
    }

    interface BluetoothStateListener {
        fun onConnected()
        fun onDisconnected()
        fun onRecordFailed()
        fun onRecordSuccessful()
    }

    interface AudioStateListener {
        fun onFucusLoss()
        fun onFucusGain()
    }

    fun addBluetoothStateListener(listener: BluetoothStateListener) {
        bluetoothStateListeners.add(listener)
    }

    fun removeBluetoothStateListener(listener: BluetoothStateListener) {
        bluetoothStateListeners.remove(listener)
    }

    fun addAudioStateListener(audioListener: AudioStateListener) {
        audioStateListeners.add(audioListener)
    }

    fun removeAudioStateListener(audioListener: AudioStateListener) {
        audioStateListeners.remove(audioListener)
    }

    fun initTaAudioManager(context: Context) {
        mContext = context
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (!isSupportBluetoothSco()) {
            mIsSupportSco = false
            log.e("[TaAudioManager] 不支持蓝牙或者蓝牙SCO模式！")
            return
        }
        initBtAndAudioStateBroadcastReceiver()
        requestAudioFocus(STREAM_VOICE_CALL)
        initHeadsetState()
    }

    fun destroy() {
        if (mBluetoothAdapter != null && mBluetoothHeadset != null) {
            log.e("[TaAudioManager] [Bluetooth] Closing HEADSET profile proxy")
            mBluetoothAdapter?.closeProfileProxy(
                BluetoothProfile.HEADSET,
                mBluetoothHeadset
            )
        }
        log.e("[TaAudioManager] [Bluetooth] Unegistering bluetooth receiver")
        if (mBtAndAudioStateBroadcastReceiver != null) {
            mContext!!.unregisterReceiver(mBtAndAudioStateBroadcastReceiver)
        }
        exitAudioSco()
    }

    fun setIsRecordFailed(isFailed:Boolean){
        mIsRecordFailed = isFailed
        if (mIsRecordFailed){
            bluetoothStateListeners.forEach { it.onRecordFailed() }
        }else{
            bluetoothStateListeners.forEach { it.onRecordSuccessful() }
        }
    }

    fun getRecordFailedState(): Boolean{
        return mIsRecordFailed
    }

    private fun initAudioSco() {
        log.e("[TaAudioManager] [Bluetooth] 开始建立sco连接")
        setAudioManagerInCallMode()
        if (isBTHeadsetConnected()) {
            changeBluetoothSco(true)
        }
    }

    private fun exitAudioSco() {
        mAudioManager?.mode = AudioManager.MODE_NORMAL

//        abandonFocus()
        if (isBTHeadsetConnected()) {
            changeBluetoothSco(false)
        }
    }

    private fun initBtAndAudioStateBroadcastReceiver() {
        mBluetoothStateFilter = IntentFilter()
        if (mIsSupportSco) {
            //当且仅当支持蓝牙sco的时候注册这些回调，如果系统不支持sco，则无需关注蓝牙状态
            mBluetoothStateFilter?.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            mBluetoothStateFilter?.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            mBluetoothStateFilter?.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            mBluetoothStateFilter?.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            mBluetoothStateFilter?.addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
        }
        mBluetoothStateFilter?.addAction(Intent.ACTION_HEADSET_PLUG)
        mBluetoothStateFilter?.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        mContext?.registerReceiver(
            mBtAndAudioStateBroadcastReceiver,
            mBluetoothStateFilter
        )
    }

    fun getPreferedAudioSource():Int{
        if(isBTHeadsetConnected()){
            return MediaRecorder.AudioSource.VOICE_COMMUNICATION
        }else if(isWiredPlugged()){
            return MediaRecorder.AudioSource.DEFAULT
        }else {
            return MediaRecorder.AudioSource.MIC
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun getPreferedDeviceInfo(): AudioDeviceInfo? {
        var audioDeviceInfo = mAudioManager?.getDevices(GET_DEVICES_INPUTS)
        if (audioDeviceInfo?.size!! <= 1) return null
        for (audioDevice: AudioDeviceInfo in audioDeviceInfo) {
            var targetDevice = if (mCurrentMicSource == MicSource.SCO) {
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            } else {
                AudioDeviceInfo.TYPE_WIRED_HEADSET
            }
            if (audioDevice?.type == targetDevice) {
                log.e(
                    "[TaAudioManager] Prefered Connected Device  Info :" +
                            "type = ${audioDevice?.type}," +
                            "product name = ${audioDevice?.productName}"
                )
                return audioDevice
            }
        }
        return null
    }

    private fun isSupportBluetooth(): Boolean {
        return mBluetoothAdapter != null
    }

    private fun isSupportBluetoothSco(): Boolean {
        return isSupportBluetooth() && mAudioManager?.isBluetoothScoAvailableOffCall()!!
    }

    private fun isBTOn(): Boolean {
        val blueAdapter = BluetoothAdapter.getDefaultAdapter()
        return blueAdapter.isEnabled
    }

    /**
     * 判断蓝牙耳机是否连接
     */
    fun isBTHeadsetConnected(): Boolean {
        val headset =
            mBluetoothAdapter?.getProfileConnectionState(BluetoothProfile.HEADSET)
        return mIsBluetoothHeadsetConnected || (headset == BluetoothAdapter.STATE_CONNECTED)
    }

    fun isBTorWiredHeadsetConnected(): Boolean {
//        log.e(
//            "[TaAudioManager] [Bluetooth] isBTorWiredHeadsetConnected() isBTHeadsetConnected = ${isBTHeadsetConnected()}" +
//                    ",isWiredPlugged = ${isWiredPlugged()} "
//        )
        return isBTHeadsetConnected() || isWiredPlugged()
    }

    fun isBTScoConnected(): Boolean {
        return (mAudioManager != null && mIsBluetoothHeadsetScoConnected)
    }

    fun isWiredPlugged(): Boolean {
        if (Build.VERSION.SDK_INT < 23)
            return (mAudioManager != null && mAudioManager?.isWiredHeadsetOn!!)
        else {
            if (mAudioManager == null) {
                return false
            } else {
                var audioDeviceInfo = mAudioManager!!.getDevices(GET_DEVICES_INPUTS)
                for (deviceInfo in audioDeviceInfo) {
                    if (deviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                        log.e("[TaAudioManager] [Bluetooth] deviceInfo.type ${deviceInfo.type}")
                        return true
                    }
                }
                return false
            }

        }


    }

    private fun setAudioManagerInCallMode() {
        log.w(
            "[TaAudioManager] [Bluetooth] setAudioManagerInCallMode() mode = ${mAudioManager?.mode.toString()}"
        )
        if (("huawei").equals(DeviceUtils.getDeviceBrand().toLowerCase())) {
            if (mAudioManager?.mode != AudioManager.MODE_IN_CALL) {
                log.w(
                    "[TaAudioManager] [Bluetooth] Changing audio mode to MODE_IN_COMMUNICATION and requesting STREAM_VOICE_CALL focus"
                )
                mAudioManager?.mode = AudioManager.MODE_IN_CALL
            }
        } else {
            if (mAudioManager?.mode != AudioManager.MODE_IN_COMMUNICATION) {
                log.w(
                    "[TaAudioManager] [Bluetooth] Changing audio mode to MODE_IN_COMMUNICATION and requesting STREAM_VOICE_CALL focus"
                )
                mAudioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            } else {
                mAudioManager?.mode = AudioManager.MODE_NORMAL
                log.w(
                    "[TaAudioManager] [Bluetooth] setAudioManagerInCallMode() mode = ${mAudioManager?.mode.toString()}"
                )
                mAudioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            }
        }
        log.w(
            "[TaAudioManager] [Bluetooth] setAudioManagerInCallMode() mode = ${mAudioManager?.mode.toString()}"
        )
    }

    public fun requestAudioFocus(stream: Int) {
        if (!mAudioFocused) {
            var res = AudioManager.AUDIOFOCUS_REQUEST_FAILED
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mPlaybackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mPlaybackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(false)//告知系统在失去焦点时，系统可以自动降低本app音量
                    .setOnAudioFocusChangeListener(mAudioFocusListener)
                    .build()
                res = mAudioManager!!.requestAudioFocus(mFocusRequest)
            } else {
                res = mAudioManager!!.requestAudioFocus(
                    mAudioFocusListener, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
                )
            }
            log.e(
                "[TaAudioManager] Audio focus requested: "
                        + if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied"
            )
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                mAudioFocused = true
                sendLocalMessage(SCO_CHECK, 200)
            }
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager?.abandonAudioFocusRequest(mFocusRequest)
        } else {
            mAudioManager?.abandonAudioFocus(mAudioFocusListener)
        }
    }

    @Synchronized
    private fun changeBluetoothSco(enable: Boolean) { // IT WILL TAKE A CERTAIN NUMBER OF CALLS TO EITHER START/STOP BLUETOOTH SCO FOR IT TO WORK
        if (enable && mIsBluetoothHeadsetScoConnected) {
            log.e("[TaAudioManager] [Bluetooth] SCO already enabled, skipping")
            return
        } else if (!enable && !mIsBluetoothHeadsetScoConnected) {
            log.e("[TaAudioManager] [Bluetooth] SCO already disabled, skipping")
            return
        }
        object : Thread() {
            override fun run() {
                mIsRetrying = true
                var resultAcknowledged: Boolean
                var retries = 0
                do {
                    CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                        delay(800)
                    }
                    synchronized(this@TaAudioManager) {
                        if (enable) {
                            log.e(
                                "[TaAudioManager] [Bluetooth] Starting SCO: try number "
                                        + retries
                            )
                            mAudioManager?.setStreamVolume(
                                AudioManager.STREAM_VOICE_CALL,
                                mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                                0
                            )
                            mAudioManager?.startBluetoothSco()
                        } else {
                            log.e(
                                "[TaAudioManager] [Bluetooth] Stopping SCO: try number "
                                        + retries
                            )
                            mAudioManager?.stopBluetoothSco()
                        }
                        resultAcknowledged = isBTScoConnected() == enable
                        retries++
                    }
                } while (!resultAcknowledged && retries < 20)
                mIsRetrying = false
            }
        }.start()
    }

    private fun initHeadsetState() {
        if(!isBTorWiredHeadsetConnected()){
            mCurrentMicSource = MicSource.BUILTIN
            bluetoothStateListeners.forEach { it.onDisconnected() }
        }else if(isBTHeadsetConnected()){
            if (isBTScoConnected()) {
                log.e("[TaAudioManager] initHeadsetState() isBTScoConnected = ${isBTScoConnected()}")
                mIsBluetoothHeadsetScoConnected = true
            } else {
                mIsBluetoothHeadsetScoConnected = false
                initAudioSco()
            }
            mCurrentMicSource = MicSource.SCO
        }else if(isWiredPlugged()){
            mCurrentMicSource = MicSource.WIRED
        }
    }

    fun restartBT() {
        log.e("[TaAudioManager] 要求重启蓝牙！")
        isOurRequestToDisableBT = true
        mBluetoothAdapter?.disable()
        try_times = 0
    }

    private fun sendLocalMessage(message: Int, delay: Long) {
        var msg = Message()
        msg.what = message
        if (message == SCO_CHECK) {
            sco_try_times = 0
        }
        handler.sendMessageDelayed(msg, delay)
    }

    private var isOurRequestToDisableBT = false
    private val SWITCH_TO_SYSTEM_RECORD: Int = 111
    private var try_times: Int = 0
    private var sco_try_times: Int = 0
    private val OPEN_BT: Int = 112
    private val SCO_CHECK: Int = 113
    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                OPEN_BT -> {
                    if (mBluetoothAdapter != null && !mBluetoothAdapter!!.isEnabled && try_times < 3) {
                        try_times++
                        mBluetoothAdapter?.enable()
                        sendLocalMessage(OPEN_BT, 1000)
                    }
                }
                SCO_CHECK -> {
                    log.e(
                        "[TaAudioManager] SCO_CHECK isAudioFocusGained = ${mAudioFocused} isScoConnected = ${isBTScoConnected()}！" +
                                "isBluetoothA2dpOn = ${mAudioManager?.isBluetoothA2dpOn} try times = ${sco_try_times} "
                    )
                    if (sco_try_times > 2 && mIsRetrying) {
                        return
                    }
                    if (isBTHeadsetConnected() && !isBTScoConnected() && mAudioFocused) {
//                        openSco()
                        initAudioSco()
                        sco_try_times++
                    }
                }
            }
        }
    }


    // Bluetooth
    @Synchronized
    fun bluetoothHeadetConnectionChanged(connected: Boolean) {
        mIsBluetoothHeadsetConnected = connected
        when (connected) {
            false -> {
                mCurrentMicSource = MicSource.BUILTIN
                if (AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER)!= null && (AzeroManager.getInstance().getHandler(AzeroManager.AUDIO_HANDLER) as MediaPlayerHandler).isPlaying) {
                    mIsChangeToPauseFromBtDisconnected = true
                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE)
                }
            }
            true -> {
                mCurrentMicSource = MicSource.SCO
                if (mIsChangeToPauseFromBtDisconnected) {
                    mIsChangeToPauseFromBtDisconnected = false
                    AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY)
                }
            }

        }
    }

    @Synchronized
    fun bluetoothHeadetAudioConnectionChanged(connected: Boolean) {
        mAudioManager?.isBluetoothScoOn = connected
        mIsBluetoothHeadsetScoConnected = if (connected) {
            mIsBluetoothHeadsetScoConnected
        } else {
            connected
        }
    }

    @Synchronized
    fun bluetoothHeadetScoConnectionChanged(connected: Boolean) {
        mIsBluetoothHeadsetScoConnected = connected
        mAudioManager?.isBluetoothScoOn = connected
    }

    private val mAudioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            log.e("[TaAudioManager] Audio Focus GAIN！")
            mAudioFocused = true
            audioStateListeners.forEach { it.onFucusGain() }
            sendLocalMessage(SCO_CHECK, 200)
            if (mIsChangeToPauseFromFocusLoss) {
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PLAY)
                mIsChangeToPauseFromFocusLoss = false
            }
            AudioInputManager.getInstance().startAudioInput()
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            log.e("[TaAudioManager] Audio Focus LOSS 提醒等！")
            mAudioFocused = false
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS) {//微信或者qq语音视频接通状态
            log.e("[TaAudioManager] Audio Focus LOSS！")
            audioStateListeners.forEach { it.onFucusLoss() }
            mAudioFocused = false
            if (AzeroManager.getInstance().isEngineInitComplete && (AzeroManager.getInstance().getHandler(
                    AzeroManager.AUDIO_HANDLER
                ) as MediaPlayerHandler).isPlaying
            ) {
                mIsChangeToPauseFromFocusLoss = true
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_PAUSE)
            }
            AudioInputManager.getInstance().stopAudioInput()
            exitAudioSco()
        }
    }

    private var mBtAndAudioStateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                // 蓝牙耳机主动进行状态变化
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothAdapter.ERROR
                    )
                    when (state) {
                        BluetoothAdapter.STATE_CONNECTED -> {
                            log.e("[TaAudioManager] 蓝牙设备连接！")
                            bluetoothHeadetConnectionChanged(true)
                            bluetoothStateListeners.forEach { it.onConnected() }
                            sendLocalMessage(SCO_CHECK, 500)
                        }
                        BluetoothAdapter.STATE_DISCONNECTED -> {
                            log.e("[TaAudioManager] 蓝牙设备断开连接！")
                            bluetoothHeadetConnectionChanged(false)
                            mIsBluetoothHeadsetScoConnected = false
                            if (!isOurRequestToDisableBT) {
                                bluetoothStateListeners.forEach { it.onDisconnected() }
                            }
                        }
                        else -> log.e("[TaAudioManager] 蓝牙设备其他状态，暂未处理！")
                    }
                }
                // 手机蓝牙主动进行 蓝牙状态连接或断开
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    var state =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            log.e("[TaAudioManager] 手机蓝牙关闭！")
                            mIsBluetoothHeadsetScoConnected = false
                            bluetoothHeadetConnectionChanged(false)
                            if (isOurRequestToDisableBT) {
                                sendLocalMessage(OPEN_BT, 500)
                            } else {
                                HeadsetStateMachine.getHeadsetState()
                                    ?.handleHeadsetState(HeadsetStateMachine.State.BLUETOOTHOFF)
                            }
                        }
                        BluetoothAdapter.STATE_ON -> {
                            log.e("[TaAudioManager] 手机蓝牙开启！")
                            bluetoothHeadetConnectionChanged(true)
                            if (isOurRequestToDisableBT) {
                                isOurRequestToDisableBT = false
                            } else {
                                HeadsetStateMachine.getHeadsetState()
                                    ?.handleHeadsetState(HeadsetStateMachine.State.BLUETOOTHON)
                            }
                        }
                        else -> log.e("[TaAudioManager] 手机蓝牙其他状态，暂未处理！")
                    }
                }
                //有线耳机的插入或者断开状态
                Intent.ACTION_HEADSET_PLUG -> {
                    if (intent.hasExtra("state")) {
                        if (intent.getIntExtra("state", 0) == 0) {
                            log.e("[TaAudioManager] 有线耳机断开！")
                            if (mCurrentMicSource == MicSource.WIRED) {
                                mCurrentMicSource = MicSource.BUILTIN
                                bluetoothStateListeners.forEach { it.onDisconnected() }
                            }
                        } else {
                            log.e("[TaAudioManager] 有线耳机插入！")
                            if (!isBTHeadsetConnected()) {
                                bluetoothStateListeners.forEach { it.onConnected() }
                                mCurrentMicSource = MicSource.WIRED
                                com.soundai.azero.azeromobile.impl.audioinput.AudioInputManager.getInstance()
                                    .startAudioInput()
                            }
                        }
                    }
                }
                //有线或者蓝牙耳机 断开的广播，该广播较快
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    log.e("[TaAudioManager] 耳机拔出或者退出！")
                }

                //Audio SCO连接状态广播
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val state = intent.getIntExtra(
                        AudioManager.EXTRA_SCO_AUDIO_STATE,
                        AudioManager.SCO_AUDIO_STATE_ERROR
                    )
                    when (state) {
                        AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                            log.e("[TaAudioManager] SCO_AUDIO_STATE_CONNECTED！")
                            if (!mAudioFocused) return
                            bluetoothHeadetScoConnectionChanged(true)
                            HeadsetStateMachine.getHeadsetState()
                                ?.handleHeadsetState(HeadsetStateMachine.State.BLUETOOTHSCOCONNECTED)
                            com.soundai.azero.azeromobile.impl.audioinput.AudioInputManager.getInstance()
                                .startAudioInput()
                        }
                        AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                            log.e("[TaAudioManager] SCO_AUDIO_STATE_DISCONNECTED！")
                            bluetoothHeadetScoConnectionChanged(false)
                            if (isOurRequestToDisableBT) {
                                return
                            }
                            if (isBTHeadsetConnected()) {
                                sendLocalMessage(SCO_CHECK, 1500)
                            }
                            HeadsetStateMachine.getHeadsetState()
                                ?.handleHeadsetState(HeadsetStateMachine.State.BLUETOOTHSCODISCONNECTED)
                        }
                        AudioManager.SCO_AUDIO_STATE_CONNECTING -> {
                            log.e("[TaAudioManager] SCO_AUDIO_STATE_CONNECTING！")
                        }
                    }
                }

                //Bluetooth A2DP连接状态广播
                BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_AUDIO_DISCONNECTED
                    )
                    when (state) {
                        BluetoothHeadset.STATE_AUDIO_CONNECTED -> {
                            log.e("[TaAudioManager] Bluetooth headset A2DP connected")
                            bluetoothHeadetAudioConnectionChanged(true)
                        }
                        BluetoothHeadset.STATE_AUDIO_DISCONNECTED -> {
                            log.e("[TaAudioManager] Bluetooth headset A2DP disconnected")
                            bluetoothHeadetAudioConnectionChanged(false)
                        }
                        BluetoothHeadset.STATE_AUDIO_CONNECTING -> {
                            log.e("[TaAudioManager] Bluetooth headset A2DP connecting")
                        }
                        else -> log.e("[TaAudioManager] Bluetooth headset A2DP state to ${state}")
                    }
                }

                BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT -> {
                    val command =
                        intent.getStringExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD)
                    val type = intent.getIntExtra(
                        BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE,
                        -1
                    )

                    val commandType: String
                    commandType = when (type) {
                        BluetoothHeadset.AT_CMD_TYPE_ACTION -> "AT Action"
                        BluetoothHeadset.AT_CMD_TYPE_READ -> "AT Read"
                        BluetoothHeadset.AT_CMD_TYPE_TEST -> "AT Test"
                        BluetoothHeadset.AT_CMD_TYPE_SET -> "AT Set"
                        BluetoothHeadset.AT_CMD_TYPE_BASIC -> "AT Basic"
                        else -> "AT Unknown"
                    }

                    log.e("[TaAudioManager] BT vendor action = ${commandType} : ${command}！")
                }
            }
        }
    }


}
