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

class DatePickerDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(year: Int, month: Int, day: Int): DatePickerDialogFragment {
            val fragment = DatePickerDialogFragment()
            val bundle = Bundle()
            bundle.putInt("year", year)
            bundle.putInt("month", month)
            bundle.putInt("day", day)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.default_dialog_style)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val rootView = inflater.inflate(R.layout.dialog_fragment_date_picker, container, false)
        val tvCancel = rootView.findViewById<TextView>(R.id.tv_cancel)
        val tvFinish = rootView.findViewById<TextView>(R.id.tv_finish)
        val datePicker = rootView.findViewById<DatePicker>(R.id.date_picker)
        datePicker.maxDate = System.currentTimeMillis()

        val argYear = arguments!!.getInt("year",-1)
        val argMonth = arguments!!.getInt("month",-1)
        val argDay = arguments!!.getInt("day",-1)

        var year:Int?
        var month:Int?
        var day:Int?

        var date = if(argYear == -1 && argMonth == -1 && argDay == -1){
            val calendar = Calendar.getInstance()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
            "$year-${month + 1}-$day"
        }else{
            year = argYear
            month = argMonth?.minus(1)
            day = argDay
            "$argYear-$argMonth-$argDay"
        }
        datePicker.init(
            year, month, day
        ) { view, year, monthOfYear, dayOfMonth ->
            date = "$year-${monthOfYear + 1}-$dayOfMonth"
        }
        tvCancel.setOnClickListener { dismiss() }
        tvFinish.setOnClickListener {
            (activity as PersonalInfoActivity).updateUserInfo(
                null,
                null,
                date,
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