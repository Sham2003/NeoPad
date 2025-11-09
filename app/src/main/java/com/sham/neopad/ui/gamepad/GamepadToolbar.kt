package com.sham.neopad.ui.gamepad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sham.neopad.R
import com.sham.neopad.model.ControllerDTO
import com.sham.neopad.model.ControllerType
import com.sham.neopad.viewmodel.GamepadViewModel


@Composable
fun GamepadToolbar(
    modifier: Modifier = Modifier,
    type : ControllerType = ControllerType.PS4,
    onBack: (() -> Unit) = {},
    onInfo: (() -> Unit) = {},
    onChange: () -> Unit = {},
) {
    val backgroundColor: Color = Color.Black.copy(alpha = 0.4f)
    val iconTypePainter = when(type) {
        ControllerType.PS4 -> painterResource(R.drawable.logo_ps4)
        ControllerType.XBOX -> painterResource(R.drawable.logo_xbox)
    }
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(top = 3.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = { onBack.invoke() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Back",
                )
            }


            Icon(
                painter = iconTypePainter,
                contentDescription = "Controller Type",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )


            IconButton(onClick = { onInfo.invoke() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Info",
                    modifier = Modifier.graphicsLayer(
                        scaleX = 0.9f,
                        scaleY = 0.9f
                    )
                )
            }

            IconButton(onClick = { onChange.invoke() }) {
                Icon(
                    Icons.Default.ChangeCircle,
                    contentDescription = "Change",
                    modifier = Modifier.graphicsLayer(
                        scaleX = 0.9f,
                        scaleY = 0.9f
                    )
                )
            }
        }
    }
}

@Composable
fun GpadChangerDialog(
    gvm: GamepadViewModel,
    onDismiss: () -> Unit
) {
    val selectedController = remember { mutableStateOf(gvm.controllerDTO) }

    val selectedBrush = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    )
    val nonSelectedBrush = MaterialTheme.colorScheme.surfaceVariant
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Change Controller",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                gvm.controllers.forEach { controller ->
                    val isSelected = selectedController.value?.clientRefId == controller.clientRefId

                    var boxModifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedController.value = ControllerDTO(
                                layoutId = controller.layoutId,
                                clientRefId = controller.clientRefId
                            )
                        }
                        .padding(16.dp)

                    boxModifier = if (isSelected) {
                        boxModifier.background(selectedBrush)
                    } else {
                        boxModifier.background(nonSelectedBrush)
                    }

                    Box( modifier = boxModifier ) {
                        Column {
                            Text(
                                text = controller.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = controller.type.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    gvm.controllerDTO = selectedController.value
                    gvm.changeController()
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
