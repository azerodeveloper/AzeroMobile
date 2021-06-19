package com.soundai.azero.azeromobile.ui.activity.question.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.activity.question.QuestionViewModel
import com.soundai.azero.azeromobile.ui.activity.question.adapter.QuestionHelpAdapter

class QuestionHelperFragment : BaseFragment() {
    private lateinit var questionViewModel: QuestionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.activity_question_help, container, false)
        val rvContent = root.findViewById<RecyclerView>(R.id.rv_content)
        questionViewModel = ViewModelProviders.of(activity!!).get(QuestionViewModel::class.java)
        questionViewModel.helpTemplate.value?.let {
            rvContent.adapter = QuestionHelpAdapter(it)
        }
        rvContent.layoutManager = LinearLayoutManager(activity!!)
        return root
    }
}