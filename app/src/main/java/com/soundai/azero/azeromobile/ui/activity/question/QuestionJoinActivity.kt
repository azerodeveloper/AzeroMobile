package com.soundai.azero.azeromobile.ui.activity.question


import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.QuestionJoinTemplate
import com.soundai.azero.azeromobile.ui.activity.question.fragment.join.QuestionJoinFragment
import com.soundai.azero.azeromobile.ui.activity.question.fragment.join.QuestionRuleFragment
import com.soundai.azero.azeromobile.ui.activity.template.BaseDisplayCardActivity

class QuestionJoinActivity : BaseDisplayCardActivity() {
    companion object {
        fun start(activity: Context, payload: String) {
            activity.startActivity(
                Intent(
                    activity,
                    QuestionJoinActivity::class.java
                ).also { it.putExtra(Constant.EXTRA_TEMPLATE, payload) })
        }
    }

    override val layoutResId: Int = R.layout.activity_question_join

    private lateinit var questionJoinTemplate: QuestionJoinTemplate

    override fun initView() {
        val payload = intent.getStringExtra(Constant.EXTRA_TEMPLATE)
        questionJoinTemplate = Gson().fromJson(payload, QuestionJoinTemplate::class.java)
        if (questionJoinTemplate.isFirst) {
            jumpToRulePage()
        } else {
            jumpToJoinPage()
        }
    }

    fun jumpToJoinPage() {
        replaceFragment(QuestionJoinFragment.newInstance(questionJoinTemplate))
    }

    fun jumpToRulePage() {
        replaceFragment(QuestionRuleFragment.newInstance(questionJoinTemplate))
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_container, fragment)
            .commit()
    }
}