/*
 * Copyright (c) 2019 SoundAI. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.soundai.azero.azeromobile.manager;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.WindowManager;

import com.azero.sdk.AzeroManager;
import com.azero.sdk.util.log;
import com.soundai.azero.azeromobile.impl.audioinput.AudioInputManager;
import com.soundai.azero.azeromobile.ui.activity.guide.GuidePageActivity;
import com.soundai.azero.azeromobile.ui.activity.launcher.LauncherActivity;
import com.soundai.azero.azeromobile.ui.activity.login.LoginActivity;
import com.soundai.azero.azeromobile.ui.activity.playerinfo.BasePlayerInfoActivity;
import com.soundai.azero.azeromobile.ui.activity.question.QuestionActivity;
import com.soundai.azero.azeromobile.ui.activity.runner.RunningActivity;
import com.soundai.azero.azeromobile.ui.activity.template.BaseDisplayCardActivity;
import com.soundai.azero.azeromobile.ui.widget.ASRDialog;

import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 负责整个程序的生命周期管理
 */
public class ActivityLifecycleManager implements Application.ActivityLifecycleCallbacks {
    private static Stack<Activity> activityStack = new Stack<>();
    private static CopyOnWriteArrayList<Activity> templateList = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<Activity> playerInfoList = new CopyOnWriteArrayList<>();
    private int mActivityCount = 0;

    private static class Holder {
        private static ActivityLifecycleManager INSTANCE = new ActivityLifecycleManager();
    }

    public static ActivityLifecycleManager getInstance() {
        return Holder.INSTANCE;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        log.e("ActivityLifecycleManager onActivityCreated===" + activity.getClass().getName());
        synchronized (this) {
            activityStack.add(activity);
        }
        if (activity instanceof BaseDisplayCardActivity) {
            templateList.add(activity);
        } else if (activity instanceof BasePlayerInfoActivity) {
            clearChannel(ChannelName.PLAYER_INFO);
            playerInfoList.add(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        log.e("ActivityLifecycleManager onActivityStarted===" + activity.getClass().getName());
        if(activity instanceof LauncherActivity){
            if (getTopActivity() instanceof GuidePageActivity) {
                activityStack.remove(getTopActivity());
            }
            ASRDialog.INSTANCE.hide();
        }
        mActivityCount++;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        log.e("ActivityLifecycleManager onActivityResumed===" + activity.getClass().getName());
        if (mActivityCount == 1) {
            if (!(activity instanceof GuidePageActivity) && !(activity instanceof LoginActivity)) {
                if ( AzeroManager.getInstance().isEngineInitComplete() && !AudioInputManager.Companion.getInstance().isRecording()) {
//                    AudioInputManager.Companion.getInstance().startAudioInput();
                }
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        log.e("ActivityLifecycleManager onActivityPaused===" + activity.getClass().getName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        log.e("ActivityLifecycleManager onActivityStopped===" + activity.getClass().getName());
        mActivityCount--;
        if (mActivityCount == 0) {
            ASRDialog.INSTANCE.hide();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        log.e("ActivityLifecycleManager onActivitySaveInstanceState===" + activity.getClass().getName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        log.e("ActivityLifecycleManager onActivityDestroyed===" + activity.getClass().getName());
        synchronized (this) {
            activityStack.remove(activity);
        }
        if (activity instanceof BaseDisplayCardActivity) {
            templateList.remove(activity);
        } else if (activity instanceof BasePlayerInfoActivity) {
            playerInfoList.remove(activity);
        }
    }

    public synchronized Activity getCurActivity() {
        return activityStack.lastElement();
    }

    /**
     * 关闭指定类型的界面
     *
     * @param channelName
     */
    public void clearChannel(ChannelName channelName) {
        switch (channelName) {
            case TEMPLATE:
                for (Activity activity : templateList) {
                    if (activity instanceof RunningActivity) {
                        ((RunningActivity) activity).finishInTimeout();
                        break;
                    } else if (activity instanceof QuestionActivity) {
                        break;
                    }else {
                        activity.finish();
                    }
                }
                templateList.clear();
                break;
            case PLAYER_INFO:
                for (Activity activity : playerInfoList) {
                    activity.finish();
                }
                playerInfoList.clear();
                if (ActivityLifecycleManager.getInstance().topIsLauncher()) {
                    ((LauncherActivity)ActivityLifecycleManager.getInstance().getTopActivity()).showLauncherFromClearPlayinfo();
                    break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取栈顶Activity
     *
     * @return
     */
    public synchronized Activity getTopActivity() {
        if (activityStack.size() > 0) {
            return activityStack.get(activityStack.size() - 1);
        }
        return null;
    }

    public synchronized Activity getLauncherActivity() {
        for (Activity activity : activityStack) {
            if (activity instanceof LauncherActivity) {
                return activity;
            }
        }
        return null;
    }

    /**
     * 判断当前是否在首页
     *
     * @return
     */
    public synchronized boolean topIsLauncher() {
        if (activityStack.size() == 1) {
            Activity activity = activityStack.get(0);
        }
        return false;
    }

    public enum ChannelName {
        TEMPLATE,
        PLAYER_INFO
    }

    /**
     * 应用是否在前台
     *
     * @return
     */
    public boolean isAppForeground() {
        return mActivityCount > 0;
    }

    /**
     * 结束所有的Activity
     */
    public synchronized void finishAllActivity() {
        if (activityStack.size() > 0) {
            for (Activity activity : activityStack) {
                activity.finish();
            }
        }
    }

    public synchronized void finishAllActivityExceptLauncher(int index) {
        if (activityStack.size() > 0) {
            for (Activity activity : activityStack) {
                if (!(activity instanceof LauncherActivity)) {
                    log.d("activity:" + activity.getClass() + " finished");
                    activity.finish();
                }
                else if (index != -1){
                    ((LauncherActivity) activity).setFragment(index);
                }
            }
        }
    }
}
