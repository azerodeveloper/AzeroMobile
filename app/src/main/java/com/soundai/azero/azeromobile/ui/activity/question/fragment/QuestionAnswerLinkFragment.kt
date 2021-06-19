package com.soundai.azero.azeromobile.ui.activity.question.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.question.AnswerQuestionTemplate
import com.soundai.azero.azeromobile.common.bean.question.QuestionResultTemplate
import com.soundai.azero.azeromobile.common.bean.question.SendQuestionTemplate
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.safeNavigate
import com.soundai.azero.azeromobile.ui.activity.base.fragment.BaseFragment
import com.soundai.azero.azeromobile.ui.activity.question.QuestionViewModel
import com.soundai.azero.azeromobile.utils.DigitalConversion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@SuppressLint("SetTextI18n")
class QuestionAnswerLinkFragment : BaseFragment(), IQuestionProcessingItem {
    private lateinit var questionViewModel: QuestionViewModel
    private val titleRenderAnimator by lazy {
        val animator = ObjectAnimator.ofFloat(0f, 1f)
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            questionViewModel.sendQuestionTemplate.value?.let { value ->
                val position = (value.title.length * it.animatedFraction).toInt()
                tvTitle.text = value.title.subSequence(0, position)
            }
        }
        animator.doOnStart {
            questionViewModel.sendQuestionTemplate.value?.let { value ->
                btnGroup.visibility = View.GONE
                tvNum.text = "第${DigitalConversion.numberToChinese(value.questionIndex + 1)}题"
            }
        }
        animator.doOnEnd {
            questionViewModel.sendQuestionTemplate.value?.let { value ->
                tvTitle.text = value.title
            }
        }
        animator
    }

    private val sendQuestionObserver = Observer<SendQuestionTemplate> { onSendQuestion(it) }
    private val answerQuestionObserver = Observer<AnswerQuestionTemplate> { onAnswerQuestion(it) }
    private val questionResultObserver = Observer<QuestionResultTemplate> { onQuestionResult(it) }

    private lateinit var tvNum: TextView
    private lateinit var tvTitle: TextView
    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button
    private lateinit var btnGroup: Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log.d("QuestionAnswerLinkFragment onCreateView")
        val root = inflater.inflate(R.layout.fragment_question_answer_link, container, false)
        initView(root)
        initViewModel()
        return root
    }

    private fun initViewModel() {
        questionViewModel = ViewModelProviders.of(activity!!).get(QuestionViewModel::class.java)
        with(questionViewModel) {
            sendQuestionTemplate.observe(activity!!, sendQuestionObserver)
            answerQuestionTemplate.observe(activity!!, answerQuestionObserver)
            questionResultTemplate.observe(activity!!, questionResultObserver)
        }
    }

    private fun showQuestionInfo() {
        questionViewModel.sendQuestionTemplate.value?.let {
            btnGroup.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                btnA.setTextColor(activity!!.getColor(R.color.textInfo))
                btnB.setTextColor(activity!!.getColor(R.color.textInfo))
                btnC.setTextColor(activity!!.getColor(R.color.textInfo))
                btnD.setTextColor(activity!!.getColor(R.color.textInfo))
            } else {
                val res = activity!!.resources
                btnA.setTextColor(res.getColor(R.color.textInfo))
                btnB.setTextColor(res.getColor(R.color.textInfo))
                btnC.setTextColor(res.getColor(R.color.textInfo))
                btnD.setTextColor(res.getColor(R.color.textInfo))
            }

            btnA.background = activity!!.getDrawable(R.drawable.corner_btn_question)
            btnB.background = activity!!.getDrawable(R.drawable.corner_btn_question)
            btnC.background = activity!!.getDrawable(R.drawable.corner_btn_question)
            btnD.background = activity!!.getDrawable(R.drawable.corner_btn_question)

            tvNum.text = "第${DigitalConversion.numberToChinese(it.questionIndex + 1)}题"
            tvTitle.text = it.title
            btnA.text = "1.${it.answers[0]?.content}"
            btnB.text = "2.${it.answers[1]?.content}"
            btnC.text = "3.${it.answers[2]?.content}"
            btnD.text = "4.${it.answers[3]?.content}"
            questionViewModel.answerQuestionTemplate.value?.run {
                onAnswerQuestion(this)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        titleRenderAnimator.cancel()
        removeObservers()
    }

    private fun initView(root: View) {
        tvNum = root.findViewById(R.id.tv_question_num)
        tvTitle = root.findViewById(R.id.tv_question_title)
        btnA = root.findViewById(R.id.btn_a)
        btnB = root.findViewById(R.id.btn_b)
        btnC = root.findViewById(R.id.btn_c)
        btnD = root.findViewById(R.id.btn_d)
        btnGroup = root.findViewById(R.id.gp_option_btn)

        btnA.setOnClickListener { questionViewModel.sendSelect("A") }
        btnB.setOnClickListener { questionViewModel.sendSelect("B") }
        btnC.setOnClickListener { questionViewModel.sendSelect("C") }
        btnD.setOnClickListener { questionViewModel.sendSelect("D") }
    }

    private fun onSendQuestion(sendQuestionTemplate: SendQuestionTemplate) {
        if (questionViewModel.showQuestionInfoImmediately) {
            questionViewModel.showQuestionInfoImmediately = false
            showQuestionInfo()
            return
        }
        val timeDiff = System.currentTimeMillis() - sendQuestionTemplate.timeReceive
        val showOptionTime = sendQuestionTemplate.showOptionTime - timeDiff
        val showTitleTime = max(sendQuestionTemplate.showTitleTime - timeDiff, 0)
        log.d("showOptionTime:${showOptionTime}")
        if (showOptionTime > 0) {
            titleRenderAnimator.duration = showOptionTime - showTitleTime
            titleRenderAnimator.startDelay = showTitleTime
            titleRenderAnimator.start()
            launch(coroutineExceptionHandler) {
                delay(showOptionTime)
                showQuestionInfo()
            }
        } else {
            showQuestionInfo()
        }
    }

    private fun onQuestionResult(questionResultTemplate: QuestionResultTemplate) {
        if (questionViewModel.sendQuestionTemplate.value?.questionIndex != questionResultTemplate.questionIndex) {
            return
        }
        val correctAnswer = questionResultTemplate.correctAnswer
        val submitAnswer = questionViewModel.submitAnswer
        when {
            correctAnswer == submitAnswer -> onCorrect(correctAnswer)
            submitAnswer.isEmpty() -> onCorrect(correctAnswer)
            else -> onWrong(correctAnswer, submitAnswer)
        }
        with(questionResultTemplate) {
            if ((questionViewModel.isWatcher || correctAnswer != submitAnswer) && questionIndex + 1 == totalQuestionNumber) {
                navigateToFailedPage(5000)
            } else if (correctAnswer != submitAnswer && !questionViewModel.isWatcher) {
                questionViewModel.isWatcher = true
                navigateToFailedPage(2000)
            }
        }
    }

    private fun navigateToFailedPage(delay: Long) {
        launch(coroutineExceptionHandler) {
            delay(delay)
            activity!!.findNavController(R.id.question_nav_host_fragment)
                .safeNavigate(
                    R.id.action_questionProcessingFragment_to_questionFailedFragment,
                    R.id.questionProcessingFragment
                )
        }
    }

    private fun onAnswerQuestion(answerQuestionTemplate: AnswerQuestionTemplate) {
        if (questionViewModel.sendQuestionTemplate.value?.questionIndex == answerQuestionTemplate.questionIndex) {
            questionViewModel.submitAnswer = answerQuestionTemplate.submitAnswer
            onSelect(answerQuestionTemplate.submitAnswer)
        }
    }

    private fun onWrong(correctAnswer: String, submitAnswer: String) {
        val btnCorrect = getButton(correctAnswer)
        val btnWrong = getButton(submitAnswer)
        btnCorrect.background = activity!!.getDrawable(R.drawable.gradient_function_button)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnCorrect.setTextColor(activity!!.getColor(R.color.textPrimary))
            btnWrong.setTextColor(activity!!.getColor(R.color.textPrimary))
        } else {
            btnCorrect.setTextColor(activity!!.resources.getColor(R.color.textPrimary))
            btnWrong.setTextColor(activity!!.resources.getColor(R.color.textPrimary))
        }
        if (!btnCorrect.text.contains("√")) btnCorrect.text = "${btnCorrect.text} √"
        btnWrong.background = activity!!.getDrawable(R.drawable.corner_gradient_question_wrong)
        if (!btnCorrect.text.contains("×"))
            btnWrong.text = "${btnWrong.text} ×"
    }

    private fun onCorrect(correctAnswer: String) {
        val btnCorrect = getButton(correctAnswer)
        btnCorrect.background =
            activity!!.getDrawable(R.drawable.gradient_function_button)
        btnCorrect.setTextColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity!!.getColor(R.color.textPrimary)
            } else {
                activity!!.resources.getColor(R.color.textPrimary)
            }
        )

        if (!btnCorrect.text.contains("√"))
            btnCorrect.text = "${btnCorrect.text} √"
    }

    private fun onSelect(selectAnswer: String) {
        val btnSelect = getButton(selectAnswer)
        btnSelect.background =
            activity!!.getDrawable(R.drawable.gradient_function_button)
        btnSelect.setTextColor(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity!!.getColor(R.color.textPrimary)
            } else {
                activity!!.resources.getColor(R.color.textPrimary)
            }
        )
    }

    private fun getButton(option: String): Button {
        return when (option) {
            "A" -> btnA
            "B" -> btnB
            "C" -> btnC
            "D" -> btnD
            else -> btnA
        }
    }

    override fun removeObservers() {
        if (this::questionViewModel.isInitialized) {
            questionViewModel.apply {
                sendQuestionTemplate.removeObserver(sendQuestionObserver)
                answerQuestionTemplate.removeObserver(answerQuestionObserver)
                questionResultTemplate.removeObserver(questionResultObserver)
            }
        }
    }
}
