package com.sham.neopad.service


import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ext.SdkExtensions
import com.sham.neopad.appLog
import com.sham.neopad.model.DiscoveredConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.Executors


class ServiceScanner(
    context: Context,
    private val serviceType: String
) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5000L       // every 5 seconds
    private val serviceTimeout = 30000L     // 30 seconds before expiration

    private val _availConnections = MutableStateFlow<List<DiscoveredConnection>>(emptyList())
    val availableConnections = _availConnections.asStateFlow()

    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val discoveredServices = mutableMapOf<String, DiscoveredConnection>()

    private val cleanupRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val iterator = discoveredServices.iterator()
            var updated = false

            for ((name, conn) in iterator) {
                if (now >= conn.expiresAt) {
                    iterator.remove()
                    updated = true
                    appLog("[mDNS] Timeout expired, removing: $name")
                }
            }

            if (updated) {
                _availConnections.value = discoveredServices.values.toList()
            }

            handler.postDelayed(this, checkInterval)
        }
    }

    fun startDiscovery() {
        stopDiscovery() // reset any old state

        handler.postDelayed(cleanupRunnable, checkInterval)

        val executor = Executors.newSingleThreadExecutor()

        discoveryListener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                appLog("[mDNS] Discovery started for $regType")
            }

            override fun onServiceFound(service: NsdServiceInfo?) {
                if (service == null || !service.serviceType.contains(serviceType)) return
                val name = service.serviceName

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7) {

                    nsdManager.registerServiceInfoCallback(service, executor,
                        object : NsdManager.ServiceInfoCallback {
                            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {}
                            override fun onServiceInfoCallbackUnregistered() {}
                            override fun onServiceLost() {
                                //appLog("[mDNS] 2. Service lost (debounced): $name")
                                markLost(name)
                            }

                            override fun onServiceUpdated(info: NsdServiceInfo) {
                                val address = info.hostAddresses
                                    .firstOrNull { it is Inet4Address } as Inet4Address? ?: return
                                val port = info.port
                                val sBytes = info.attributes["sessionId"] ?: return
                                val timestamp = info.attributes["timestamp"]?.decodeToString() ?: "Unknown Timestamp"
                                val sessionId = sBytes.decodeToString()
                                appLog("Service name : $name Timestamp : $timestamp")
                                addOrUpdateConnection(name, sessionId, address, port)
                                nsdManager.unregisterServiceInfoCallback(this)
                            }
                        })
                } else {
                    @Suppress("DEPRECATION")
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onServiceResolved(info: NsdServiceInfo) {
                            val name = info.serviceName
                            val address = info.host
                            val port = info.port
                            val sBytes = info.attributes["sessionId"] ?: return
                            val sessionId = sBytes.toString()
                            addOrUpdateConnection(name, sessionId, address, port)
                        }

                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            appLog("[mDNS] Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                        }
                    })
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                markLost(service.serviceName)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                //appLog("[mDNS] Discovery stopped for $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                appLog("[mDNS] Start discovery failed: $errorCode")
                stopDiscovery()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                appLog("[mDNS] Stop discovery failed: $errorCode")
                stopDiscovery()
            }
        }

        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopDiscovery() {
        handler.removeCallbacks(cleanupRunnable)
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                appLog("[mDNS] Stop discovery error: ${e.message}")
            }
        }
        discoveryListener = null
        discoveredServices.clear()
        _availConnections.value = emptyList()
    }

    private fun addOrUpdateConnection(
        name: String,
        sessionId: String,
        address: InetAddress,
        port: Int
    ) {
        val expiresAt = Long.MAX_VALUE
        val conn = DiscoveredConnection(sessionId, address, port, name, expiresAt)
        discoveredServices[name] = conn
        _availConnections.value = discoveredServices.values.toList()
    }

    private fun markLost(name: String) {
        val existing = discoveredServices[name]
        if (existing != null) {
            discoveredServices[name] = existing.copy(expiresAt = System.currentTimeMillis() + serviceTimeout)
            _availConnections.value = discoveredServices.values.toList()
        }
    }

}