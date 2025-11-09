package com.sham.neopad.ui.main


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sham.neopad.R
import com.sham.neopad.model.ConnectionStatus
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.VirtualController
import com.sham.neopad.model.MainViewModelEvent
import com.sham.neopad.ui.dialogs.ControllerCreatorDialog
import com.sham.neopad.ui.pair.ConnectionInfoSection
import com.sham.neopad.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    mvm: MainViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Text(
                text = "Neo Pad",
                fontSize = 34.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD73131),
                modifier = Modifier.padding(top = if (mvm.isPortrait) 30.dp else 3.dp).align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(if (mvm.isPortrait) 10.dp else 2.dp))
            if (mvm.isPortrait) {
                PortraitLayout(mvm)
            } else {
                LandscapeLayout(mvm)
            }
        }


        if (mvm.showControllerCreator) {
            ControllerCreatorDialog(mvm)
        }
    }
}

@Composable
private fun PortraitLayout(mvm: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(9.dp))
        ConnectionSection(mvm)

        Spacer(modifier = Modifier.height(25.dp))

        // Controllers Section
        ControllersSection(
            mvm = mvm,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun LandscapeLayout(mvm: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Side - Title, Connection, and GO Button
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ConnectionSection(mvm, compact = true)
        }

        VerticalDivider(modifier = Modifier.padding(start = 10.dp, end = 10.dp),3.dp)

        // Right Side - Controllers List
        ControllersSection(
            mvm = mvm,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun ConnectionSection(
    mvm: MainViewModel,
    compact: Boolean = false
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 3.dp),
        verticalArrangement = Arrangement.SpaceBetween

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mvm.connectionStatus.trimmed(),
                fontSize = if (compact) 16.sp else 20.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 3.dp)
            )

            Button(
                onClick = {
                    if (!mvm.connected) mvm.emitEvent(MainViewModelEvent.Connect)
                    else mvm.emitEvent(MainViewModelEvent.Disconnect)
                },
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 1.dp,
                    end = 13.dp,
                    bottom = 1.dp
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_connect_btn),
                    contentDescription = null,
                    modifier = Modifier.size(if (mvm.isPortrait) 15.dp else 11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (mvm.connected) "Disconnect" else "Connect",
                    fontSize = if(mvm.isPortrait) 20.sp else 12.sp

                )
            }
        }

        if (mvm.isPortrait) Spacer(Modifier.height(20.dp))
        ConnectionInfoSection(mvm.connectionStatus,mvm.connectionInfo,compact)
    }

}



@Composable
private fun ControllersSection(
    mvm: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Controllers Header
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Controllers",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E2323),
                modifier = Modifier.padding(start = 2.dp, top = 4.dp)
            )

            IconButton(
                onClick = { mvm.showControllerCreator = true },
                enabled = mvm.connectionStatus == ConnectionStatus.Connected
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_controller),
                    contentDescription = "Add a new controller",
                    modifier = Modifier.size(45.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
                .verticalScroll(rememberScrollState())
                .padding(1.dp)
        ) {
            if (mvm.controllers.isEmpty()) {
                Text(
                    text = "No Controllers Created",
                    modifier = Modifier.padding(20.dp).align(Alignment.CenterHorizontally)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mvm.controllers.forEach { controller ->
                        ControllerItem(
                            controller = controller,
                            onControllerClick = {
                                mvm.openController(controller)
                            },
                            onControllerDelete = {
                                mvm.deleteController(controller)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ControllerItem(
    controller: VirtualController,
    onControllerClick: () -> Unit,
    onControllerDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onControllerClick)
            ) {
                val iconRes = when (controller.type) {
                    ControllerType.XBOX -> R.drawable.logo_xbox
                    ControllerType.PS4 -> R.drawable.logo_ps4
                }

                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = controller.type.name,
                    modifier = Modifier
                        .size(42.dp)
                        .padding(end = 12.dp)
                )

                Column {
                    Text(
                        text = controller.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = controller.type.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Delete button
            IconButton(
                onClick = onControllerDelete,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Controller",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}




@Preview(device = "spec:width=1080px,height=2400px,dpi=480,orientation=landscape",
    name = "Realme 8s"
)
@Composable
fun HomeScreenPreview() {
    val mvm = MainViewModel.EMPTY
    mvm.isPortrait = false
    Surface(modifier = Modifier.padding(top = 10.dp).fillMaxSize()) {
        HomeScreen(mvm)
    }
}