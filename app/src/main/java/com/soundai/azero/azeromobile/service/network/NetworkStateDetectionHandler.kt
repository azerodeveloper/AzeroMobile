package com.soundai.azero.azeromobile.service.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.*
import android.os.Build
import android.util.Log
import kotlin.collections.HashSet

@SuppressLint("LongLogTag")
class NetworkStateDetectionHandler {

    private var mContext: Context? = null
    private var connectivityManager: ConnectivityManager? = null
    private val request = NetworkRequest.Builder().build()

    private var networkState = NetworkState.NONE

    private val tag: String = "NetworkStateDetectionHandler"

    private val listeners: HashSet<NetworkStateCallback> = HashSet()
    private val networkCallback: ConnectivityManager.NetworkCallback

    companion object {

        val instance: NetworkStateDetectionHandler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkStateDetectionHandler()
        }
    }

    init {
        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(tag, "onAvailable")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    when {
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            if (networkState != NetworkState.WIFI) {
                                Log.d(tag, "wifi connected.")
                                synchronized(listeners) {
                                    val iterator = listeners.iterator()
                                    while (iterator.hasNext()) {
                                        val listener = iterator.next()
                                        listener.onWifiAvailable()
                                    }
                                }
                                networkState = NetworkState.WIFI
                            }
                        }
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            if (networkState != NetworkState.CELLULAR) {
                                Log.d(tag, "cellular connected.")
                                synchronized(listeners) {
                                    val iterator = listeners.iterator()
                                    while (iterator.hasNext()) {
                                        val listener = iterator.next()
                                        listener.onCellularAvailable()
                                    }
                                }
                                networkState = NetworkState.CELLULAR
                            }
                        }
                        else -> {
                            if (networkState != NetworkState.OTHER) {
                                Log.d(tag, "other network connected.")
                                synchronized(listeners) {
                                    val iterator = listeners.iterator()
                                    while (iterator.hasNext()) {
                                        val listener = iterator.next()
                                        listener.onOtherNetworkAvailable()
                                    }
                                }
                                networkState = NetworkState.OTHER
                            }
                        }
                    }
                } else {
                    if (networkState != NetworkState.UNAVAILABLE) {
                        Log.d(tag, "network connected but can not use.")
                        synchronized(listeners) {
                            val iterator = listeners.iterator()
                            while (iterator.hasNext()) {
                                val listener = iterator.next()
                                listener.onNetworkUnAvailable()
                            }
                        }
                        networkState = NetworkState.UNAVAILABLE
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                if (networkState != NetworkState.UNCONNECTED) {
                    Log.d(tag, "onLost")
                    synchronized(listeners) {
                        val iterator = listeners.iterator()
                        while (iterator.hasNext()) {
                            val listener = iterator.next()
                            listener.onNetworkUnConnected()
                        }
                    }
                    networkState = NetworkState.UNCONNECTED
                }
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                Log.d(tag, "onLinkPropertiesChanged")
            }

            override fun onUnavailable() {
                Log.d(tag, "onUnavailable")
                super.onUnavailable()
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                Log.d(tag, "onLosing")
            }
        }
    }

    fun init(context: Context) {
        if (mContext == null) {
            mContext = context
            connectivityManager = mContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager?.registerDefaultNetworkCallback(networkCallback)
                Log.d(tag, "registerDefaultNetworkCallback")
            } else {
                connectivityManager?.registerNetworkCallback(request, networkCallback)
                Log.d(tag, "registerNetworkCallback")
            }
        }
    }

    fun destroy() {
        if (connectivityManager != null) {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        }
    }

    fun registerNetworkStateCallback(listener: NetworkStateCallback) {
        Log.d(tag, "register")
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun unregisterNetworkStateCallback(listener: NetworkStateCallback) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }
}