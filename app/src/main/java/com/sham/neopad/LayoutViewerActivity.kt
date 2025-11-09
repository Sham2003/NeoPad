package com.sham.neopad

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.sham.neopad.layout.DefaultLayoutManager
import com.sham.neopad.misc.OrientationSensorManager
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.LayoutViewerDTO
import com.sham.neopad.model.SettingStore
import com.sham.neopad.ui.theme.NeoPadTheme
import com.sham.neopad.ui.viewer.ViewerScreen
import com.sham.neopad.viewmodel.LayoutViewerModel

class LayoutViewerActivity: ComponentActivity() {

    private val lvm by viewModels<LayoutViewerModel>()

    private val orientationManager by lazy {
        OrientationSensorManager(this) { a, p, r ->
            lvm.updateTilt(a,p,r)
        }
    }
    private val prefsName = "neopad_connection"
    private val prefs by lazy {
        getSharedPreferences(prefsName, MODE_PRIVATE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lvm.isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        setContent{
            NeoPadTheme {
                ViewerScreen(lvm)
                LaunchedEffect(Unit) {
                    hideSystemBars()
                }
            }
        }
        initViewModel()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        lvm.onClose = {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        lvm.isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
    }


    override fun onStart() {
        super.onStart()
        hideSystemBars()
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



    private fun initViewModel() {
        val layoutDto = loadDTOFromIntent()
        lvm.tiltSettings = loadSettingsFromPrefs()

        runCatching {
            lvm.layoutData = if (layoutDto.isDefault) {
                DefaultLayoutManager.getLayout(layoutDto.layoutId)
            }else {
                val stream = openFileInput(layoutDto.filename)
                ControllerLayoutData.fromStream(stream)
            }
            lvm.isReady = true
        }.onFailure { e->
            appError("Could not load $layoutDto Error in loading",e)
            Toast.makeText(this,"Layout Loading error", Toast.LENGTH_LONG).show()
            finish()
        }
    }


    fun loadDTOFromIntent() : LayoutViewerDTO {
        val dto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra<LayoutViewerDTO>(VIEW_DTO_TAG, LayoutViewerDTO::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<LayoutViewerDTO>(VIEW_DTO_TAG)!!
        }
        return dto
    }

    override fun onResume() {
        super.onResume()
        orientationManager.start()
    }

    override fun onPause() {
        super.onPause()
        orientationManager.stop()
    }

}