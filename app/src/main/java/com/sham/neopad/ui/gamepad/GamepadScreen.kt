package com.sham.neopad.ui.gamepad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sham.neopad.appLog
import com.sham.neopad.ui.viewer.InfoDialog
import com.sham.neopad.viewmodel.GamepadViewModel

@Composable
fun GamepadScreen(
    gvm : GamepadViewModel = viewModel()
) {
    if (gvm.isPortrait) {
        Surface {
            Box(contentAlignment = Alignment.Center) {
                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Doesn't work in portrait mode")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (gvm.layoutData == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Apps, "", modifier = Modifier.fillMaxHeight(0.5f))
                Text("No Layout Data available", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = {gvm.closeApp()}) {
                    Text("Back")
                }
            }

        }

        gvm.layoutData?.let { layout ->
            appLog("There is layout data so drawing")
            GamepadViewer(
                gvm, layout,
                modifier = Modifier.fillMaxSize()
            )
            GamepadToolbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 1.dp),
                type = layout.controllerType,
                onBack = {
                    gvm.closeApp()
                },
                onChange = {
                    gvm.changeGpad = true
                },
                onInfo = {
                    gvm.showInfo = true
                }
            )
            if (gvm.showInfo) {
                InfoDialog(layout) {
                    gvm.showInfo = false
                }
            }
            if (gvm.changeGpad) {
                GpadChangerDialog(gvm) {
                    gvm.changeGpad = false
                }
            }
        }
    }
}


