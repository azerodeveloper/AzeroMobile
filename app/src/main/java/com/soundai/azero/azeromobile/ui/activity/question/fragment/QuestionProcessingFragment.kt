package com.soundai.azero.azeromobile.ui.activity.question.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.*
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.safeNavigate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.activity.question.QuestionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min

class QuestionProcessingFragment : BaseFragment() {
    private lateinit var questionViewModel: QuestionViewModel

    private val sendQuestionObserver = Observer<SendQuestionTemplate> { onSendQuestion(it) }
    private val questionResultObserver = Observer<QuestionResultTemplate> { onQuestionResult(it) }
    private val voteResultObserver = Observer<VoteResultTemplate> { onVoteResult() }
    private val voteObserver = Observer<VoteTemplate> { onVote(it) }
    private val helpObserver = Observer<QuestionHelperTemplate> { onHelp() }
    private val timerStartObserver = Observer<Int> { onTimerStart(it) }
    private val ivHintObserver = Observer<Int?> { onIvHint(it) }

    private lateinit var root: View
    private lateinit var tvParticipantsNumber: TextView
    private lateinit var tvRemainNumber: TextView
    private lateinit var tvCountDown: TextView
    private lateinit var ivHint: ImageView
    private lateinit var ivCountDown: ImageView
    private lateinit var tvHelp: TextView
    private lateinit var tvWitness: TextView
    private var countDownTimer: CountDownTimer? = null
    private var currentFragment: Fragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_question_processing, container, false)
        initView(root)
        initViewModel()
        return root
    }

    private fun initView(root: View) {
        tvParticipantsNumber = root.findViewById(R.id.tv_participants_number)
        tvRemainNumber = root.findViewById(R.id.tv_remain_number)
        ivHint = root.findViewById(R.id.iv_img_hint)
        ivCountDown = root.findViewById(R.id.iv_count_down)
        tvCountDown = root.findViewById(R.id.tv_count_down)
        tvHelp = root.findViewById(R.id.tv_help)
        tvWitness = root.findViewById(R.id.tv_witness)
        tvHelp.setOnClickListener {
            if (questionViewModel.helpTemplate.value == null) {
                questionViewModel.getHelp()
            } else {
                findNavController().safeNavigate(
                    R.id.action_questionProcessingFragment_to_questionHelperFragment,
                    R.id.questionProcessingFragment
                )
            }
        }
        currentFragment = QuestionAnswerLinkFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_option_container, currentFragment!!).commit()
    }

    private fun initViewModel() {
        questionViewModel = ViewModelProviders.of(activity!!).get(QuestionViewModel::class.java)
        with(questionViewModel) {
            sendQuestionTemplate.observe(activity!!, sendQuestionObserver)
            questionResultTemplate.observe(activity!!, questionResultObserver)
            voteResultTemplate.observe(activity!!, voteResultObserver)
            voteTemplate.observe(activity!!, voteObserver)
            helpTemplate.observe(activity!!, helpObserver)
            timerStart.observe(activity!!, timerStartObserver)
            ivHint.observe(activity!!, ivHintObserver)
        }
    }

    private fun onTimerStart(time: Int) {
        log.d("timerStart time:${time}")
        ivCountDown.setImageDrawable(
            TaApp.application.getDrawable(R.drawable.dt_bj_countdown)
        )
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(time * 1000L, 1000) {
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
                tvCountDown.visibility = View.VISIBLE
                tvCountDown.text = (millisUntilFinished / 1000).toString()
            }
        }
        countDownTimer?.start()
    }

    private fun onIvHint(imgResId: Int?) {
        if (imgResId == null) {
            ivHint.visibility = View.GONE
        } else {
            ivHint.setImageDrawable(TaApp.application.getDrawable(imgResId))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onVote(voteTemplate: VoteTemplate) {
        currentFragment = QuestionVoteFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fl_option_container, currentFragment!!).commit()
        tvCountDown.text = 15.toString()
        tvRemainNumber.text = "当前人数${formatHumanNumber(voteTemplate.remainPeopleAmount)}"
    }

    @SuppressLint("SetTextI18n")
    private fun onQuestionResult(questionTemplate: QuestionResultTemplate) {
        tvParticipantsNumber.text =
            "参赛人数${formatHumanNumber(questionTemplate.joinPeopleAmount)}"
        tvRemainNumber.text =
            "当前人数${formatHumanNumber(questionTemplate.remainPeopleAmount)}"
        val correctAnswer = questionTemplate.correctAnswer
        countDownTimer?.cancel()
        if (questionViewModel.sendQuestionTemplate.value?.questionIndex != questionTemplate.questionIndex) {
            return
        }
        when {
            correctAnswer == questionViewModel.submitAnswer -> {
                ivHint.setImageDrawable(
                    TaApp.application.getDrawable(R.drawable.dt_icon_correct)
                )
                tvCountDown.visibility = View.GONE
                ivCountDown.setImageDrawable(
                    TaApp.application.getDrawable(R.drawable.dt_bj_correct)
                )
            }
            questionViewModel.submitAnswer.isEmpty() -> {
                ivHint.setImageDrawable(
                    TaApp.application.getDrawable(R.drawable.dt_icon_overtime)
                )
                tvCountDown.visibility = View.GONE
                ivCountDown.setImageDrawable(
                    TaApp.application.getDrawable(R.drawable.dt_bj_overtime)
                )
            }
            else -> {
                ivHint.setImageDrawable(TaApp.application.getDrawable(R.drawable.dt_icon_error))
                tvCountDown.visibility = View.GONE
                ivCountDown.setImageDrawable(
                    TaApp.application.getDrawable(R.drawable.dt_bj_error)
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onSendQuestion(sendQuestionTemplate: SendQuestionTemplate) {
        tvParticipantsNumber.text =
            "参赛人数${formatHumanNumber(sendQuestionTemplate.joinPeopleAmount)}"
        tvRemainNumber.text =
            "当前人数${formatHumanNumber(sendQuestionTemplate.remainPeopleAmount)}"

        ivHint.setImageDrawable(activity?.getDrawable(R.drawable.dt_img_hear))
        if (sendQuestionTemplate.questionIndex != 0 && questionViewModel.submitAnswer.isEmpty()) {
            questionViewModel.isWatcher = true
        }
        questionViewModel.submitAnswer = ""
        if (questionViewModel.isWatcher) {
            tvWitness.visibility = View.VISIBLE
        } else {
            tvWitness.visibility = View.GONE
        }
        if (!questionViewModel.showQuestionInfoImmediately) {
            val timeDiff = System.currentTimeMillis() - sendQuestionTemplate.timeReceive
            val showOptionTime = sendQuestionTemplate.showOptionTime - timeDiff
            launch(coroutineExceptionHandler) {
                delay(max(showOptionTime, 0))
                onTimerStart(
                    sendQuestionTemplate.countDown + (min(
                        showOptionTime,
                        0
                    ) / 1000).toInt()
                )
            }
        }
    }

    private fun onVoteResult() {
        findNavController().safeNavigate(
            R.id.action_questionProcessingFragment_to_questionWinFragment,
            R.id.questionProcessingFragment
        )
    }

    private fun onHelp() {
        findNavController().safeNavigate(
            R.id.action_questionProcessingFragment_to_questionHelperFragment,
            R.id.questionProcessingFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (currentFragment is IQuestionProcessingItem) {
            (currentFragment as IQuestionProcessingItem).removeObservers()
        }
        with(questionViewModel) {
            sendQuestionTemplate.removeObserver(sendQuestionObserver)
            questionResultTemplate.removeObserver(questionResultObserver)
            voteResultTemplate.removeObserver(voteResultObserver)
            voteTemplate.removeObserver(voteObserver)
            timerStart.removeObserver(timerStartObserver)
            ivHint.removeObserver(ivHintObserver)
        }
    }

    private fun formatHumanNumber(number: Int): String {
        val sb = StringBuffer()
        val b0 = BigDecimal("100")
        val b1 = BigDecimal("10000")
        val b2 = BigDecimal("100000000")
        val b3 = BigDecimal(number)

        var formatNumStr = ""
        var unit = ""

        // 以万为单位处理
        if (b3.compareTo(b1) == -1) {
            formatNumStr = b3.toString()
        } else if (b3.compareTo(b1) == 0 && b3.compareTo(b1) == 1
            || b3.compareTo(b2) == -1
        ) {
            unit = "万"
            formatNumStr = b3.divide(b1).toString()
        } else if (b3.compareTo(b2) == 0 || b3.compareTo(b2) == 1) {
            unit = "亿"
            formatNumStr = b3.divide(b2).toString()
        }
        if ("" != formatNumStr) {
            var i = formatNumStr.indexOf(".")
            if (i == -1) {
                sb.append(formatNumStr).append(unit)
            } else {
                i += 1
                val v = formatNumStr.substring(i, i + 1)
                if (v != "0") {
                    sb.append(formatNumStr.substring(0, i + 1)).append(unit)
                } else {
                    sb.append(formatNumStr.substring(0, i - 1)).append(unit)
                }
            }
        }
        return if (sb.isEmpty()) sb.append("0").toString() else sb.toString()
    }
}