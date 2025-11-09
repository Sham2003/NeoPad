package com.sham.neopad.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sham.neopad.R
import com.sham.neopad.model.ControllerType


@Composable
fun InfoToolbar(
    modifier: Modifier = Modifier,
    type : ControllerType = ControllerType.PS4,
    onBack: (() -> Unit) = {},
    onInfo: (() -> Unit) = {},
) {
    val backgroundColor: Color = Color.Black.copy(alpha = 0.4f)
    val iconTypePainter = when(type) {
        ControllerType.PS4 -> painterResource(R.drawable.logo_ps4)
        ControllerType.XBOX -> painterResource(R.drawable.logo_xbox)
    }
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(top = 12.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        // Row containing back, title (center), and info
        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
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
        }
    }
}
