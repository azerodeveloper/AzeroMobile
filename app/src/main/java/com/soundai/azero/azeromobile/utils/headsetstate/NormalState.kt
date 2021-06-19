package com.soundai.azero.azeromobile.utils.headsetstate

/**
 * Description ：
 * Created by SoundAI jiesean on 2020-02-09.
 */
public class NormalState constructor(headsetStateMachine: HeadsetStateMachine):
    HeadsetState(headsetStateMachine) {
    override fun disconnectToSco() {
//        BluetoothUtil.changeAudioSource(0)
    }
    override fun connectToSco() {
    }

    public override fun handleHeadsetState(state: HeadsetStateMachine.State) {
        when(state){
            HeadsetStateMachine.State.BLUETOOTDISCONNECTED ->{

            }
            HeadsetStateMachine.State.BLUETOOTHCONNECTED ->{
//                headsetStateMachine?.setHeadsetState(HeadsetStateMachine.State.BLUETOOTHCONNECTED)
            }
            HeadsetStateMachine.State.BLUETOOTHOFF ->{
            }
            HeadsetStateMachine.State.BLUETOOTHON ->{
                headsetStateMachine?.changeToBTOn()
            }
            HeadsetStateMachine.State.BLUETOOTHSCOCONNECTED ->{
            }
            HeadsetStateMachine.State.BLUETOOTHSCODISCONNECTED ->{

            }
        }

    }

}