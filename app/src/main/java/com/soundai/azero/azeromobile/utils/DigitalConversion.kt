package com.soundai.azero.azeromobile.utils

/**
 * 数字 转换
 */
object DigitalConversion {
    private val nums =
        arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")
    private val pos_units = arrayOf("", "十", "百", "千")
    private val weight_units = arrayOf("", "万", "亿")
    /**
     * 数字转汉字【新】
     *
     * @param num
     * @return
     */
    fun numberToChinese(num: Int): String {
        var num = num
        if (num == 0) {
            return "零"
        }
        var weigth = 0 //节权位
        var chinese = ""
        var chinese_section = ""
        var setZero = false //下一小节是否需要零，第一次没有上一小节所以为false
        while (num > 0) {
            val section = num % 10000 //得到最后面的小节
            if (setZero) { //判断上一小节的千位是否为零，是就设置零
                chinese = nums[0] + chinese
            }
            chinese_section = sectionTrans(section)
            if (section != 0) { //判断是都加节权位
                chinese_section = chinese_section + weight_units[weigth]
            }
            chinese = chinese_section + chinese
            chinese_section = ""
            setZero = section < 1000 && section > 0
            num = num / 10000
            weigth++
        }
        if ((chinese.length == 2 || chinese.length == 3) && chinese.contains("一十")) {
            chinese = chinese.substring(1, chinese.length)
        }
        if (chinese.indexOf("一十") == 0) {
            chinese = chinese.replaceFirst("一十".toRegex(), "十")
        }
        return chinese
    }

    /**
     * 将每段数字转汉子
     *
     * @param section
     * @return
     */
    fun sectionTrans(section: Int): String {
        var section = section
        val section_chinese = StringBuilder()
        var pos = 0 //小节内部权位的计数器
        var zero = true //小节内部的置零判断，每一个小节只能有一个零。
        while (section > 0) {
            val v = section % 10 //得到最后一个数
            if (v == 0) {
                if (!zero) {
                    zero = true //需要补零的操作，确保对连续多个零只是输出一个
                    section_chinese.insert(0, nums[0])
                }
            } else {
                zero = false //有非零数字就把置
                section_chinese.insert(0, pos_units[pos])
                section_chinese.insert(0, nums[v])
            }
            pos++
            section = section / 10
        }
        return section_chinese.toString()
    }
}

