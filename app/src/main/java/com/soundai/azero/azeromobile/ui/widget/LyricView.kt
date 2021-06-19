package com.soundai.azero.azeromobile.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Looper
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.soundai.azero.azeromobile.R
import java.io.*
import java.util.*
import kotlin.math.abs

class LyricView : View {

    private var mHintColor = 0
    private var mDefaultColor = 0
    private var mHighLightColor = 0


    private var mLineCount = 0
    private var mTextSize = 0
    private var mLineHeight = 0f
    private var mLyricInfo: LyricInfo? = null
    private var mDefaultHint: String? = null
    private var mMaxLength = 0

    private var mTextPaint: Paint? = null

    private var mFling = false
    private var mFlingAnimator: ValueAnimator? = null
    private var mScrollY = 0f
    private var mLineSpace = 0f
    private var mIsShade = false
    private var mShaderWidth = 0f
    private var mCurrentPlayLine = 0

    private var mVelocityTracker: VelocityTracker? = null
    private var mVelocity = 0f
    private var mDownY = 0f
    private var mLastScrollY = 0f
    private var maxVelocity = 0

    private val mLineFeedRecord: ArrayList<Int> = ArrayList()
    private var mEnableLineFeed = false
    private var mExtraHeight = 0

    private var mTextHeight = 0

    private var mCurrentLyricFilePath: String? = null

    private val mWidth = resources.displayMetrics.widthPixels
    private val tag = "LyricView"

    companion object {

        private const val SLIDE_COEFFICIENT = 0.2f

        private const val UNITS_SECOND = 1000
        private const val UNITS_MILLISECOND = 1

        private const val FLING_ANIMATOR_DURATION = 500 * UNITS_MILLISECOND

        private const val THRESHOLD_Y_VELOCITY = 1600

        private const val DEFAULT_TEXT_SIZE = 16 //sp

        private const val DEFAULT_LINE_SPACE = 25 //dp
    }

    constructor(context: Context) : super(context) {
        initLyricView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        getAttrs(context, attributeSet)
        initLyricView(context)
    }

    constructor(context: Context, attributeSet: AttributeSet, i: Int) : super(context, attributeSet, i) {
        getAttrs(context, attributeSet)
        initLyricView(context)
    }

    private fun getAttrs(context: Context, attributeSet: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.LyricView)

        mIsShade = typedArray.getBoolean(R.styleable.LyricView_fadeInFadeOut, false)
        mDefaultHint = if (typedArray.getString(R.styleable.LyricView_hint) != null) {
            typedArray.getString(R.styleable.LyricView_hint)
        } else {
            resources.getString(R.string.default_hint)
        }
        mHintColor = typedArray.getColor(R.styleable.LyricView_hintColor, Color.parseColor("#FFFFFF"))
        mDefaultColor = typedArray.getColor(R.styleable.LyricView_textColor, Color.parseColor("#8D8D8D"))
        mHighLightColor = typedArray.getColor(R.styleable.LyricView_highlightColor, Color.parseColor("#FFFFFF"))

