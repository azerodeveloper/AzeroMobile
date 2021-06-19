package com.soundai.azero.azeromobile.utils.headsetstate

/**
 * Description ：
 * Created by SoundAI jiesean on 2020-02-09.
 */
public class BluetoothConnectedState constructor(headsetStateMachine: HeadsetStateMachine):
    HeadsetState(headsetStateMachine) {
    override fun disconnectToSco() {
    }

    override fun connectToSco() {
//        BluetoothUtil.openSco()
    }

    public override fun handleHeadsetState(state: HeadsetStateMachine.State) {
        when(state){
            HeadsetStateMachine.State.BLUETOOTDISCONNECTED ->{
                headsetStateMachine?.changeToBTOn()
            }
            HeadsetStateMachine.State.BLUETOOTHCONNECTED ->{
            }
            HeadsetStateMachine.State.BLUETOOTHOFF ->{
                headsetStateMachine?.changeToNormal()
            }
            HeadsetStateMachine.State.BLUETOOTHON ->{
                headsetStateMachine?.changeToBTOn()
            }
            HeadsetStateMachine.State.BLUETOOTHSCOCONNECTED ->{
                headsetStateMachine?.changeToSCOConnected()
            }
            HeadsetStateMachine.State.BLUETOOTHSCODISCONNECTED ->{

            }
        }

    }

}