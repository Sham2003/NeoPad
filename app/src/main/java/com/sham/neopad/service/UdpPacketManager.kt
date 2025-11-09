// UdpPacketManager.kt
package com.sham.neopad.service

import com.sham.neopad.appError
import com.sham.neopad.appLog
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap


class UdpPacketManager(
    private val hubUrl: String,
    private val inputManager: InputManager,
    private val scope: CoroutineScope
) {

    companion object {
        private const val SEND_INTERVAL_MS = 8L // ~120Hz update rate
    }

    @Volatile
    private var isRunning = false

    val hasNotStarted : Boolean
        get() = !isRunning
    private var senderJob: Job? = null
    private val socket: DatagramSocket = DatagramSocket()
    private val serverAddress: InetAddress
        get() {
            val baseUrl = if (hubUrl.contains("/pcxhub")) {
                hubUrl.substringBefore("/pcxhub")
            } else {
                hubUrl
            }
            val host = extractHostFromUrl(baseUrl)
            return InetAddress.getByName(host)
        }
    private var udpPort : Int = -1

    private val pastPayloads: MutableMap<String, ByteArray> = ConcurrentHashMap()


    fun startWithPort(port : Int) {
        if (isRunning) return
        udpPort = port
        try {
            appLog("[UDP Manager] Created datagram socket for $serverAddress and $udpPort")
            isRunning = true

            senderJob = scope.launch(Dispatchers.IO) {
                appLog("[UDP Manager] Run Sender loop")
                runSenderLoop()
            }
        } catch (e: Exception) {
            appError("Error in starting udp manager ",e)
            isRunning = false
            socket.close()
        }
    }


    fun stop() {
        isRunning = false
        senderJob?.cancel()
        senderJob = null
        socket.close()
    }

    /**
     * Main sender loop - continuously sends input data without delay
     */
    private suspend fun runSenderLoop() {
        while (isRunning && !senderJob!!.isCancelled) {
            try {
                sendAllGamepadData()
                delay(SEND_INTERVAL_MS) // Small delay for ~120Hz
            } catch (e: SocketException) {
                appError("Socket exception in run sender loop",e)
                break
            } catch (e: Exception) {
                appError("Error in run loop",e)
                delay(100)
            }
        }
    }


    private fun sendAllGamepadData() {
        val sock = socket
        val address = serverAddress

        val gamepads = inputManager.getAllGamepads()

        for ((_, gamepad) in gamepads) {
            try {
                val data = gamepad.toByteArray()
                val lastPayload = pastPayloads[gamepad.clientRefId]

                if (lastPayload == null || !lastPayload.contentEquals(data)) {
                    appLog("Sending payload of size ${data.size} data = ${data.decodeToString()}")
                    val packet = DatagramPacket(data, data.size, address, udpPort)
                    sock.send(packet)
                    pastPayloads[gamepad.clientRefId] = data
                }
            } catch (e: Exception) {
                appError("Error in sending data",e)
                continue
            }
        }
    }


    private fun extractHostFromUrl(url: String): String {
        var host = url.replace("http://", "").replace("https://", "")

        val pathIndex = host.indexOf('/')
        if (pathIndex != -1) {
            host = host.substring(0, pathIndex)
        }

        val portIndex = host.indexOf(':')
        if (portIndex != -1) {
            host = host.substring(0, portIndex)
        }

        return host
    }
}