package com.soundai.azero.azeromobile.ui.activity.personal

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.widget.wheelview.WheelView

class GenderSelectDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(): GenderSelectDialogFragment {
            val fragment = GenderSelectDialogFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val rootView = inflater.inflate(R.layout.dialog_fragment_gender_select, container, false)
        val tvCancel = rootView.findViewById<TextView>(R.id.tv_cancel)
        val tvFinish = rootView.findViewById<TextView>(R.id.tv_finish)
        val wheelGender = rootView.findViewById<WheelView>(R.id.wheelView_gender)
        var gender = "男"
        wheelGender.onWheelChangedListener = object : WheelView.OnWheelChangedListener {
            override fun onChanged(view: WheelView?, oldIndex: Int, newIndex: Int, data: String?) {
                gender = data!!
            }

        }
        tvCancel.setOnClickListener { dismiss() }
        tvFinish.setOnClickListener {
            (activity as PersonalInfoActivity).updateUserInfo(
                null,
                gender,
                null,
                null,
                null
            )
            dismiss()
        }
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
}