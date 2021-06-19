package com.soundai.azero.azeromobile.ui.activity.personal

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.toast

class TextInputDialogFragment : DialogFragment() {
    companion object {
        const val TYPE_NAME = 1
        const val TYPE_EMAIL = 2

        fun newInstance(type: Int, defaultValue: String? = null): TextInputDialogFragment {
            val fragment = TextInputDialogFragment()
            val bundle = Bundle()
            bundle.putInt("type", type)
            bundle.putString("defaultValue", defaultValue)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_fragment_name_input, container, false)
        val etValue = rootView.findViewById<EditText>(R.id.et_value)
        val tvCancel = rootView.findViewById<TextView>(R.id.tv_cancel)
        val tvFinish = rootView.findViewById<TextView>(R.id.tv_finish)
        val type = arguments?.getInt("type") ?: -1
        rootView.findViewById<TextView>(R.id.tv_title).text = getTitle(type)
        rootView.findViewById<EditText>(R.id.et_value).hint = getHint(type)
        etValue.setText(arguments?.getString("defaultValue", ""))
        tvCancel.setOnClickListener { dismiss() }
        tvFinish.setOnClickListener {
            if (etValue.text.toString().trim().isEmpty()) {
                getToast(type)?.let { activity?.toast(it) }
                return@setOnClickListener
            }
            sendRequest(type, etValue.text.toString().trim())
            dismiss()
        }
        return rootView
    }

    private fun sendRequest(type: Int, value: String) {
        val name: String? = if (type == TYPE_NAME) value else null
        val email: String? = if (type == TYPE_EMAIL) value else null
        (activity as PersonalInfoActivity).updateUserInfo(
            null,
            null,
            null,
            name,
            email
        )
    }

    private fun getTitle(type: Int) =
        when (type) {
            TYPE_NAME -> {
                "修改昵称"
            }
            TYPE_EMAIL -> {
                "修改邮箱"
            }
            else -> {
                null
            }
        }

    private fun getHint(type: Int) =
        when (type) {
            TYPE_NAME -> {
                "请输入昵称"
            }
            TYPE_EMAIL -> {
                "请输入邮箱"
            }
            else -> {
                null
            }
        }

    private fun getToast(type: Int) =
        when (type) {
            TYPE_NAME -> {
                "昵称不能为空"
            }
            TYPE_EMAIL -> {
                "邮箱不能为空"
            }
            else -> {
                null
            }
        }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
        val params = dialog!!.window!!.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.CENTER
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}