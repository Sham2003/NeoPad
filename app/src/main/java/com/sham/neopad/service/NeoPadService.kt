// NeoPadService.kt
package com.sham.neopad.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.sham.neopad.MainActivity
import com.sham.neopad.R
import com.sham.neopad.appError
import com.sham.neopad.appLog
import com.sham.neopad.model.ConnectionInfo
import com.sham.neopad.model.ConnectionStatus
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.DiscoveredConnection
import com.sham.neopad.model.SettingStore
import com.sham.neopad.model.VirtualController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class NeoPadService : Service() {
    companion object {
        const val CHANNEL_ID = "neopad_service_channel"
        const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "neopad_connection"
        private const val KEY_URL = "connection_url"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_SESSION_TOKEN = "session_token"
        const val ACTION_STOP_SERVICE = "com.sham.neopad.ACTION_STOP_SERVICE"
        const val ACTION_EXIT_APP = "com.sham.neopad.ACTION_EXIT_APP"
        private const val SERVICE_TYPE = "_neopad._tcp"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val binder = NeoPadBinder()

    private lateinit var prefs: SharedPreferences

    private val deviceId : String
        get() = getOrCreateDeviceId()

    var username : String
        get() = getMyUsername()
        set(value) = setMyUsername(value)

    var settings : SettingStore
        get() = loadSettingsFromPrefs()
        set(value) = saveSettingsToPrefs(value)

    private var serviceState: ServiceState? = null

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectionInfo = MutableStateFlow<ConnectionInfo>(ConnectionInfo())
    val connectionInfo: StateFlow<ConnectionInfo> = _connectionInfo.asStateFlow()

    private val _gamepadStatus = MutableStateFlow<List<VirtualController>>(emptyList())
    val gamepadSnapshot = _gamepadStatus.asStateFlow()



    private lateinit var serviceScanner: ServiceScanner

    val availableConnections : StateFlow<List<DiscoveredConnection>>
        get() = serviceScanner.availableConnections

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        serviceScanner = ServiceScanner(this,SERVICE_TYPE)
        prefs = applicationContext.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val url = prefs.getString(KEY_URL, null)
        val sessionId = prefs.getString(KEY_SESSION_ID, null)
        val sessionToken = prefs.getString(KEY_SESSION_TOKEN, null)

        if (url != null && sessionId != null &&  sessionToken != null) {
            appLog("Connecting to earlier connection")
            serviceScope.launch {
                delay(500) // Small delay to ensure service is fully initialized
                connectInternal(url, sessionId,sessionToken)
            }
        }

        serviceScanner.startDiscovery()
    }


    private suspend fun connectInternal(
        url: String,
        sessionId: String,
        sessionToken: String? = null
    ) {
        delay(1000L)
        withContext(Dispatchers.Main) {
            _connectionStatus.value = ConnectionStatus.Connecting
            updateNotification("Connecting...")
        }

        try {
            // Initialize service state with managers
            appLog("Creating service state")
            val state = ServiceState(
                hubUrl = url,
                sessionId = sessionId,
                username = username,
                deviceId = deviceId,
                serviceScope = serviceScope,
                onTokenReceived = { token ->
                    saveConnectionParams(url,sessionId,token)
                },
                onConnected = { info ->
                    _connectionInfo.value = info
                    _connectionStatus.value = ConnectionStatus.Connected
                    updateNotification("Connected")
                },
                onConnectionFailed = {
                    _connectionInfo.value = ConnectionInfo()
                    _connectionStatus.value = ConnectionStatus.Error("Failed")
                    serviceState = null
                    clearConnectionParams()
                    updateNotification("Connection Failed Failed")
                },
                onDisconnected = {
                    _connectionInfo.value = ConnectionInfo()
                    _connectionStatus.value = ConnectionStatus.Disconnected
                    serviceState = null
                    clearConnectionParams()
                    updateNotification("Disconnected")
                },
                onGamepadChange = {
                    _gamepadStatus.value = it
                },
                existingToken = sessionToken,
                softTrigger = settings.softTrigger
            )

            serviceState = state

            // Build and start SignalR connection
            state.connect()
            appLog("Trying to connect")

        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.Error("Connection failed")
            appError("Connection failed",e)
            updateNotification("Connection failed")
        }
    }


    private fun clearConnectionParams() {
        prefs.edit {
            remove(KEY_URL)
            remove(KEY_SESSION_ID)
            remove(KEY_SESSION_TOKEN)
        }
    }

    private fun saveConnectionParams(url: String, sessionId: String, sessionToken: String) {
        prefs.edit {
            putString(KEY_URL, url)
            putString(KEY_SESSION_ID, sessionId)
            putString(KEY_SESSION_TOKEN, sessionToken)
        }
    }


    // Binder for activity communication
    inner class NeoPadBinder : Binder() {
        fun getService(): NeoPadService = this@NeoPadService
    }

    fun connectToService(info: DiscoveredConnection) {
        if (connectionStatus == ConnectionStatus.Connected) {
            closeConnection()
        }

        val targetIp = if (isRunningOnEmulator()) {
            appLog("Detected emulator switching to 10.0.2.2")
            "localhost"
        } else {
            info.address.hostAddress
        }

        val port = info.port
        val url = "http://$targetIp:$port/pcxhub"
        serviceScope.launch {
            delay(200L)
            connectInternal(url,info.sessionId)
        }
    }

    fun closeConnection() {
        clearConnectionParams()
        if (serviceState?.isConnected == true) serviceState?.closeConnection()
        serviceState = null
        _connectionStatus.value = ConnectionStatus.Disconnected
        updateNotification("Closed Connection")

        serviceScope.launch {
            delay(3000L)
            if (_connectionStatus.value != ConnectionStatus.Connected && _connectionStatus.value != ConnectionStatus.Connecting)
                _connectionStatus.value = ConnectionStatus.NotConnected
        }
    }


    // Public API for MainActivity
    fun addGamepad(name: String ,type: ControllerType,layoutId: String, callback: (Result<Unit>) -> Unit) {
        appLog("Add gamepad $name - $type $layoutId")
        serviceState?.addGamepad(name,type, layoutId, callback) ?: callback(
            Result.failure(IllegalStateException("Not connected"))
        )
    }

    fun deleteGamepad(cid: String , callback: (Result<Unit>) -> Unit) {
        serviceState?.deleteGamepad(cid , callback) ?: callback(
            Result.failure(IllegalStateException("Not connected"))
        )
    }

    fun getGamepadObject(cid: String): GamepadInput {
        if (connectionStatus.value != ConnectionStatus.Connected)
            throw Exception("Not even connected")

        return serviceState?.inputManager?.getGamepad(cid) ?: throw Exception("No gamepad")
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "NeoPad Connection Service",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Maintains connection to PC server"
            setShowBadge(false)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }



    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun getOrCreateDeviceId(): String {
        val existingId = prefs.getString(KEY_DEVICE_ID, null)
        appLog("Getting earlier Device id = $existingId")

        if (!existingId.isNullOrEmpty()) return existingId

        val newId = UUID.randomUUID().toString()
        appLog("Null so creating new id = $newId")
        prefs.edit(commit = true) { putString(KEY_DEVICE_ID, newId) }
        return newId
    }


    private fun getMyUsername(): String {
        val username = prefs.getString(KEY_USERNAME,"Unknown")
        return username ?: "Unknown"
    }

    private fun setMyUsername(u: String) {
        prefs.edit(commit = true) {
            putString(KEY_USERNAME,u)
        }
        serviceState?.updateUsername(u)
    }

    private fun saveSettingsToPrefs(settingsData: SettingStore) {
        prefs.edit().apply {
            putBoolean("hapticFeedback", settingsData.hapticFeedback)
            putInt("animationDuration", settingsData.animationDuration)
            putFloat("steeringRange", settingsData.steeringRange)
            putFloat("glowWidth", settingsData.glowWidth)
            putFloat("buttonThreshold", settingsData.buttonThreshold)
            putFloat("neutralAzimuth",settingsData.neutralAzimuth)
            putFloat("neutralPitch",settingsData.neutralPitch)
            putFloat("neutralRoll",settingsData.neutralRoll)
            apply()
        }
    }

    fun loadSettingsFromPrefs(): SettingStore {
        return SettingStore(
            hapticFeedback = prefs.getBoolean("hapticFeedback", true),
            animationDuration = prefs.getInt("animationDuration", 100),
            steeringRange = prefs.getFloat("steeringRange", 50f),
            glowWidth = prefs.getFloat("glowWidth", 10f),
            buttonThreshold = prefs.getFloat("buttonThreshold", 0.5f),
            neutralAzimuth = prefs.getFloat("neutralAzimuth",0f),
            neutralPitch = prefs.getFloat("neutralPitch",0f),
            neutralRoll = prefs.getFloat("neutralRoll",0f),
        )
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            val closeIntent = Intent(ACTION_EXIT_APP)
            sendBroadcast(closeIntent)

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundService()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        closeConnection()
        serviceScanner.stopDiscovery()
        serviceScope.cancel()
    }


    private fun startForegroundService() {
        val notification = createNotification("NeoPad Service Running")

        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun createNotification(contentText: String = "Tap stop to end it."): Notification {

        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, this::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NeoPad Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_gamepad)
            .setContentIntent(mainPendingIntent) // opens main activity on tap
            .addAction(android.R.drawable.ic_menu_delete, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }


    fun isRunningOnEmulator(): Boolean {
        val buildProps = listOf(
            Build.FINGERPRINT,
            Build.MODEL,
            Build.MANUFACTURER,
            Build.BRAND,
            Build.DEVICE,
            Build.PRODUCT,
            Build.HARDWARE
        ).joinToString(" ")

        return buildProps.contains("generic", ignoreCase = true)
                || buildProps.contains("sdk_gphone", ignoreCase = true)
                || buildProps.contains("emulator", ignoreCase = true)
                || Build.HARDWARE.contains("goldfish", ignoreCase = true)
                || Build.HARDWARE.contains("ranchu", ignoreCase = true)
    }
}

