package com.soundai.azero.azeromobile.ui.activity.personal

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.toast
import org.w3c.dom.Text
import java.time.Year
import java.util.*

class AvatarDialogFragment : DialogFragment() {

    private var onSelectedListener: OnSelectedListener? = null

    companion object {
        fun newInstance(): AvatarDialogFragment {
            val fragment = AvatarDialogFragment()
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.default_dialog_style)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val rootView = inflater.inflate(R.layout.dialog_fragment_avatar, container, false)
        val tvCancel = rootView.findViewById<TextView>(R.id.tv_cancel)
        val tvPick = rootView.findViewById<TextView>(R.id.tv_pick_from_album)
        val tvTake = rootView.findViewById<TextView>(R.id.tv_take_photo)
        tvPick.setOnClickListener {
            onSelectedListener?.onPickFromAlbum()
            dismiss()
        }
        tvTake.setOnClickListener {
            onSelectedListener?.onTakePhoto()
            dismiss()
        }
        tvCancel.setOnClickListener { dismiss() }
        return rootView
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.decorView?.setPadding(0, 0, 0, 0)

        val params = dialog!!.window!!.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.BOTTOM
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawable(ColorDrawable())
    }

    fun setOnSelectedListener(listener: OnSelectedListener) {
        this.onSelectedListener = listener
    }

    interface OnSelectedListener {
        fun onTakePhoto()
        fun onPickFromAlbum()
    }
}