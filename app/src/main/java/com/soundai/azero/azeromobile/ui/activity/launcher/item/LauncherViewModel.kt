package com.soundai.azero.azeromobile.ui.activity.launcher.item

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.TemplateRuntime.TemplateDispatcher
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler
import com.azero.sdk.util.log
import com.google.gson.Gson
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.common.bean.helloweather.HelloWeather
import com.soundai.azero.azeromobile.common.bean.skilltip.SkillListTipsResponse
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.manager.coroutineExceptionHandler
import com.soundai.azero.azeromobile.ui.activity.base.viewmodel.BaseRequestViewModel
import com.soundai.azero.azeromobile.ui.activity.launcher.head.HelloWeatherHeadView
import com.soundai.azero.azeromobile.ui.activity.launcher.head.IHeadView
import com.soundai.azero.azeromobile.ui.activity.launcher.head.MusicHeadView
import com.soundai.azero.azeromobile.ui.activity.launcher.head.SingleNewsHeadView
import com.soundai.azero.azeromobile.ui.activity.playerinfo.NewsDetailsActivity
import com.soundai.azero.azeromobile.ui.activity.playerinfo.PlayingDetailsActivity
import com.soundai.azero.azeromobile.utils.getLauncherTestData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 * Create by xingw on 2019/10/31
 */
class LauncherViewModel(application: Application) : BaseRequestViewModel(application) {
    companion object {
        const val SKILL_PAGE = 0
        const val LIST_PAGE = 1
        private const val SPHERE_TEMPLATE = "SphereTemplate"
        private const val WEATHER_TEMPLATE = "WeatherTemplate"
        private const val DEFAULT_TEMPLATE1 = "DefaultTemplate1"
        private const val BODY_TEMPLATE1 = "BodyTemplate1"
        private const val RUNNING_TEMPLATE = "RunningTemplate"
        private const val WALKING_TEMPLATE = "WalkingTemplate"
        private const val NEWS_TEMPLATE = "NewsTemplate"
        private const val ENGLISH_TEMPLATE = "EnglishTemplate"
        private const val QUESTION_TEMPLATE = "QuestionGameTemplate"
        private const val HELLO_WEATHER_TEMPLATE = "HelloWeatherTemplate"
        private const val SKILL_LIST_TIPS_TEMPLATE = "SkillListTipsTemplate"
        private val PENDING_TEMPLATE_LIST = listOf(QUESTION_TEMPLATE)
    }

    val sphereTemplate = MutableLiveData<String>()
    val weatherTemplate = MutableLiveData<String>()
    val bodyTemplate1 = MutableLiveData<String>()
    val runningTemplate = MutableLiveData<String>()
    val walkingTemplate = MutableLiveData<String>()
    val questionTemplate = MutableLiveData<String>()
    val gridItems = MutableLiveData<MutableList<IGridItem>>()
    val headView = MutableLiveData<IHeadView>()
    val page = MutableLiveData<Int>()
    val skillListTips = MutableLiveData<SkillListTipsResponse>()

    private var currentAudioItemId: String? = null
    private var pendingTemplate: Triple<String, String, Long>? = null
    private var indexMediaResponse = 0
    private var currentMediaType: String? = null

    private val templateRuntimeHandler by lazy {
        AzeroManager.getInstance()
            .getHandler(AzeroManager.TEMPLATE_HANDLER) as TemplateRuntimeHandler
    }

    private val templateDispatcher by lazy {
        object : TemplateDispatcher() {
            override fun renderTemplate(payload: String, type: String) {
                if (!ActivityLifecycleManager.getInstance().isAppForeground) {
                    if (PENDING_TEMPLATE_LIST.contains(type)) {
                        pendingTemplate = Triple(payload, type, System.currentTimeMillis())
                    }
                    return
                }
                dispatchTemplate(payload, type)
            }

            override fun renderPlayerInfo(payload: String) {
                dispatchPlayerInfo(payload)
            }
        }
    }

    init {
        templateRuntimeHandler.registerTemplateDispatchedListener(templateDispatcher)
    }

    fun requestPendingTemplate() {
        pendingTemplate?.run {
            dispatchPendingTemplate(first, second, third)
            pendingTemplate = null
        }
    }

    fun clearPendingTemplate() {
        pendingTemplate = null
    }

    fun acquireLauncherList() {
        CoroutineScope(Dispatchers.IO).launch(coroutineExceptionHandler) {
            if (gridItems.value.isNullOrEmpty()) {
                indexMediaResponse++
                AzeroManager.getInstance()
                    .acquireLauncherList("AcquireLauncherContent", "10", 10)
            }
        }
    }

    fun getMoreData() {
        val moreDatas = getLauncherTestData()
        gridItems.postValue(moreDatas)
    }

