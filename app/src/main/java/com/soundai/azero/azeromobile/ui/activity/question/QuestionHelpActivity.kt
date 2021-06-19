package com.soundai.azero.azeromobile.ui.activity.question

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.QuestionHelperTemplate
import com.soundai.azero.azeromobile.ui.activity.question.adapter.QuestionHelpAdapter
import com.soundai.azero.azeromobile.ui.activity.template.BaseDisplayCardActivity

class QuestionHelpActivity : BaseDisplayCardActivity() {
    companion object {
        fun start(activity: Context, payload: String) {
            activity.startActivity(
                Intent(
                    activity,
                    QuestionHelpActivity::class.java
                ).also { it.putExtra(Constant.EXTRA_TEMPLATE, payload) })
        }
    }
    private lateinit var rvContent: RecyclerView

    override val layoutResId = R.layout.activity_question_help

    override fun initView() {
        val payload = intent.getStringExtra(Constant.EXTRA_TEMPLATE)
        rvContent = findViewById(R.id.rv_content)
        rvContent.adapter = QuestionHelpAdapter(Gson().fromJson(payload, QuestionHelperTemplate::class.java))
        rvContent.layoutManager = LinearLayoutManager(this)
    }
}