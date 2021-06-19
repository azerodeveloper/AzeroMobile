package com.soundai.azero.azeromobile.utils

import com.soundai.azero.azeromobile.ui.activity.launcher.item.IGridItem
import com.soundai.azero.azeromobile.ui.activity.launcher.item.MusicGridItem
import com.soundai.azero.azeromobile.ui.activity.launcher.item.NewsGridItem
import com.soundai.azero.azeromobile.ui.activity.launcher.item.VideoGridItem

/**
 * Create by xingw on 2019/10/31
 */
fun getLauncherTestData(): MutableList<IGridItem> {
    val datas = mutableListOf<IGridItem>()
    datas.add(
        NewsGridItem(
            "习近平主席致辞第六届互联网大会",
            arrayListOf(
                "https://www.cac.gov.cn/rootimages/2019/10/23/1573362900382812-1573362900455497.jpg",
                "https://www.cac.gov.cn/rootimages/2019/10/23/1573362900382812-1573362900455497.jpg"
            ),
            "新华社",
            "前一小时"
        )
    )
    datas.add(MusicGridItem("《Song Name》", "singer", "Album Name", "","", ""))
    datas.add(
        VideoGridItem(
            "1",
            VideoGridItem.SIZE.BIG,
            "https://www.cac.gov.cn/rootimages/2019/10/23/1573362900382812-1573362900455497.jpg"
        )
    )
    datas.add(
        VideoGridItem(
            "2",
            VideoGridItem.SIZE.SMALL,
            "https://www.cac.gov.cn/rootimages/2019/10/23/1573362900382812-1573362900455497.jpg"
        )
    )
    datas.add(
        VideoGridItem(
            "3",
            VideoGridItem.SIZE.SMALL,
            "https://www.cac.gov.cn/rootimages/2019/10/23/1573362900382812-1573362900455497.jpg"
        )
    )
    datas.add(
        VideoGridItem(
            "4",
            VideoGridItem.SIZE.SMALL,
            "https://www.cac.gov.cn/rootimages/2019/10/23/1573362900382812-1573362900455497.jpg"
        )
    )
    return datas
}