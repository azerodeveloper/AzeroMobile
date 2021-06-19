package com.soundai.azero.azeromobile.common.bean.question

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class AnswersItem(val content: String, val option: String)

data class SendQuestionTemplate(
    val questionIndex: Int,
    val showTitleTime: Long,
    val showOptionTime: Long,
    val joinPeopleAmount: Int,
    val answers: List<AnswersItem?>,
    val remainPeopleAmount: Int,
    val countDown: Int,
    val title: String,
    val timeReceive: Long
)

data class AnswerQuestionTemplate(val questionIndex: Int, val submitAnswer: String)

data class QuestionResultTemplate(
    val questionIndex: Int,
    val totalQuestionNumber: Int,
    val joinPeopleAmount: Int,
    val remainPeopleAmount: Int,
    val correctAnswer: String
)

data class VoteItem(val option: String, val content: String)

data class VoteTemplate(
    val remainPeopleAmount: Int,
    val prologue: String,
    val speechLastTime: Long,
    val voteEnum: List<VoteItem>
)

data class SubmitVoteTemplate(val vote: String? = null)

data class VoteResultTemplate(val ownBonus: String)

data class ContinueTemplate(val status: String)

data class QuestionHelperTemplate(val pageDisplay: List<HelpItem>)

@Parcelize
data class HelpItem(val title: String, val contents: List<String>) : Parcelable

@Parcelize
data class QuestionJoinTemplate(
    val isFirst: Boolean,
    val time: String,
    val title: String,
    val pageDisplay: List<HelpItem>
) : Parcelable