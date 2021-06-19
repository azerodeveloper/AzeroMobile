package com.soundai.azero.azeromobile.ui.activity.launcher.head

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.bumptech.glide.request.RequestOptions
import com.soundai.azero.azeromobile.GlideApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.activity.launcher.item.NewsGridItem
import com.soundai.azero.azeromobile.ui.activity.playerinfo.NewsDetailsActivity
import com.soundai.azero.azeromobile.utils.Utils

class SingleNewsHeadView(private val newsGridItem: NewsGridItem) : IHeadView {
    override fun inflateHeadView(container: ViewGroup) {
        val activity = Utils.findActivity(container.context) ?: return
        val rootView = LayoutInflater.from(activity)
            .inflate(R.layout.cardview_news_with_picture, container)
        val title: TextView = rootView.findViewById(R.id.tv_song)
        val author: TextView = rootView.findViewById(R.id.tv_author)
        val time: TextView = rootView.findViewById(R.id.tv_time)
        val img: ImageView = rootView.findViewById(R.id.iv_img_1)
        title.text = newsGridItem.title
        author.text = newsGridItem.author
        time.text = newsGridItem.time
        GlideApp.with(activity).load(newsGridItem.img[0])
            .apply(RequestOptions().fitCenter().override(400, 300))
            .into(img)
        ViewCompat.setTransitionName(img, "TitleImg:" + newsGridItem.title)
        ViewCompat.setTransitionName(title, "Title:" + newsGridItem.title)
        rootView.setOnClickListener {
            NewsDetailsActivity.start(activity, newsGridItem, img, title)
        }
    }

    override fun release() {
        // do nothing
    }
}