        mTextSize = typedArray.getDimensionPixelSize(
            R.styleable.LyricView_textSize,
            getRawSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE.toFloat()).toInt()
        )
        mMaxLength = typedArray.getDimensionPixelSize(
            R.styleable.LyricView_maxLength,
            getRawSize(TypedValue.COMPLEX_UNIT_PX, mWidth * 0.86f).toInt()
        )
        mLineSpace = typedArray.getDimensionPixelSize(
            R.styleable.LyricView_lineSpace,
            getRawSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_SPACE.toFloat()).toInt()
        ).toFloat()
        typedArray.recycle()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        when (event.action) {
            MotionEvent.ACTION_CANCEL -> actionCancel()
            MotionEvent.ACTION_DOWN -> actionDown(event)
            MotionEvent.ACTION_MOVE -> actionMove(event)
            MotionEvent.ACTION_UP -> actionUp()
            else -> {
            }
        }
        invalidateView()
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mShaderWidth = width * 0.141f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (scrollable()) {
            for (i in 0 until mLineCount) {
                val x = width * 0.5f
                val y: Float = if (mEnableLineFeed && i > 0) {
                    measuredHeight * 0.5f + i * mLineHeight - mScrollY + mLineFeedRecord[i - 1]
                } else {
                    measuredHeight * 0.5f + i * mLineHeight - mScrollY
                }
                if (y < 0) {
                    continue
                }
                if (y > height) {
                    break
                }
                if (i == mCurrentPlayLine - 1) {
                    mTextPaint!!.color = mHighLightColor
                } else {
                    mTextPaint!!.color = mDefaultColor
                }
                if (mIsShade && (y > height - mShaderWidth || y < mShaderWidth)) {
                    if (y < mShaderWidth) {
                        mTextPaint!!.alpha = 26 + (23000.0f * y / mShaderWidth * 0.01f).toInt()
                    } else {
                        mTextPaint!!.alpha = 26 + (23000.0f * (height - y) / mShaderWidth * 0.01f).toInt()
                    }
                } else {
                    mTextPaint!!.alpha = 255
                }
                if (mEnableLineFeed) {
                    val staticLayout = StaticLayout.Builder.obtain(
                        mLyricInfo!!.songLines!![i].content!!, 0,
                        mLyricInfo!!.songLines!![i].content!!.length, mTextPaint!! as TextPaint, mMaxLength
                    ).build()
                    canvas.save()
                    canvas.translate(x, y)
                    staticLayout.draw(canvas)
                    canvas.restore()
                } else {
                    canvas.drawText(mLyricInfo!!.songLines!![i].content!!, x, y, mTextPaint!!)
                }
            }
        } else {
            // 无歌词
            mTextPaint!!.color = mHintColor
            canvas.drawText(
                mDefaultHint!!,
                measuredWidth / 2.toFloat(),
                measuredHeight / 2.toFloat(),
                mTextPaint!!
            )
        }
    }

    private fun doFlingAnimator(velocity: Float) {
        val distance =
            velocity / abs(velocity) * (abs(velocity) * SLIDE_COEFFICIENT)
        val to = 0f.coerceAtLeast(mScrollY - distance)
            .coerceAtMost(
                (mLineCount - 1) * mLineHeight + mLineFeedRecord[mLineCount - 1] +
                        if (mEnableLineFeed) mTextHeight else 0
            )
        mFlingAnimator = ValueAnimator.ofFloat(mScrollY, to)
        mFlingAnimator?.addUpdateListener { animation: ValueAnimator ->
            mScrollY = animation.animatedValue as Float
            invalidateView()
        }
        mFlingAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mVelocity = 0f
                mFling = true
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mFling = false
            }
        })
        mFlingAnimator?.duration = FLING_ANIMATOR_DURATION.toLong()
        mFlingAnimator?.interpolator = DecelerateInterpolator()
        mFlingAnimator?.start()
    }

    private fun smoothScrollTo(toY: Float) {
        val animator = ValueAnimator.ofFloat(mScrollY, toY)
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            mScrollY = valueAnimator.animatedValue as Float
            invalidateView()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                mFling = false
                invalidateView()
            }

            override fun onAnimationRepeat(animator: Animator) {}
            override fun onAnimationStart(animator: Animator) {
                mFling = true
            }
        })
        animator.duration = 640
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun overScrolled(): Boolean {
        return scrollable() && (mScrollY > mLineHeight * (mLineCount - 1) + mLineFeedRecord[mLineCount - 1] + (0) || mScrollY < 0)
    }

    private fun scrollable(): Boolean {
        return mLyricInfo?.songLines != null && mLyricInfo?.songLines!!.isNotEmpty()
    }

    private fun actionMove(event: MotionEvent) {
        if (scrollable()) {
            val tracker = mVelocityTracker!!
            tracker.computeCurrentVelocity(UNITS_SECOND, maxVelocity.toFloat())
            mScrollY = mLastScrollY + mDownY - event.y
            mVelocity = tracker.yVelocity
        }
    }

    private fun actionUp() {
        postDelayed(hideIndicator, 3 * UNITS_SECOND.toLong())
        releaseVelocityTracker()
        if (scrollable()) {
            if (overScrolled() && mScrollY < 0) {
                smoothScrollTo(0f)
                return
            }
            if (overScrolled() && mScrollY > mLineHeight * (mLineCount - 1) + mLineFeedRecord[mLineCount - 1] + (if (mEnableLineFeed) mTextHeight else 0)) {
                smoothScrollTo(mLineHeight * (mLineCount - 1) + mLineFeedRecord[mLineCount - 1] + if (mEnableLineFeed) mTextHeight else 0)
                return
            }
            if (abs(mVelocity) > THRESHOLD_Y_VELOCITY) {
                doFlingAnimator(mVelocity)
            }
        }
    }

    private fun actionCancel() {
        releaseVelocityTracker()
    }

    private fun releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.clear()
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

    private fun actionDown(event: MotionEvent) {
        removeCallbacks(hideIndicator)
        mLastScrollY = mScrollY
        mDownY = event.y
        if (mFlingAnimator != null) {
            mFlingAnimator?.cancel()
            mFlingAnimator = null
        }
    }

    private fun initLyricView(context: Context) {
        maxVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        initPaint()
        initAllBounds()
    }

    private fun initPaint() {
        mTextPaint = TextPaint()
        mTextPaint?.isDither = true
        mTextPaint?.isAntiAlias = true
        mTextPaint?.textAlign = Paint.Align.CENTER
    }

    private fun initAllBounds() {
        setRawTextSize(mTextSize.toFloat())
        setLineSpace(mLineSpace)
        measureLineHeight()
    }

    private fun resetLyricInfo() {
        if (mLyricInfo != null) {
            if (mLyricInfo?.songLines != null) {
                mLyricInfo?.songLines!!.clear()
                mLyricInfo?.songLines = null
            }
            mLyricInfo = null
        }
    }

    private fun reset() {
        mCurrentPlayLine = 0
        resetLyricInfo()
        invalidateView()
        mLineCount = 0
        mScrollY = 0f
        mEnableLineFeed = false
        mLineFeedRecord.clear()
        mExtraHeight = 0
    }

    fun setCurrentTimeMillis(current: Long) {

        var position = 0
        if (scrollable()) {
            var i = 0
            val size = mLineCount
            while (i < size) {
                val lineInfo: LineInfo = mLyricInfo!!.songLines!![i]
                if (lineInfo.start >= current) {
                    position = i
                    break
                }
                if (i == mLineCount - 1) {
                    position = mLineCount
                }
                i++
            }
        }
        if (mCurrentPlayLine != position) {
            mCurrentPlayLine = position
            if (!mFling) {
                smoothScrollTo(measureCurrentScrollY(position))
            }
        }
    }

    fun setLyricFile(file: File?) {
        if (file == null || !file.exists()) {
            reset()
            mCurrentLyricFilePath = ""
            return
        } else if (file.path == mCurrentLyricFilePath) {
            return
        } else {
            mCurrentLyricFilePath = file.path
            reset()
        }
        if (file.exists()) {
            try {
                setupLyricResource(FileInputStream(file))
                for (i in mLyricInfo!!.songLines!!.indices) {
                    val staticLayout = StaticLayout.Builder.obtain(
                        mLyricInfo?.songLines!![i].content!!, 0, mLyricInfo?.songLines!![i].content!!.length,
                        mTextPaint!! as TextPaint, getRawSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            mWidth * 0.86f
                        ).toInt()
                    ).build()
                    if (staticLayout.lineCount > 1) {
                        mEnableLineFeed = true
                        mExtraHeight += (staticLayout.lineCount - 1) * mTextHeight
                    }
                    mLineFeedRecord.add(i, mExtraHeight)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            invalidateView()
        }
    }

    /**
     * 动态识别歌词编码格式
     * 防止出现乱码问题
     */
    private fun getBufferedReader(bufferedInputStream: BufferedInputStream): BufferedReader {
        var charset = "GBK"
        val first3bytes = ByteArray(3)
        var checked = false
        bufferedInputStream.mark(-1)
        var read = bufferedInputStream.read(first3bytes)
        if (read == -1) {
            bufferedInputStream.reset()
            return BufferedReader(InputStreamReader(bufferedInputStream, charset))
        }
        if (first3bytes[0] == 0xEF.toByte() && first3bytes[1] == 0xBB.toByte() && first3bytes[2] == 0xBF.toByte()) { // utf-8
            Log.d(tag, "Lyric text type = utf-8")
            checked = true
            charset = "UTF-8"
        } else if (first3bytes[0] == 0xFF.toByte()
            && first3bytes[1] == 0xFE.toByte()
        ) {
            Log.d(tag, "Lyric text type = utf-unicode")
            charset = "unicode"
            checked = true
        } else if (first3bytes[0] == 0xFE.toByte()
            && first3bytes[1] == 0xFF.toByte()
        ) {
            Log.d(tag, "Lyric text type = utf-16be")
            charset = "UTF-16BE"
            checked = true
        } else if (first3bytes[0] == 0xFF.toByte()
            && first3bytes[1] == 0xFF.toByte()
        ) {
            charset = "UTF-16LE"
            checked = true
        }
        bufferedInputStream.reset()
        if (!checked) {
            while (bufferedInputStream.read().also { read = it } != -1) {
                if (read >= 0xF0) {
                    break
                }
                if (read in 0x80..0xBF) {
                    break
                }
                if (read in 0xC0..0xDF) {
                    read = bufferedInputStream.read()
                    if (read in 0x80..0xBF) {
                        Log.d(tag, "read in 0x80..0xBF")
                    } else {
                        break
                    }
                } else if (0xE0 <= read) {
                    read = bufferedInputStream.read()
                    if (read in 0x80..0xBF) {
                        read = bufferedInputStream.read()
                        if (read in 0x80..0xBF) {
                            charset = "UTF-8"
                            break
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }
        }

        bufferedInputStream.reset()
        return BufferedReader(InputStreamReader(bufferedInputStream, charset))
    }

    private fun setupLyricResource(inputStream: InputStream?) {
        if (inputStream != null) {
            try {
                val lyricInfo = LyricInfo()
                lyricInfo.songLines = ArrayList()
                val inputStreamReader = BufferedInputStream(inputStream)
                val bufferedReader: BufferedReader? = getBufferedReader(inputStreamReader)
                var line: String?
                while (bufferedReader?.readLine().also { line = it } != null) {
                    analyzeLyric(lyricInfo, line)
                }
                bufferedReader?.close()
                inputStream.close()
                inputStreamReader.close()
                mLyricInfo = lyricInfo
                mLineCount = mLyricInfo!!.songLines!!.size
                invalidateView()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            invalidateView()
        }
    }

    /**
     * 逐行解析歌词内容
     */
    private fun analyzeLyric(lyricInfo: LyricInfo, line: String?) {
        line?.let {
            val index = it.lastIndexOf("]")
            val timestampStr = it.split("]").toTypedArray()
            if (it.startsWith("[offset:")) { // time offset
                lyricInfo.songOffset = it.substring(8, index).trim().toLong()
                return
            }
            if (it.startsWith("[ti:")) { // title
                lyricInfo.songTitle = it.substring(4, index).trim()
                return
            }
            if (it.startsWith("[ar:")) { // artist
                lyricInfo.songArtist = it.substring(4, index).trim()
                return
            }
            if (it.startsWith("[al:")) { // album
                lyricInfo.songAlbum = it.substring(4, index).trim()
                return
            }
            if (it.startsWith("[by:")) {
                return
            }
            if (timestampStr.size > 1) {
                for (i in 0 until timestampStr.size - 1) {
                    val lineInfo = LineInfo()
                    val temp = timestampStr[i]
                    val millisecond = measureStartTimeMillis(temp.replace("[", ""))
                    lineInfo.start = millisecond
                    lineInfo.content = timestampStr[timestampStr.size - 1]
                    lyricInfo.songLines?.add(lineInfo)
                }
            }
        }
    }

    /**
     * 从字符串中获得时间值
     */
    private fun measureStartTimeMillis(string: String): Long {
        return try {
            val s1: Array<String> = string.split(":").toTypedArray()
            val minute: Long = s1[0].toLong()
            val s2: Array<String> = s1[1].split(".").toTypedArray()
            val second = s2[0].toLong()
            var millisecond: Long = 0
            if (s2.size > 1) {
                millisecond = s2[1].toLong()
            }
            millisecond + second * 1000 + minute * 60 * 1000
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }


    private fun setLineSpace(lineSpace: Float) {
        if (mLineSpace != lineSpace) {
            mLineSpace = getRawSize(TypedValue.COMPLEX_UNIT_DIP, lineSpace)
            measureLineHeight()
            mScrollY = measureCurrentScrollY(mCurrentPlayLine)
            invalidateView()
        }
    }

    private fun setRawTextSize(size: Float) {
        if (size != mTextPaint!!.textSize) {
            mTextPaint!!.textSize = size
            measureLineHeight()
            mScrollY = measureCurrentScrollY(mCurrentPlayLine)
            invalidateView()
        }
    }

    private fun measureCurrentScrollY(line: Int): Float {
        return if (mEnableLineFeed && line > 1) {
            (line - 1) * mLineHeight + mLineFeedRecord[line - 1]
        } else (line - 1) * mLineHeight
    }

    private fun measureLineHeight() {
        val lineBound = Rect()
        mTextPaint!!.getTextBounds(mDefaultHint, 0, mDefaultHint!!.length, lineBound)
        mTextHeight = lineBound.height()
        mLineHeight = mTextHeight + mLineSpace
    }

    private fun getRawSize(unit: Int, size: Float): Float {
        val context = context
        val resources: Resources
        resources = if (context == null) {
            Resources.getSystem()
        } else {
            context.resources
        }
        return TypedValue.applyDimension(unit, size, resources.displayMetrics)
    }

    private class LyricInfo {
        var songLines: ArrayList<LineInfo>? = null
        var songArtist: String? = null
        var songTitle: String? = null
        var songAlbum: String? = null
        var songOffset: Long = 0
    }

    private class LineInfo {
        var content: String? = null
        var start: Long = 0
    }

    private fun invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    private var hideIndicator = Runnable { this.invalidateView() }
}