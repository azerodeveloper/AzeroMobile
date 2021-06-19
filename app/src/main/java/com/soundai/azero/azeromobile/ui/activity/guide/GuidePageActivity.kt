package com.soundai.azero.azeromobile.ui.activity.guide

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.azero.sdk.AzeroManager
import com.azero.sdk.event.AzeroEvent
import com.azero.sdk.impl.TemplateRuntime.TemplateDispatcher
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler
import com.azero.sdk.util.log
import com.github.paolorotolo.appintro.AppIntro
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.system.LocaleModeHandle
import com.soundai.azero.azeromobile.system.TaAudioManager
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity
import com.soundai.azero.azeromobile.ui.activity.login.LoginActivity
import com.soundai.azero.azeromobile.utils.SPUtils.getAccountPref
import com.umeng.message.PushAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class GuidePageActivity : AppIntro(), EasyPermissions.PermissionCallbacks,
    AzeroManager.AzeroOSListener {
    companion object {
        private const val PERMISSION_GRANTED: Int = 1002
        private const val OVERLAYS_GRANTED: Int = 1003
        private const val NEED_LOGIN: Int = 1004
    }

    private val sRequiredPermissions by lazy {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS)
        }
        permissions.toTypedArray()
    }

    private val guidePageViewModel by lazy {
        ViewModelProviders.of(this).get(GuidePageViewModel::class.java)
    }
    private val sPermissionRequestCode = 100
    private var isInSplash = false
    private val splashFragment by lazy { SplashFragment() }
    private var currentFragment: Fragment? = null
    private val localHandler: Handler by lazy {
        object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                when (msg?.what) {
                    PERMISSION_GRANTED -> {
                        log.e("GuidePageActivity PERMISSION_GRANTED")
                        checkCanDrawOverlays()
                    }
                    OVERLAYS_GRANTED -> {
                        log.e("GuidePageActivity OVERLAYS_GRANTED")
                        checkAccountCacheAndBind()
//                        MyApplication.instance().startAzero()
                    }
                    NEED_LOGIN -> {
                        log.e("GuidePageActivity NEED_LOGIN")
                        goToLogin()
                    }
                }
            }
        }
    }

    private val bluetoothStateListener by lazy {
        object : TaAudioManager.BluetoothStateListener {
            override fun onConnected() {
                log.i("GuidePageActivity HEADSET_CONNECTED")
                splashFragment.setHeadsetState(true)
                if (!AzeroManager.getInstance().isEngineInitComplete) {
                    checkAndRequestPermission()
                }else{
                    goToLauncher()
                }
            }

            override fun onDisconnected() {
                log.i("GuidePageActivity HEADSET_DISCONNECTED")
                splashFragment.setHeadsetState(false)
            }

            override fun onRecordFailed() {
            }

            override fun onRecordSuccessful() {
            }
        }
    }

    private val templateRuntimeHandler by lazy {
        if (AzeroManager.getInstance().getHandler(AzeroManager.TEMPLATE_HANDLER) == null) {
            null
        } else {
            AzeroManager.getInstance()
                .getHandler(AzeroManager.TEMPLATE_HANDLER) as TemplateRuntimeHandler
        }
    }

    private val templateDispatcher by lazy {
        object : TemplateDispatcher() {
            override fun renderTemplate(payload: String, type: String) {
                if (!ActivityLifecycleManager.getInstance().isAppForeground) {
                    return
                }
                log.e("templateRuntimeHandler  from GuidePageActivity type:${type},payload:${payload}")
                when (type) {
                    "GuideTemplate2" -> goNextPage(1)
                    "GuideTemplate3" -> goNextPage(2)
                    "ReadyTemplate" -> {
                        LocaleModeHandle.switchLocaleMode(LocaleModeHandle.LocaleMode.HEADSET)
                        goToLauncher()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushAgent.getInstance(this).onAppStart()
        log.e("GuidePageActivity onCreate()")
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
        showPagerIndicator(false)
        showSkipButton(false)
        showSeparator(false)
        showStatusBar(false)
        showDoneButton(false)
        TaAudioManager.initTaAudioManager(this.applicationContext)
        TaAudioManager.addBluetoothStateListener(bluetoothStateListener)
        AzeroManager.getInstance().addAzeroOSListener(this)
//        if (!MyApplication.isFirstIntall) {
//            log.e("GuidePageActivity onCreate() not first install ")
//            addSlide(splashFragment)
//            showPagerIndicator(false)
//            isInSplash = true
//            currentFragment = splashFragment
//        } else {
            log.e("GuidePageActivity onCreate() first install ")
            isInSplash = false
            currentFragment = splashFragment
            addSlide(splashFragment)
            addSlide(FirstGuidePageFragment())
            addSlide(SecordGuidePageFragment())
//        }
        initViewModel()
    }

    private fun initViewModel() {
        guidePageViewModel.loginChecker.observe (this@GuidePageActivity, Observer {
            when (it) {
                GuidePageViewModel.TOKEN_VALID -> initSDK()
                GuidePageViewModel.TOKEN_INVALID -> sendLocalMessage(NEED_LOGIN)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        log.e("GuidePageActivity onResume()")
    }

    override fun onStart() {
        super.onStart()
        log.e("GuidePageActivity onStart()")
        if (currentFragment is SplashFragment) {
            if (TaAudioManager.isBTorWiredHeadsetConnected()) {
                splashFragment.setHeadsetState(true)
                checkAndRequestPermission()
            } else {
                splashFragment.setHeadsetState(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        log.e("GuidePageActivity onDestroy()")
        AzeroManager.getInstance().removeAzeroOSListener(this)
        TaAudioManager.removeBluetoothStateListener(bluetoothStateListener)
        templateRuntimeHandler?.unregisterTemplateDispatchedListener(templateDispatcher)
    }

    override fun onStop() {
        super.onStop()
        log.e("GuidePageActivity onStop()")
    }

    override fun onPause() {
        super.onPause()
        log.e("GuidePageActivity onPause()")
    }

    override fun onEvent(event: AzeroEvent?, msg: String?) {
        if (event == AzeroEvent.EVENT_CONNECTION_STATUS_CHANGED) {
            CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
                log.e("EVENT_CONNECTION_STATUS_CHANGED status = $msg")
                if ("CONNECTED ACL_CLIENT_REQUEST" == msg) {

                    onAzeroInitComplete()
                }
            }
        }
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        if (currentFragment is ForthGuidePageFragment) {
            sendLocalMessage(OVERLAYS_GRANTED)
        }
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        if (newFragment != null) {
            currentFragment = newFragment
        }
        // old is first guide is to avoid do the same action with onStart()
        if (newFragment is SplashFragment) {
            if (TaAudioManager.isBTorWiredHeadsetConnected()) {
                splashFragment.setHeadsetState(true)
            } else {
                splashFragment.setHeadsetState(false)
            }
        } else if (newFragment is FirstGuidePageFragment) {
            log.e("FirstGuidePageFragment is showed")
        } else {
            showDoneButton(false)
        }
    }

    private fun loadGuidePage() {
        AzeroManager.getInstance().sendQueryText("获取指南页二")
    }

    private fun checkAndRequestPermission() {
        if (EasyPermissions.hasPermissions(this, *sRequiredPermissions)) {
            log.e("all permissions are granted")
            sendLocalMessage(PERMISSION_GRANTED)
        } else {
            log.e("some permissions are not granted")
            EasyPermissions.requestPermissions(
                this, "需要添加权限，方便您快捷使用Azero服务",
                sPermissionRequestCode, *sRequiredPermissions
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        //永久性被拒绝
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .setThemeResId(R.style.Theme_AppCompat_Light_Dialog).build().show()
        } else {
            checkAndRequestPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        log.d("$perms permissions are granted")
        if (perms.size == sRequiredPermissions.size) {
            Log.e("GuidePageActivity", "All needed permissions are granted")
            sendLocalMessage(PERMISSION_GRANTED)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                Log.d("Azero.SDK", "onActivityResult() DEFAULT_SETTINGS_REQ_CODE")
            }
            101 -> {
                Log.e("Azero.SDK", "onActivityResult() result code = ${resultCode}")
                if ((getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager).checkOpNoThrow(
                        "android:system_alert_window",
                        Process.myUid(),
                        packageName
                    ) == AppOpsManager.MODE_ALLOWED
                ) {
                    Log.e("Azero.SDK", "onActivityResult() 获得了悬浮权限")
                    sendLocalMessage(OVERLAYS_GRANTED)

                } else {
                    sendLocalMessage(OVERLAYS_GRANTED)
                }
            }
        }
    }

    fun showAsrText(asrText: String, gone: Boolean) {
        if (currentFragment is FirstGuidePageFragment) {
            (currentFragment as FirstGuidePageFragment).setAsrText(asrText, gone)
        } else if (currentFragment is SecordGuidePageFragment) {
            (currentFragment as SecordGuidePageFragment).setAsrText(asrText, gone)
        }
    }

    fun getRecordData(byteArray: ByteArray, size: Int) {
        if (currentFragment is FirstGuidePageFragment) {
            (currentFragment as FirstGuidePageFragment).getRecordData(byteArray, size)
        } else if (currentFragment is SecordGuidePageFragment) {
            (currentFragment as SecordGuidePageFragment).getRecordData(byteArray, size)
        }
    }

    private fun goToLauncher() {
        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun initSDK() {
        if (!AzeroManager.getInstance().isEngineInitComplete) {
            TaApp.application.startAzero()
        }
    }

    private suspend fun onAzeroInitComplete() {
        templateRuntimeHandler?.registerTemplateDispatchedListener(templateDispatcher)
//        if (MyApplication.isFirstIntall) {
//            LocaleModeHandle.switchLocaleMode(LocaleModeHandle.LocaleMode.GUIDE)
//            delay(500)
//            while (!LocaleModeHandle.checkMode(LocaleModeHandle.LocaleMode.GUIDE)) {
//                log.e("wait for mode switch, except mode is GUIDE")
//                delay(1000)
//            }
//            loadGuidePage()
//        } else {
            LocaleModeHandle.switchLocaleMode(LocaleModeHandle.LocaleMode.HEADSET)
            delay(500)
            while (!LocaleModeHandle.checkMode(LocaleModeHandle.LocaleMode.HEADSET)) {
                log.e("wait for mode switch, except mode is HEADSET")
                delay(1000)
            }
            goToLauncher()
//        }
    }

    private fun sendLocalMessage(what: Int) {
        val msg = Message.obtain()
        msg.what = what
        localHandler.sendMessage(msg)
    }

    private fun checkAccountCacheAndBind() {
        getAccountPref().apply {
            val token = getString(Constant.SAVE_TOKEN, "")
            val userId = getString(Constant.SAVE_USERID, "")
            val bondState = getString(Constant.BOND_STATE, null)
            if (token.isNullOrEmpty() || userId.isNullOrEmpty()) {
                Log.e("Azero.SDK", "checkAccountCacheAndBind token is null")
                sendLocalMessage(NEED_LOGIN)
            } else if ("bonded" == bondState) {
                Log.e("Azero.SDK", "checkAccountCacheAndBind binded")
                guidePageViewModel.extendToken()
            } else {
                Log.e("Azero.SDK", "checkAccountCacheAndBind need bind")
                sendLocalMessage(NEED_LOGIN)
            }
        }
    }


    private fun checkCanDrawOverlays() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, 101)
            Log.e("Azero.SDK", "checkCanDrawOverlays() 请求悬浮权限")
        } else {
            Log.e("Azero.SDK", "checkCanDrawOverlays() 已经拥有权限")
            sendLocalMessage(OVERLAYS_GRANTED)
        }
    }
}
