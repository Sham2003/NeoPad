package com.sham.neopad

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.layout.DefaultLayoutManager
import com.sham.neopad.model.ControllerDTO
import com.sham.neopad.model.MainViewModelEvent
import com.sham.neopad.service.NeoPadService
import com.sham.neopad.service.NeoPadService.Companion.ACTION_EXIT_APP
import com.sham.neopad.ui.theme.NeoPadTheme
import com.sham.neopad.ui.main.MainScreen
import com.sham.neopad.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private val mvm: MainViewModel by viewModels<MainViewModel>()

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


    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {

            val neoPadBinder = binder as NeoPadService.NeoPadBinder
            val svc = neoPadBinder.getService()
            bound = true
            onBoundService(svc)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            appLog("Service disconnected")
            appLog("Clearing service jobs")
            clearServiceJobs()
            bound = false
        }
    }

    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_EXIT_APP) {
                finishAffinity() // closes all activities
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvm.isPortrait = resources.configuration.orientation  == Configuration.ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        Intent(this, NeoPadService::class.java).also { intent ->
            appLog("Binding service $intent")
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
        setContent {
            NeoPadTheme {
                MainScreen(mvm)
            }
        }
    }


    private fun onBoundService(svc: NeoPadService) {
        mvm.username = svc.username
        mvm.settingsData = svc.settings
        lifecycleScope.launch {
            svc.connectionStatus.collect {
                mvm.connectionStatus = it
            }
        }.also { jobQueue.add(it) }
        lifecycleScope.launch {
            svc.gamepadSnapshot.collect {
                mvm.controllers.clear()
                mvm.controllers.addAll(it)
            }
        }.also { jobQueue.add(it) }
        appLog("Collecting info")
        lifecycleScope.launch {
            svc.connectionInfo.collect {
                mvm.connectionInfo = it
            }
        }.also { jobQueue.add(it) }
        appLog("Collecting events")
        lifecycleScope.launch {
            mvm.events.collect { et -> handleVMEvent(et,svc) }
        }.also { jobQueue.add(it) }
    }


    private fun handleVMEvent(et : MainViewModelEvent, svc: NeoPadService) {
        when (et) {
            MainViewModelEvent.Connect -> {
                val intent = Intent(this, PairDeviceActivity::class.java)
                startActivity(intent)
            }
            is MainViewModelEvent.ViewLayout -> {
                val intent = Intent(this, LayoutViewerActivity::class.java)
                intent.putExtra(VIEW_DTO_TAG,et.dto)
                startActivity(intent)
            }
            is MainViewModelEvent.ControllerCreator -> {
                svc.addGamepad(name = et.name, type = et.type, layoutId = et.layoutId) {
                    it.onSuccess {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity,"Controller created ❤️❤️", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.onFailure {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity,"Controller creation failed 🤡🤡🤡🤡🤡", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            is MainViewModelEvent.LayoutCreator -> {
                val intent = Intent(this, LayoutCreatorActivity::class.java)
                intent.putExtra(EDIT_DTO_TAG,et.dto)

                startActivity(intent)
            }
            is MainViewModelEvent.LayoutEditor -> {
                val intent = Intent(this, LayoutCreatorActivity::class.java)
                intent.putExtra(EDIT_DTO_TAG,et.dto)
                startActivity(intent)
            }
            MainViewModelEvent.Disconnect -> {
                svc.closeConnection()
            }

            MainViewModelEvent.TiltCalibration -> {
                requestedOrientation = if (mvm.showTiltCalibrationDialog)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else
                    ActivityInfo.SCREEN_ORIENTATION_USER
            }
            is MainViewModelEvent.DeleteLayout -> {
                mvm.myLayouts.remove(et.l)
                deleteFile(et.l.filename)
            }

            is MainViewModelEvent.DeleteController -> {
                svc.deleteGamepad(et.dto.clientRefId) {
                    it.onSuccess {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Controller deleted 🚮🚮", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }.onFailure {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Controller deletion failed 🤡🤡🤡🤡🤡", Toast.LENGTH_LONG).show()
                            }
                        }

                    }
                }
            }
            is MainViewModelEvent.OpenController -> {
                val intent = Intent(this, GamepadActivity::class.java)
                val cdto = ControllerDTO(layoutId = et.dto.layoutId, clientRefId = et.dto.clientRefId)
                intent.putExtra(GPAD_DTO_TAG,cdto)
                startActivity(intent)
            }

            is MainViewModelEvent.ChangeUsername -> { svc.username = et.u ; mvm.username = et.u }

            is MainViewModelEvent.ChangeSettings -> { svc.settings = et.s }

        }
    }

    fun loadAllLayouts() {
        mvm.myLayouts.addAll(DefaultLayoutManager.getAll())
        val lFiles = filesDir.listFiles{ f, name -> name.endsWith(".pad") } ?: return
        if (lFiles.isEmpty()) return
        lFiles.forEach {
            runCatching {
                mvm.myLayouts.add(ControllerLayoutData.fromStream(it.inputStream()))
            }.onFailure { err->
                appError("Cannot Load File ${it.name}" ,err)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        mvm.clearLayouts()
        loadAllLayouts()
    }


    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ACTION_EXIT_APP)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(exitReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            registerReceiver(exitReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(exitReceiver)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mvm.isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    override fun onDestroy() {
        appLog("OnDestroy  Clearing service jobs")
        clearServiceJobs()
        if (bound) {
            try {
                unbindService(connection)
            } catch (e: IllegalArgumentException) {
                appError("Service already unbound", e)
            }
            bound = false
        }

        super.onDestroy()
    }
}

