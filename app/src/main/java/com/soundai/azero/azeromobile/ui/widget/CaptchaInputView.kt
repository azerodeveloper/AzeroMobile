package com.soundai.azero.azeromobile.ui.widget

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.EditText
import com.soundai.azero.azeromobile.R

class CaptchaInputView(context: Context, attrs: AttributeSet?) :
    EditText(context, attrs) {
    private var borderColor = 0
    private var borderWidth = 0f
    private var borderRadius = 0f
    private var passwordLength = 6
    private var passwordColor = 0
    private var passwordWidth = 0f
    private var passwordRadius = 0f
    private val passwordPaint: Paint = Paint(ANTI_ALIAS_FLAG)
    private val borderPaint: Paint = Paint(ANTI_ALIAS_FLAG)
    private val linePaint: Paint = Paint(ANTI_ALIAS_FLAG)
    private val defaultContMargin = 5
    private val defaultSplitLineWidth = 3
    private var mTextChangeListener: TextChangeListener? = null
    private var mDefaultInputViewTextSize = 0f
    private var mDefaultInputViewPadding = 0f
    private var mDefaultInputTextSize = 0f
    private val mCursorWidth: Float
    private val mCursorHeight: Float
    private val mDefalutMargin = 10f
    private var mPwdVisiable = true
    private var mInputText: String = ""
    private val mContext: Context = context
    private var mSelectIndex = 0
    private val mCursorHandler: Handler?
    private val mCursorRunnable: CursorRunnable?
    private val CURSOR_DELAY_TIME = 500L

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var width = 0
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize
            mDefaultInputViewPadding =
                (width - mDefalutMargin * 2 - mDefaultInputViewTextSize * passwordLength).div(passwordLength - 1)
        } else {
            val width =
                passwordLength * mDefaultInputViewTextSize.toInt() + mDefaultInputViewPadding.toInt() * 3 + mDefalutMargin.toInt() * 2
        }
        val height = mDefaultInputViewTextSize.toInt() + mDefalutMargin.toInt() * 2
        setMeasuredDimension(width, height)
    }

    private inner class CursorRunnable : Runnable {
        private var mCancelled = false
        private var mCursorVisible = false
        override fun run() {
            if (mCancelled) {
                return
            }
            postInvalidate()
            postDelayed(this, CURSOR_DELAY_TIME)
        }

        fun cancel() {
            if (!mCancelled) {
                mCursorHandler?.removeCallbacks(this)
                mCancelled = true
            }
        }

        val cursorVisiable: Boolean
            get() = !mCursorVisible.also { mCursorVisible = it }
    }

    fun stopCursor() {
        if (mCursorRunnable != null && mCursorHandler != null) {
            mCursorRunnable.cancel()
        }
    }

    //	点击事件的处理
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		float x = event.getX();
//		float y = event.getY();
//		switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//			for(int i = 0; i< rectList.size(); i++) {
//				RectF rectF = rectList.get(i);
//				if(rectF.contains(x, y)) {
//					mSelectIndex = i;
//					postInvalidate();
//
//					Log.d("draw", "index="+i);
//					break;
//				}
//			}
//			break;
//		}
//		return super.onTouchEvent(event);
//	}

    override fun onDraw(canvas: Canvas) {
        //边框
        var left = mDefalutMargin
        val top = mDefalutMargin + mDefaultInputViewTextSize
        for (i in 0 until passwordLength) {
            borderPaint.color = borderColor
            canvas.drawLine(left, top, left + mDefaultInputViewTextSize, top, borderPaint)
            left += mDefaultInputViewPadding + mDefaultInputViewTextSize
        }
        //内容,密码可见
        var textLeft = mDefalutMargin + mDefaultInputViewTextSize / 2
        if (mPwdVisiable) {
            for (i in mInputText.indices) {
                val text = mInputText.substring(i, i + 1)
                val textWidth =
                    if (!TextUtils.isEmpty(text)) getTextWidth(passwordPaint, text) / 2 else 0
                canvas.drawText(
                    text,
                    textLeft - textWidth,
                    mDefaultInputViewTextSize / 2 + mDefaultInputTextSize / 2,
                    passwordPaint
                )
                textLeft += mDefaultInputViewPadding + mDefaultInputViewTextSize
            }
        } else {
            for (i in mInputText.indices) {
                val text = mInputText.substring(i, i + 1)
                val textWidth =
                    if (!TextUtils.isEmpty(text)) getTextWidth(passwordPaint, "*") / 2 else 0
                canvas.drawText(
                    "*",
                    textLeft - textWidth,
                    mDefaultInputViewTextSize / 2 + mDefaultInputTextSize / 2 + 5,
                    passwordPaint
                )
                textLeft += mDefaultInputViewPadding + mDefaultInputViewTextSize
            }
        }
        //光标
        if (mSelectIndex < passwordLength && mCursorRunnable!!.cursorVisiable) {
            val cursorLeft = mDefalutMargin
            val cursorTop =
                mDefalutMargin + (mDefaultInputViewTextSize - mCursorHeight) / 2
            val startX =
                cursorLeft + mDefaultInputViewTextSize / 2 + mSelectIndex * mDefaultInputViewTextSize + mSelectIndex * mDefaultInputViewPadding
            val stopY = cursorTop + mCursorHeight
            canvas.drawLine(startX, cursorTop, startX, stopY, linePaint)
        }
    }

    fun getTextWidth(paint: Paint, str: String?): Int {
        var iRet = 0
        if (str != null && str.length > 0) {
            val len = str.length
            val widths = FloatArray(len)
            paint.getTextWidths(str, widths)
            for (j in 0 until len) {
                iRet += Math.ceil(widths[j].toDouble()).toInt()
            }
        }
        return iRet
    }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        if (text.length > passwordLength) {
            setText(text.substring(0, passwordLength))
            requestFocus()
            setSelection(passwordLength)
            return
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (mTextChangeListener != null) {
            mTextChangeListener!!.onTextChanged(text, start, lengthBefore, lengthAfter)
        }
        mInputText = text.toString()
        mSelectIndex = if (mInputText.isNotEmpty()) {
            mInputText.length
        } else {
            0
        }
        postInvalidate()
    }

    fun setPwdVisiable(pwdVisiable: Boolean) {
        mPwdVisiable = pwdVisiable
    }

    fun setTextLength(length: Int) {
        passwordLength = length
        postInvalidate()
    }

    fun getBorderColor(): Int {
        return borderColor
    }

    fun setBorderColor(borderColor: Int) {
        this.borderColor = borderColor
        borderPaint.setColor(borderColor)
        invalidate()
    }

    fun getBorderWidth(): Float {
        return borderWidth
    }

    fun setBorderWidth(borderWidth: Float) {
        this.borderWidth = borderWidth
        borderPaint.setStrokeWidth(borderWidth)
        invalidate()
    }

    fun getBorderRadius(): Float {
        return borderRadius
    }

    fun setBorderRadius(borderRadius: Float) {
        this.borderRadius = borderRadius
        invalidate()
    }

    fun getPasswordLength(): Int {
        return passwordLength
    }

    fun setPasswordLength(passwordLength: Int) {
        this.passwordLength = passwordLength
        invalidate()
    }

    fun getPasswordColor(): Int {
        return passwordColor
    }

    fun setPasswordColor(passwordColor: Int) {
        this.passwordColor = passwordColor
        passwordPaint.setColor(passwordColor)
        invalidate()
    }

    fun getPasswordWidth(): Float {
        return passwordWidth
    }

    fun setPasswordWidth(passwordWidth: Float) {
        this.passwordWidth = passwordWidth
        passwordPaint.setStrokeWidth(passwordWidth)
        invalidate()
    }

    fun getPasswordRadius(): Float {
        return passwordRadius
    }

    fun setPasswordRadius(passwordRadius: Float) {
        this.passwordRadius = passwordRadius
        invalidate()
    }

    fun setTextChangeListener(mTextChangeListener: TextChangeListener?) {
        this.mTextChangeListener = mTextChangeListener
    }

    interface TextChangeListener {
        fun onTextChanged(
            text: CharSequence?,
            start: Int,
            lengthBefore: Int,
            lengthAfter: Int
        )
    }

    init {
        val res: Resources = resources
        val defaultBorderColor: Int = res.getColor(R.color.default_ev_border_color)
        val defaultBorderWidth: Float = res.getDimension(R.dimen.default_ev_border_width)
        val defaultBorderRadius: Float = res.getDimension(R.dimen.default_ev_border_radius)
        val defaultPasswordLength: Int = res.getInteger(R.integer.default_ev_password_length)
        val defaultPasswordColor: Int = res.getColor(R.color.default_ev_password_color)
        val defaultPasswordWidth: Float = res.getDimension(R.dimen.default_ev_password_width)
        val defaultPasswordRadius: Float =
            res.getDimension(R.dimen.default_ev_password_radius)
        val defaultInputViewTextSize: Float =
            res.getDimension(R.dimen.default_input_text_view_size)
        val defaultInputViewPadding: Float =
            res.getDimension(R.dimen.default_input_text_view_padding)
        val defaultInputTextSize: Float = res.getDimension(R.dimen.default_input_text_size)
        val a: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.PasswordInputView, 0, 0)
        try {
            borderColor =
                a.getColor(R.styleable.PasswordInputView_captchaBorderColor, defaultBorderColor)
            borderWidth =
                a.getDimension(R.styleable.PasswordInputView_captchaBorderWidth, defaultBorderWidth)
            borderRadius = a.getDimension(
                R.styleable.PasswordInputView_captchaBorderRadius,
                defaultBorderRadius
            )
            passwordLength =
                a.getInt(R.styleable.PasswordInputView_captchaLength, defaultPasswordLength)
            passwordColor =
                a.getColor(R.styleable.PasswordInputView_captchaColor, defaultPasswordColor)
            passwordWidth =
                a.getDimension(R.styleable.PasswordInputView_captchaWidth, defaultPasswordWidth)
            passwordRadius =
                a.getDimension(R.styleable.PasswordInputView_captchaRadius, defaultPasswordRadius)
            mDefaultInputViewTextSize = a.getDimension(
                R.styleable.PasswordInputView_captchaViewSize,
                defaultInputViewTextSize
            )
            mDefaultInputViewPadding =
                a.getDimension(R.styleable.PasswordInputView_captchaViewPadding, defaultInputViewPadding)
            mDefaultInputTextSize = a.getDimension(
                R.styleable.PasswordInputView_captchaTextSize,
                    defaultInputTextSize
            )
        } finally {
            a.recycle()
        }
        mCursorWidth = mContext.resources.getDimension(R.dimen.captcha_cursor_width)
        mCursorHeight = mContext.resources.getDimension(R.dimen.captcha_cursor_height)
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor
        linePaint.color = resources.getColor(R.color.default_ev_border_color)
        linePaint.strokeWidth = mCursorWidth //绘制直线
        borderPaint.style = Paint.Style.STROKE
        borderPaint.isAntiAlias = true
        passwordPaint.color = passwordColor
        passwordPaint.textSize = mDefaultInputTextSize
        mCursorHandler = Handler()
        mCursorRunnable = CursorRunnable()
        mCursorHandler.post(mCursorRunnable)
    }
}