package com.sham.neopad.ui.dialogs


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sham.neopad.R
import com.sham.neopad.appLog
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutCreatorDialog(
    mvm : MainViewModel
) {
    if (!mvm.showLayoutCreator) return
    var layoutName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ControllerType.PS4) }
    var selectedLayout: ControllerLayoutData? by remember { mutableStateOf(null) } // Replace with actual default string/resource

    var expanded by remember { mutableStateOf(false) }


    val currentLayouts = remember(selectedType) {
        when (selectedType) {
            ControllerType.PS4 -> mvm.ps4Layouts
            ControllerType.XBOX -> mvm.xboxLayouts
        }
    }

    LaunchedEffect(selectedType) {
        selectedLayout = try {
            currentLayouts.first()
        }catch (_: Exception){
            null
        }
    }

    Dialog(onDismissRequest =  { mvm.showLayoutCreator = false }) {
        Surface(modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,)
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                OutlinedTextField(
                    value = layoutName,
                    onValueChange = { layoutName = it },
                    label = {Text("Layout Name")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControllerTypeCard(
                        painter = painterResource(R.drawable.logo_ps4),
                        contentDescription = "Ps4 type",
                        type= ControllerType.PS4,
                        isSelected = selectedType == ControllerType.PS4,
                        modifier = Modifier.weight(1f),
                        compact = !mvm.isPortrait
                    ) {
                        selectedType = ControllerType.PS4
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    ControllerTypeCard(
                        painter = painterResource(R.drawable.logo_xbox),
                        contentDescription = "Xbox type",
                        type = ControllerType.XBOX,
                        isSelected = selectedType == ControllerType.XBOX,
                        modifier = Modifier.weight(1f),
                        compact = !mvm.isPortrait
                    ) {
                        selectedType = ControllerType.XBOX
                    }
                }

                if (mvm.isPortrait) {
                    Text(
                        text = "Select Layout", // Replace R.string.select_layout
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // Spinner/Dropdown Menu equivalent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart)
                        .padding(top = if (mvm.isPortrait) 10.dp else 4.dp)
                ) {
                    OutlinedTextField(
                        value = selectedLayout?.layoutName ?: "EMPTY",
                        onValueChange = { /* Read-only field for selection */ },
                        label = { Text("Layout") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown",
                                Modifier.clickable { expanded = true })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )
                    LayoutDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        },
                        currentLayouts = currentLayouts,
                        compact = !mvm.isPortrait
                    ) {
                        selectedLayout = it
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (mvm.isPortrait) 24.dp else 8.dp),
                    horizontalArrangement = Arrangement.End // Matches gravity="end"
                ) {
                    TextButton(onClick = { mvm.showLayoutCreator = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            selectedLayout?.let {
                                mvm.createLayout(layoutName, selectedType, it)
                            }
                        },
                        enabled = layoutName.isNotBlank() && selectedLayout != null
                    ) {
                        Text("Create")
                    }
                }
            }
        }

    }

}
