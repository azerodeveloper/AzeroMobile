package com.soundai.azero.azeromobile.ui.activity.launcher

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.MotionEvent
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.azero.platforms.iface.AlexaClient
import com.azero.platforms.phonecontrol.PhoneCallController
import com.azero.sdk.AzeroManager
import com.azero.sdk.event.AzeroEvent
import com.azero.sdk.event.Command
import com.azero.sdk.impl.AzeroClient.AzeroClientHandler
import com.azero.sdk.impl.PhoneCallController.PhoneCallControllerHandler
import com.azero.sdk.impl.TemplateRuntime.TemplateDispatcher
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler
import com.azero.sdk.util.log
import com.github.paolorotolo.appintro.AppIntro
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.common.bean.weather.Weather
import com.soundai.azero.azeromobile.common.bean.weather.WeatherResponse
import com.soundai.azero.azeromobile.impl.audioinput.SpeechRecognizerHandler
import com.soundai.azero.azeromobile.impl.azeroexpress.AzeroExpressHandler
import com.soundai.azero.azeromobile.impl.phonecallcontroller.PhoneCallControllerImpl
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.system.LocaleModeHandle
import com.soundai.azero.azeromobile.system.TaAudioManager
import com.soundai.azero.azeromobile.ui.activity.guide.GuidePageActivity
import com.soundai.azero.azeromobile.ui.activity.launcher.head.WeatherHeadView
import com.soundai.azero.azeromobile.ui.activity.launcher.item.LauncherViewModel
import com.soundai.azero.azeromobile.ui.activity.question.QuestionActivity
import com.soundai.azero.azeromobile.ui.activity.question.QuestionHelpActivity
import com.soundai.azero.azeromobile.ui.activity.question.QuestionJoinActivity
import com.soundai.azero.azeromobile.ui.activity.runner.RunningActivity
import com.soundai.azero.azeromobile.ui.activity.template.EncyclopediaTemplateActivity
import com.soundai.azero.azeromobile.ui.activity.template.walk.StepCountActivity
import com.soundai.azero.azeromobile.ui.activity.template.weather.WeatherActivity
import com.soundai.azero.azeromobile.ui.widget.ASRDialog
import com.soundai.azero.azeromobile.ui.widget.WakeupButton
import com.soundai.azero.azeromobile.ui.widget.dialog.DialogManager
import com.umeng.message.PushAgent
import kotlinx.coroutines.*
import org.json.JSONObject

