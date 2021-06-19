package com.soundai.azero.azeromobile.ui.activity.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import com.soundai.azero.azeromobile.R

class SelectLoginModeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_login_mode, container, false)
        // Inflate the layout for this fragment
        view.findViewById<Button>(R.id.btn_act_and_pwd).setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_selectLoginModeFragment_to_pwdLoginFragment)
        }
        return view
    }
}
