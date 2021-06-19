package com.soundai.azero.azeromobile.ui.activity.question

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.azero.sdk.AzeroManager
import com.azero.sdk.util.Utils
import com.azero.sdk.util.log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.soundai.azero.azeromobile.common.bean.question.*
import org.json.JSONObject

class QuestionViewModel(
    application: Application
) : AndroidViewModel(application) {
    val voteResultTemplate: MutableLiveData<VoteResultTemplate> = MutableLiveData()
    val sendQuestionTemplate: MutableLiveData<SendQuestionTemplate> = MutableLiveData()
    val voteTemplate: MutableLiveData<VoteTemplate> = MutableLiveData()
    val submitVoteTemplate: MutableLiveData<SubmitVoteTemplate> = MutableLiveData()
    val answerQuestionTemplate: MutableLiveData<AnswerQuestionTemplate> = MutableLiveData()
    val questionResultTemplate: MutableLiveData<QuestionResultTemplate> = MutableLiveData()
    val continueTemplate: MutableLiveData<ContinueTemplate> = MutableLiveData()
    val helpTemplate: MutableLiveData<QuestionHelperTemplate> = MutableLiveData()
    val timerStart: MutableLiveData<Int> = MutableLiveData()
    val ivHint: MutableLiveData<Int?> = MutableLiveData()
    var submitAnswer = ""
    var showQuestionInfoImmediately = false
    var isWatcher = false

    fun update(template: String?) {
        if (template == null) {
            return
        }
        val json = JSONObject(template)
        log.e("QuestionViewModel, update template, scene= ${json.getString("scene")}")
        when (json.getString("scene")) {
            "voteResult" -> onVoteResult(template)
            "answerQuestion" -> onAnswerQuestion(template)
            "questionResult" -> onQuestionResult(template)
            "sendQuestion" -> onSendQuestion(template)
            "continue" -> onContinue(template)
            "vote" -> onVote(template)
            "submitVote" -> onSubmitVote(template)
            "gameHelp" -> onHelper(template)
        }
    }

    fun sendSelect(option: String) {
        val payload = JsonObject().apply {
            addProperty("type", "answerGameSend")
            addProperty("value", option)
        }
        sendUserEvent(payload)
    }

    fun exitGame() {
        val payload = JsonObject().apply {
            addProperty("type", "answerGameEnd")
        }
        sendUserEvent(payload)
    }

    fun continueGame() {
        val payload = JsonObject().apply {
            addProperty("type", "answerGameContinue")
        }
        sendUserEvent(payload)
    }

    fun getHelp() {
        val payload = JsonObject().apply {
            addProperty("type", "answerGameHelp")
        }
        sendUserEvent(payload)
    }

    private fun sendUserEvent(payload: JsonObject) {
        val header = JsonObject().apply {
            addProperty("namespace", "AzeroExpress")
            addProperty("name", "UserEvent")
            addProperty("messageId", Utils.getUuid())
        }
        val event = JsonObject().apply {
            add("event", JsonObject().also { event ->
                event.add("header", header)
                event.add("payload", payload)
            })
        }
        AzeroManager.getInstance().customAgent.sendEvent(event.toString())
    }

    private fun onSubmitVote(template: String) {
        submitVoteTemplate.postValue(Gson().fromJson(template, SubmitVoteTemplate::class.java))
    }

    private fun onVote(template: String) {
        voteTemplate.postValue(Gson().fromJson(template, VoteTemplate::class.java))
    }

    private fun onQuestionResult(template: String) {
        questionResultTemplate.postValue(
            Gson().fromJson(
                template,
                QuestionResultTemplate::class.java
            )
        )
    }

    private fun onSendQuestion(template: String) {
        sendQuestionTemplate.postValue(Gson().fromJson(template, SendQuestionTemplate::class.java))
    }

    private fun onAnswerQuestion(template: String) {
        answerQuestionTemplate.postValue(
            Gson().fromJson(
                template,
                AnswerQuestionTemplate::class.java
            )
        )
    }

    private fun onVoteResult(template: String) {
        voteResultTemplate.postValue(Gson().fromJson(template, VoteResultTemplate::class.java))
    }

    private fun onContinue(template: String) {
        continueTemplate.postValue(Gson().fromJson(template, ContinueTemplate::class.java))
    }

    private fun onHelper(template: String) {
        if (helpTemplate.value == null) {
            helpTemplate.postValue(Gson().fromJson(template, QuestionHelperTemplate::class.java))
        }
    }
}