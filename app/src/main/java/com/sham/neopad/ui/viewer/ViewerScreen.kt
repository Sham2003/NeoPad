package com.sham.neopad.ui.viewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sham.neopad.viewmodel.LayoutViewerModel

@Composable
fun ViewerScreen(
    lvm : LayoutViewerModel = viewModel()
) {
    if (lvm.isPortrait) {
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
        if (!lvm.isReady) {
            Text("No Layout Data available")
        }

        lvm.layoutData.let { layout ->
            LayoutViewer(lvm,layout,
                modifier = Modifier.fillMaxSize()
            )
            InfoToolbar(
                type = layout.controllerType,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 1.dp),
                onBack = {
                    lvm.closeApp()
                },
                onInfo = {
                    lvm.showInfo = true
                }
            )

            if (lvm.showInfo) {
                InfoDialog(layout) {
                    lvm.showInfo = false
                }
            }
        }
    }
}