class LauncherActivity : AppIntro(), CoroutineScope by MainScope(),
    AzeroManager.AzeroOSListener {
    private var mIsTalkButtonLongPressed: Boolean = false
    private var ignoreOffset = false
    private lateinit var floatingActionButton: FloatingActionButton
    private var currentFragment: Fragment? = null
    private lateinit var skillFragment: SkillShowFragment
    private lateinit var listFragment: ListShowFragment
    private val bluetoothStateListener by lazy {
        object : TaAudioManager.BluetoothStateListener {
            override fun onConnected() {
                DialogManager.dismissAlertDialog(this@LauncherActivity)
            }

            override fun onDisconnected() {
                DialogManager.showAlertDialog(this@LauncherActivity,"请连接耳机使用")
                AzeroManager.getInstance().stopAllPlayers()
            }

            override fun onRecordFailed() {
                DialogManager.showAlertDialog(this@LauncherActivity,"录音被占用，无法使用")
                AzeroManager.getInstance().stopAllPlayers()
            }

            override fun onRecordSuccessful() {
                DialogManager.dismissAlertDialog(this@LauncherActivity)
            }
        }
    }

    private val launcherViewModel by lazy {
        ViewModelProviders.of(this).get(LauncherViewModel::class.java)
    }
    private val sphereObserver = Observer<String> { onSphereTemplate(it) }
    private val weatherObserver = Observer<String> { onWeatherTemplate(it) }
    private val body1Observer = Observer<String> { onBodyTemplate1(it) }
    private val runningObserver = Observer<String> { onRunningTemplate(it) }
    private val walkingObserver = Observer<String> { onWalkingTemplate(it) }
    private val questionObserver = Observer<String> { onQuestionTemplate(it) }
    private val pageObserver = Observer<Int> { onPageChange(it) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushAgent.getInstance(this).onAppStart()
        ASRDialog.attachToWindow(this)
        if (!AzeroManager.getInstance().isEngineInitComplete) {
            val intent = Intent(this, GuidePageActivity::class.java)
            this.startActivity(intent)
            finish()
        }
        AzeroManager.getInstance().addAzeroOSListener(this)
        LocaleModeHandle.switchLocaleMode(LocaleModeHandle.LocaleMode.HEADSET)

        floatingActionButton = WakeupButton.getInstance(this).floatingActionButton
        if (AzeroManager.getInstance().isEngineInitComplete) {
            initAzero()
            AzeroManager.getInstance().sendQueryText("获取指南球体数据")
        }
        TaAudioManager.addBluetoothStateListener(bluetoothStateListener)
        skillFragment = SkillShowFragment()
        listFragment = ListShowFragment()
        showSkipButton(false)
        showSeparator(false)
        showStatusBar(false)
        showDoneButton(false)
        showPagerIndicator(false)
        addSlide(PersonalFragment.newInstance())
        addSlide(skillFragment)
        addSlide(listFragment)
        window.decorView.post { pager.setCurrentItem(1, false) }
        setOffScreenPageLimit(3)
        currentFragment = skillFragment
        launch { delay(500);skillFragment.initData("", this@LauncherActivity) }
        if (TaAudioManager.isBTHeadsetConnected() && !TaAudioManager.isBTScoConnected()) {
            log.e("=====EarMode 无法建立sco连接，请连接耳机或者重启蓝牙后重试")
            TaAudioManager.restartBT()
        }
        initViewModel()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        if (newFragment == null) {
            return
        }
        currentFragment = newFragment
        if (newFragment is SkillShowFragment) {
            newFragment.startLoop()
            WakeupButton.getInstance(this@LauncherActivity).hide()
            ASRDialog.hide(0L)
        } else {
            ASRDialog.show()
            if (oldFragment == null) {
                return
            }
            if (oldFragment is SkillShowFragment) {
                oldFragment.stopLoop()
                oldFragment.hideSkillInfo()
            }
            WakeupButton.getInstance(this@LauncherActivity).show()
            WakeupButton.getInstance(this@LauncherActivity).show()
        }
    }

    fun getCurrentFragment(): Fragment? {
        return currentFragment
    }

    fun setFragment(index: Int) {
        super.setPageIndex(index)
    }

    fun showLauncherFromClearPlayinfo() {
        if (currentFragment == null || currentFragment == skillFragment) {
            return
        }
        setFragment(1)
    }

    fun showAsrText(asrText: String, gone: Boolean) {
        if (currentFragment != null) {
            if (currentFragment is SkillShowFragment) {
                var temp = asrText
                (currentFragment as SkillShowFragment).setAsrText(temp,gone)
            }
        }
    }

    fun getRecordData(byteArray: ByteArray, size: Int) {
        if (currentFragment != null && currentFragment is SkillShowFragment) {
            val fragment = currentFragment as SkillShowFragment
            if (fragment.isVisible) {
                fragment.getRecordData(byteArray, size)
            }
        }
    }

    private fun setTransition() {
        requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.exitTransition = Slide(Gravity.START)
    }

    private fun showListFragment() {
        if (currentFragment is ListShowFragment) {
            return
        }
        setFragment(2)
    }

    private fun showSkillFragment() {
        if (currentFragment is SkillShowFragment) {
            return
        }
        setFragment(1)
    }

    private fun initViewModel() {
        with(launcherViewModel) {
            sphereTemplate.observeForever(sphereObserver)
            weatherTemplate.observeForever(weatherObserver)
            bodyTemplate1.observeForever(body1Observer)
            runningTemplate.observeForever(runningObserver)
            walkingTemplate.observeForever(walkingObserver)
            questionTemplate.observeForever(questionObserver)
            page.observeForever(pageObserver)
        }
    }

    private fun removeViewModelObservers() {
        with(launcherViewModel) {
            sphereTemplate.removeObserver(sphereObserver)
            weatherTemplate.removeObserver(weatherObserver)
            bodyTemplate1.removeObserver(body1Observer)
            runningTemplate.removeObserver(runningObserver)
            walkingTemplate.removeObserver(walkingObserver)
            questionTemplate.removeObserver(questionObserver)
            page.removeObserver(pageObserver)
        }
    }

    private fun onSphereTemplate(payload: String) {
        launch(coroutineExceptionHandler) {
            skillFragment.initData(payload, this@LauncherActivity)
        }
    }

    private fun onWeatherTemplate(payload: String) {
        val weatherRsp = Gson().fromJson(payload, WeatherResponse::class.java)
        startActivity(Intent(this@LauncherActivity, WeatherActivity::class.java).also {
            it.putExtra(Constant.EXTRA_TEMPLATE, payload)
        })
        launcherViewModel.headView.postValue(WeatherHeadView(weatherRsp))
        val weather = Gson().fromJson(payload, Weather::class.java)
        if (weather.date.isNullOrEmpty() || weather.weather == null || weather.notSupport != null) {
            return
        }
        launch(coroutineExceptionHandler) {
            ignoreOffset = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = baseContext.resources.getColor(R.color.colorPrimary, null)
            } else {
                window.statusBarColor = baseContext.resources.getColor(R.color.colorPrimary)
            }
        }
    }

    private fun onBodyTemplate1(payload: String) {
        if (payload.contains("baike") || payload.contains("cookbook") || payload.contains("astrology")
            || payload.contains("poem")
        ) {
            showEncyclopediaTemplate(payload)
        }
    }

    private fun onRunningTemplate(payload: String) {
        startActivity(Intent(this@LauncherActivity, RunningActivity::class.java))
    }

    private fun onWalkingTemplate(payload: String) {
        startActivity(Intent(this@LauncherActivity, StepCountActivity::class.java))
    }

    private fun onQuestionTemplate(payload: String) {
        val scene = JSONObject(payload).getString("scene")
        log.e("show QuestionTemplate, scene= $scene")
        if ("join" == scene) {
            QuestionJoinActivity.start(this, payload)
        } else if ("exitGame" == scene) {
            val topActivity = ActivityLifecycleManager.getInstance().topActivity
            if (topActivity is QuestionActivity || topActivity is QuestionHelpActivity) {
                topActivity.finish()
            }
        } else {
            if ("gameHelp" == scene && ActivityLifecycleManager.getInstance().topActivity !is QuestionActivity)
                return
            QuestionActivity.start(this, payload)
        }
    }

    private fun onPageChange(page: Int) {
        when (page) {
            LauncherViewModel.SKILL_PAGE -> showSkillFragment()
            LauncherViewModel.LIST_PAGE -> showListFragment()
        }
    }

    private fun initAzero() {
        resetState()
//        registerCustomerAgent()
        //获取首页内容
        launcherViewModel.acquireLauncherList()
        initFloatingButton()

        //初始化成功后 上传通讯录
        val phoneCallControllerHandler = PhoneCallControllerImpl(this)

        val templateRuntimeHandler =
            AzeroManager.getInstance()
                .getHandler(AzeroManager.TEMPLATE_HANDLER) as TemplateRuntimeHandler
        templateRuntimeHandler.registerTemplateDispatchedListener(object : TemplateDispatcher() {
            override fun clearPlayerInfo() {
                ActivityLifecycleManager.getInstance()
                    .clearChannel(ActivityLifecycleManager.ChannelName.PLAYER_INFO)
//                showSkillFragment()
            }
        })
    }

    private fun resetState() {
        (if (AzeroManager.getInstance().getHandler(AzeroManager.PHONECALL_HANDLER) == null) {
            null
        } else {
            AzeroManager.getInstance().getHandler(AzeroManager.PHONECALL_HANDLER)
        } as PhoneCallControllerHandler).onCallStateChanged(
            PhoneCallController.CallState.IDLE,
            "123"
        )
    }

    private fun showEncyclopediaTemplate(payload: String) {
        launch(coroutineExceptionHandler) {
            startActivity(
                Intent(
                    this@LauncherActivity,
                    EncyclopediaTemplateActivity::class.java
                ).apply {
                    putExtra(Constant.EXTRA_TEMPLATE, payload)
                })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingButton() {
        val mAzeroClient =
            AzeroManager.getInstance()
                .getHandler(AzeroManager.AZERO_CLIENT_HANDLER) as AzeroClientHandler
        val speechRecognizerHandler =
            AzeroManager.getInstance()
                .getHandler(AzeroManager.SPEECH_RECOGNIZER_HANDLER) as SpeechRecognizerHandler
        floatingActionButton.setOnClickListener {
            if (mAzeroClient.connectionStatus == AlexaClient.ConnectionStatus.CONNECTED) {
                speechRecognizerHandler.onHoldToTalk()
            } else {
                WakeupButton.getInstance(this).displayErrorAnimation()
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_SERVER_ERROR)
            }
        }
        floatingActionButton.setOnLongClickListener {
            if (mAzeroClient.connectionStatus == AlexaClient.ConnectionStatus.CONNECTED) {
                speechRecognizerHandler.onHoldToTalk()
                mIsTalkButtonLongPressed = true
            } else {
                WakeupButton.getInstance(this).displayErrorAnimation()
                AzeroManager.getInstance().executeCommand(Command.CMD_PLAY_AUDIO_SERVER_ERROR)
            }
            return@setOnLongClickListener true
        }
        floatingActionButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP
                && mIsTalkButtonLongPressed
            ) {
                mIsTalkButtonLongPressed = false
                speechRecognizerHandler.onReleaseHoldToTalk()
            }
            return@setOnTouchListener false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        log.e("LauncherActivity onDestroy()")
        ASRDialog.detachToWindow()
        cancel()
        AzeroManager.getInstance().removeAzeroOSListener(this)
        TaAudioManager.removeBluetoothStateListener(bluetoothStateListener)
        TaAudioManager.destroy()
//        (AzeroManager.getInstance().customAgent as AzeroExpressHandler).exerciseHandler?.release()
        AzeroManager.getInstance().release()
        removeViewModelObservers()
    }

    override fun onResume() {
        super.onResume()
        log.e("LauncherActivity onResume()")

        if (TaAudioManager.isBTorWiredHeadsetConnected()) {
            if (!isDestroyed) {
                DialogManager.dismissAlertDialog(this@LauncherActivity)
            }
            TaAudioManager.requestAudioFocus(AudioManager.STREAM_VOICE_CALL);
            if (TaAudioManager.getRecordFailedState()) {
                DialogManager.showAlertDialog(this@LauncherActivity,"录音被占用，无法使用")
            }
        } else {
            if (!isDestroyed) {
                DialogManager.showAlertDialog(this@LauncherActivity,"请连接耳机后使用")
            }
        }

        if (currentFragment is SkillShowFragment) {
            (currentFragment as SkillShowFragment).startLoop()
            WakeupButton.getInstance(this@LauncherActivity).hide()
            ASRDialog.hide()
        } else {
            WakeupButton.getInstance(this@LauncherActivity).show()
            ASRDialog.show()
        }

        launcherViewModel.requestPendingTemplate()
    }

    override fun onStart() {
        super.onStart()
        log.e("LauncherActivity onStart()")
    }

    override fun onStop() {
        super.onStop()
        log.e("LauncherActivity onStop()")
    }

    override fun onPause() {
        super.onPause()
        log.e("LauncherActivity onPause()")
        WakeupButton.getInstance(this@LauncherActivity).hide()
    }

    override fun onEvent(p0: AzeroEvent?, p1: String?) {
        if (p0 == AzeroEvent.EVENT_CONNECTION_STATUS_CHANGED) {
            CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                log.e("EVENT_CONNECTION_STATUS_CHANGED status = $p1")
                if ("CONNECTED ACL_CLIENT_REQUEST" == p1) {
                    AzeroManager.getInstance().executeCommand(Command.CMD_NETWORK_CONNECTED)
                }else if(p1!!.contains("DISCONNECTED",false)){
                    AzeroManager.getInstance().executeCommand(Command.CMD_NETWORK_DISCONNECTED)
                }
            }
        }
    }
}
