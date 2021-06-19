package com.soundai.azero.azeromobile.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.text.TextUtils
import com.azero.sdk.util.log
import com.github.promeg.pinyinhelper.Pinyin
import com.soundai.azero.azeromobile.common.bean.ContactsBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.HashMap
import java.util.Locale

object ContactProvider {
    private val indexStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private var contactCache: MutableList<ContactsBean>? = null

    fun callPhone(context: Context, phoneNum: String) {
        val intent = Intent(Intent.ACTION_CALL)
        val data = Uri.parse("tel:$phoneNum")
        intent.data = data
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 从通讯录读取联系人并转为ContactsBean格式
     * useCache 如果有缓存则使用缓存直接返回
     */
    suspend fun getContactData(
        context: Context,
        useCache: Boolean = true
    ): MutableList<ContactsBean> = withContext(Dispatchers.IO) {
        if (useCache && contactCache != null) {
            return@withContext contactCache!!
        } else {
            val searchContactLists = mutableListOf<ContactsBean>()
            //得到ContentResolver对象
            val cr = context.contentResolver
            //取得电话本中开始一项的光标
            val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            //向下移动光标
            while (cursor!!.moveToNext()) {
                //取得联系人名字
                val nameFieldColumnIndex =
                    cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val name = cursor.getString(nameFieldColumnIndex)
                //取得联系人ID
                val contactId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                //要获取所有的联系人,一个联系人会有多个号码
                getContactById(cr, contactId, name, searchContactLists)
            }
            cursor.close()
            contactCache = searchContactLists
            return@withContext searchContactLists
        }
    }

    //根据通讯录ID
    private fun getContactById(
        cr: ContentResolver,
        contactId: String,
        name: String,
        searchContactLists: MutableList<ContactsBean>
    ) {
        if (contactId.isNotEmpty()) {
            val phone = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                null,
                null
            )
            if (null != phone) {
                val contact = ContactsBean(contactId)
                contact.name = name

                if (!contact.name.isNullOrEmpty()) {
                    getPinyinList(contact)
                }
                while (phone.moveToNext()) {
                    val phoneNumber =
                        phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    contact.numberList.add(phoneNumber)
                }
                if (contact.numberList.isNotEmpty()) {
                    searchContactLists.add(contact)
                }
            }
        }
    }

    private fun getPinyinList(contactsBean: ContactsBean) {
        val bufferNamePiny = StringBuffer()//NIHAO
        val bufferNameMatch = StringBuffer()//NH
        val name = contactsBean.name
        for (element in name!!) {
            val bufferNamePer = StringBuffer()
            val namePer = element + ""//名字的每个字
            for (character in namePer) {
                val pinCh = Pinyin.toPinyin(character).toUpperCase(Locale.CHINA)
                bufferNamePer.append(pinCh)
                bufferNameMatch.append(pinCh[0])
                bufferNamePiny.append(pinCh)
            }
            contactsBean.namePinyinList.add(bufferNamePer.toString())//单个名字集合
        }
        contactsBean.namePinYin = bufferNamePiny.toString()
        contactsBean.matchPin = bufferNameMatch.toString()

        val firstPinyin = contactsBean.namePinYin!![0].toString()
        if (indexStr.contains(firstPinyin)) {
            contactsBean.pinyinFirst = firstPinyin
        }
    }

