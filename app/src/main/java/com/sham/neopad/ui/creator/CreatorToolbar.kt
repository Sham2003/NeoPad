package com.sham.neopad.ui.creator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sham.neopad.model.ComponentDimension
import com.sham.neopad.viewmodel.LayoutCreatorModel

@Composable
fun CreatorToolbar(
    modifier: Modifier = Modifier,
    lcm: LayoutCreatorModel = viewModel()
) {
    val roundBarShape = MaterialTheme.shapes.medium

    Column(
        modifier = modifier
            .wrapContentSize()
            .clipToBounds()
    ) {
        AnimatedVisibility(
            visible = lcm.selectedComponent != null && lcm.showExpandedControls,
            enter = slideInVertically() + expandVertically(),
            exit = slideOutVertically() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.4f)
                .padding(bottom = 4.dp)
        ) {
            lcm.selectedComponent?.let { selectedComponent ->
                Column(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF424242), // Placeholder for a dark toolbar background
                            shape = roundBarShape
                        )
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                ) {

                    if (selectedComponent.type.canBe2D){
                        Row(
                            modifier = Modifier.wrapContentWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedComponent.dimension is ComponentDimension.DoubleSize,
                                onCheckedChange = { lcm.changeDimension() },
                                colors = CheckboxDefaults.colors(
                                    uncheckedColor = Color.White,
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Custom Width / Height",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    val dimension = selectedComponent.dimension
                    val isHeightSliderVisible = dimension is ComponentDimension.DoubleSize
                    Column(Modifier.fillMaxWidth()) {
                        SliderRow(
                            label = if (dimension is ComponentDimension.DoubleSize)
                                "Width"
                            else
                                "Size",
                            value = when(dimension) {
                                is ComponentDimension.DoubleSize -> dimension.width
                                is ComponentDimension.SameSize -> dimension.size
                            },
                            onValueChange = {
                                lcm.changeFirstDimension(it)
                            },
                            valueRange = 0.05f..0.55f
                        )

                        if (isHeightSliderVisible && selectedComponent.type.canBe2D) {
                            SliderRow(
                                label = "Height",
                                value = dimension.height,
                                onValueChange = {
                                    lcm.changeSecondDimension(it)
                                },
                                valueRange = 0.05f..0.55f
                            )
                        }
                    }

                    // Section 2: Four Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 1.dp)
                            .height(30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Icons
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            ToolbarIconButton(
                                icon = Icons.Default.ColorLens, // Placeholder for ic_color_palette
                                contentDescription = "Color",
                                onClick = {
                                    lcm.showColorChooser = true
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            if (selectedComponent.type.isSquareModeCompatible) {
                                ToolbarIconButton(
                                    icon = if (selectedComponent.squareMode) Icons.Default.Circle else Icons.Default.Square, // Placeholder for ic_circle
                                    contentDescription = "Shape",
                                    onClick = {
                                        lcm.toggleShape()
                                    }
                                )
                            }

                        }

                        // Right Icons
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ToolbarIconButton(
                                icon = Icons.Default.Refresh, // Placeholder for ic_reset
                                contentDescription = "Reset",
                                onClick = {
                                    lcm.resetSelectedShape()
                                },
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            ToolbarIconButton(
                                icon = Icons.Default.Delete, // Placeholder for ic_delete
                                contentDescription = "Delete",
                                onClick = {
                                    lcm.deleteSelected()
                                }
                            )
                        }
                    }
                }
            }

        }

        // 2. Main Toolbar (custom_toolbar)
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = Color(0xFF424242), // Placeholder for dark background
                    shape = roundBarShape
                )
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            ToolbarIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack, // Placeholder for ic_arrow_back
                contentDescription = "Back",
                onClick = {
                    lcm.showSaveDialog = true
                }
            )
            Spacer(Modifier.width(10.dp))

            if (lcm.selectedComponent != null) {
                ToolbarIconButton(
                    icon = if (lcm.showExpandedControls) Icons.Default.KeyboardDoubleArrowUp else Icons.Default.KeyboardDoubleArrowDown, // Placeholder for ic_arrow_up
                    contentDescription = "Up",
                    onClick = {
                        lcm.showExpandedControls = !lcm.showExpandedControls
                    }
                )
                Spacer(Modifier.width(10.dp))
            }


            ToolbarIconButton(
                icon = Icons.Default.Add, // Placeholder for ic_add
                contentDescription = "Add",
                onClick = {
                    lcm.showComponentCatalog = true
                },
                modifier = Modifier.size(36.dp) // Mimics scaleX/scaleY="1.2"
            )
            Spacer(Modifier.width(10.dp))


            // Settings Button
            ToolbarIconButton(
                icon = Icons.Default.Settings, // Placeholder for ic_settings
                contentDescription = "Settings",
                onClick = {
                    lcm.showSpecialSettings = true
                }
            )
        }
    }

}

// --- Helper Composable for Slider Row ---

@Composable
fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp) // Covers 36dp + 6dp spacing
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
    }
}

// --- Helper Composable for ImageButton replacement ---

@Composable
fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = Color.White,
        modifier = modifier
            .size(30.dp)
            .clickable(onClick = onClick)
            .padding(4.dp) // Adjust padding to act as the clickable area
    )
}

// --- Example Usage (Preview) ---

@Preview
@Composable
fun PreviewCustomToolbar() {
    LaunchedEffect(Unit) {
    }

    Column(Modifier.padding(16.dp)) {
        CreatorToolbar()
    }
}