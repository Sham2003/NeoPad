package com.sham.neopad.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sham.neopad.components.RoundColorPicker
import com.sham.neopad.ui.dialogs.AddComponentContainer
import com.sham.neopad.ui.dialogs.SpecialButtonsDialog
import com.sham.neopad.viewmodel.LayoutCreatorModel

@Composable
fun CreatorScreen(
    lcm : LayoutCreatorModel = viewModel()
) {
    if (lcm.isPortrait) {
        Surface {
            Box(contentAlignment = Alignment.Center) {
                Column {
                    CircularProgressIndicator()
                    Text("Doesn't work in portrait mode")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LayoutCreator(lcm,
            modifier = Modifier.fillMaxSize()
        )
        CreatorToolbar(
            modifier = Modifier.align(Alignment.TopCenter),
            lcm = lcm
        )

        lcm.selectedComponent?.let {
            if (!lcm.showColorChooser) return@let
            val selectedColor = Color(it.color)
            RoundColorPicker(
                modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(6.dp),
                selectedColor = selectedColor,
                onColorSelected = { color ->
                    lcm.changeColor(color)
                },
                onDismissRequest = {
                    lcm.showColorChooser = false
                }
            )
        }

        if (lcm.showComponentCatalog) {
            AddComponentContainer(
                modifier = Modifier.fillMaxWidth(0.6f),
                type = lcm.controllerType,
                onSelect = {
                    lcm.addGamepadComponent(it)
                    lcm.showComponentCatalog = false
                }
            )
        }

        if (lcm.showSpecialSettings) {
            SpecialButtonsDialog(
                modifier = Modifier.fillMaxWidth(0.6f),
                lcm = lcm,
                onDismissRequest =  {
                    lcm.showSpecialSettings = false
                }
            )
        }

        if (lcm.showSaveDialog) {
            SaveLayoutDialog(lcm)
        }
    }

}

@Composable
fun SaveLayoutDialog(lcm: LayoutCreatorModel) {
    AlertDialog(
        onDismissRequest = { lcm.showSaveDialog = false },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        icon = { Icon(Icons.Default.VideogameAsset, contentDescription = "Save") },
        title = { Text("Save Layout") },

        text = {
            Text(
                "Do you want to save your current layout before exiting?",
                style = MaterialTheme.typography.bodyLarge
            )
        },

        confirmButton = {
            Button(
                onClick = { lcm.saveLayout() },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Layout")
            }
        },

        dismissButton = {
            OutlinedButton(
                onClick = { lcm.closeCreator() },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exit")
            }
        },

        modifier = Modifier
            .fillMaxWidth(0.5f)
            .wrapContentHeight()
            .padding(24.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.medium)
    )
}
