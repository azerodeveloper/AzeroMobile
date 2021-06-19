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
import android.view.WindowManager
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.TemplateRuntime.TemplateRuntimeHandler
import com.soundai.azero.azeromobile.ui.activity.base.activity.BaseSwipeActivity
import com.soundai.azero.azeromobile.ui.widget.ASRDialog

abstract class BaseTemplateActivity : BaseSwipeActivity() {
    var isForeground = false
        private set
    protected var templateRuntimeHandler: TemplateRuntimeHandler? = null
        get() {
            if (field != null) {
                return field
            }
            return (AzeroManager.getInstance()
                .getHandler(AzeroManager.TEMPLATE_HANDLER) as TemplateRuntimeHandler).also {
                field = it
            }
        }
    protected abstract val layoutResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(layoutResId)
        initView()
        initData(intent)

        templateRuntimeHandler = AzeroManager.getInstance()
            .getHandler(AzeroManager.TEMPLATE_HANDLER) as TemplateRuntimeHandler
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        initData(intent)
    }

    protected abstract fun initView()

    protected abstract fun initData(intent: Intent)

    override fun onResume() {
        super.onResume()
        isForeground = true
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
    }

    override fun onStart() {
        super.onStart()
        ASRDialog.show()
    }
}
