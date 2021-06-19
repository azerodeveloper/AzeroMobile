package com.soundai.azero.azeromobile.ui.activity.runner

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.Chronometer
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.sendStopRunningEvent
import com.soundai.azero.azeromobile.ui.activity.template.BaseDisplayCardActivity
import com.soundai.azero.azeromobile.utils.runner.CountTimerUtil
import com.soundai.azero.lib_todayrunrecord.IRunRecorderInterface
import com.soundai.azero.lib_todayrunrecord.IRunStateCallback
import com.soundai.azero.lib_todayrunrecord.TodayRunManager
import com.soundai.azero.lib_todayrunrecord.TodayRunService
import com.soundai.azero.lib_todayrunrecord.bean.RunRecord
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class RunningActivity : BaseDisplayCardActivity() {
    private val tvNumberAnim: TextView by lazy { findViewById<TextView>(R.id.tv_number_anim) }
    private val speedItemView: View by lazy { findViewById<View>(R.id.runner_item_speed) }
    private val kcalItemView: View by lazy { findViewById<View>(R.id.runner_item_kcal) }

    private val tvMileage: TextView by lazy { findViewById<TextView>(R.id.tv_kilometer) }
    private val flCountTimer: FrameLayout by lazy { findViewById<FrameLayout>(R.id.fl_count_timer) }
    private val gpPause: Group by lazy { findViewById<Group>(R.id.gp_pause) }
    private val gpStop: Group by lazy { findViewById<Group>(R.id.gp_stop) }
    private val pauseButtonItemView: View by lazy { findViewById<View>(R.id.runner_button_item_pause) }
    private val resumeButtonItemView: View by lazy { findViewById<View>(R.id.runner_button_item_resume) }
    private val stopButtonItemView: View by lazy { findViewById<View>(R.id.runner_button_item_stop) }
    private val cmPasstime: Chronometer by lazy {
        findViewById<Chronometer>(R.id.cm_passtime).apply {
            base = SystemClock.elapsedRealtime()
        }
    }
    private val itbResume by lazy {
        resumeButtonItemView.findViewById<ImageView>(R.id.runner_detail_button_image).apply {
            setBackgroundResource(R.drawable.pb_btn_continue)
            setOnClickListener { iRunRecorderInterface?.resume() }
        }
    }
    private val itbPause by lazy {
        pauseButtonItemView.findViewById<ImageView>(R.id.runner_detail_button_image).apply {
            setBackgroundResource(R.drawable.pb_btn_stop)
            setOnClickListener { iRunRecorderInterface?.pause() }
        }
    }

    private val itbStop by lazy {
        stopButtonItemView.findViewById<ImageView>(R.id.runner_detail_button_image).apply {
            setBackgroundResource(R.drawable.pb_btn_end)
            val stopRunnable = Runnable {
                iRunRecorderInterface?.finish()
                sendStopRunningEvent()
            }
            val handler = Handler()
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> handler.postDelayed(stopRunnable, 3000)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handler.removeCallbacks(
                        stopRunnable
                    )
                }
                true
            }
        }
    }

    private lateinit var tvSpeed: TextView
    private lateinit var tvCalorie: TextView

    private var isFinish = false

    //运动计算相关
    private val decimalFormat = DecimalFormat("0.00")

    private lateinit var appearAnimStop: ObjectAnimator
    private lateinit var appearAnimPause: ObjectAnimator
    private lateinit var appearAnimResume: ObjectAnimator
    private var iRunRecorderInterface: IRunRecorderInterface? = null

    private val runStateCallback by lazy {
        object : IRunStateCallback.Stub() {
            override fun onFinish() {
//                finish()
            }

            override fun onResume() {
                launch(coroutineExceptionHandler) {
                    appearAnimResume.reverse()
                    appearAnimStop.reverse()
                    appearAnimPause.start()
                }
            }

            override fun onPause() {
                launch(coroutineExceptionHandler) {
                    appearAnimResume.start()
                    appearAnimStop.start()
                    appearAnimPause.reverse()
                }
            }

            override fun onUpdate(record: RunRecord?) {
                updateRecord(record ?: return)
            }

            override fun onError(reason: String?) {
                when (reason) {
                    "restTimeOut" -> finish()
                }
            }

            override fun onStart() {
                flCountTimer.visibility = View.GONE
            }
        }
    }

    private val runServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            isFinish = intent.getBooleanExtra("isFinish", false)
            iRunRecorderInterface = IRunRecorderInterface.Stub.asInterface(service)
            iRunRecorderInterface?.registerCallback(runStateCallback)
            val runState = iRunRecorderInterface?.currentRunState ?: ""
            when (runState) {
                TodayRunService.RUN_STATE_IDLE -> {
                    if (!isFinish) {
                        CountTimerUtil.start(tvNumberAnim, object : CountTimerUtil.AnimationState {
                            override fun start() {}
                            override fun repeat() {}
                            override fun end() {
                                iRunRecorderInterface?.start()
                            }
                        })
                    } else {
                        flCountTimer.visibility = View.GONE
                        gpPause.visibility = View.GONE
                        gpStop.visibility = View.GONE
                    }
                }
                TodayRunService.RUN_STATE_PAUSE -> {
                    flCountTimer.visibility = View.GONE
                    gpPause.visibility = View.GONE
                    gpStop.visibility = View.VISIBLE
                }
                else -> {
                    runStateCallback.onStart()
                }
            }
            val runRecord =
                if (isFinish) iRunRecorderInterface?.lastRunData else iRunRecorderInterface?.currentRunData
            updateRecord(runRecord ?: return)
        }
    }

    private fun updateRecord(record: RunRecord) {
        launch(coroutineExceptionHandler) {
            cmPasstime.text = formatSeconds(record.duration)
            tvCalorie.text = "${record.calorie.toInt()}"
            tvMileage.text = decimalFormat.format(record.distance / 1000.0)
            tvSpeed.text =
                if (decimalFormat.format(record.distribution) == "0.00") {
                    "--"
                } else {
                    decimalFormat.format(record.distribution)
                }
        }
    }

    override val layoutResId: Int
        get() = R.layout.activity_runner_detail

    override fun initView() {
        (speedItemView.findViewById(R.id.runner_detail_message_item_image) as ImageView)
            .setImageResource(R.drawable.pb_icon_speed)
        (kcalItemView.findViewById(R.id.runner_detail_message_item_image) as ImageView)
            .setImageResource(R.drawable.pb_icon_kcal)

        (speedItemView.findViewById(R.id.textView) as TextView).text = "配速"
        (kcalItemView.findViewById(R.id.textView) as TextView).text = "千卡"

        tvSpeed = speedItemView.findViewById(R.id.runner_detail_message_item_text)
        tvCalorie = kcalItemView.findViewById(R.id.runner_detail_message_item_text)

        (pauseButtonItemView.findViewById(R.id.runner_detail_button_text) as TextView).text =
            "暂停"
        (resumeButtonItemView.findViewById(R.id.runner_detail_button_text) as TextView).text =
            "继续"
        (stopButtonItemView.findViewById(R.id.runner_detail_button_text) as TextView).text =
            "长按结束"

        setAnimation()
        TodayRunManager.bindService(this, runServiceConnection)
    }

    private fun formatSeconds(duration: Long): String? {
        val hh =
            if (duration / 3600 > 9) (duration / 3600).toString() else "0${duration / 3600}"
        val mm =
            if (duration % 3600 / 60 > 9) (duration % 3600 / 60).toString() else "0${duration % 3600 / 60}"
        val ss =
            if (duration % 3600 % 60 > 9) (duration % 3600 % 60).toString() else "0${duration % 3600 % 60}"
        return "$hh:$mm:$ss"
    }

    private fun setAnimation() {
        val alphaHolder = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        appearAnimStop = ObjectAnimator.ofPropertyValuesHolder(itbStop, alphaHolder)
            .setDuration(500)
        appearAnimStop.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                itbStop?.isEnabled = isReverse
                if (!isReverse) {
                    gpStop.visibility = View.VISIBLE
                }
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                itbStop?.isEnabled = !isReverse
                if (isReverse) {
                    gpStop.visibility = View.GONE
                }
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
                itbStop?.isEnabled = true
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        appearAnimPause = ObjectAnimator.ofPropertyValuesHolder(itbPause, alphaHolder)
            .setDuration(500)
        appearAnimPause.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                itbPause?.isEnabled = isReverse
                if (!isReverse) {
                    gpPause.visibility = View.VISIBLE
                }
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                itbPause?.isEnabled = !isReverse
                if (isReverse) {
                    gpPause.visibility = View.GONE
                }
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
                itbPause?.isEnabled = true
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        appearAnimResume = ObjectAnimator.ofPropertyValuesHolder(itbResume, alphaHolder)
            .setDuration(500)
        appearAnimResume.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                itbResume?.isEnabled = isReverse
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                itbResume?.isEnabled = !isReverse
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
                itbResume?.isEnabled = true
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        iRunRecorderInterface?.unRegisterCallback(runStateCallback)
        unbindService(runServiceConnection)
    }

    fun finishInTimeout() {
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 10000)
    }
}