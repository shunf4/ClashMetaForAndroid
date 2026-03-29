package com.github.kr328.clash.service.clash.module

import android.app.Service
import android.net.*
import android.os.Build
import androidx.lifecycle.lifecycleScope
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleCoroutineScope
import com.github.kr328.clash.common.Global
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.core.Clash
import com.github.kr328.clash.service.util.asSocketAddressText
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import kotlin.coroutines.coroutineContext
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

class NetworkObserveModule(service: Service) : Module<Network>(service) {
    private val connectivity = service.getSystemService<ConnectivityManager>()!!
    private val networks: Channel<Network> = Channel(Channel.UNLIMITED)
    private val request = NetworkRequest.Builder().apply {
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            addCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)
        }
        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
    }.build()

    private data class NetworkInfo(
        @Volatile var losingMs: Long = 0,
        @Volatile var dnsList: List<InetAddress> = emptyList()
    ) {
        fun isAvailable(): Boolean = losingMs < System.currentTimeMillis()
    }

    private data class NetworkQuarternion(
        @Volatile var network: Network,
        @Volatile var networkInfo: NetworkInfo,
        @Volatile var netPriority: Int,
        @Volatile var netPrimaryTransport: Int,
    )

    private val networkInfos = ConcurrentHashMap<Network, NetworkInfo>()

    @Volatile
    private var curDnsList = emptyList<String>()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.i("NetworkObserve onAvailable network=$network")
            networkInfos[network] = NetworkInfo()
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            Log.i("NetworkObserve onLosing network=$network")
            networkInfos[network]?.losingMs = System.currentTimeMillis() + maxMsToLive
            notifyNetworkAndDnsChange()

            networks.trySend(network)
        }

        override fun onLost(network: Network) {
            Log.i("NetworkObserve onLost network=$network")
            networkInfos.remove(network)
            notifyNetworkAndDnsChange()

            networks.trySend(network)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            Log.i("NetworkObserve onLinkPropertiesChanged network=$network $linkProperties")
            networkInfos[network]?.dnsList = linkProperties.dnsServers
            notifyNetworkAndDnsChange()

            networks.trySend(network)
        }

        override fun onUnavailable() {
            Log.i("NetworkObserve onUnavailable")
        }
    }

    private var debouncedNetworkRefreshJob: Job? = null
    private var debouncedNetworkRefreshLastReducedDns: List<String>? = null
    private var debouncedNetworkRefreshLastReducedTransport: Int? = null

    private fun register(): Boolean {
        Log.i("NetworkObserve start register")
        return try {
            connectivity.registerNetworkCallback(request, callback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                connectivity.requestNetwork(request, callback, 1000)
            }

            true
        } catch (e: Exception) {
            Log.w("NetworkObserve register failed", e)

            false
        }
    }

    private fun unregister(): Boolean {
        Log.i("NetworkObserve start unregister")
        try {
            connectivity.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            Log.w("NetworkObserve unregister failed", e)
        }

        return false
    }

    private fun networkToNetAndNetInfoAndPriorityAndPrimaryTransport(entry: Map.Entry<Network, NetworkInfo>): NetworkQuarternion {
        val capabilities = connectivity.getNetworkCapabilities(entry.key)
        // calculate priority based on transport type, available state
        // lower value means higher priority
        // wifi > ethernet > usb tethering > bluetooth tethering > cellular > satellite > other
        var primaryTransport: Int = -1
        val prio = when {
            capabilities == null -> { primaryTransport = -1; 100 }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> { primaryTransport = NetworkCapabilities.TRANSPORT_VPN; 90 }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> { primaryTransport = NetworkCapabilities.TRANSPORT_WIFI; 0 }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> { primaryTransport = NetworkCapabilities.TRANSPORT_ETHERNET; 1 }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> { primaryTransport = NetworkCapabilities.TRANSPORT_USB; 2 }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> { primaryTransport = NetworkCapabilities.TRANSPORT_BLUETOOTH; 3 }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> { primaryTransport = NetworkCapabilities.TRANSPORT_CELLULAR; 4 }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE) -> { primaryTransport = NetworkCapabilities.TRANSPORT_SATELLITE; 5 }
            // TRANSPORT_LOWPAN / TRANSPORT_THREAD / TRANSPORT_WIFI_AWARE are not for general internet access, which will not set as default route.
            else -> { primaryTransport = -1; 20 }
        }
        val offset = (if (entry.value.isAvailable()) 0 else 10)
        return NetworkQuarternion(entry.key, entry.value, prio + offset, primaryTransport)
    }

    private fun notifyNetworkAndDnsChange() {
        val netQuat = networkInfos.asSequence().map { networkToNetAndNetInfoAndPriorityAndPrimaryTransport(it) }.minByOrNull { it.netPriority }
        val network = netQuat?.network
        val transport = netQuat?.netPrimaryTransport ?: -1

        Global.launch(Dispatchers.IO) {
            debouncedNetworkRefreshJob?.cancel()
            debouncedNetworkRefreshJob = launch {
                debouncedNetworkRefreshLastReducedTransport = transport

                delay(1000L)

                // val dns = networks.mapNotNull {
                //     connectivity.resolvePrimaryDns(it)
                // }
                val dnsList = (netQuat?.networkInfo?.dnsList ?: emptyList()).map { x -> x.asSocketAddressText(53) }

                val prevDnsList = curDnsList
                if (dnsList.isNotEmpty() && prevDnsList != dnsList) {
                    Log.i("notifyNetworkAndDnsChange $prevDnsList -> $dnsList")
                    curDnsList = dnsList
                    debouncedNetworkRefreshLastReducedDns = dnsList
                    debouncedNetworkRefreshLastReducedDns?.let { Clash.notifyDnsChanged(it) }
                }

                Log.d("DNS: $debouncedNetworkRefreshLastReducedDns")

                Log.d("Refresh reverse with transport: $debouncedNetworkRefreshLastReducedTransport")
                debouncedNetworkRefreshLastReducedTransport?.let { Clash.refreshReverse(it) }

                debouncedNetworkRefreshLastReducedDns = null
                debouncedNetworkRefreshLastReducedTransport = null
            }
        }



    }

    override suspend fun run() {
        register()

        try {
            while (true) {
                val quit = select {
                    networks.onReceive {
                        enqueueEvent(it)

                        false
                    }
                }
                if (quit) {
                    return
                }
            }
        } finally {
            withContext(NonCancellable) {
                unregister()

                Log.i("NetworkObserve dns = []")
                Clash.notifyDnsChanged(emptyList())
            }
        }
    }
}