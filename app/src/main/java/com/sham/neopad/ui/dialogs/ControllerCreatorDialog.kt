package com.sham.neopad.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sham.neopad.R
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerCreatorDialog(
    mvm : MainViewModel
) {
    var controllerName by remember { mutableStateOf("sajd") }
    var selectedType by remember { mutableStateOf(ControllerType.PS4) }
    var selectedLayout: ControllerLayoutData? by remember { mutableStateOf(null) } // Replace with actual default string/resource

    var expanded by remember { mutableStateOf(false) }
    val compact = !mvm.isPortrait

    val currentLayouts = remember(selectedType) {
        when (selectedType) {
            ControllerType.PS4 -> mvm.ps4Layouts
            ControllerType.XBOX -> mvm.xboxLayouts
        }
    }

    LaunchedEffect( selectedType) {
        selectedLayout = try {
            currentLayouts.first()
        }catch (_: Exception){
            null
        }
    }

    Dialog(
        onDismissRequest =  { mvm.showControllerCreator = false },
        properties = DialogProperties(false,false)
    ) {
        Surface(modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (compact) 18.dp else 24.dp)
            ) {
                OutlinedTextField(
                    value = controllerName,
                    onValueChange = { controllerName = it },
                    label = {Text("New Controller")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (compact) 10.dp else 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ControllerTypeCard(
                        painter = painterResource(R.drawable.logo_ps4),
                        contentDescription = "Ps4 type",
                        type= ControllerType.PS4,
                        isSelected = selectedType == ControllerType.PS4,
                        modifier = Modifier.weight(1f),
                        compact = compact
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
                        compact = compact
                    ) {
                        selectedType = ControllerType.XBOX
                    }
                }

                if (!compact) {
                    Text(
                        text = "Select Layout", // Replace R.string.select_layout
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    OutlinedTextField(
                        value = selectedLayout?.layoutName ?: "NIL",
                        onValueChange = {  },
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
                        compact = compact
                    ) {
                        selectedLayout = it
                    }
                }


                // --- Buttons (Horizontal LinearLayout with gravity="end") ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (compact) 12.dp else 24.dp),
                    horizontalArrangement = Arrangement.End // Matches gravity="end"
                ) {
                    TextButton(onClick = { mvm.showControllerCreator = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            selectedLayout?.let {
                                mvm.createController(controllerName, selectedType, it)
                                mvm.showControllerCreator = false
                            }
                        },
                        enabled = controllerName.isNotBlank() && selectedLayout != null
                    ) {
                        Text("Create")
                    }
                }
            }
        }

    }

}

@Composable
fun LayoutDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentLayouts: List<ControllerLayoutData>,
    compact : Boolean,
    onLayoutSelected: (ControllerLayoutData) -> Unit
) {
    val defaultItems = currentLayouts.filter { !it.isCustom }
    val customItems = currentLayouts.filter { it.isCustom }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth(if (compact) 0.3f else 0.6f)
    ) {
        // --- Custom Layouts Section ---
        if (defaultItems.isNotEmpty()) {
            LayoutSectionLabel("Default Layouts")

            defaultItems.forEach { layout ->
                LayoutSelectableItem(layout, onLayoutSelected, onDismissRequest)
            }
            if (customItems.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        // --- Default Layouts Section ---
        if (customItems.isNotEmpty()) {
            LayoutSectionLabel("Custom Layouts")
            customItems.forEach { layout ->
                LayoutSelectableItem(layout, onLayoutSelected, onDismissRequest)
            }
        }
    }
}

@Composable
fun LayoutSectionLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun LayoutSelectableItem(
    layout: ControllerLayoutData,
    onLayoutSelected: (ControllerLayoutData) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = layout.layoutName,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        onClick = {
            onLayoutSelected(layout)
            onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp) // Item padding
    )
}

@Composable
fun ControllerTypeCard(
    painter: Painter,
    contentDescription: String,
    type: ControllerType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean,
    onClick: () -> Unit
) {
    val cardElevation = if (isSelected) 8.dp else 2.dp // Higher elevation when selected
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer // A slightly different background color from Material3 theme
    } else {
        MaterialTheme.colorScheme.surface // Default surface color from Material3 theme
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(cardElevation),
        colors = CardDefaults.cardColors(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 4.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(if (compact) 24.dp else 48.dp)
            )
            if (!compact) {
                Text(
                    text = type.name,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

        }
    }
}

