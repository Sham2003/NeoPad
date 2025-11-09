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
import androidx.lifecycle.lifecycleScope
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.layout.DefaultLayoutManager
import com.sham.neopad.model.LayoutCreatorDTO
import com.sham.neopad.ui.creator.CreatorScreen
import com.sham.neopad.ui.theme.NeoPadTheme
import com.sham.neopad.viewmodel.CloseOptions
import com.sham.neopad.viewmodel.LayoutCreatorModel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random


class LayoutCreatorActivity: ComponentActivity() {

    private val lcm by viewModels<LayoutCreatorModel>()
    var filename = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lcm.isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        setContent{
            NeoPadTheme {
                CreatorScreen()
                LaunchedEffect(Unit) {
                    hideSystemBars()
                }
            }
        }

        appLog("Creator OnCreate")
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        lcm.isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
    }


    override fun onStart() {
        super.onStart()
        initViewModel()
        hideSystemBars()
        lifecycleScope.launch {
            lcm.emitter.collect { et ->
                appLog("Emitter Changed $et")
                when(et) {
                    CloseOptions.NIL -> {}
                    CloseOptions.SAVE -> saveLayout()
                    CloseOptions.EXIT -> closeActivity()
                }
            }
        }

        appLog("Creator OnStart")
    }


    private fun initViewModel() {
        val layoutDto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra<LayoutCreatorDTO>(EDIT_DTO_TAG, LayoutCreatorDTO::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<LayoutCreatorDTO>(EDIT_DTO_TAG)
        }
        if (layoutDto ==null) {
            Toast.makeText(this,"File name null" , Toast.LENGTH_LONG).show()
            finish()
            return
        }

        var layoutData : ControllerLayoutData? = null
        try {
            if (layoutDto.isDefault) {
                layoutData = DefaultLayoutManager.getLayout(layoutDto.selectedLayout)
            } else {
                val stream = openFileInput(layoutDto.selectedLayout)
                layoutData = ControllerLayoutData.fromStream(stream)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could Not previous",Toast.LENGTH_LONG).show()
            appError("Could not load $layoutDto Error in loading",e)
        }
        lcm.layoutId = UUID.randomUUID().toString()
        lcm.layoutName = layoutDto.layoutName
        lcm.controllerType = layoutDto.layoutType
        filename = "layout_"+Random.Default.nextInt()+".pad"
        layoutData?.let { lta ->
            lcm.components.addAll(lta.components)
            lcm.specialButtons = lta.specialButtons
            if (layoutDto.isEditMode) {
                lcm.layoutId = lta.layoutId
                lcm.layoutName = lta.layoutName
                lcm.controllerType = lta.controllerType
                filename = lta.filename
            }

        }

    }

    fun saveLayout() {
        val layoutData = lcm.getLayoutData(filename)
        appLog("printing layout data \n $layoutData")
        openFileOutput(filename,MODE_PRIVATE).use {outputStream ->
            ControllerLayoutData.toStream(layoutData,outputStream)
        }
        Toast.makeText(this,"Layout Saved" ,Toast.LENGTH_LONG).show()
        closeActivity()
    }


    private fun closeActivity() {
        finish()
    }


}