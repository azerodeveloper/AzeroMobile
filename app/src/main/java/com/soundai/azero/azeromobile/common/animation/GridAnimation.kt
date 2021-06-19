package com.soundai.azero.azeromobile.common.animation

import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation

/**
 * Create by xingw on 2019/11/2
 */
fun getAnimationScaleBig(): ScaleAnimation {
    val scaleAnimation =
        ScaleAnimation(0.1f, 1f, 0.1f, 1f, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f)
    scaleAnimation.interpolator = DecelerateInterpolator()
    scaleAnimation.duration = 400
    return scaleAnimation
}

/**
 * 从左侧进入，并带有弹性的动画
 *
 * @return
 */
fun getAnimationSetFromLeft(): AnimationSet {
    val animationSet = AnimationSet(true)
    val translateX1 = TranslateAnimation(
        RELATIVE_TO_SELF, -1.0f, RELATIVE_TO_SELF, 0.1f,
        RELATIVE_TO_SELF, 0f, RELATIVE_TO_SELF, 0f
    )
    translateX1.duration = 300
    translateX1.interpolator = DecelerateInterpolator()
    translateX1.startOffset = 0

    val translateX2 = TranslateAnimation(
        RELATIVE_TO_SELF, 0.1f, RELATIVE_TO_SELF, -0.1f,
        RELATIVE_TO_SELF, 0f, RELATIVE_TO_SELF, 0f
    )
    translateX2.startOffset = 300
    translateX2.interpolator = DecelerateInterpolator()
    translateX2.duration = 50

    val translateX3 = TranslateAnimation(
        RELATIVE_TO_SELF, -0.1f, RELATIVE_TO_SELF, 0f,
        RELATIVE_TO_SELF, 0f, RELATIVE_TO_SELF, 0f
    )
    translateX3.startOffset = 350
    translateX3.interpolator = DecelerateInterpolator()
    translateX3.duration = 50

    val alphaAnimation = AlphaAnimation(0.5f, 1.0f)
    alphaAnimation.duration = 400
    alphaAnimation.interpolator = AccelerateDecelerateInterpolator()


    animationSet.addAnimation(translateX1)
    animationSet.addAnimation(translateX2)
    animationSet.addAnimation(translateX3)
    //animationSet.addAnimation(alphaAnimation);
    animationSet.duration = 400

    return animationSet
}

/**
 * 从底部进入
 *
 * @return
 */
fun getAnimationSetFromBottom(): AnimationSet {
    val animationSet = AnimationSet(true)
    val translateX1 = TranslateAnimation(
        RELATIVE_TO_SELF, 0f, RELATIVE_TO_SELF, 0f,
        RELATIVE_TO_SELF, 2.5f, RELATIVE_TO_SELF, 0f
    )
    translateX1.duration = 400
    translateX1.interpolator = DecelerateInterpolator()
    translateX1.startOffset = 0

    animationSet.addAnimation(translateX1)
    animationSet.duration = 400

    return animationSet
}