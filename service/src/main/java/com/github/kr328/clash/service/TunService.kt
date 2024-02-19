package com.github.kr328.clash.service

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.net.ProxyInfo
import android.net.VpnService
import android.os.Build
import com.github.kr328.clash.common.compat.pendingIntentFlags
import com.github.kr328.clash.common.constants.Components
import com.github.kr328.clash.common.log.Log
import com.github.kr328.clash.service.clash.clashRuntime
import com.github.kr328.clash.service.clash.module.*
import com.github.kr328.clash.service.model.AccessControlMode
import com.github.kr328.clash.service.store.ServiceStore
import com.github.kr328.clash.service.util.cancelAndJoinBlocking
import com.github.kr328.clash.service.util.parseCIDR
import com.github.kr328.clash.service.util.sendClashStarted
import com.github.kr328.clash.service.util.sendClashStopped
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

class TunService : VpnService(), CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val self: TunService
        get() = this

    private var reason: String? = null

    private val runtime = clashRuntime {
        val store = ServiceStore(self)

        val close = install(CloseModule(self))
        val tun = install(TunModule(self))
        val config = install(ConfigurationModule(self))
        val network = install(NetworkObserveModule(self))
        val sideload = install(SideloadDatabaseModule(self))

        if (store.dynamicNotification)
            install(DynamicNotificationModule(self))
        else
            install(StaticNotificationModule(self))

        install(AppListCacheModule(self))
        install(TimeZoneModule(self))
        install(SuspendModule(self))
        install(ClashraySendReceiveModule(self))

        try {
            tun.open()

            while (isActive) {
                val quit = select<Boolean> {
                    close.onEvent {
                        true
                    }
                    config.onEvent {
                        reason = it.message

                        true
                    }
                    sideload.onEvent {
                        reason = it.message

                        true
                    }
                    network.onEvent { n ->
                        if (Build.VERSION.SDK_INT in 22..28) @TargetApi(22) {
                            setUnderlyingNetworks(n?.let { arrayOf(it) })
                        }

                        false
                    }
                }

                if (quit) break
            }
        } catch (e: Exception) {
            Log.e("Create clash runtime: ${e.message}", e)

            reason = e.message
        } finally {
            withContext(NonCancellable) {
                tun.close()

                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (StatusProvider.serviceRunning)
            return stopSelf()

        StatusProvider.serviceRunning = true

        StaticNotificationModule.createNotificationChannel(this)
        StaticNotificationModule.notifyLoadingNotification(this)

        runtime.launch()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendClashStarted()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        TunModule.requestStop()

        StatusProvider.serviceRunning = false

        sendClashStopped(reason)

        cancelAndJoinBlocking()

        Log.i("TunService destroyed: ${reason ?: "successfully"}")

        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        runtime.requestGc()
    }

    private fun TunModule.open() {
        val store = ServiceStore(self)

        val device = with(Builder()) {
            // Interface address
            addAddress(TUN_GATEWAY, TUN_SUBNET_PREFIX)

            addAddress("7.114.19.1", 24)
            addAddress("7.114.19.2", 24)
            addAddress("7.114.19.3", 24)
            addAddress("7.114.19.4", 24)
            addAddress("7.114.19.5", 24)
            addAddress("7.114.19.6", 24)
            addAddress("7.114.19.7", 24)
            addAddress("7.114.19.8", 24)
            addAddress("7.114.19.9", 24)
            addAddress("7.114.19.10", 24)
            addAddress("7.114.19.11", 24)
            addAddress("7.114.19.12", 24)
            addAddress("7.114.19.13", 24)
            addAddress("7.114.19.14", 24)
            addAddress("7.114.19.15", 24)
            addAddress("7.114.19.16", 24)
            addAddress("7.114.19.17", 24)
            addAddress("7.114.19.18", 24)
            addAddress("7.114.19.19", 24)
            addAddress("7.114.19.20", 24)
            addAddress("7.114.19.21", 24)
            addAddress("7.114.19.22", 24)
            addAddress("7.114.19.23", 24)
            addAddress("7.114.19.24", 24)
            addAddress("7.114.19.25", 24)
            addAddress("7.114.19.26", 24)
            addAddress("7.114.19.27", 24)
            addAddress("7.114.19.28", 24)
            addAddress("7.114.19.29", 24)
            addAddress("7.114.19.30", 24)
            addAddress("7.114.19.31", 24)
            addAddress("7.114.19.32", 24)
            addAddress("7.114.19.33", 24)
            addAddress("7.114.19.34", 24)
            addAddress("7.114.19.35", 24)
            addAddress("7.114.19.36", 24)
            addAddress("7.114.19.37", 24)
            addAddress("7.114.19.38", 24)
            addAddress("7.114.19.39", 24)
            addAddress("7.114.19.40", 24)
            addAddress("7.114.19.41", 24)
            addAddress("7.114.19.42", 24)
            addAddress("7.114.19.43", 24)
            addAddress("7.114.19.44", 24)
            addAddress("7.114.19.45", 24)
            addAddress("7.114.19.46", 24)
            addAddress("7.114.19.47", 24)
            addAddress("7.114.19.48", 24)
            addAddress("7.114.19.49", 24)
            addAddress("7.114.19.50", 24)
            addAddress("7.114.19.51", 24)
            addAddress("7.114.19.52", 24)
            addAddress("7.114.19.53", 24)
            addAddress("7.114.19.54", 24)
            addAddress("7.114.19.55", 24)
            addAddress("7.114.19.56", 24)
            addAddress("7.114.19.57", 24)
            addAddress("7.114.19.58", 24)
            addAddress("7.114.19.59", 24)
            addAddress("7.114.19.60", 24)
            addAddress("7.114.19.61", 24)
            addAddress("7.114.19.62", 24)
            addAddress("7.114.19.63", 24)
            addAddress("7.114.19.64", 24)
            addAddress("7.114.19.65", 24)
            addAddress("7.114.19.66", 24)
            addAddress("7.114.19.67", 24)
            addAddress("7.114.19.68", 24)
            addAddress("7.114.19.69", 24)
            addAddress("7.114.19.70", 24)
            addAddress("7.114.19.71", 24)
            addAddress("7.114.19.72", 24)
            addAddress("7.114.19.73", 24)
            addAddress("7.114.19.74", 24)
            addAddress("7.114.19.75", 24)
            addAddress("7.114.19.76", 24)
            addAddress("7.114.19.77", 24)
            addAddress("7.114.19.78", 24)
            addAddress("7.114.19.79", 24)
            addAddress("7.114.19.80", 24)
            addAddress("7.114.19.81", 24)
            addAddress("7.114.19.82", 24)
            addAddress("7.114.19.83", 24)
            addAddress("7.114.19.84", 24)
            addAddress("7.114.19.85", 24)
            addAddress("7.114.19.86", 24)
            addAddress("7.114.19.87", 24)
            addAddress("7.114.19.88", 24)
            addAddress("7.114.19.89", 24)
            addAddress("7.114.19.90", 24)
            addAddress("7.114.19.91", 24)
            addAddress("7.114.19.92", 24)
            addAddress("7.114.19.93", 24)
            addAddress("7.114.19.94", 24)
            addAddress("7.114.19.95", 24)
            addAddress("7.114.19.96", 24)
            addAddress("7.114.19.97", 24)
            addAddress("7.114.19.98", 24)
            addAddress("7.114.19.99", 24)
            addAddress("7.114.19.100", 24)
            addAddress("7.114.19.101", 24)
            addAddress("7.114.19.102", 24)
            addAddress("7.114.19.103", 24)
            addAddress("7.114.19.104", 24)
            addAddress("7.114.19.105", 24)
            addAddress("7.114.19.106", 24)
            addAddress("7.114.19.107", 24)
            addAddress("7.114.19.108", 24)
            addAddress("7.114.19.109", 24)
            addAddress("7.114.19.110", 24)
            addAddress("7.114.19.111", 24)
            addAddress("7.114.19.112", 24)
            addAddress("7.114.19.113", 24)
            addAddress("7.114.19.114", 24)
            addAddress("7.114.19.115", 24)
            addAddress("7.114.19.116", 24)
            addAddress("7.114.19.117", 24)
            addAddress("7.114.19.118", 24)
            addAddress("7.114.19.119", 24)
            addAddress("7.114.19.120", 24)
            addAddress("7.114.19.121", 24)
            addAddress("7.114.19.122", 24)
            addAddress("7.114.19.123", 24)
            addAddress("7.114.19.124", 24)
            addAddress("7.114.19.125", 24)
            addAddress("7.114.19.126", 24)
            addAddress("7.114.19.127", 24)
            addAddress("7.114.19.128", 24)
            addAddress("7.114.19.129", 24)
            addAddress("7.114.19.130", 24)
            addAddress("7.114.19.131", 24)
            addAddress("7.114.19.132", 24)
            addAddress("7.114.19.133", 24)
            addAddress("7.114.19.134", 24)
            addAddress("7.114.19.135", 24)
            addAddress("7.114.19.136", 24)
            addAddress("7.114.19.137", 24)
            addAddress("7.114.19.138", 24)
            addAddress("7.114.19.139", 24)
            addAddress("7.114.19.140", 24)
            addAddress("7.114.19.141", 24)
            addAddress("7.114.19.142", 24)
            addAddress("7.114.19.143", 24)
            addAddress("7.114.19.144", 24)
            addAddress("7.114.19.145", 24)
            addAddress("7.114.19.146", 24)
            addAddress("7.114.19.147", 24)
            addAddress("7.114.19.148", 24)
            addAddress("7.114.19.149", 24)
            addAddress("7.114.19.150", 24)
            addAddress("7.114.19.151", 24)
            addAddress("7.114.19.152", 24)
            addAddress("7.114.19.153", 24)
            addAddress("7.114.19.154", 24)
            addAddress("7.114.19.155", 24)
            addAddress("7.114.19.156", 24)
            addAddress("7.114.19.157", 24)
            addAddress("7.114.19.158", 24)
            addAddress("7.114.19.159", 24)
            addAddress("7.114.19.160", 24)
            addAddress("7.114.19.161", 24)
            addAddress("7.114.19.162", 24)
            addAddress("7.114.19.163", 24)
            addAddress("7.114.19.164", 24)
            addAddress("7.114.19.165", 24)
            addAddress("7.114.19.166", 24)
            addAddress("7.114.19.167", 24)
            addAddress("7.114.19.168", 24)
            addAddress("7.114.19.169", 24)
            addAddress("7.114.19.170", 24)
            addAddress("7.114.19.171", 24)
            addAddress("7.114.19.172", 24)
            addAddress("7.114.19.173", 24)
            addAddress("7.114.19.174", 24)
            addAddress("7.114.19.175", 24)
            addAddress("7.114.19.176", 24)
            addAddress("7.114.19.177", 24)
            addAddress("7.114.19.178", 24)
            addAddress("7.114.19.179", 24)
            addAddress("7.114.19.180", 24)
            addAddress("7.114.19.181", 24)
            addAddress("7.114.19.182", 24)
            addAddress("7.114.19.183", 24)
            addAddress("7.114.19.184", 24)
            addAddress("7.114.19.185", 24)
            addAddress("7.114.19.186", 24)
            addAddress("7.114.19.187", 24)
            addAddress("7.114.19.188", 24)
            addAddress("7.114.19.189", 24)
            addAddress("7.114.19.190", 24)
            addAddress("7.114.19.191", 24)
            addAddress("7.114.19.192", 24)
            addAddress("7.114.19.193", 24)
            addAddress("7.114.19.194", 24)
            addAddress("7.114.19.195", 24)
            addAddress("7.114.19.196", 24)
            addAddress("7.114.19.197", 24)
            addAddress("7.114.19.198", 24)
            addAddress("7.114.19.199", 24)
            addAddress("7.114.19.200", 24)
            addAddress("7.114.19.201", 24)
            addAddress("7.114.19.202", 24)
            addAddress("7.114.19.203", 24)
            addAddress("7.114.19.204", 24)
            addAddress("7.114.19.205", 24)
            addAddress("7.114.19.206", 24)
            addAddress("7.114.19.207", 24)
            addAddress("7.114.19.208", 24)
            addAddress("7.114.19.209", 24)
            addAddress("7.114.19.210", 24)
            addAddress("7.114.19.211", 24)
            addAddress("7.114.19.212", 24)
            addAddress("7.114.19.213", 24)
            addAddress("7.114.19.214", 24)
            addAddress("7.114.19.215", 24)
            addAddress("7.114.19.216", 24)
            addAddress("7.114.19.217", 24)
            addAddress("7.114.19.218", 24)
            addAddress("7.114.19.219", 24)
            addAddress("7.114.19.220", 24)
            addAddress("7.114.19.221", 24)
            addAddress("7.114.19.222", 24)
            addAddress("7.114.19.223", 24)
            addAddress("7.114.19.224", 24)
            addAddress("7.114.19.225", 24)
            addAddress("7.114.19.226", 24)
            addAddress("7.114.19.227", 24)
            addAddress("7.114.19.228", 24)
            addAddress("7.114.19.229", 24)
            addAddress("7.114.19.230", 24)
            addAddress("7.114.19.231", 24)
            addAddress("7.114.19.232", 24)
            addAddress("7.114.19.233", 24)
            addAddress("7.114.19.234", 24)
            addAddress("7.114.19.235", 24)
            addAddress("7.114.19.236", 24)
            addAddress("7.114.19.237", 24)
            addAddress("7.114.19.238", 24)
            addAddress("7.114.19.239", 24)
            addAddress("7.114.19.240", 24)
            addAddress("7.114.19.241", 24)
            addAddress("7.114.19.242", 24)
            addAddress("7.114.19.243", 24)
            addAddress("7.114.19.244", 24)
            addAddress("7.114.19.245", 24)
            addAddress("7.114.19.246", 24)
            addAddress("7.114.19.247", 24)
            addAddress("7.114.19.248", 24)
            addAddress("7.114.19.249", 24)
            addAddress("7.114.19.250", 24)
            addAddress("7.114.19.251", 24)
            addAddress("7.114.19.252", 24)
            addAddress("7.114.19.253", 24)
            addAddress("7.114.19.254", 24)


            addAddress("172.19.0.3", 24)
            addAddress("172.19.0.4", 24)
            addAddress("172.19.0.5", 24)
            addAddress("172.19.0.6", 24)
            addAddress("172.19.0.7", 24)
            addAddress("172.19.0.8", 24)
            addAddress("172.19.0.9", 24)
            addAddress("172.19.0.10", 24)
            addAddress("172.19.0.11", 24)
            addAddress("172.19.0.12", 24)
            addAddress("172.19.0.13", 24)
            addAddress("172.19.0.14", 24)
            addAddress("172.19.0.15", 24)
            addAddress("172.19.0.16", 24)
            addAddress("172.19.0.17", 24)
            addAddress("172.19.0.18", 24)
            addAddress("172.19.0.19", 24)
            addAddress("172.19.0.20", 24)
            addAddress("172.19.0.21", 24)
            addAddress("172.19.0.22", 24)
            addAddress("172.19.0.23", 24)
            addAddress("172.19.0.24", 24)
            addAddress("172.19.0.25", 24)
            addAddress("172.19.0.26", 24)
            addAddress("172.19.0.27", 24)
            addAddress("172.19.0.28", 24)
            addAddress("172.19.0.29", 24)
            addAddress("172.19.0.30", 24)
            addAddress("172.19.0.31", 24)
            addAddress("172.19.0.32", 24)
            addAddress("172.19.0.33", 24)
            addAddress("172.19.0.34", 24)
            addAddress("172.19.0.35", 24)
            addAddress("172.19.0.36", 24)
            addAddress("172.19.0.37", 24)
            addAddress("172.19.0.38", 24)
            addAddress("172.19.0.39", 24)
            addAddress("172.19.0.40", 24)
            addAddress("172.19.0.41", 24)
            addAddress("172.19.0.42", 24)
            addAddress("172.19.0.43", 24)
            addAddress("172.19.0.44", 24)
            addAddress("172.19.0.45", 24)
            addAddress("172.19.0.46", 24)
            addAddress("172.19.0.47", 24)
            addAddress("172.19.0.48", 24)
            addAddress("172.19.0.49", 24)
            addAddress("172.19.0.50", 24)
            addAddress("172.19.0.51", 24)
            addAddress("172.19.0.52", 24)
            addAddress("172.19.0.53", 24)
            addAddress("172.19.0.54", 24)
            addAddress("172.19.0.55", 24)
            addAddress("172.19.0.56", 24)
            addAddress("172.19.0.57", 24)
            addAddress("172.19.0.58", 24)
            addAddress("172.19.0.59", 24)
            addAddress("172.19.0.60", 24)
            addAddress("172.19.0.61", 24)
            addAddress("172.19.0.62", 24)
            addAddress("172.19.0.63", 24)
            addAddress("172.19.0.64", 24)
            addAddress("172.19.0.65", 24)
            addAddress("172.19.0.66", 24)
            addAddress("172.19.0.67", 24)
            addAddress("172.19.0.68", 24)
            addAddress("172.19.0.69", 24)
            addAddress("172.19.0.70", 24)
            addAddress("172.19.0.71", 24)
            addAddress("172.19.0.72", 24)
            addAddress("172.19.0.73", 24)
            addAddress("172.19.0.74", 24)
            addAddress("172.19.0.75", 24)
            addAddress("172.19.0.76", 24)
            addAddress("172.19.0.77", 24)
            addAddress("172.19.0.78", 24)
            addAddress("172.19.0.79", 24)
            addAddress("172.19.0.80", 24)
            addAddress("172.19.0.81", 24)
            addAddress("172.19.0.82", 24)
            addAddress("172.19.0.83", 24)
            addAddress("172.19.0.84", 24)
            addAddress("172.19.0.85", 24)
            addAddress("172.19.0.86", 24)
            addAddress("172.19.0.87", 24)
            addAddress("172.19.0.88", 24)
            addAddress("172.19.0.89", 24)
            addAddress("172.19.0.90", 24)
            addAddress("172.19.0.91", 24)
            addAddress("172.19.0.92", 24)
            addAddress("172.19.0.93", 24)
            addAddress("172.19.0.94", 24)
            addAddress("172.19.0.95", 24)
            addAddress("172.19.0.96", 24)
            addAddress("172.19.0.97", 24)
            addAddress("172.19.0.98", 24)
            addAddress("172.19.0.99", 24)
            addAddress("172.19.0.100", 24)
            addAddress("172.19.0.101", 24)
            addAddress("172.19.0.102", 24)
            addAddress("172.19.0.103", 24)
            addAddress("172.19.0.104", 24)
            addAddress("172.19.0.105", 24)
            addAddress("172.19.0.106", 24)
            addAddress("172.19.0.107", 24)
            addAddress("172.19.0.108", 24)
            addAddress("172.19.0.109", 24)
            addAddress("172.19.0.110", 24)
            addAddress("172.19.0.111", 24)
            addAddress("172.19.0.112", 24)
            addAddress("172.19.0.113", 24)
            addAddress("172.19.0.114", 24)
            addAddress("172.19.0.115", 24)
            addAddress("172.19.0.116", 24)
            addAddress("172.19.0.117", 24)
            addAddress("172.19.0.118", 24)
            addAddress("172.19.0.119", 24)
            addAddress("172.19.0.120", 24)
            addAddress("172.19.0.121", 24)
            addAddress("172.19.0.122", 24)
            addAddress("172.19.0.123", 24)
            addAddress("172.19.0.124", 24)
            addAddress("172.19.0.125", 24)
            addAddress("172.19.0.126", 24)
            addAddress("172.19.0.127", 24)
            addAddress("172.19.0.128", 24)
            addAddress("172.19.0.129", 24)
            addAddress("172.19.0.130", 24)
            addAddress("172.19.0.131", 24)
            addAddress("172.19.0.132", 24)
            addAddress("172.19.0.133", 24)
            addAddress("172.19.0.134", 24)
            addAddress("172.19.0.135", 24)
            addAddress("172.19.0.136", 24)
            addAddress("172.19.0.137", 24)
            addAddress("172.19.0.138", 24)
            addAddress("172.19.0.139", 24)
            addAddress("172.19.0.140", 24)
            addAddress("172.19.0.141", 24)
            addAddress("172.19.0.142", 24)
            addAddress("172.19.0.143", 24)
            addAddress("172.19.0.144", 24)
            addAddress("172.19.0.145", 24)
            addAddress("172.19.0.146", 24)
            addAddress("172.19.0.147", 24)
            addAddress("172.19.0.148", 24)
            addAddress("172.19.0.149", 24)
            addAddress("172.19.0.150", 24)
            addAddress("172.19.0.151", 24)
            addAddress("172.19.0.152", 24)
            addAddress("172.19.0.153", 24)
            addAddress("172.19.0.154", 24)
            addAddress("172.19.0.155", 24)
            addAddress("172.19.0.156", 24)
            addAddress("172.19.0.157", 24)
            addAddress("172.19.0.158", 24)
            addAddress("172.19.0.159", 24)
            addAddress("172.19.0.160", 24)
            addAddress("172.19.0.161", 24)
            addAddress("172.19.0.162", 24)
            addAddress("172.19.0.163", 24)
            addAddress("172.19.0.164", 24)
            addAddress("172.19.0.165", 24)
            addAddress("172.19.0.166", 24)
            addAddress("172.19.0.167", 24)
            addAddress("172.19.0.168", 24)
            addAddress("172.19.0.169", 24)
            addAddress("172.19.0.170", 24)
            addAddress("172.19.0.171", 24)
            addAddress("172.19.0.172", 24)
            addAddress("172.19.0.173", 24)
            addAddress("172.19.0.174", 24)
            addAddress("172.19.0.175", 24)
            addAddress("172.19.0.176", 24)
            addAddress("172.19.0.177", 24)
            addAddress("172.19.0.178", 24)
            addAddress("172.19.0.179", 24)
            addAddress("172.19.0.180", 24)
            addAddress("172.19.0.181", 24)
            addAddress("172.19.0.182", 24)
            addAddress("172.19.0.183", 24)
            addAddress("172.19.0.184", 24)
            addAddress("172.19.0.185", 24)
            addAddress("172.19.0.186", 24)
            addAddress("172.19.0.187", 24)
            addAddress("172.19.0.188", 24)
            addAddress("172.19.0.189", 24)
            addAddress("172.19.0.190", 24)
            addAddress("172.19.0.191", 24)
            addAddress("172.19.0.192", 24)
            addAddress("172.19.0.193", 24)
            addAddress("172.19.0.194", 24)
            addAddress("172.19.0.195", 24)
            addAddress("172.19.0.196", 24)
            addAddress("172.19.0.197", 24)
            addAddress("172.19.0.198", 24)
            addAddress("172.19.0.199", 24)
            addAddress("172.19.0.200", 24)
            addAddress("172.19.0.201", 24)
            addAddress("172.19.0.202", 24)
            addAddress("172.19.0.203", 24)
            addAddress("172.19.0.204", 24)
            addAddress("172.19.0.205", 24)
            addAddress("172.19.0.206", 24)
            addAddress("172.19.0.207", 24)
            addAddress("172.19.0.208", 24)
            addAddress("172.19.0.209", 24)
            addAddress("172.19.0.210", 24)
            addAddress("172.19.0.211", 24)
            addAddress("172.19.0.212", 24)
            addAddress("172.19.0.213", 24)
            addAddress("172.19.0.214", 24)
            addAddress("172.19.0.215", 24)
            addAddress("172.19.0.216", 24)
            addAddress("172.19.0.217", 24)
            addAddress("172.19.0.218", 24)
            addAddress("172.19.0.219", 24)
            addAddress("172.19.0.220", 24)
            addAddress("172.19.0.221", 24)
            addAddress("172.19.0.222", 24)
            addAddress("172.19.0.223", 24)
            addAddress("172.19.0.224", 24)
            addAddress("172.19.0.225", 24)
            addAddress("172.19.0.226", 24)
            addAddress("172.19.0.227", 24)
            addAddress("172.19.0.228", 24)
            addAddress("172.19.0.229", 24)
            addAddress("172.19.0.230", 24)
            addAddress("172.19.0.231", 24)
            addAddress("172.19.0.232", 24)
            addAddress("172.19.0.233", 24)
            addAddress("172.19.0.234", 24)
            addAddress("172.19.0.235", 24)
            addAddress("172.19.0.236", 24)
            addAddress("172.19.0.237", 24)
            addAddress("172.19.0.238", 24)
            addAddress("172.19.0.239", 24)
            addAddress("172.19.0.240", 24)
            addAddress("172.19.0.241", 24)
            addAddress("172.19.0.242", 24)
            addAddress("172.19.0.243", 24)
            addAddress("172.19.0.244", 24)
            addAddress("172.19.0.245", 24)
            addAddress("172.19.0.246", 24)
            addAddress("172.19.0.247", 24)
            addAddress("172.19.0.248", 24)
            addAddress("172.19.0.249", 24)
            addAddress("172.19.0.250", 24)
            addAddress("172.19.0.251", 24)
            addAddress("172.19.0.252", 24)
            addAddress("172.19.0.253", 24)
            addAddress("172.19.0.254", 24)


            // Route
            if (store.bypassPrivateNetwork) {
                resources.getStringArray(R.array.bypass_private_route).map(::parseCIDR).forEach {
                    addRoute(it.ip, it.prefix)
                }

                // Route of virtual DNS
                addRoute(TUN_DNS, 32)
            } else {
                addRoute(NET_ANY, 0)
            }

            // Access Control
            when (store.accessControlMode) {
                AccessControlMode.AcceptAll -> Unit
                AccessControlMode.AcceptSelected -> {
                    (store.accessControlPackages + packageName).forEach {
                        runCatching { addAllowedApplication(it) }
                    }
                }
                AccessControlMode.DenySelected -> {
                    (store.accessControlPackages - packageName).forEach {
                        runCatching { addDisallowedApplication(it) }
                    }
                }
            }

            // Blocking
            setBlocking(false)

            // Mtu
            setMtu(TUN_MTU)

            // Session Name
            setSession("Clash")

            // Virtual Dns Server
            addDnsServer(TUN_DNS)

            // Open MainActivity
            setConfigureIntent(
                PendingIntent.getActivity(
                    self,
                    R.id.nf_vpn_status,
                    Intent().setComponent(Components.MAIN_ACTIVITY),
                    pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT)
                )
            )

            // Metered
            if (Build.VERSION.SDK_INT >= 29) {
                setMetered(false)
            }

            // System Proxy
            if (Build.VERSION.SDK_INT >= 29 && store.systemProxy) {
                listenHttp()?.let {
                    setHttpProxy(
                        ProxyInfo.buildDirectProxy(
                            it.address.hostAddress,
                            it.port,
                            HTTP_PROXY_BLACK_LIST + if (store.bypassPrivateNetwork) HTTP_PROXY_LOCAL_LIST else emptyList()
                        )
                    )
                }
            }

            if (store.allowBypass) {
                allowBypass()
            }

            TunModule.TunDevice(
                fd = establish()?.detachFd()
                    ?: throw NullPointerException("Establish VPN rejected by system"),
                gateway = "$TUN_GATEWAY/$TUN_SUBNET_PREFIX",
                portal = TUN_PORTAL,
                dns = if (store.dnsHijacking) NET_ANY else TUN_DNS,
            )
        }

        attach(device)
    }

    companion object {
        private const val TUN_MTU = 9000
        // private const val TUN_SUBNET_PREFIX = 30
        private const val TUN_SUBNET_PREFIX = 24
        private const val TUN_GATEWAY = "172.19.0.1"
        private const val TUN_PORTAL = "172.19.0.2"
        private const val TUN_DNS = TUN_PORTAL
        private const val NET_ANY = "0.0.0.0"

        private val HTTP_PROXY_LOCAL_LIST: List<String> = listOf(
            "localhost",
            "*.local",
            "127.*",
            "10.*",
            "172.16.*",
            "172.17.*",
            "172.18.*",
            "172.19.*",
            "172.2*",
            "172.30.*",
            "172.31.*",
            "192.168.*"
        )
        private val HTTP_PROXY_BLACK_LIST: List<String> = listOf(
            "*zhihu.com",
            "*zhimg.com",
            "*jd.com",
            "100ime-iat-api.xfyun.cn",
            "*360buyimg.com",
        )
    }
}
