package com.soundai.azero.azeromobile.ui.widget.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.soundai.azero.azeromobile.R

class DialogView(context: Context) {

    private val mContext = context
    private var dialogView: View? = null
    private var dialog: Dialog? = null
    private var dialogTextView: TextView? = null
    private var dialogImageView: ImageView? = null

    private fun initDialogView() {
        if (dialogView == null && dialog == null) {
            dialogView =
                LayoutInflater.from(mContext).inflate(R.layout.view_network_dialog, null, false)
            dialog = Dialog(mContext, R.style.NetworkDialogStyle)
            dialog?.setContentView(dialogView!!)
            dialog?.setCanceledOnTouchOutside(true)
            dialog?.window?.setType(
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1
                    }
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 -> {
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                    else -> {
                        WindowManager.LayoutParams.TYPE_TOAST
                    }
                }
            )
            dialogTextView = dialogView?.findViewById(R.id.network_dialog_card_text)
            dialogImageView = dialogView?.findViewById(R.id.network_dialog_card_image)
        }
    }

    fun showNetworkDialog(message: String) {
        initDialogView()
        dialogTextView?.text = message
        dialogImageView?.visibility = View.GONE
        if (!dialog!!.isShowing) {
            dialog?.show()
        }
    }

    fun showNetworkDialog(message: String, @DrawableRes drawableResId: Int) {
        initDialogView()
        dialogTextView?.text = message
        dialogImageView?.setImageDrawable(mContext.getDrawable(drawableResId))
        dialogImageView?.visibility = View.VISIBLE
        if (!dialog!!.isShowing) {
            dialog?.show()
        }
    }

    fun dismiss() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }
}