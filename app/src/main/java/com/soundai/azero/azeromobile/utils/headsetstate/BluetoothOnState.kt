package com.soundai.azero.azeromobile.utils.headsetstate


/**
 * Description ：
 * Created by SoundAI jiesean on 2020-02-09.
 */
public class BluetoothOnState constructor(headsetStateMachine: HeadsetStateMachine):
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
                headsetStateMachine?.changeToBTConnected()
            }
            HeadsetStateMachine.State.BLUETOOTHOFF ->{
                headsetStateMachine?.changeToNormal()
            }
            HeadsetStateMachine.State.BLUETOOTHON ->{

            }
            HeadsetStateMachine.State.BLUETOOTHSCOCONNECTED ->{
                headsetStateMachine?.changeToSCOConnected()
            }
            HeadsetStateMachine.State.BLUETOOTHSCODISCONNECTED ->{

            }
        }

    }

}