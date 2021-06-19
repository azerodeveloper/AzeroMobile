package com.soundai.azero.azeromobile.ui.widget.topbanner

import androidx.annotation.DrawableRes
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager

object TopBannerManager {

    private var topBannerView: TopBannerView? = null

    @JvmStatic
    fun showNetworkTopBanner(message: String, @DrawableRes drawableResId: Int, isNetworkUnconnected: Boolean) {
        if (!ActivityLifecycleManager.getInstance().isAppForeground) {
            return
        }
        if (topBannerView == null) {
            topBannerView = TopBannerView()
        }
        topBannerView?.createView(message, drawableResId, isNetworkUnconnected)
        topBannerView?.show()
    }

    fun dismissNetworkTopBanner() {
        topBannerView?.dismiss()
    }

}