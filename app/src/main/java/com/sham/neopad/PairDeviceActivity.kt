package com.sham.neopad

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import com.sham.neopad.model.PairViewModelEvent
import com.sham.neopad.service.NeoPadService
import com.sham.neopad.service.NeoPadService.Companion.ACTION_EXIT_APP
import com.sham.neopad.ui.pair.PairScreen
import com.sham.neopad.viewmodel.PairViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PairDeviceActivity : ComponentActivity() {

    private val pvm by viewModels<PairViewModel>()
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
            clearServiceJobs()
            bound = false
        }
    }

    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_EXIT_APP) {
                finishAffinity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pvm.isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        Intent(this, NeoPadService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
        setContent {
            PairScreen(pvm)
            LaunchedEffect(Unit) {
                //hideSystemBars()
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        pvm.isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun onBoundService(svc: NeoPadService) {
        lifecycleScope.launch {
            svc.connectionStatus.collect {
                pvm.connectionStatus = it
            }
        }.also { jobQueue.add(it) }
        lifecycleScope.launch {
            svc.availableConnections.collect {
                pvm.availableConnections = it
            }
        }

        lifecycleScope.launch {
            svc.connectionInfo.collect {
                pvm.connectionInfo = it
            }
        }.also { jobQueue.add(it) }
        collectVmEvents(svc)
    }

    private fun collectVmEvents(svc: NeoPadService) {
        lifecycleScope.launch {
            pvm.events.collect { et ->
                when(et) {
                    PairViewModelEvent.CloseActivity -> finish()
                    is PairViewModelEvent.ConnectToService -> {
                        svc.connectToService(et.conn)
                    }
                    is PairViewModelEvent.CopyUrl -> {
                        val url = "http://${et.conn.address.hostAddress}:${et.conn.port}"
                        copyToClipboard(url)
                    }
                }
            }
        }.also { jobQueue.add(it) }
    }



    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ACTION_EXIT_APP)
        //hideSystemBars()
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

    override fun onDestroy() {
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


    fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Server Url", text)
        clipboard.setPrimaryClip(clip)
    }

}
