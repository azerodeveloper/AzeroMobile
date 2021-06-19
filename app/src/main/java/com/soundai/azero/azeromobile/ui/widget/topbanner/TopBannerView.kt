package com.soundai.azero.azeromobile.ui.widget.topbanner

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager

internal class TopBannerView {

    private lateinit var mDecorView: ViewGroup
    private lateinit var topBannerLayout: TopBannerLayout
    private var isShow: Boolean = false
//    private var currentActivity: Activity? = TopBannerLifecycleHandler.INSTANCE.getCurrentActivity()
    private var currentActivity: Activity? = ActivityLifecycleManager.getInstance().topActivity

    private fun animateIn() {
        var value = 0f
        currentActivity?.application?.getStatusHeight()?.let {
            value = it.toFloat()
        }
        topBannerLayout.animate()
            .translationY(value)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    topBannerLayout.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    topBannerLayout.visibility = View.GONE
                    isShow = true
                }
            }).start()

    }

    private fun animateOut() {
        topBannerLayout.animate()
            .translationY(0f)
            .setInterpolator(FastOutSlowInInterpolator())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    topBannerLayout.visibility = View.INVISIBLE
                    isShow = false
                }
            })
    }

    private fun showBannerView() {
        if (!topBannerLayout?.isShown) {
            animateIn()
        }
    }

    private fun hideBannerView() {
        if (topBannerLayout.isShown || topBannerLayout.isGone || topBannerLayout.isVisible) {
            animateOut()
        }
    }

    private fun getBannerViewIndex(decorView: ViewGroup): Int {
        for (i in 0 until decorView.childCount) {
            if (decorView.getChildAt(i) is TopBannerLayout) {
                return i
            }
        }
        return -2
    }

    internal fun createView(message: String, @DrawableRes drawableResId: Int, isNetworkUnconnected: Boolean) {
        currentActivity = ActivityLifecycleManager.getInstance().topActivity
        if (currentActivity != null) {
            mDecorView = currentActivity!!.window.decorView as ViewGroup
            val messageViewIndex = getBannerViewIndex(mDecorView)

            if (-2 != messageViewIndex) {
                mDecorView.removeViewAt(messageViewIndex)
            }
            topBannerLayout = TopBannerLayout(currentActivity!!)
            val view: View = if (!isNetworkUnconnected) {
                LayoutInflater
                    .from(currentActivity)
                    .inflate(R.layout.view_network_bar, topBannerLayout, false)
            } else {
                LayoutInflater
                    .from(currentActivity)
                    .inflate(R.layout.view_network_bar_unconnected, topBannerLayout, false)
            }

            topBannerLayout.addView(view)
            mDecorView.addView(topBannerLayout, mDecorView.childCount)

            topBannerLayout.visibility = View.INVISIBLE

            val networkTopBannerConstraintLayout: ConstraintLayout =
                view.findViewById(R.id.network_snack_bar_constraint_layout) as ConstraintLayout

            val networkTopBannerTextView: TextView = view.findViewById(R.id.network_snack_bar_text) as TextView
            val networkTopBannerImageView: ImageView = view.findViewById(R.id.network_snack_bar_image) as ImageView

            networkTopBannerConstraintLayout.setPadding(0, 0, 0, 0)
            networkTopBannerTextView.text = message
            networkTopBannerImageView.setImageDrawable(currentActivity!!.resources.getDrawable(drawableResId, null))
        }
    }

    internal fun Context.getStatusHeight(): Int {
        var result: Int = -1
        val identifier = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (identifier > 0) {
            result = resources.getDimensionPixelOffset(identifier)
        }
        return result
    }

    fun show() {
        showBannerView()
    }

    fun dismiss() {
        hideBannerView()
    }
}