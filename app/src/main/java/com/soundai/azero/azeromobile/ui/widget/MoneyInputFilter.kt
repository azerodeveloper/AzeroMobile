package com.soundai.azero.azeromobile.ui.widget

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class MoneyInputFilter(): InputFilter {
    private val mPattern =  Pattern.compile("([0-9]|\\.)*")
    private val MAX_VALUE = Int.MAX_VALUE
    private val POINTER_LENGTH = 2
    private val POINTER = "."
    private val ZERO = "0"


    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val sourceText = source.toString()
        val destText = dest.toString()

        if(sourceText.isNullOrEmpty()){
            return ""
        }

        val matcher = mPattern.matcher(source)

        if(destText.contains(POINTER)){
            if(!matcher.matches()){
                return ""
            }else{
                if(POINTER.equals(source.toString())){
                    return ""
                }
            }
            val index = destText.indexOf(POINTER)
            val length = dend - index
            if(length > POINTER_LENGTH){
                return dest?.subSequence(dstart,dend)
            }
        }else{
            if(!matcher.matches()){
                return ""
            }else{
                if ((POINTER.equals(source.toString())) && destText.isNullOrEmpty()) {  //首位不能输入小数点
                    return "";
                } else if (!POINTER.equals(source.toString()) && ZERO.equals(destText)) { //如果首位输入0，接下来只能输入小数点
                    return "";
                }
            }
        }
        val sumText = (destText + sourceText).toDouble()
        if(sumText > MAX_VALUE){
            return dest?.subSequence(dstart,dend)
        }
        return "" + dest?.subSequence(dstart, dend) + sourceText
    }
}