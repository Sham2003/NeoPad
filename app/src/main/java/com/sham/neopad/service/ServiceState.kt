// ServiceState.kt
package com.sham.neopad.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.sham.neopad.appError
import com.sham.neopad.appLog
import com.sham.neopad.model.ClosingStatus
import com.sham.neopad.model.ConnectionInfo
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.VirtualController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import java.util.Collections


class ServiceState(
    private val hubUrl: String,
    private val sessionId: String,
    private var username: String,
    private val deviceId: String,
    private val serviceScope: CoroutineScope,
    private val onTokenReceived: (String) -> Unit,
    private val onGamepadChange: (List<VirtualController>) -> Unit,
    private val existingToken : String? = null,
    private val onConnected: (ConnectionInfo) -> Unit = {},
    private val onDisconnected: () -> Unit = {},
    private val onConnectionFailed: () -> Unit = {},
    softTrigger : Boolean = false
) {

    @Volatile
    private lateinit var hubConnection: HubConnection

    private val gamepadList = Collections.synchronizedList(mutableListOf<VirtualController>())

    val inputManager = InputManager(softTrigger)

    private val udpManager: UdpPacketManager = UdpPacketManager(hubUrl, inputManager, serviceScope)

    @Volatile
    private var computerName: String = ""

    @Volatile
    private var udpPort: Int = 0

    val isConnected: Boolean
        get() = hubConnection.connectionState == HubConnectionState.CONNECTED



    @Volatile
    private var closingState = ClosingStatus.NONE

    fun connect() {
        val hubHeaders = HashMap<String, String>().apply {
            put(SESSION_ID_HEADER,sessionId)
            put(DEVICE_ID_HEADER,deviceId)
            put(USERNAME_HEADER,username)
            if (existingToken != null)
                put(SESSION_TOKEN_HEADER,existingToken)
        }


        val hub = createHub(hubUrl, hubHeaders)
        appLog("Hib is connecting to $hubUrl ")
        registerHandlers(hub)

        hub.onClosed { exception ->
            appLog("On Closed")
            if (closingState == ClosingStatus.NONE){
                clearAllObjects()
                onDisconnected()
            }
        }


        hubConnection = hub

        try {
            hub.start().blockingAwait()
        } catch (e: Exception) {
            closingState = ClosingStatus.HUB
            clearAllObjects()
            onConnectionFailed()
            appError("Failed to start hub",e)
        }
    }

    private fun registerHandlers(hub: HubConnection) {
        hub.on(
            "Connected",
            { data: Map<String, Any> ->
                handleConnected(data)
            },
            Map::class.java
        )

        hub.on("ClearGamepad") { appLog("Cleared by Server"); clearGamepads() }


        hub.on("Removed") { appLog("Removed by Server") ; clearAllObjects() ; onDisconnected() }


        hub.on("ConnectionFailed", { errorMessage: String ->
            handleError(errorMessage)
        }, String::class.java)
    }

    private fun handleConnected(data: Map<String, Any>) {
        try {
            appLog("Data  : $data")
            val reconnected = data["reconnected"] as? Boolean == true
            val newToken = data["token"] as? String ?: return
            val computer = data["computer"] as? String ?: ""
            val port = (data["port"] as? Number)?.toInt() ?: 0
            @Suppress("UNCHECKED_CAST")
            val gamepads = (data["gamepads"] as? List<Map<String, Any>>) ?: emptyList()

            onTokenReceived(newToken)
            computerName = computer
            udpPort = port

            onConnected(ConnectionInfo(sessionId, hubUrl, computerName, username))

            if (reconnected)
                appLog("Reconnected successfully")

            // Synchronize gamepad list
            synchronized(gamepadList) {
                gamepadList.clear()
                appLog("Clearing existing data")
                gamepads.forEach { gamepadData ->
                    // Type comes as integer (0 = PS4, 1 = XBOX)
                    appLog("Creating virtual controller for $gamepadData")
                    val typeValue = (gamepadData["Type"] as? Number)?.toInt() ?: return@forEach
                    val type = when (typeValue) {
                        0 -> ControllerType.PS4
                        1 -> ControllerType.XBOX
                        else -> return@forEach
                    }
                    val id = (gamepadData["Id"] as? Number)?.toInt() ?: return@forEach
                    val clientRefId = gamepadData["ClientRefId"] as? String ?: return@forEach
                    val name = gamepadData["Name"] as? String ?: ""
                    val layoutId = gamepadData["LayoutId"] as? String ?: ""

                    val virtualController = VirtualController(
                        type = type,
                        clientRefId = clientRefId,
                        id = id,
                        name = name,
                        layoutId = layoutId
                    )
                    appLog("Adding gamepad $virtualController")
                    gamepadList.add(virtualController)

                    // Initialize input manager for this gamepad
                    inputManager.initializeGamepad(id, clientRefId, type)
                }
                appLog("Changing gamepad state inside service state")
                onGamepadChange(ArrayList(gamepadList))
            }


            // Start UDP manager if we have gamepads and port
            if (gamepadList.isNotEmpty() && udpManager.hasNotStarted) {
                udpManager.startWithPort(udpPort)
            }

        } catch (e: Exception) {
            appError("Error handling Connected message", e)
        }
    }



    private fun clearGamepads() {
        synchronized(gamepadList) {
            gamepadList.clear()
            onGamepadChange(ArrayList(gamepadList))
            inputManager.clearAll()
            udpManager.stop()
        }
    }
    /**
     * Handles error message from server
     */
    private fun handleError(errorMessage: String) {
        appError("Error from hub : $errorMessage")
        clearAllObjects()
        closingState = ClosingStatus.HUB
        onConnectionFailed()
    }



    fun addGamepad(name: String, type: ControllerType, layoutId: String, callback: (Result<Unit>) -> Unit) {
        val hub = hubConnection
        if (!isConnected) {
            callback(Result.failure(IllegalStateException("Not connected")))
            return
        }

        serviceScope.launch {
            try {
                val req = AddGamepadRequest(name, type.ordinal, layoutId)
                appLog("Invoking AddGamepad with $req")
                hub.invoke(String::class.java, "AddGamepad", req)
                    .blockingSubscribe({ json ->
                        val newGamepad = Gson().fromJson(json, GamepadInfo::class.java)
                        synchronized(gamepadList) {
                            gamepadList.add(newGamepad.toVirtualController())
                            inputManager.initializeGamepad(newGamepad.Id, newGamepad.ClientRefId, type)
                            onGamepadChange(ArrayList(gamepadList))
                        }

                        if (udpManager.hasNotStarted) {
                            udpManager.startWithPort(udpPort)
                            appLog("Starting udp manager")
                        }
                        callback(Result.success(Unit))
                    }, { err ->
                        appError("[AddGamepad] ❌ Failed Err = ", err)
                        callback(Result.failure(err))
                    })

            } catch (e: JsonSyntaxException) {
                appError("Gson Error : ",e)
                callback(Result.failure(e))
            } catch (e: Exception) {
                appError("Error creating gamepad", e)
                callback(Result.failure(e))
            }
        }
    }


    fun deleteGamepad(clientRefId: String, callback: (Result<Unit>) -> Unit) {
        val hub = hubConnection
        if (!isConnected) {
            callback(Result.failure(IllegalStateException("Not connected")))
            return
        }

        serviceScope.launch {
            try {
                appLog("Invoking DeleteGamepad with $clientRefId  type = ${clientRefId.javaClass}")
                val result = hub.invoke(Int::class.java,"DeleteGamepad", clientRefId).blockingGet()
                if (result == 200) {
                    synchronized(gamepadList) {
                        gamepadList.removeAll { it.clientRefId == clientRefId }
                        onGamepadChange(ArrayList(gamepadList))
                    }
                    inputManager.removeGamepad(clientRefId)
                    callback(Result.success(Unit))
                } else
                    callback(Result.failure(Exception("Couldn't complete it")))
            } catch (e: Exception) {
                appError("Error deleting gamepad", e)
                callback(Result.failure(e))
            }
        }
    }

    fun updateUsername(u: String) {
        val hub = hubConnection
        if (!isConnected) return

        serviceScope.launch {
            appLog("Invoking SetUsername with $u  type = ${u.javaClass}")
            runCatching {
                hub.invoke("SetUsername",u).subscribe(
                    {
                        appLog("Successfully updated")
                        username = u
                        onConnected(ConnectionInfo(sessionId, hubUrl, computerName, username))
                    },
                    { err ->
                        appError("Error in updating username ",err)
                    }
                )
            }.onFailure {
                appError("Failure in invoking",it)
            }

        }
    }




    fun closeConnection() {
        appLog("Closing")
        closingState = ClosingStatus.SELF
        runCatching {
            if (isConnected)
                hubConnection.invoke("Disconnected")
            hubConnection.stop()
        }.onFailure {
            appLog("Failure to call Disconnected $it")
        }
        clearAllObjects()
    }

    private fun clearAllObjects() {
        runCatching {
            udpManager.stop()
            inputManager.clearAll()
            gamepadList.clear()
            onGamepadChange(ArrayList(gamepadList))
        }.onSuccess {
            appLog("Cleared All Objects")
        }.onFailure {
            appLog("Failure to clear : $it")
        }
    }


    companion object {
        private const val SESSION_ID_HEADER = "X-Session-Id"
        private const val SESSION_TOKEN_HEADER = "X-Session-Token"
        private const val DEVICE_ID_HEADER = "X-Device-Id"
        private const val USERNAME_HEADER = "X-User-Name"
    }

    private fun createHub(url: String,headers : Map<String, String>): HubConnection {
        val logger = LoggerFactory.getLogger("SignalRHTTP")

        // Create OkHttp interceptor to log HTTP + WebSocket traffic
        val httpLogger = HttpLoggingInterceptor { message ->
            logger.info(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Build hub connection
        return HubConnectionBuilder.create(url)
            .setHttpClientBuilderCallback { builder: OkHttpClient.Builder ->

                builder.addInterceptor(httpLogger)

            }
            .withTransport(TransportEnum.LONG_POLLING)
            .withHeaders(headers)
            .build()
    }

    data class AddGamepadRequest(
        val Name: String,
        val CType: Int,
        val LayoutId: String
    )

    data class GamepadInfo(
        val Id: Int,
        val Type: Int,
        val Name: String,
        val ClientRefId: String,
        val LayoutId: String
    ) {
        fun toVirtualController() : VirtualController {
            return VirtualController(
                id = Id,
                type = if (Type == 0) ControllerType.PS4 else ControllerType.XBOX,
                name = Name,
                clientRefId = ClientRefId,
                layoutId = LayoutId
            )
        }
    }

}