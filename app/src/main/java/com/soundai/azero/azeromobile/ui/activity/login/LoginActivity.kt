package com.soundai.azero.azeromobile.ui.activity.login

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.soundai.azero.azeromobile.R
import com.umeng.message.PushAgent

class LoginActivity : AppCompatActivity() {
    private lateinit var otherLogin: TextView
    private lateinit var navController: NavController
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushAgent.getInstance(this).onAppStart()
        setContentView(R.layout.activity_login)
        initView()
        ivBack = findViewById(R.id.iv_back)
        findViewById<TextView>(R.id.tv_other_login).setOnClickListener {
            if (navController.currentDestination?.id == R.id.phoneLoginFragment)
                navController.navigate(R.id.action_phoneLoginFragment_to_selectLoginModeFragment)
        }

        ivBack.setOnClickListener {
            navController.navigateUp()
        }
        navController = findNavController(R.id.login_nav_host_fragment)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.phoneLoginFragment -> {
                    ivBack.visibility = View.INVISIBLE
                    otherLogin.visibility = View.VISIBLE
                }
                else -> {
                    ivBack.visibility = View.VISIBLE
                    otherLogin.visibility = View.GONE
                }
            }
        }
    }

    private fun initView() {
        otherLogin = findViewById(R.id.tv_other_login)
    }

    override fun onSupportNavigateUp() =
        navController.navigateUp()
}