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

package com.soundai.azero.azeromobile.ui.activity.template

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import com.azero.sdk.util.Constant

abstract class BaseDisplayCardActivity : BaseTemplateActivity() {
    var template: String = ""

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //触碰界面时，阻止界面收回，时钟重新计时
        reclockTemplateTimer()
        return super.dispatchTouchEvent(ev)
    }

    protected fun reclockTemplateTimer(): Boolean {
        return if (templateRuntimeHandler != null) {
            templateRuntimeHandler!!.reclockTemplateTimer()
        } else false
    }

    override fun initData(intent: Intent) {
        if (intent.hasExtra(Constant.EXTRA_TEMPLATE))
            template = intent.getStringExtra(Constant.EXTRA_TEMPLATE)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constant.EXTRA_TEMPLATE, template)
    }
}
