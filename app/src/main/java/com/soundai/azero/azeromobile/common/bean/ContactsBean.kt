package com.soundai.azero.azeromobile.common.bean

class ContactsBean(
    val contactId: String
) : Comparable<ContactsBean> {
    var name: String? = null
    var matchPin: String = ""
    var namePinYin: String? = null
    var matchType = 1 //匹配类型 名字1，电话号2
    var pinyinFirst = "#"
    var namePinyinList = arrayListOf<String>()
    var numberList = arrayListOf<String>()

    override fun compareTo(other: ContactsBean): Int {
        if (this.pinyinFirst == "#") {
            return 1
        } else if (other.pinyinFirst == "#") {
            return -1
        }
        return this.pinyinFirst.compareTo(other.pinyinFirst)
    }

}