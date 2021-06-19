package com.soundai.azero.azeromobile.service.network

interface NetworkStateCallback {
    /**
     * 无线网未连接
     *
     */
    fun onNetworkUnConnected()

    /**
     * wifi已连接并可用
     *
     */
    fun onWifiAvailable()

    /**
     * 移动网络已连接并可用
     */
    fun onCellularAvailable()

    /**
     * 其他网络已连接并可用
     */
    fun onOtherNetworkAvailable()
    /**
     * 无线网络链接但不可用
     */
    fun onNetworkUnAvailable()
}