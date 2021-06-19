package com.soundai.azero.azeromobile.ui.activity.personal

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.network.Cms
import com.soundai.azero.azeromobile.network.Surrogate
import com.soundai.azero.azeromobile.postNext
import com.soundai.azero.azeromobile.setNext
import com.soundai.azero.azeromobile.toast
import com.soundai.azero.azeromobile.ui.activity.base.viewmodel.BaseRequestViewModel
import com.soundai.azero.lib_surrogate.response.UserInfo
import java.io.File

class PersonalInfoViewModel(application: Application) : BaseRequestViewModel(application) {
    val userInfo: LiveData<UserInfo>
        get() = userInfoLiveData
    val state: LiveData<PersonalInfoViewState>
        get() = stateLiveData

    private val stateLiveData: MutableLiveData<PersonalInfoViewState> =
        MutableLiveData(PersonalInfoViewState.initial())

    private val userInfoLiveData = MutableLiveData<UserInfo>()

    fun queryUserInfo(userId: String) {
        stateLiveData.setNext {
            it.copy(isLoading = true, isUpdateSuccess = false, throwable = null)
        }
        surrogateRequest(
            {
                Surrogate.queryUserInfo(userId)
            },
            {
                Log.i("PersonalInfoViewModel", it.toString())
                userInfoLiveData.postValue(it)
                stateLiveData.postNext {
                    it.copy(isLoading = false, isUpdateSuccess = false, throwable = null)
                }
            },
            { exception ->
                stateLiveData.postNext {
                    it.copy(isLoading = false, isUpdateSuccess = false, throwable = exception)
                }
            }
        )
    }


    fun uploadAvatar(userId: String, file: File) {
        stateLiveData.setNext {
            it.copy(isLoading = true, isUpdateSuccess = false, throwable = null)
        }
        surrogateRequest(
            { Cms.uploadResource(file) },
            {
                stateLiveData.postNext {
                    it.copy(isLoading = false, isUpdateSuccess = false, throwable = null)
                }
                updateUserInfo(userId, it.url, null, null, null, null)
            }, { exception ->
                stateLiveData.postNext {
                    it.copy(isLoading = false, isUpdateSuccess = false, throwable = exception)
                }
                TaApp.application.toast("上传头像失败")
            }
        )
    }

    fun updateUserInfo(
        userId: String,
        pictureUrl: String?,
        sex: String?,
        birthday: String?,
        name: String?,
        email: String?
    ) {
        stateLiveData.setNext {
            it.copy(isLoading = true, isUpdateSuccess = false, throwable = null)
        }
        surrogateRequest({
            Surrogate.updateUserInfo(userId, pictureUrl, sex, birthday, name, email)
        }, {
            queryUserInfo(userId)
            stateLiveData.postNext {
                it.copy(isLoading = false, isUpdateSuccess = true, throwable = null)
            }
        }, { exception ->
            stateLiveData.postNext {
                it.copy(isLoading = false, isUpdateSuccess = false, throwable = exception)
            }
        })
    }
}