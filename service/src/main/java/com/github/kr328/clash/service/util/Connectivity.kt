package com.github.kr328.clash.service.util

import android.net.ConnectivityManager
import android.net.Network
import java.net.Inet4Address

fun ConnectivityManager.resolvePrimaryDns(network: Network?): String? {
    val properties = getLinkProperties(network) ?: return null

    return properties.dnsServers.sortedBy { if (it is Inet4Address) { 0 } else { 999 } }.firstOrNull()?.asSocketAddressText(53)
}
