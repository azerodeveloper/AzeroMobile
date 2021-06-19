package com.soundai.azero.azeromobile.ui.activity.launcher.item

import android.os.Parcelable
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.Setting
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Create by xingw on 2019/10/26
 */
@Parcelize
class NewsGridItem(
    var title: String,
    val img: MutableList<String>,
    val author: String,
    val time: String
) : IGridItem(), Parcelable {
    override val layoutResId: Int = when (img.size) {
        1 -> R.layout.grid_news_single_img_item
        2 -> R.layout.grid_news_double_img_item
        else -> throw UnsupportedOperationException()
    }

    override fun getSpanSize(): Int {
        return Setting.LAUNCHER_SPANSIZE
    }
}

fun parseTemplate2NewsGridItemList(playerInfo: JSONObject): MutableList<IGridItem>? {
    try {
        // 1.尝试更新数据列表
        val contents = playerInfo.getJSONArray("contents")
        val newsList = mutableListOf<IGridItem>()
        for (i in 0 until contents.length()) {
            try {
                val content = contents.getJSONObject(i)
                val newsInfo = parseContent2NewsGridItem(content)
                newsList.add(newsInfo)
            } catch (e: JSONException) {
                log.e(e.message)
            }
        }
        return newsList
    } catch (e: JSONException) {
        log.e(e.message)
    }
    return null
}

fun parseContent2NewsGridItem(content: JSONObject): NewsGridItem {
    val provider = content.getJSONObject("provider")
    val author = provider.getString("name")
    val title = content.getString("title")
    val art = content.getJSONObject("art")
    val sources = art.getJSONArray("sources")
    val source = sources.getJSONObject(0)
    val url = source.getString("url")
    val urlList = mutableListOf<String>(url)
    if (sources.length() > 1) {
        val url2 = sources.getJSONObject(1).getString("url")
        urlList.add(url2)
    }
    return NewsGridItem(title, urlList, author, "一小时前")
}