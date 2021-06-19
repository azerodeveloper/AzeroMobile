package com.soundai.azero.azeromobile.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.azero.sdk.AzeroManager
import com.azero.sdk.util.log
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.soundai.azero.azeromobile.GlideApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.RecycleAdapter
import com.soundai.azero.azeromobile.ui.activity.launcher.item.*
import com.soundai.azero.azeromobile.ui.activity.playerinfo.NewsDetailsActivity
import com.soundai.azero.azeromobile.ui.activity.playerinfo.PlayingDetailsActivity
import com.soundai.azero.azeromobile.utils.NumberUtil
import com.soundai.azero.azeromobile.utils.Utils

/**
 * Create by xingw on 2019/10/26
 */
class MusicViewHolder(itemView: View) : RecycleAdapter.ViewHolder<MusicGridItem>(itemView) {
    private val serial: TextView = itemView.findViewById(R.id.tv_serial)
    private val title: TextView = itemView.findViewById(R.id.tv_song)
    private val singerName: TextView = itemView.findViewById(R.id.tv_singer)
    private val background: ConstraintLayout = itemView.findViewById(R.id.cl_bg)
    private val icon: ImageView = itemView.findViewById(R.id.iv_icon)

    @SuppressLint("SetTextI18n")
    override fun onBind(
        iGridItem: MusicGridItem,
        activity: Activity
    ) {
        serial.text = iGridItem.serial.toString()
        title.text = iGridItem.title
        singerName.text = if (iGridItem.AlbumName.isNotEmpty()) {
            "${iGridItem.singer}-${iGridItem.AlbumName}"
        } else {
            iGridItem.singer
        }
       GlideApp.with(icon)
            .asBitmap()
            .load(iGridItem.iconUrl)
            .into(icon)

        itemView.setOnClickListener {
            PlayingDetailsActivity.start(activity, iGridItem)
            log.e("MusicViewHolder click 第${NumberUtil.intToChinese(adapterPosition +1)}个")
            AzeroManager.getInstance().sendQueryText("播放第${NumberUtil.intToChinese(adapterPosition +1)}个")
        }
    }
}

class EnglishViewHolder(itemView: View) : RecycleAdapter.ViewHolder<EnglishGridItem>(itemView) {
    private val serial: TextView = itemView.findViewById(R.id.tv_serial)
    private val title: TextView = itemView.findViewById(R.id.tv_title)
    private val providerName: TextView = itemView.findViewById(R.id.tv_provider)
    private val background: ConstraintLayout = itemView.findViewById(R.id.cl_bg)
    private val img: ImageView = itemView.findViewById(R.id.iv_img)

    @SuppressLint("SetTextI18n")
    override fun onBind(
        iGridItem: EnglishGridItem,
        activity: Activity
    ) {
        serial.text = iGridItem.serial.toString()
        title.text = iGridItem.title
        providerName.text = iGridItem.provider

        GlideApp.with(img)
            .asBitmap()
            .load(iGridItem.imgUrl)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(Utils.dp2px(6f).toInt())))
            .into(img)

        itemView.setOnClickListener {
//            PlayingDetailsActivity.start(activity, convertEnglishGridItem2MusicGridItem(iGridItem))
            log.e("EnglishViewHolder click 第${NumberUtil.intToChinese(adapterPosition + 1)}个")
//            AzeroManager.getInstance().sendQueryText("播放第${NumberUtil.intToChinese(adapterPosition+1)}个")
        }
    }
}

class NewsViewHolder1(itemView: View) : RecycleAdapter.ViewHolder<NewsGridItem>(itemView) {
    private val serial: TextView = itemView.findViewById(R.id.tv_serial)
    private val title: TextView = itemView.findViewById(R.id.tv_song)
    private val author: TextView = itemView.findViewById(R.id.tv_author)
    private val time: TextView = itemView.findViewById(R.id.tv_time)
    private val img: ImageView = itemView.findViewById(R.id.iv_img_1)

    override fun onBind(
        iGridItem: NewsGridItem,
        activity: Activity
    ) {
        serial.text = iGridItem.serial.toString()
        title.text = iGridItem.title
        author.text = iGridItem.author
        time.text = iGridItem.time
        GlideApp.with(itemView).load(iGridItem.img[0])
            .centerCrop()
            .into(img)

        ViewCompat.setTransitionName(img, "TitleImg:" + iGridItem.title)
        ViewCompat.setTransitionName(title, "Title:" + iGridItem.title)

        itemView.setOnClickListener {
//            NewsDetailsActivity.start(activity, iGridItem, img, title)
            log.e("NewsViewHolder1 click 第${NumberUtil.intToChinese(adapterPosition)}个")
//            AzeroManager.getInstance().sendQueryText("播放第${NumberUtil.intToChinese(adapterPosition)}个")
        }
    }
}

class NewsViewHolder2(itemView: View) : RecycleAdapter.ViewHolder<NewsGridItem>(itemView) {
    private val serial: TextView = itemView.findViewById(R.id.tv_serial)
    private val title: TextView = itemView.findViewById(R.id.tv_song)
    private val author: TextView = itemView.findViewById(R.id.tv_author)
    private val time: TextView = itemView.findViewById(R.id.tv_time)
    private val img1: ImageView = itemView.findViewById(R.id.iv_img_1)
    private val img2: ImageView = itemView.findViewById(R.id.iv_img_2)

    override fun onBind(
        iGridItem: NewsGridItem,
        activity: Activity
    ) {
        serial.text = iGridItem.serial.toString()
        title.text = iGridItem.title
        author.text = iGridItem.author
        time.text = iGridItem.time
        GlideApp.with(itemView).load(iGridItem.img[0])
            .apply(RequestOptions().fitCenter().override(400, 300))
            .into(img1)
        GlideApp.with(itemView).load(iGridItem.img[1])
            .apply(RequestOptions().fitCenter().override(400, 300))
            .into(img2)
    }
}

class VideoViewHolder(itemView: View) : RecycleAdapter.ViewHolder<VideoGridItem>(itemView) {
    private val serial: TextView = itemView.findViewById(R.id.tv_serial)
    private val img: ImageView = itemView.findViewById(R.id.iv_img)
    private val background: ConstraintLayout = itemView.findViewById(R.id.cl_bg)

    override fun onBind(
        iGridItem: VideoGridItem,
        activity: Activity
    ) {
        serial.text = iGridItem.serial.toString()
        GlideApp.with(itemView).load(iGridItem.img)
            .apply(RequestOptions().fitCenter().override(400, 300))
            .into(img)

        if (iGridItem.focus) {
            background.background =
                activity.resources.getDrawable(R.drawable.ripple_touch_recycleitem_focus, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                serial.setTextAppearance(R.style.Badge_Selected)
            } else {
                serial.setTextAppearance(activity, R.style.Badge_Selected)
            }
            serial.background = activity.resources.getDrawable(R.color.colorAccent, null)
        } else {
            background.background =
                activity.resources.getDrawable(R.drawable.ripple_touch_recycleitem, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                serial.setTextAppearance(R.style.Badge_UnSelected)
            } else {
                serial.setTextAppearance(activity, R.style.Badge_UnSelected)
            }
            serial.background = activity.resources.getDrawable(R.color.colorPrimary, null)
        }
    }
}