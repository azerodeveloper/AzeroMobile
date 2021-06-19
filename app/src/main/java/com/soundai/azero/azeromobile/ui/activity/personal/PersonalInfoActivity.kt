package com.soundai.azero.azeromobile.ui.activity.personal

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.azero.sdk.util.log
import com.soundai.azero.azeromobile.Constant
import com.soundai.azero.azeromobile.GlideApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.ui.activity.base.activity.BaseSwipeActivity
import com.soundai.azero.azeromobile.ui.widget.CircleImageView
import com.soundai.azero.azeromobile.utils.SPUtils
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class PersonalInfoActivity : BaseSwipeActivity(), EasyPermissions.PermissionCallbacks {

    private val personalInfoViewModel: PersonalInfoViewModel by lazy {
        ViewModelProviders.of(this).get(PersonalInfoViewModel::class.java)
    }

    private val sRequiredPermissions by lazy {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissions.toTypedArray()
    }

    private val civAvatar: CircleImageView by lazy { findViewById<CircleImageView>(R.id.civ_avatar) }
    private val tvAvatar: TextView by lazy { findViewById<TextView>(R.id.tv_avatar) }
    private val tvName: TextView by lazy { findViewById<TextView>(R.id.tv_name) }
    private val tvGender: TextView by lazy { findViewById<TextView>(R.id.tv_gender) }
    private val tvBirthday: TextView by lazy { findViewById<TextView>(R.id.tv_birthday) }
    private val tvEmail: TextView by lazy { findViewById<TextView>(R.id.tv_email) }
    private val rlAvatar: RelativeLayout by lazy { findViewById<RelativeLayout>(R.id.rl_avatar) }
    private val rlName: RelativeLayout by lazy { findViewById<RelativeLayout>(R.id.rl_name) }
    private val rlGender: RelativeLayout by lazy { findViewById<RelativeLayout>(R.id.rl_gender) }
    private val rlBirthday: RelativeLayout by lazy { findViewById<RelativeLayout>(R.id.rl_birthday) }
    private val rlEmail: RelativeLayout by lazy { findViewById<RelativeLayout>(R.id.rl_email) }
    private var name: String? = null
    private var birthday: String? = null
    private var gender: String? = null
    private var cameraImageUri: Uri? = null
    private var cropImageUri: Uri? = null
    private var cropFile: File? = null
    private var email: String? = null

    companion object {
        const val REQUEST_CODE_ALBUM = 0x100
        const val REQUEST_CODE_CAMERA = 0x101
        const val REQUEST_CODE_CROP = 0x102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)
        initView()
        initViewModel()
    }

    private fun initViewModel() {
        personalInfoViewModel.userInfo.observe(this, Observer {
            it.apply {
                if (pictureUrl.isNullOrEmpty()) {
                    civAvatar.visibility = View.GONE
                    tvAvatar.visibility = View.VISIBLE
                } else {
                    civAvatar.visibility = View.VISIBLE
                    tvAvatar.visibility = View.GONE
                    GlideApp.with(this@PersonalInfoActivity)
                        .load(pictureUrl).into(civAvatar)
                }
                this@PersonalInfoActivity.name = name
                this@PersonalInfoActivity.gender = sex
                this@PersonalInfoActivity.birthday = birthday
                this@PersonalInfoActivity.email = email

                tvName.text = if (name.isNullOrEmpty()) "添加" else name
                tvGender.text = if (sex.isNullOrEmpty()) "选择" else sex
                tvBirthday.text = if (birthday.isNullOrEmpty()) "选择" else birthday
                tvEmail.text = if (email.isNullOrEmpty()) "选择" else email
            }
        })

        personalInfoViewModel.state.observe(this, Observer {
        })
    }

    private fun initView() {
        rlAvatar.setOnClickListener {
            val dialog = AvatarDialogFragment.newInstance()
            dialog.setOnSelectedListener(object : AvatarDialogFragment.OnSelectedListener {
                override fun onTakePhoto() {
                    requestPermissions()
                }

                override fun onPickFromAlbum() {
                    AvatarUtil.pickPhoto(this@PersonalInfoActivity)
                }

            })
            dialog.show(supportFragmentManager, "avatarDialog")
        }

        rlName.setOnClickListener {
            var argName: String? = null
            if (!name.isNullOrEmpty()) {
                argName = name
            }
            TextInputDialogFragment.newInstance(TextInputDialogFragment.TYPE_NAME, argName)
                .show(supportFragmentManager, "nameInputDialog")
        }

        rlGender.setOnClickListener {
            GenderSelectDialogFragment.newInstance()
                .show(supportFragmentManager, "genderDialog")
        }

        rlBirthday.setOnClickListener {
            var year = -1
            var month = -1
            var day = -1
            if (!birthday.isNullOrEmpty()) {
                val date = birthday?.split("-")
                date?.apply {
                    year = this[0].toInt()
                    month = this[1].toInt()
                    day = this[2].toInt()
                }
            }
            DatePickerDialogFragment.newInstance(year, month, day)
                .show(supportFragmentManager, "datePickerDialog")
        }

        rlEmail.setOnClickListener {
            var argEmail: String? = null
            if (!email.isNullOrEmpty()) {
                argEmail = email
            }
            TextInputDialogFragment.newInstance(TextInputDialogFragment.TYPE_EMAIL, argEmail)
                .show(supportFragmentManager, "emailInputDialog")
        }
    }


    override fun onStart() {
        super.onStart()
        SPUtils.getAccountPref().apply {
            val userId = getString(Constant.SAVE_USERID, "")!!
            personalInfoViewModel.queryUserInfo(userId)
        }
    }

    fun updateUserInfo(
        pictureUrl: String?,
        sex: String?,
        birthday: String?,
        name: String?,
        email: String?
    ) {
        val sp = getSharedPreferences(
            Constant.SHARDPREF_ACCOUNT,
            Context.MODE_PRIVATE
        )
        val userId = sp.getString(Constant.SAVE_USERID, "")!!
        personalInfoViewModel.updateUserInfo(userId, pictureUrl, sex, birthday, name, email)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CAMERA -> {
                    createCropUri()
                    AvatarUtil.cropPhoto(this, cameraImageUri, cropImageUri!!)
                }
                REQUEST_CODE_ALBUM -> {
                    createCropUri()
                    AvatarUtil.cropPhoto(this, data?.data, cropImageUri!!)
                }

                REQUEST_CODE_CROP -> {
                    val sp = getSharedPreferences(
                        Constant.SHARDPREF_ACCOUNT,
                        Context.MODE_PRIVATE
                    )
                    val userId = sp.getString(Constant.SAVE_USERID, "")!!
                    personalInfoViewModel.uploadAvatar(userId, cropFile!!)
                }
            }
        }
    }

    private fun createCropUri() {
        cropFile = AvatarUtil.createCropFile(this)
        cropImageUri = AvatarUtil.createCropUri(cropFile!!)
    }

    private fun requestPermissions() {
        if (!EasyPermissions.hasPermissions(this, *sRequiredPermissions)) {
            EasyPermissions.requestPermissions(
                this,
                "需要摄像头权限，sd卡权限",
                100,
                *sRequiredPermissions
            )
        } else {
            takePhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        //永久性被拒绝
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                .setThemeResId(R.style.Theme_AppCompat_Light_Dialog).build().show()
        } else {

        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        log.d("$perms permissions are granted")
        if (perms.size == sRequiredPermissions.size) {
            takePhoto()
        }
    }

    private fun takePhoto() {
        cameraImageUri = AvatarUtil.createCameraUri(
            this@PersonalInfoActivity,
            AvatarUtil.createTakePhotoFile(this@PersonalInfoActivity)
        )
        AvatarUtil.takePhoto(this@PersonalInfoActivity, cameraImageUri!!)
    }
}