    private fun dispatchTemplate(payload: String, type: String) {
        log.i("dispatchTemplate type: $type, payload: $payload")
        when (type) {
            SPHERE_TEMPLATE -> sphereTemplate.postValue(payload)
            WEATHER_TEMPLATE -> weatherTemplate.postValue(payload)
            DEFAULT_TEMPLATE1, BODY_TEMPLATE1 -> bodyTemplate1.postValue(payload)
            RUNNING_TEMPLATE -> runningTemplate.postValue(payload)
            WALKING_TEMPLATE -> walkingTemplate.postValue(payload)
            QUESTION_TEMPLATE -> dispatchQuestionTemplate(payload, System.currentTimeMillis())
            HELLO_WEATHER_TEMPLATE -> {
                AzeroManager.getInstance().sendQueryText("应用当前正在早上好技能中")
                TaApp.isHelloSkill = true
                headView.postValue(
                    HelloWeatherHeadView(
                        Gson().fromJson(
                            payload,
                            HelloWeather::class.java
                        )
                    )
                )
            }
            SKILL_LIST_TIPS_TEMPLATE -> {
                skillListTips.postValue(Gson().fromJson(payload, SkillListTipsResponse::class.java))
            }
            else -> log.e("unCatch type:${type}")
        }
    }

    private fun dispatchPendingTemplate(payload: String, type: String, time: Long) {
        log.i("dispatchPendingTemplate type: $type, payload: $payload, time: $time")
        when (type) {
            QUESTION_TEMPLATE -> dispatchQuestionTemplate(payload, time)
            else -> dispatchTemplate(payload, type)
        }
    }

    private fun dispatchQuestionTemplate(payload: String, time: Long) {
        val json = JSONObject(payload)
        json.put("timeReceive", time)
        questionTemplate.postValue(json.toString())
    }

    private fun dispatchPlayerInfo(payload: String) {
        try {
            TaApp.isHelloSkill = false
            val playerInfo = JSONObject(payload)
            val audioItemId = playerInfo.getString("audioItemId")
            if (audioItemId != currentAudioItemId) {
                val content = playerInfo.getJSONObject("content")
                val type = playerInfo.getString("type")
                if (indexMediaResponse > 1) {
                    page.postValue(LIST_PAGE)
                }
                indexMediaResponse++
                currentMediaType = type
                val position = content.getInt("position")
                val showDetails = if (content.has("showDetails")) {
                    content.getBoolean("showDetails")
                } else {
                    true
                }
                //首页显示不清除界面
                val clearTemplate = type != "LauncherTemplate"

                // Log only if audio item has changed
                currentAudioItemId = audioItemId
                val targetHeadView: IHeadView
                val contentItem: IGridItem
                var gridItemList = when (type) {
                    NEWS_TEMPLATE -> {
                        AzeroManager.getInstance().sendQueryText("应用当前正在新闻技能中")
                        contentItem = parseContent2NewsGridItem(content)
                        targetHeadView = SingleNewsHeadView(contentItem)
                        parseTemplate2NewsGridItemList(playerInfo)
                    }
                    ENGLISH_TEMPLATE -> {
                        AzeroManager.getInstance().sendQueryText("应用当前正在学英语技能中")
                        contentItem = parseContent2EnglishGridItem(content)
                        targetHeadView = MusicHeadView(contentItem)
                        parseTemplate2EnglishGridItemList(playerInfo)
                    }
                    else -> {
                        AzeroManager.getInstance().sendQueryText("应用当前正在有声技能中")
                        contentItem = parseContent2MusicGridItem(content)
                        targetHeadView = MusicHeadView(contentItem)
                        parseTemplate2MusicGridItemList(playerInfo)
                    }
                }
                headView.postValue(targetHeadView)
                if (!gridItemList.isNullOrEmpty()) {
                    if (position < gridItemList.size)
                        gridItemList[position].focus = true
                    if (position != 0 && position < gridItemList.size) {
                        gridItemList = gridItemList.subList(position, gridItemList.size)
                    }
                    gridItems.postValue(gridItemList)
                }

                //如果正在显示音乐界面，则更新音乐界面
                val topActivity =
                    ActivityLifecycleManager.getInstance().topActivity
                when (contentItem) {
                    is MusicGridItem -> {
                        if (topActivity is PlayingDetailsActivity) {
                            topActivity.updateIntent(contentItem)
                        } else {
                            if (clearTemplate)
                                ActivityLifecycleManager.getInstance()
                                    .clearChannel(ActivityLifecycleManager.ChannelName.PLAYER_INFO)
                            if (showDetails && ActivityLifecycleManager.getInstance().isAppForeground) {
                                PlayingDetailsActivity.start(topActivity, contentItem)
                            }
                        }
                    }
                    is NewsGridItem -> {
                        if (topActivity is NewsDetailsActivity && showDetails) {
                            topActivity.updateIntent(contentItem)
                        } else {
                            if (clearTemplate)
                                ActivityLifecycleManager.getInstance()
                                    .clearChannel(ActivityLifecycleManager.ChannelName.PLAYER_INFO)
                            if (showDetails && ActivityLifecycleManager.getInstance().isAppForeground) {
                                NewsDetailsActivity.start(topActivity, contentItem)
                            }
                        }
                    }
                    is EnglishGridItem -> {
                        if (topActivity is PlayingDetailsActivity) {
                            topActivity.updateIntent(
                                convertEnglishGridItem2MusicGridItem(
                                    contentItem
                                ), true
                            )
                        } else if (ActivityLifecycleManager.getInstance().isAppForeground) {
                            PlayingDetailsActivity.start(
                                topActivity,
                                convertEnglishGridItem2MusicGridItem(contentItem),
                                true
                            )
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            log.e(e.message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        templateRuntimeHandler.unregisterTemplateDispatchedListener(templateDispatcher)
    }
}