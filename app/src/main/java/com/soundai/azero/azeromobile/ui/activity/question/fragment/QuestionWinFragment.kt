package com.soundai.azero.azeromobile.ui.activity.question.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.azero.sdk.AzeroManager
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.VoteResultTemplate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.activity.question.QuestionViewModel
import com.soundai.azero.azeromobile.ui.activity.wallet.WalletActivity
import com.soundai.azero.azeromobile.utils.Utils

class QuestionWinFragment : BaseFragment() {
    private lateinit var questionViewModel: QuestionViewModel

    private val voteResultObserver = Observer<VoteResultTemplate> { t -> onVoteResult(t) }

    private val exitHandler by lazy { Handler() }

    private lateinit var tvBonus: TextView
    private lateinit var btnCheckWallet: Button
    private lateinit var btnExit: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_question_win, container, false)
        initView(root)
        initViewModel()
        return root
    }

    private fun initView(root: View) {
        tvBonus = root.findViewById(R.id.tv_bonus)
        btnCheckWallet = root.findViewById(R.id.btn_check_wallet)
        btnExit = root.findViewById(R.id.btn_exit_game)
        btnExit.setOnClickListener { questionViewModel.exitGame() }
        btnCheckWallet.setOnClickListener {
            startActivity(Intent(activity!!, WalletActivity::class.java))
        }
        exitHandler.postDelayed({ btnExit.performClick() }, 50000)
    }

    private fun initViewModel() {
        questionViewModel = ViewModelProviders.of(activity!!).get(QuestionViewModel::class.java)
        questionViewModel.voteResultTemplate.observe(activity!!, voteResultObserver)
    }

    private fun onVoteResult(voteResultTemplate: VoteResultTemplate) {
        val bonus = voteResultTemplate.ownBonus
        val builder = SpannableStringBuilder(bonus)
        builder.setSpan(
            AbsoluteSizeSpan(Utils.dp2px(30f).toInt()), bonus.indexOf("."), bonus.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvBonus.text = builder
    }

    override fun onDestroyView() {
        super.onDestroyView()
        questionViewModel.voteResultTemplate.removeObserver(voteResultObserver)
    }
}