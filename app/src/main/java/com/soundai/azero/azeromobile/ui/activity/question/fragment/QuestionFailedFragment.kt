package com.soundai.azero.azeromobile.ui.activity.question.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.azero.sdk.AzeroManager
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.ContinueTemplate
import com.soundai.azero.azeromobile.ui.activity.question.QuestionViewModel
import com.soundai.azero.azeromobile.common.bean.question.SendQuestionTemplate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment

class QuestionFailedFragment : BaseFragment() {
    private lateinit var questionViewModel: QuestionViewModel
    private lateinit var continueBtn: Button
    private lateinit var exitBtn: Button

    private val sendQuestionObserver by lazy {
        Observer<SendQuestionTemplate> {
            questionViewModel.questionResultTemplate.value?.let { value ->
                if (value.questionIndex != it.questionIndex) {
                    back(false)
                }
            }
        }
    }

    private val continueObserver by lazy {
        Observer<ContinueTemplate> { back(true) }
    }

    private val exitHandler by lazy { Handler() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_question_failed, container, false)
        initView(root)
        initViewModel()
        return root
    }

    private fun initViewModel() {
        questionViewModel = ViewModelProviders.of(activity!!).get(QuestionViewModel::class.java)
        with(questionViewModel) {
            questionResultTemplate.value?.let {
                if (it.questionIndex + 1 == it.totalQuestionNumber) {
                    continueBtn.visibility = View.GONE
                    exitHandler.postDelayed({ exitBtn.performClick() }, 10000)
                } else {
                    continueBtn.visibility = View.VISIBLE
                }
            }
            continueTemplate.observe(activity!!, continueObserver)
            sendQuestionTemplate.observe(activity!!, sendQuestionObserver)
        }
    }

    private fun initView(root: View) {
        continueBtn = root.findViewById(R.id.btn_question_continue)
        exitBtn = root.findViewById(R.id.btn_question_exit)
        continueBtn.setOnClickListener {
            back(true)
        }
        exitBtn.setOnClickListener { questionViewModel.exitGame() }
    }

    private fun back(showImmediately: Boolean) {
        exitHandler.removeCallbacksAndMessages(null)
        questionViewModel.showQuestionInfoImmediately = showImmediately
        activity?.onBackPressed()
    }

    override fun onDestroyView() {
        exitHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
        questionViewModel.continueTemplate.removeObserver(continueObserver)
        questionViewModel.sendQuestionTemplate.removeObserver(sendQuestionObserver)
    }
}