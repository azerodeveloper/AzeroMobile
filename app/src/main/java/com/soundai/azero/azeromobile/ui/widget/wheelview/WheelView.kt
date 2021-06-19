package com.soundai.azero.azeromobile.ui.widget.wheelview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.soundai.azero.azeromobile.R
import java.util.*

class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var mCyclic: Boolean
    var mItemCount: Int
    var mItemWidth: Int
    @JvmField
    var mItemHeight: Int
    var mClipRectTop: Rect? = null
    var mClipRectMiddle: Rect? = null
    var mClipRectBottom: Rect? = null
    var mTextPaint: TextPaint
    var mSelectedTextPaint: TextPaint
    var mDividerPaint: Paint
    var mHighlightPaint: Paint
    private val mCamera: Camera
    private val mMatrix: Matrix
    var mScroller: WheelScroller
    @JvmField
    val mEntries: MutableList<CharSequence> =
        ArrayList()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.EXACTLY
            && heightSpecMode == MeasureSpec.EXACTLY
        ) {
            setMeasuredDimension(widthSpecSize, heightSpecSize)
        } else if (widthSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSpecSize, prefHeight)
        } else if (heightSpecMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(prefWidth, heightSpecSize)
        } else {
            setMeasuredDimension(prefWidth, prefHeight)
        }
        updateClipRect()
    }

    val prefHeight: Int
        get() {
            val padding = paddingTop + paddingBottom
            val innerHeight = (mItemHeight * mItemCount * 2 / Math.PI).toInt()
            return innerHeight + padding
        }

    private fun updateClipRect() {
        val clipLeft = paddingLeft
        val clipRight = measuredWidth - paddingRight
        val clipTop = paddingTop
        val clipBottom = measuredHeight - paddingBottom
        val clipVMiddle = (clipTop + clipBottom) / 2
        mClipRectMiddle = Rect()
        mClipRectMiddle!!.left = clipLeft
        mClipRectMiddle!!.right = clipRight
        mClipRectMiddle!!.top = clipVMiddle - mItemHeight / 2
        mClipRectMiddle!!.bottom = clipVMiddle + mItemHeight / 2
        mClipRectTop = Rect()
        mClipRectTop!!.left = clipLeft
        mClipRectTop!!.right = clipRight
        mClipRectTop!!.top = clipTop
        mClipRectTop!!.bottom = clipVMiddle - mItemHeight / 2
        mClipRectBottom = Rect()
        mClipRectBottom!!.left = clipLeft
        mClipRectBottom!!.right = clipRight
        mClipRectBottom!!.top = clipVMiddle + mItemHeight / 2
        mClipRectBottom!!.bottom = clipBottom
    }

    fun `$dp`(resId: Int): Int {
        return resources.getDimensionPixelOffset(resId)
    }

    fun `$sp`(resId: Int): Int {
        return resources.getDimensionPixelSize(resId)
    }

    fun `$color`(resId: Int): Int {
        return resources.getColor(resId)
    }

    /**
     * @return 控件的预算宽度
     */
    val prefWidth: Int
        get() {
            val paddingHorizontal = paddingLeft + paddingRight
            return paddingHorizontal + mItemWidth
        }

    override fun onDraw(canvas: Canvas) {
        drawHighlight(canvas)
        drawItems(canvas)
        drawDivider(canvas)
    }

    private fun drawItems(canvas: Canvas) {
        val index = mScroller.itemIndex
        val offset = mScroller.itemOffset
        val hf = (mItemCount + 1) / 2
        val minIdx: Int
        val maxIdx: Int
        if (offset < 0) {
            minIdx = index - hf - 1
            maxIdx = index + hf
        } else if (offset > 0) {
            minIdx = index - hf
            maxIdx = index + hf + 1
        } else {
            minIdx = index - hf
            maxIdx = index + hf
        }
        for (i in minIdx until maxIdx) {
            drawItem(canvas, i, offset)
        }
    }

    protected fun drawItem(canvas: Canvas, index: Int, offset: Int) {
        val text = getCharSequence(index) ?: return
        // 滚轮的半径
        val r = (height - paddingTop - paddingBottom) / 2
        // 和中间选项的距离
        val range = (index - mScroller.itemIndex) * mItemHeight - offset
        // 当滑动的角度和y轴垂直时（此时文字已经显示为一条线），不绘制文字
        if (Math.abs(range) > r * Math.PI / 2) return
        val centerX = mClipRectMiddle!!.centerX()
        val centerY = mClipRectMiddle!!.centerY()
        val angle = range.toDouble() / r
        // 绕x轴滚动的角度
        val rotate = Math.toDegrees(-angle).toFloat()
        // 滚动的距离映射到x轴的长度
//        float translateX = (float) (Math.cos(angle) * Math.sin(Math.PI / 36) * r * mToward);
// 滚动的距离映射到y轴的长度
        val translateY = (Math.sin(angle) * r).toFloat()
        // 滚动的距离映射到z轴的长度
        val translateZ = ((1 - Math.cos(angle)) * r).toFloat()
        // 折射偏移量x
        val refractX = textSize * .05f
        // 透明度
        val alpha = (Math.cos(angle) * 255).toInt()
        // 绘制与下分界线相交的文字
        if (range > 0 && range < mItemHeight) {
            canvas.save()
            canvas.translate(refractX, 0f)
            canvas.clipRect(mClipRectMiddle)
            drawText(
                canvas,
                text,
                centerX.toFloat(),
                centerY.toFloat(),
                0f,
                translateY,
                translateZ,
                rotate,
                mSelectedTextPaint
            )
            canvas.restore()
            mTextPaint.alpha = alpha
            canvas.save()
            canvas.clipRect(mClipRectBottom)
            drawText(
                canvas,
                text,
                centerX.toFloat(),
                centerY.toFloat(),
                0f,
                translateY,
                translateZ,
                rotate,
                mTextPaint
            )
            canvas.restore()
        } else if (range >= mItemHeight) {
            mTextPaint.alpha = alpha
            canvas.save()
            canvas.clipRect(mClipRectBottom)
            drawText(
                canvas,
                text,
                centerX.toFloat(),
                centerY.toFloat(),
                0f,
                translateY,
                translateZ,
                rotate,
                mTextPaint
            )
            canvas.restore()
        } else if (range < 0 && range > -mItemHeight) {
            canvas.save()
            canvas.translate(refractX, 0f)
            canvas.clipRect(mClipRectMiddle)
            drawText(
                canvas,
                text,
                centerX.toFloat(),
                centerY.toFloat(),
                0f,
                translateY,
                translateZ,
                rotate,
                mSelectedTextPaint
            )
            canvas.restore()
            mTextPaint.alpha = alpha
            canvas.save()
            canvas.clipRect(mClipRectTop)
            drawText(
                canvas,
                text,
                centerX.toFloat(),
                centerY.toFloat(),
                0f,
                translateY,
                translateZ,
                rotate,
                mTextPaint
            )
            canvas.restore()
        } else if (range <= -mItemHeight) {
            mTextPaint.alpha = alpha
            canvas.save()
            canvas.clipRect(mClipRectTop)
            drawText(
                canvas,
                text,
                centerX.toFloat(),
                centerY.toFloat(),
                0f,
                translateY,
                translateZ,
                rotate,
                mTextPaint
            )
            canvas.restore()
        } else {
            canvas.save()
            canvas.translate(refractX, 0f)
            canvas.clipRect(mClipRectMiddle)
            drawText(
                canvas,
                text,
                centerX.toFloat(),
                centerY.toFloat(),
                0f,
                translateY,
                translateZ,
                rotate,
                mSelectedTextPaint
            )
            canvas.restore()
        }
    }

    private fun drawText(
        canvas: Canvas,
        text: CharSequence,
        centerX: Float,
        centerY: Float,
        translateX: Float,
        translateY: Float,
        translateZ: Float,
        rotateX: Float,
        paint: Paint
    ) {
        mCamera.save()
        mCamera.translate(translateX, 0f, translateZ)
        mCamera.rotateX(rotateX)
        mCamera.getMatrix(mMatrix)
        mCamera.restore()
        val y = centerY + translateY
        // 设置绕x轴旋转的中心点位置
        mMatrix.preTranslate(-centerX, -y)
        mMatrix.postTranslate(centerX, y)
        val fontMetrics = paint.fontMetrics
        val baseline = ((fontMetrics.top + fontMetrics.bottom) / 2).toInt()
        canvas.concat(mMatrix)
        canvas.drawText(text, 0, text.length, centerX, y - baseline, paint)
    }

    fun getCharSequence(index: Int): CharSequence? {
        val size = mEntries.size
        if (size == 0) return null
        var text: CharSequence? = null
        if (isCyclic) {
            var i = index % size
            if (i < 0) {
                i += size
            }
            text = mEntries[i]
        } else {
            if (index >= 0 && index < size) {
                text = mEntries[index]
            }
        }
        return text
    }

    private fun drawHighlight(canvas: Canvas) {
        canvas.drawRect(mClipRectMiddle, mHighlightPaint)
    }

    private fun drawDivider(canvas: Canvas) { // 绘制上层分割线
        canvas.drawLine(
            mClipRectMiddle!!.left.toFloat(),
            mClipRectMiddle!!.top.toFloat(),
            mClipRectMiddle!!.right.toFloat(),
            mClipRectMiddle!!.top.toFloat(),
            mDividerPaint
        )
        // 绘制下层分割线
        canvas.drawLine(
            mClipRectMiddle!!.left.toFloat(),
            mClipRectMiddle!!.bottom.toFloat(),
            mClipRectMiddle!!.right.toFloat(),
            mClipRectMiddle!!.bottom.toFloat(),
            mDividerPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mScroller.onTouchEvent(event)
    }

    override fun computeScroll() {
        mScroller.computeScroll()
    }

    var isCyclic: Boolean
        get() = mCyclic
        set(cyclic) {
            mCyclic = cyclic
            mScroller.reset()
            invalidate()
        }

    val textSize: Float
        get() = mTextPaint.textSize

    fun setTextSize(textSize: Int) {
        mTextPaint.textSize = textSize.toFloat()
        mSelectedTextPaint.textSize = textSize.toFloat()
        invalidate()
    }

    var textColor: Int
        get() = mTextPaint.color
        set(color) {
            mTextPaint.color = color
            invalidate()
        }

    var selectedTextColor: Int
        get() = mSelectedTextPaint.color
        set(color) {
            mSelectedTextPaint.color = color
            invalidate()
        }

    val itemSize: Int
        get() = mEntries.size

    fun getItem(index: Int): CharSequence? {
        return if (index < 0 && index >= mEntries.size) null else mEntries[index]
    }

    val currentItem: CharSequence?
        get() = getItem(currentIndex)

    var currentIndex: Int
        get() = mScroller.currentIndex
        set(index) {
            setCurrentIndex(index, false)
        }

    fun setCurrentIndex(index: Int, animated: Boolean) {
        mScroller.setCurrentIndex(index, animated)
    }

    fun setEntries(vararg entries: CharSequence?) {
        mEntries.clear()
        if (entries != null && entries.size > 0) {
            Collections.addAll<CharSequence>(mEntries, *entries)
        }
        mScroller.reset()
        invalidate()
    }

    fun setEntries(entries: Collection<CharSequence>?) {
        mEntries.clear()
        if (entries != null && entries.size > 0) {
            mEntries.addAll(entries)
        }
        mScroller.reset()
        invalidate()
    }

    var onWheelChangedListener: OnWheelChangedListener?
        get() = mScroller.onWheelChangedListener
        set(onWheelChangedListener) {
            mScroller.onWheelChangedListener = onWheelChangedListener
        }

    interface OnWheelChangedListener {
        fun onChanged(
            view: WheelView?,
            oldIndex: Int,
            newIndex: Int,
            data: String?
        )
    }

    init {
        mCamera = Camera()
        mMatrix = Matrix()
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.WheelView
        )
        val cyclic =
            a.getBoolean(R.styleable.WheelView_wheelCyclic, false)
        val itemCount =
            a.getInt(R.styleable.WheelView_wheelItemCount, 9)
        val itemWidth = a.getDimensionPixelOffset(
            R.styleable.WheelView_wheelItemWidth,
            `$dp`(R.dimen.wheel_item_width)
        )
        val itemHeight = a.getDimensionPixelOffset(
            R.styleable.WheelView_wheelItemHeight,
            `$dp`(R.dimen.wheel_item_height)
        )
        val textSize = a.getDimensionPixelSize(
            R.styleable.WheelView_wheelTextSize,
            `$sp`(R.dimen.wheel_text_size)
        )
        val textColor = a.getColor(
            R.styleable.WheelView_wheelTextColor,
            `$color`(R.color.wheel_text_color)
        )
        val selectedTextColor = a.getColor(
            R.styleable.WheelView_wheelSelectedTextColor,
            `$color`(R.color.wheel_selected_text_color)
        )
        val dividerColor = a.getColor(
            R.styleable.WheelView_wheelDividerColor,
            `$color`(R.color.wheel_divider_color)
        )
        val highlightColor = a.getColor(
            R.styleable.WheelView_wheelHighlightColor,
            `$color`(R.color.wheel_highlight_color)
        )
        val entries =
            a.getTextArray(R.styleable.WheelView_wheelEntries)
        a.recycle()
        mCyclic = cyclic
        mItemCount = itemCount
        mItemWidth = itemWidth
        mItemHeight = itemHeight
        mTextPaint = TextPaint()
        mTextPaint.isAntiAlias = true
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.textSize = textSize.toFloat()
        mTextPaint.color = textColor
        mSelectedTextPaint = TextPaint()
        mSelectedTextPaint.isAntiAlias = true
        mSelectedTextPaint.textAlign = Paint.Align.CENTER
        mSelectedTextPaint.textSize = textSize.toFloat()
        mSelectedTextPaint.color = selectedTextColor
        mDividerPaint = Paint()
        mDividerPaint.isAntiAlias = true
        mDividerPaint.strokeWidth =
            resources.getDimensionPixelOffset(R.dimen.wheel_divider_height).toFloat()
        mDividerPaint.color = dividerColor
        mHighlightPaint = Paint()
        mHighlightPaint.isAntiAlias = true
        mHighlightPaint.style = Paint.Style.FILL
        mHighlightPaint.color = highlightColor
        if (entries != null && entries.size > 0) {
            mEntries.addAll(Arrays.asList(*entries))
        }
        mScroller = WheelScroller(context, this)
    }
}