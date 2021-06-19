package com.soundai.azero.azeromobile.ui.activity.question.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.azero.sdk.AzeroManager
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.SubmitVoteTemplate
import com.soundai.azero.azeromobile.common.bean.question.VoteTemplate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.activity.question.QuestionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuestionVoteFragment : BaseFragment(), IQuestionProcessingItem {
    private lateinit var questionViewModel: QuestionViewModel
    private val submitVoteObserver = Observer<SubmitVoteTemplate> { onSubmitVote(it) }
    private val voteObserver = Observer<VoteTemplate> { onVote(it) }

    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var voteTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_question_choose, container, false)
        initView(root)
        initViewModel()
        return root
    }

    private fun initView(root: View) {
        btnA = root.findViewById(R.id.btn_a)
        btnB = root.findViewById(R.id.btn_b)
        voteTitle = root.findViewById(R.id.tv_vote_title)
    }

    private fun initViewModel() {
        questionViewModel = ViewModelProviders.of(activity!!).get(QuestionViewModel::class.java)
        questionViewModel.run {
            voteTemplate.observe(activity!!, voteObserver)
            submitVoteTemplate.observe(activity!!, submitVoteObserver)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

    private fun onSubmitVote(submitVoteTemplate: SubmitVoteTemplate) {
        when (submitVoteTemplate.vote) {
            "1" -> onSelect("A")
            "2" -> onSelect("B")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onVote(voteTemplate: VoteTemplate) {
        questionViewModel.ivHint.postValue(null)
        launch {
            delay(voteTemplate.speechLastTime)
            questionViewModel.timerStart.value = 15
        }
        btnA.text = "${voteTemplate.voteEnum[0].option}.${voteTemplate.voteEnum[0].content}"
        btnB.text = "${voteTemplate.voteEnum[1].option}.${voteTemplate.voteEnum[1].content}"
        btnA.setOnClickListener { questionViewModel.sendSelect("A") }
        btnB.setOnClickListener { questionViewModel.sendSelect("B") }
        voteTitle.text = voteTemplate.prologue
    }

    @SuppressLint("SetTextI18n")
    private fun onSelect(selectAnswer: String) {
        val btnSelect = getButton(selectAnswer)
        btnSelect.setTextColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity!!.getColor(R.color.textPrimary)
            } else {
                activity!!.resources.getColor(R.color.textPrimary)
            }
        )
        val text = btnSelect.text
        if (!text.contains("√")) {
            btnSelect.text = "$text √"
        }
    }

    private fun getButton(option: String): Button {
        return when (option) {
            "A" -> btnA
            "B" -> btnB
            else -> btnA
        }
    }

    override fun removeObservers() {
        if (this::questionViewModel.isInitialized) {
            questionViewModel.submitVoteTemplate.removeObserver(submitVoteObserver)
            questionViewModel.voteTemplate.removeObserver(voteObserver)
        }
    }
}
