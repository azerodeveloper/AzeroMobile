package com.soundai.azero.azeromobile.ui.activity.login.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.utils.dp

class ProtocolDialogFragment : DialogFragment() {

    companion object {
        const val TYPE_USER_PROTOCOL = 0
        const val TYPE_PRIVACY_PROTOCOL = 1

        fun newInstance(type: Int): ProtocolDialogFragment =
            ProtocolDialogFragment().apply {
                arguments = Bundle().also { it.putInt("type", type) }
            }
    }

    private lateinit var rootView: ViewGroup

    private lateinit var protocolWebView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = FrameLayout(context!!).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val type = arguments!!.getInt("type")
        initWebView(type)
        return rootView
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(type: Int) {
        with(WebView(TaApp.application)) {
            protocolWebView = this
            settings.javaScriptEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.textZoom = 200
            webViewClient = WebViewClient()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            overScrollMode = View.OVER_SCROLL_NEVER
            loadUrl(getProtocolUrl(type))
        }
        rootView.addView(protocolWebView)

    }

    private fun getProtocolUrl(type: Int) = when (type) {
        TYPE_PRIVACY_PROTOCOL -> "https://api-dev-azero.soundai.cn/v1/surrogate/page/agreement"
        TYPE_USER_PROTOCOL -> "https://api-dev-azero.soundai.cn/v1/surrogate/page/personal"
        else -> ""
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(true)
        val params = dialog!!.window!!.attributes
        params.width = 320.dp.toInt()
        params.height = 600.dp.toInt()
        params.gravity = Gravity.CENTER
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_protocol_dialog)
    }
}