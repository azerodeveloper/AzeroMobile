package com.soundai.azero.azeromobile.ui.activity.launcher.item

import android.os.Parcelable
import com.azero.sdk.util.log
import com.google.gson.JsonParseException
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.Setting
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.json.JSONException
import org.json.JSONObject

@Parcelize
class EnglishGridItem(
    var title: String,
    var provider: String,
    var albumName: String,
    val imgUrl: String,
    val iconUrl: String,
    val lyricUrl: String
) : IGridItem(), Parcelable {
    override fun getSpanSize(): Int {
        return Setting.LAUNCHER_SPANSIZE
    }

    @IgnoredOnParcel
    override val layoutResId: Int = R.layout.grid_english_item
}

fun parseTemplate2EnglishGridItemList(playerInfo: JSONObject): MutableList<IGridItem>? {
    try {
        val gridItemList = mutableListOf<IGridItem>()
        if (playerInfo.has("contents")) {
            val contents = playerInfo.getJSONArray("contents")
            for (i in 0 until contents.length()) {
                try {
                    val content = contents.getJSONObject(i)
                    val gridItem = parseContent2EnglishGridItem(content)
                    gridItem.let { gridItemList.add(gridItem) }
                } catch (e: JSONException) {
                    log.e(e.message)
                }
            }
        } else {
            val content = playerInfo.getJSONObject("content")
            val gridItem = parseContent2EnglishGridItem(content)
            gridItem.let { gridItemList.add(gridItem) }
        }
        return gridItemList
    } catch (e: JSONException) {
        log.e(e.message)
    }
    return null
}

fun parseContent2EnglishGridItem(content: JSONObject): EnglishGridItem {
    val provider = content.getJSONObject("provider")
    val logo = provider.getJSONObject("logo")
    val logoSources = logo.getJSONArray("sources")
    val iconUrl = (logoSources[0] as JSONObject).getString("url")
    val title = content.getString("title")
    val lyricUrl = if (provider.has("lyric")) {
        provider.getString("lyric")
    } else {
        ""
    }
    val providerName = if (provider.has("name")) {
        provider.getString("name")
    } else {
        ""
    }
    val imgUrl = try {
        val art = content.getJSONObject("art")
        val imgSources = art.getJSONArray("sources")
        (imgSources[0] as JSONObject).getString("url")
    } catch (e: JsonParseException) {
        ""
    }
    return EnglishGridItem(title, providerName, "", imgUrl, iconUrl, lyricUrl)
}

fun convertEnglishGridItem2MusicGridItem(englishGridItem: EnglishGridItem) =
    MusicGridItem(
        englishGridItem.title,
        englishGridItem.provider,
        englishGridItem.albumName,
        englishGridItem.imgUrl,
        englishGridItem.iconUrl,
        englishGridItem.lyricUrl
    )