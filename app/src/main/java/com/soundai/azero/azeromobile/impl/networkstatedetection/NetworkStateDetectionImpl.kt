package com.soundai.azero.azeromobile.impl.networkstatedetection

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.azero.platforms.iface.AlexaClient
import com.azero.platforms.network.NetworkInfoProvider
import com.azero.sdk.AzeroManager
import com.azero.sdk.impl.AzeroClient.AzeroClientHandler
import com.azero.sdk.impl.NetworkInfoProvider.NetworkConnectionObserver
import com.azero.sdk.impl.NetworkInfoProvider.NetworkInfoProviderHandler
import com.azero.sdk.util.executors.AppExecutors
import com.soundai.azero.azeromobile.TaApp
import com.soundai.azero.azeromobile.R
import com.soundai.azero.azeromobile.manager.ActivityLifecycleManager
import com.soundai.azero.azeromobile.service.network.NetworkState
import com.soundai.azero.azeromobile.service.network.NetworkStateCallback
import com.soundai.azero.azeromobile.service.network.NetworkStateDetectionHandler
import com.soundai.azero.azeromobile.ui.widget.dialog.DialogManager
import com.soundai.azero.azeromobile.ui.widget.topbanner.TopBannerManager

@SuppressLint("LongLogTag")
class NetworkStateDetectionImpl(context: Context, appExecutors: AppExecutors) : NetworkStateCallback,
    NetworkConnectionObserver, AzeroClientHandler.ConnectionStatusListener {

    private val tag = "NetworkStateDetectionImpl"
    private val mContext = context
    private val mAppExecutors = appExecutors
    private var networkInfoProviderHandler: NetworkInfoProviderHandler
    private var azeroClientHandler: AzeroClientHandler
    private var mNetworkStatus: NetworkInfoProvider.NetworkStatus? = null

    init {
        NetworkStateDetectionHandler.instance.registerNetworkStateCallback(this)
        networkInfoProviderHandler =
            AzeroManager.getInstance().getHandler("NetworkInfoProviderHandler") as NetworkInfoProviderHandler
        networkInfoProviderHandler.registerNetworkConnectionObserver(this)
        azeroClientHandler =
            AzeroManager.getInstance().getHandler(AzeroManager.AZERO_CLIENT_HANDLER) as AzeroClientHandler
        azeroClientHandler.addConnectionStatusListener(this)
    }

    override fun onNetworkUnConnected() {
        Log.d(tag, "onNetworkUnConnected")
        showTopBanner(NetworkState.SERVICE_UNCONNECTED)
    }

    override fun onWifiAvailable() {
        Log.d(tag, "onWifiAvailable")
        if (azeroClientHandler == null || azeroClientHandler?.connectionStatus == AlexaClient.ConnectionStatus.DISCONNECTED) {
            Log.d(tag, "service disconnect")
//            showTopBanner(NetworkState.SERVICE_UNCONNECTED)
        } else {
            dismissTopBanner()
        }
        showDialog(NetworkState.WIFI)
    }

    override fun onCellularAvailable() {
        Log.d(tag, "onCellularAvailable")
        if (azeroClientHandler == null ||azeroClientHandler?.connectionStatus == AlexaClient.ConnectionStatus.DISCONNECTED) {
            Log.d(tag, "service disconnect")
//            showTopBanner(NetworkState.SERVICE_UNCONNECTED)
        } else {
            dismissTopBanner()
        }
        showDialog(NetworkState.CELLULAR)
    }

    override fun onOtherNetworkAvailable() {
        Log.d(tag, "onOtherNetworkAvailable")
        if (azeroClientHandler == null ||azeroClientHandler?.connectionStatus == AlexaClient.ConnectionStatus.DISCONNECTED) {
            Log.d(tag, "service disconnect")
//            showTopBanner(NetworkState.SERVICE_UNCONNECTED)
            return
        }
        dismissTopBanner()
        showDialog(NetworkState.OTHER)

    }

    override fun onNetworkUnAvailable() {
        Log.d(tag, "onNetworkUnAvailable")
        showTopBanner(NetworkState.UNAVAILABLE)
        showDialog(NetworkState.UNAVAILABLE)
    }

    override fun onConnectionStatusChanged(networkStatus: NetworkInfoProvider.NetworkStatus) {
        if (mNetworkStatus == null || mNetworkStatus!! != networkStatus) {
            mNetworkStatus = networkStatus
            when (networkStatus) {
                /**
                 * 未联网时启动app可收到UNKNOWN
                 */
                NetworkInfoProvider.NetworkStatus.UNKNOWN -> {
                    Log.d(tag, "NetworkStatus: UNKNOWN")
                    showTopBanner(NetworkState.UNCONNECTED)
                }
                NetworkInfoProvider.NetworkStatus.DISCONNECTED -> {
                    Log.d(tag, "NetworkStatus: DISCONNECTED")
                }
                NetworkInfoProvider.NetworkStatus.DISCONNECTING -> {
                    Log.d(tag, "NetworkStatus: DISCONNECTING")
                }
                NetworkInfoProvider.NetworkStatus.CONNECTED -> {
                    Log.d(tag, "NetworkStatus: CONNECTED")
                    showDialog(NetworkState.OTHER)
                }
                NetworkInfoProvider.NetworkStatus.CONNECTING -> {
                    Log.d(tag, "NetworkStatus: CONNECTING")
                }
            }
        }
    }

    override fun connectionStatusChanged(
        connectionStatus: AlexaClient.ConnectionStatus,
        connectionChangedReason: AlexaClient.ConnectionChangedReason
    ) {
        Log.d(tag, "connectionStatus: $connectionStatus, connectionChangedReason: $connectionChangedReason")
        when (connectionStatus) {
            AlexaClient.ConnectionStatus.DISCONNECTED -> {
                showTopBanner(NetworkState.SERVICE_UNCONNECTED)
            }
            AlexaClient.ConnectionStatus.PENDING -> {
            }
            AlexaClient.ConnectionStatus.CONNECTED -> {
                dismissTopBanner()
            }
        }
    }

    private fun showTopBanner(networkState: NetworkState) {
        var message = ""
        var drawableResId: Int = R.drawable.wlyc_img_fail
        var isNetworkUnconnected = false
        when (networkState) {
            NetworkState.UNCONNECTED -> {
                TaApp.isInNoneedToNotifyEngineState = true
                message = mContext.resources.getString(R.string.network_unconnected)
                drawableResId = R.drawable.dw_icon_close
                isNetworkUnconnected = true
            }
            NetworkState.UNAVAILABLE -> {
                TaApp.isInNoneedToNotifyEngineState = true
                message = mContext.resources.getString(R.string.network_unavailable)
                drawableResId = R.drawable.dw_icon_tips
                isNetworkUnconnected = false
            }
            NetworkState.SERVICE_UNCONNECTED -> {
                TaApp.isInNoneedToNotifyEngineState = true
                message = mContext.resources.getString(R.string.network_service_unconnected)
                drawableResId = R.drawable.dw_icon_tips
                isNetworkUnconnected = false
            }
            else -> {
            }
        }
        mAppExecutors.mainThread().execute {
            TopBannerManager.showNetworkTopBanner(message, drawableResId, isNetworkUnconnected)
        }
    }

    private fun dismissTopBanner() {
        Log.d(tag, "dismiss top banner")
        TaApp.isInNoneedToNotifyEngineState = false
        mAppExecutors.mainThread().execute {
            TopBannerManager.dismissNetworkTopBanner()
        }
    }

    private fun showDialog(networkState: NetworkState) {
        if(!ActivityLifecycleManager.getInstance().isAppForeground) return
        when (networkState) {
            NetworkState.CELLULAR -> {
                DialogManager.showNetworkDialogView(
                    mContext,
                    mContext.resources.getString(R.string.network_mobile_data_alert)
                )
            }
            NetworkState.WIFI -> {
                DialogManager.dismissDialog()
            }
            NetworkState.UNAVAILABLE -> {
                DialogManager.showNetworkDialogView(
                    mContext,
                    mContext.resources.getString(R.string.network_weak),
                    R.drawable.wlyc_img_fail
                )
            }
            else -> {
                DialogManager.dismissDialog()
            }
        }
    }
}