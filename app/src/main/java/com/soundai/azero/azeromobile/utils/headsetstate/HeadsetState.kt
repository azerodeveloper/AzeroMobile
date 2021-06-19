package com.soundai.azero.azeromobile.utils.headsetstate

/**
 * Description ：
 * Created by SoundAI jiesean on 2020-02-09.
 */
public abstract class HeadsetState constructor(headsetStateMachine: HeadsetStateMachine){
    var headsetStateMachine:HeadsetStateMachine?

    init {
        this.headsetStateMachine = headsetStateMachine
    }

    abstract fun handleHeadsetState(state: HeadsetStateMachine.State);

    abstract fun connectToSco();

    abstract fun disconnectToSco();

}