    //通过拼音或者英文字母
    suspend fun findDataByEN(
        inputStr: String,
        searchContactLists: MutableList<ContactsBean>
    ): MutableList<ContactsBean> = withContext(Dispatchers.IO) {
        val contactLists = mutableListOf<ContactsBean>()
        //把输入的内容变为大写
        val searPinyin = inputStr.toUpperCase(Locale.CHINA)
        //搜索字符串的长度
        val searLength = searPinyin.length
        //搜索的第一个大写字母
        val searPinyinFirst = searPinyin[0]
        for (i in 0 until searchContactLists.size) {
            val contactsBean = searchContactLists[i]
            contactsBean.matchType = 1//字母匹配肯定是名字
            //如果输入的每一个字母都和名字的首字母一样，那就可以匹配比如：你好，NH，输入nh就ok
            if (contactsBean.matchPin.contains(searPinyin)) {
                contactLists.add(contactsBean)
            } else {
                var isMatch = false
                //先去匹配单个字，比如你好：NI,HAO.输入NI，肯定匹配第一个
                for (j in 0 until contactsBean.namePinyinList.size) {
                    val namePinyinPer = contactsBean.namePinyinList[j]
                    if (namePinyinPer.isNotEmpty() && namePinyinPer.startsWith(searPinyin)) {
                        //符合的话就是当前字匹配成功
                        contactLists.add(contactsBean)
                        isMatch = true
                        break
                    }
                }
                if (isMatch) {
                    continue
                }
                //根据拼音包含来实现，比如你好：NIHAO,输入NIHA或者NIHAO。
                if (!contactsBean.namePinYin.isNullOrEmpty() && contactsBean.namePinYin!!.contains(
                        searPinyin
                    )
                ) {
                    //这样的话就要从每个字的拼音开始匹配起
                    for (j in 0 until contactsBean.namePinyinList.size) {
                        val sbMatch = StringBuilder()
                        for (k in j until contactsBean.namePinyinList.size) {
                            sbMatch.append(contactsBean.namePinyinList[k])
                        }
                        if (sbMatch.toString().startsWith(searPinyin)) {
                            //匹配成功
                            //比如输入是NIH，或者NIHA,或者NIHAO,这些都可以匹配上，从而就可以通过NIHAO>=NIH,HIHA,NIHAO
                            isMatch = true
                            contactLists.add(contactsBean)
                        }
                    }
                }

                if (isMatch) {
                    continue
                }

                //最后一种情况比如：广发银行，输入GuangFY或者GuangFYH都可以匹配成功，这样的情况名字集合必须大于等于3
                if (contactsBean.namePinyinList.size > 2) {
                    for (j in 0 until contactsBean.namePinyinList.size) {

                        val sbMatch0 = StringBuilder()
                        sbMatch0.append(contactsBean.namePinyinList[j])
                        //只匹配到倒数第二个
                        if (j < contactsBean.namePinyinList.size - 2) {
                            for (k in j + 1 until contactsBean.matchPin.length) {
                                //依次添加后面每个字的首字母
                                sbMatch0.append(contactsBean.matchPin[k])
                                if (sbMatch0.toString() == searPinyin) {
                                    contactLists.add(contactsBean)
                                    isMatch = true
                                    break
                                }
                            }
                        }

                        if (isMatch) {
                            //跳出循环已找到
                            break
                        }

                        //sbMatch1是循环匹配对象比如GUANGFYH，GUANGFAYH，GUANGFAYINH,GUANGFAYINHANG，
                        //FAYH,YINH
                        val sbMatch1 = StringBuilder()
                        for (k in 0..j) {//依次作为初始匹配的起点
                            sbMatch1.append(contactsBean.namePinyinList[k])
                        }
                        //只匹配到倒数第二个
                        if (j < contactsBean.namePinyinList.size - 2) {
                            for (k in j + 1 until contactsBean.matchPin.length) {
                                //依次添加后面每个字的首字母
                                sbMatch1.append(contactsBean.matchPin[k])
                                if (sbMatch1.toString() == searPinyin) {
                                    contactLists.add(contactsBean)
                                    isMatch = true
                                    break
                                }
                            }
                        }
                        if (isMatch) {
                            //跳出循环已找到
                            break
                        }

                        if (j >= contactsBean.namePinyinList.size - 2) {
                            //如果说是剩余最后两个拼音不需要匹配了
                            break
                        }
                        val sbMatch2 = StringBuilder()
                        sbMatch2.append(contactsBean.namePinyinList[j])
                        for (k in j + 1 until contactsBean.namePinyinList.size) {
                            sbMatch2.append(contactsBean.namePinyinList[k])
                            val sbMatch3 = StringBuilder()
                            sbMatch3.append(sbMatch2.toString())
                            //只匹配到倒数第二个
                            if (j < contactsBean.namePinyinList.size - 2) {
                                for (m in k + 1 until contactsBean.matchPin.length) {
                                    //依次添加后面每个字的首字母
                                    sbMatch3.append(contactsBean.matchPin[m])
                                    if (sbMatch3.toString() == searPinyin) {
                                        contactLists.add(contactsBean)
                                        isMatch = true
                                        break
                                    }
                                }
                            }
                            if (isMatch) {
                                //跳出循环已找到
                                break
                            }
                        }

                        if (isMatch) {
                            //跳出循环已找到
                            break
                        }
                    }
                }
            }
        }
        return@withContext contactLists
    }

