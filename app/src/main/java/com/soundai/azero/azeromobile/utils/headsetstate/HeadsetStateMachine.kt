package com.soundai.azero.azeromobile.utils.headsetstate

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.azero.sdk.util.log

/**
 * Description ：
 * Created by SoundAI jiesean on 2020-02-09.
 */
object HeadsetStateMachine {
    var normalState: HeadsetState = NormalState(this)
    var bluetoothOnState: HeadsetState = BluetoothOnState(this)
    var bluetoothConnectedState: HeadsetState = BluetoothConnectedState(this)
    var scoConnectedState: HeadsetState = ScoConnectedState(this)
    var state:HeadsetState? = normalState
//    var handler: Handler = object :Handler(){
//        override fun handleMessage(msg: Message?) {
//            when(msg?.what){
//                SWITCH_TO_SYSTEM_RECORD -> {
//                    if (getHeadsetState() is BluetoothOnState){
//                        BluetoothUtil.changeAudioSource(0)
//                    }
//                }
//            }
//        }
//    }

    public enum class State{
        NORMAL,
        BLUETOOTHON,
        BLUETOOTHOFF,
        BLUETOOTHCONNECTED,
        BLUETOOTDISCONNECTED,
        BLUETOOTHSCOCONNECTED,
        BLUETOOTHSCODISCONNECTED,
        WIREDPLUGGED,
    }

    private fun setHeadsetState(nstate: HeadsetState){
        this.state = nstate
    }

    public fun getHeadsetState(): HeadsetState? {
//        log.e("=====EarMode HeadsetStateMachine getHeadsetState = ${this.state}")
        return this.state
    }

    public fun changeToBTConnected(){
        setHeadsetState(bluetoothConnectedState)
    }
    public fun changeToSCOConnected(){
        setHeadsetState(scoConnectedState)
//        changeAudioSource(1)
    }
    public fun changeToNormal(){
        setHeadsetState(normalState)
    }
    public fun changeToBTOn(){
        setHeadsetState(bluetoothOnState)
    }
}