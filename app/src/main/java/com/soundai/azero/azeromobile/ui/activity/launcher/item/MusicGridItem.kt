package com.soundai.azero.azeromobile.ui.activity.launcher.item

import android.os.Parcelable
import com.azero.sdk.util.log
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.Setting
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject

/**
 * Create by xingw on 2019/10/26
 */
@Parcelize
class MusicGridItem(
    var title: String,
    var singer: String,
    var AlbumName: String,
    val imgUrl: String,
    val iconUrl: String,
    val lyricUrl: String
) : IGridItem(), Parcelable {
    override fun getSpanSize(): Int {
        return Setting.LAUNCHER_SPANSIZE
    }

    override val layoutResId: Int = R.layout.grid_music_item
}

//包含音乐和新闻
fun parseTemplate2MusicGridItemList(playerInfo: JSONObject): MutableList<IGridItem>? {
    try {
        // 1.尝试更新数据列表
        val gridItemList = mutableListOf<IGridItem>()
        if (playerInfo.has("contents")) {
            val contents = playerInfo.getJSONArray("contents")
            for (i in 0 until contents.length()) {
                try {
                    val content = contents.getJSONObject(i)
                    val provider = content.getJSONObject("provider")
                    val gridItem = if (provider.has("type")) {
                        when (provider.getString("type")) {
                            "news" -> parseContent2NewsGridItem(content)
                            "music" -> parseContent2MusicGridItem(content)
                            else -> parseContent2MusicGridItem(content)
                        }
                    } else {
                        parseContent2MusicGridItem(content)
                    }
                    gridItem?.let { gridItemList.add(gridItem) }
                } catch (e: JSONException) {
                    log.e(e.message)
                }
            }
        } else {
            val content = playerInfo.getJSONObject("content")
            val gridItem = parseContent2MusicGridItem(content)
            gridItem?.let { gridItemList.add(gridItem) }
        }
        return gridItemList
    } catch (e: JSONException) {
        log.e(e.message)
    }
    return null
}

fun parseContent2MusicGridItem(content: JSONObject): IGridItem {
    val provider = content.getJSONObject("provider")
    val logo = provider.getJSONObject("logo")
    val sources = logo.getJSONArray("sources")
    val iconUrl = (sources[0] as JSONObject).getString("url")
    val title = content.getString("title")
    val lyricUrl = if (provider.has("lyric")) {
        provider.getString("lyric")
    } else {
        ""
    }
    // if (!(title.contains("《") && title.contains("》"))) {
    //     title = "《${content.getString("title")}》"
    // }
    val singer = if (provider.has("name")) {
        provider.getString("name")
    } else {
        ""
    }
    val imgUrl = try {
        val art = content.getJSONObject("art")
        val sources = art.getJSONArray("sources")
        (sources[0] as JSONObject).getString("url")
    } catch (e: JsonParseException) {
        ""
    }
    return MusicGridItem(title, singer, "", imgUrl, iconUrl, lyricUrl)
}