    fun findContactNum(
        namePinYin: String,
        contactList: List<ContactsBean>?
    ): ContactsBean? {
        val namePinYinStringList: List<String> = namePinYin.split(" ")
        log.d("findContactNum, namePinYin: $namePinYin")
        return if (contactList != null) {
            val contactListSize = contactList.size
            log.d("contactList != null, contactListSize is: $contactListSize")
            if (contactListSize > 0) {
                val contactEntityHashMap: MutableMap<String, ContactsBean> =
                    HashMap(contactListSize)
                for (contact in contactList) {
                    val name: String = contact.name ?: continue
                    log.d("nickName: $name")
                    contactEntityHashMap[name] = contact
                }
                val resultContactEntityMap: Map<String, ContactsBean>
                val contactEntityIterator: Iterator<*> =
                    contactEntityHashMap.entries.iterator()
                resultContactEntityMap =
                    queryByFirstLetter(contactEntityIterator, namePinYinStringList)
                log.d("resultContactEntityMap.size(): " + resultContactEntityMap.size)
                if (resultContactEntityMap.isNotEmpty()) {
                    queryByInitialConsonant(resultContactEntityMap, namePinYinStringList)
                } else {
                    null
                }
            } else {
                null
            }
        } else {
            log.e("woCallContactEntityList == null")
            null
        }
    }

    private fun queryByFirstLetter(
        contactEntityIterator: Iterator<*>,
        namePinYinStringList: List<String>
    ): Map<String, ContactsBean> {
        val resultContactEntityMap: MutableMap<String, ContactsBean> =
            HashMap()
        val firstLetter = namePinYinStringList[0][0].toString()
        log.d("firstLetter: $firstLetter")
        while (contactEntityIterator.hasNext()) {
            val entry =
                contactEntityIterator.next() as Map.Entry<*, *>
            val nickName = entry.key as String
            val contactsBean: ContactsBean = entry.value as ContactsBean
            val nickNamePinYin: String = contactsBean.namePinYin ?: ""
            log.d("nickNamePinYin: $nickNamePinYin")
            if (!TextUtils.isEmpty(nickNamePinYin)) {
                val nickNamePinYinList =
                    nickNamePinYin.split(" ").toTypedArray()
                val nickNamePinYinFirstLetterBuilder =
                    java.lang.StringBuilder()
                for (nickPinYin in nickNamePinYinList) {
                    nickNamePinYinFirstLetterBuilder.append(nickPinYin[0])
                }
                val nickNamePinYinFirstLetter =
                    nickNamePinYinFirstLetterBuilder.toString()
                log.d("nickNamePinYinFirstLetter: $nickNamePinYinFirstLetter")
                if (nickNamePinYinFirstLetter.equals(firstLetter, true)) {
                    log.d("nickNamePinYinFirstLetter.equals(firstLetter)")
                    resultContactEntityMap[nickName] = contactsBean
                }
            }
        }
        return resultContactEntityMap
    }

    fun queryByInitialConsonant(
        contactEntityMap: Map<String, ContactsBean>,
        namePinYinStringList: List<String>
    ): ContactsBean? {
        log.d("queryByInitialConsonant")
        val pinYinPercentMap: MutableMap<String, Double> =
            HashMap()
        for (entity in contactEntityMap) {
            val nickName = entity.key
            val contactsBean: ContactsBean = entity.value
            val nickNamePinYin = contactsBean.namePinYin!!
            val namePinYinSearch = namePinYinStringList.joinToString(separator = "")
            val percent = countPercent(nickNamePinYin, namePinYinSearch).toDouble()
            log.d("name:$nickNamePinYin,percent: $percent")
            pinYinPercentMap[nickName] = percent
        }
        var maxPercent = 0.00
        var maxPercentNickName = ""
        for (mutableEntry in pinYinPercentMap) {
            val nickName = mutableEntry.key
            val percent = mutableEntry.value
            if (percent > maxPercent) {
                log.d("percent > maxPercent, percent: $percent, maxPercent: $maxPercent")
                maxPercent = percent
                maxPercentNickName = nickName
            }
        }
        log.d("maxPercent: $maxPercent, maxPercentNickName: $maxPercentNickName")
        return if (maxPercent > 0.66) {
            contactEntityMap[maxPercentNickName]
        } else {
            null
        }
    }

    private fun countPercent(str: String, str1: String): String {
        var str = str.toUpperCase()
        var str1 = str1.toUpperCase()
        if (str.length > str1.length) {
            return countPercent(str1, str)
        }
        val df = DecimalFormat("0.00")
        var count = 0
        val map: MutableMap<Char, Int> = HashMap()
        val cs = str1.toCharArray()
        for (c in cs) {
            map[c] = if (map[c] == null) 1 else map[c]!! + 1
        }
        for (i in str.indices) {
            if (map[str[i]] != null) {
                if (map[str[i]] != -1) {
                    val s = map[str[i]]!! - 1
                    if (s == 0) {
                        map[str[i]] = -1
                        count++
                    } else if (s > 0) {
                        map[str[i]] = s
                        count++
                    }
                }
            }
        }
        val result = count.toDouble() / str1.length
        return df.format(result)
    }
}