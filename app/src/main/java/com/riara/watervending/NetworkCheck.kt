package com.riara.watervending

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

// Tells us if the phone is connected to the internet
object NetworkCheck {
    fun isOnline(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}