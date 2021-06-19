package com.soundai.azero.azeromobile.ui.widget.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.annotation.DrawableRes

object DialogManager {

    private var dialogView: DialogView? = null
    private var alertDialog: AlertDialog? = null

    fun showNetworkDialogView(context: Context, message: String, @DrawableRes drawableResId: Int) {
        if (dialogView == null) {
            dialogView = DialogView(context)
        }
        dialogView?.showNetworkDialog(message, drawableResId)
    }

    fun showNetworkDialogView(context: Context, message: String) {
        if (dialogView == null) {
            dialogView = DialogView(context)
        }
        dialogView?.showNetworkDialog(message)
    }

    fun dismissDialog() {
        dialogView?.dismiss()
    }

    fun showAlertDialog(activity: Activity, text: String) {
        if (activity.isFinishing || activity.isDestroyed) return
        alertDialog?.let {
            if (it.isShowing)
                return
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity).apply {
            setMessage(text).setTitle("警告")
            setNegativeButton("退出") { _, _ ->
                android.os.Process.killProcess(android.os.Process.myPid())
            }
            setCancelable(false)
        }
        alertDialog = builder.create()
        alertDialog?.show()
    }

    fun dismissAlertDialog(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        alertDialog?.let {
            if (!it.isShowing) {
                return
            } else {
                it.dismiss()
            }
        }

    }
}