package com.soundai.azero.azeromobile.ui.activity.question.fragment.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.Constant.EXTRA_TEMPLATE
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.QuestionHelperTemplate
import com.soundai.azero.azeromobile.common.bean.question.QuestionJoinTemplate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.activity.question.adapter.QuestionHelpAdapter

class QuestionRuleFragment : BaseFragment() {
    companion object {
        fun newInstance(questionJoinTemplate: QuestionJoinTemplate): QuestionRuleFragment {
            val fragment = QuestionRuleFragment()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_TEMPLATE, questionJoinTemplate)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_question_rule, container, false)
        val rvContent = root.findViewById<RecyclerView>(R.id.rv_content)
        val questionJoinTemplate = arguments!!.getParcelable(EXTRA_TEMPLATE) as QuestionJoinTemplate
        rvContent.adapter =
            QuestionHelpAdapter(QuestionHelperTemplate(questionJoinTemplate.pageDisplay))
        rvContent.layoutManager = LinearLayoutManager(activity)
        return root
    }
}