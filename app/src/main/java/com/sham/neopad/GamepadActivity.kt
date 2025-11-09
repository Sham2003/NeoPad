package com.sham.neopad

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import com.sham.neopad.layout.DefaultLayoutManager
import com.sham.neopad.misc.OrientationSensorManager
import com.sham.neopad.model.ConnectionStatus
import com.sham.neopad.model.ControllerDTO
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.TiltState
import com.sham.neopad.service.GamepadInput
import com.sham.neopad.service.NeoPadService
import com.sham.neopad.ui.gamepad.GamepadScreen
import com.sham.neopad.ui.theme.NeoPadTheme
import com.sham.neopad.viewmodel.GamepadViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GamepadActivity: ComponentActivity() {
    private val gvm by viewModels<GamepadViewModel>()



    private val orientationManager by lazy {
        OrientationSensorManager(this) { a, p, r  ->
            gvm.updateTilt(a, p, r)
        }
    }


    private val deviceOrientation = MutableStateFlow<TiltState>(TiltState(0f,0f))


    private var bound = false


    private val jobQueue = mutableListOf<Job>()
    private fun clearServiceJobs() {
        jobQueue.forEach { job ->
            runCatching {
                job.cancel()
            }.onFailure {
                appLog("Cancellation Exception $it" )
            }
        }
        jobQueue.clear()
    }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            appLog("Gamepad Activity Service connected")
            val neoPadBinder = binder as NeoPadService.NeoPadBinder
            val svc = neoPadBinder.getService()
            bound = true
            onBoundService(svc)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            appLog("Service disconnected")
            clearServiceJobs()
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gvm.isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        initViewModel()
        Intent(this, NeoPadService::class.java).also { intent ->
            appLog("Binding service $intent")
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
        setContent{
            NeoPadTheme {
                GamepadScreen(gvm)
                LaunchedEffect(Unit) {
                    hideSystemBars()
                }
            }
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        gvm.onClose = {
            finish()
        }
    }

    private fun onBoundService(svc: NeoPadService) {
        lifecycleScope.launch {
            svc.connectionStatus.collect {
                if (it != ConnectionStatus.Connected) finish()
            }
        }.also { jobQueue.add(it) }
        lifecycleScope.launch {
            svc.gamepadSnapshot.collect { controllers ->
                gvm.controllers.clear()
                gvm.controllers.addAll(controllers)
                runCatching {
                    controllers.first{ it.clientRefId == gvm.controllerDTO?.clientRefId}
                }.onFailure {
                    if (it is NoSuchElementException) {
                        Toast.makeText(this@GamepadActivity,"Gamepad deleted", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }

            }
        }

        gvm.onChange = {
            initInputObject(svc)
        }
        initInputObject(svc)
    }

    private fun initInputObject(svc: NeoPadService) {
        runCatching {
            gvm.controllerDTO?.let {
                gvm.gamepad = svc.getGamepadObject(it.clientRefId)
            }
            gvm.tiltSettings = svc.settings
        }.onFailure {
            appError("Couldn't get gamepad",it)
            Toast.makeText(this,"Flawed object", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        gvm.isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
    }


    override fun onStart() {
        super.onStart()
        hideSystemBars()
    }


    private fun initViewModel() {
        loadAllLayouts()
        gvm.controllerDTO = loadDTOFromIntent()

        lifecycleScope.launch {
            deviceOrientation.collect {

            }
        }.also { jobQueue.add(it) }

    }


    override fun onResume() {
        super.onResume()
        orientationManager.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            try {
                unbindService(connection)
            } catch (e: IllegalArgumentException) {
                appError("Service already unbound", e)
            }
            bound = false
        }
        gvm.gamepad = GamepadInput(0,"", ControllerType.PS4)
    }

    override fun onPause() {
        super.onPause()
        orientationManager.stop()
    }

    fun loadDTOFromIntent() : ControllerDTO {
        val dto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra<ControllerDTO>(GPAD_DTO_TAG, ControllerDTO::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<ControllerDTO>(GPAD_DTO_TAG)!!
        }
        return dto
    }

    fun loadAllLayouts() {
        gvm.myLayouts.addAll(DefaultLayoutManager.getAll())
        val lFiles = filesDir.listFiles{ f, name -> name.endsWith(".pad") } ?: return
        if (lFiles.isEmpty()) return
        lFiles.forEach {
            runCatching {
                gvm.myLayouts.add(ControllerLayoutData.fromStream(it.inputStream()))
            }.onFailure { err->
                appError("Cannot Load File ${it.name}" ,err)
            }
        }
    }
}