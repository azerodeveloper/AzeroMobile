package com.soundai.azero.azeromobile.ui.activity.question.fragment.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.QuestionJoinTemplate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.activity.question.QuestionJoinActivity
import kotlinx.android.synthetic.main.fragment_question_join.view.*

class QuestionJoinFragment : BaseFragment() {
    companion object {
        fun newInstance(questionJoinTemplate: QuestionJoinTemplate): QuestionJoinFragment {
            val fragment = QuestionJoinFragment()
            val bundle = Bundle()
            bundle.putParcelable(Constant.EXTRA_TEMPLATE, questionJoinTemplate)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_question_join, container, false)
        val questionJoinTemplate =
            arguments!!.getParcelable(Constant.EXTRA_TEMPLATE) as QuestionJoinTemplate
        root.tv_time.text = questionJoinTemplate.time
        root.btn_check_rule.setOnClickListener { (activity as QuestionJoinActivity).jumpToRulePage() }
        return root
    }
}