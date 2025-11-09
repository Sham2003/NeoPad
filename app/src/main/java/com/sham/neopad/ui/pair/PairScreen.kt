package com.sham.neopad.ui.pair

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.ConnectWithoutContact
import androidx.compose.material.icons.filled.ConnectedTv
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.NearbyError
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sham.neopad.R
import com.sham.neopad.appLog
import com.sham.neopad.model.ConnectionInfo
import com.sham.neopad.model.ConnectionStatus
import com.sham.neopad.model.DiscoveredConnection
import com.sham.neopad.model.PairViewModelEvent
import com.sham.neopad.viewmodel.PairViewModel
import kotlinx.coroutines.delay

@Composable
fun PairScreen(pvm: PairViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "Neo Pad",
                fontSize = 34.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // ✅ Ensure it's centered
                    .padding(bottom = if (pvm.isPortrait) 16.dp else 6.dp)
            )

            Spacer(Modifier.height(8.dp))

            if (pvm.isPortrait) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ConnectionInfoSection(pvm.connectionStatus, pvm.connectionInfo)
                    Spacer(Modifier.height(2.dp))
                    DiscoveryList(
                        connections = pvm.availableConnections,
                        connectionInfo = pvm.connectionInfo,
                        onConnect = { pvm.emit(PairViewModelEvent.ConnectToService(it)) },
                        onCopy = { pvm.emit(PairViewModelEvent.CopyUrl(it)) },
                        compact = false
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        ConnectionInfoSection(pvm.connectionStatus, pvm.connectionInfo)
                    }

                    VerticalDivider(thickness = 2.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        DiscoveryList(
                            connections = pvm.availableConnections,
                            connectionInfo = pvm.connectionInfo,
                            onConnect = { pvm.emit(PairViewModelEvent.ConnectToService(it)) },
                            onCopy = { pvm.emit(PairViewModelEvent.CopyUrl(it)) },
                            compact = true
                        )
                    }
                }
            }
        }

        // Floating Action Button - anchored to bottom end
        FloatingActionButton(
            onClick = { pvm.emit(PairViewModelEvent.CloseActivity) },
            shape = CircleShape, // ✅ Make it truly round
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 30.dp)
                .size(70.dp) // ✅ Slightly larger for better touch target
                .border(2.dp, Color.Yellow, CircleShape), // ✅ Border matches round shape
            containerColor = MaterialTheme.colorScheme.primary, // optional: control background
            contentColor = Color.White // optional: control icon tint
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Back",
                modifier = Modifier.size(36.dp)
            )
        }

    }
}


@Composable
fun DiscoveryList(
    connections: List<DiscoveredConnection>,
    connectionInfo: ConnectionInfo,
    onConnect: (DiscoveredConnection) -> Unit,
    onCopy: (DiscoveredConnection) -> Unit,
    compact: Boolean
) {
    Column {
        Text(
            text = "Discovered Connections",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(connections.size) { index ->
                val conn = connections[index]
                val isActive = conn.sessionId == connectionInfo.sessionId

                DiscoveredConnectionCard(
                    connection = conn,
                    isActive = isActive,
                    onConnectClick = { onConnect(conn) },
                    onCopy = { onCopy(conn) },
                    compact = compact
                )
            }
        }
    }

}


@Composable
fun ConnectionInfoSection(status: ConnectionStatus, info: ConnectionInfo,compact: Boolean = false) {
    when (status) {
        is ConnectionStatus.Connected -> {
            ConnectionInfoCard(info = info,compact = compact)
        }

        else -> {
            val textColor = when (status) {
                is ConnectionStatus.NotConnected -> Color.Gray
                is ConnectionStatus.Disconnected -> MaterialTheme.colorScheme.error
                is ConnectionStatus.Connecting -> MaterialTheme.colorScheme.primary
                is ConnectionStatus.Error -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }

            val iconPainter = when (status) {
                ConnectionStatus.Connected -> Icons.Default.ConnectedTv
                ConnectionStatus.Connecting -> Icons.Default.ConnectWithoutContact
                ConnectionStatus.Disconnected -> Icons.Default.SignalWifiConnectedNoInternet4
                is ConnectionStatus.Error -> Icons.Default.NearbyError
                ConnectionStatus.NotConnected -> Icons.Default.Cancel
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
                shape = AbsoluteCutCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = iconPainter,
                        contentDescription = status.toString(),
                        modifier = Modifier.size(if (compact) 35.dp else 50.dp)
                    )
                    Text(
                        text = (status.trimmed()).also {
                            appLog("Text : $it")
                        },

                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = textColor,
                            fontSize = if (compact) 24.sp else 30.sp
                        ),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionInfoCard(info: ConnectionInfo, modifier: Modifier = Modifier,compact : Boolean = false) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(if (compact) 6.dp else 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 8.dp else 16.dp)
        ) {
            // Session ID - emphasized
            Text(
                text = "Session ID: ${info.sessionId}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraLight,
                    color = MaterialTheme.colorScheme.primaryFixedDim
                ),
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // URL - italic
            Text(
                text = info.url,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Host name
            Text(
                text = "Host Name: ${info.name}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Connected as username
            Text(
                text = "Connected as \"${info.username}\"",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}




@Composable
private fun DiscoveredConnectionCard(
    connection: DiscoveredConnection,
    isActive: Boolean,
    onConnectClick: () -> Unit,
    onCopy: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                Color.Green.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = connection.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Session ID: ${connection.sessionId}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )

            Text(
                text = "Address: ${connection.address.hostAddress}:${connection.port}",
                style = MaterialTheme.typography.bodyMedium
            )

            ExpiryCountdown(connection.expiresAt)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (compact) Arrangement.spacedBy(10.dp) else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onCopy) {
                    if (compact)
                        Icon(imageVector = Icons.Default.CopyAll , contentDescription = "")
                    else
                        Text("Copy Url")
                }

                if (isActive) {
                    Text(
                        text = "Connected",
                        color = Color(0xFF2E7D32),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                } else {
                    Button(onClick = onConnectClick) {
                        if (compact)
                            Icon(Icons.Default.CastConnected , contentDescription = "Connect")
                        else
                            Text("Connect")
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ExpiryCountdown(expiresAt: Long) {
    if (expiresAt == Long.MAX_VALUE) return

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(expiresAt) {
        while (now < expiresAt) {
            delay(100)
            now = System.currentTimeMillis()
        }
    }

    val secondsLeft = ((expiresAt - now) / 1000f).coerceAtLeast(0f)
    val formatted = String.format("%.1f", secondsLeft)
    Row {
        Text(
            text = "Expires in",
            color = if (secondsLeft > 10) Color(0xFF757575) else Color.Red,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.width(1.dp))
        Text(
            text = " $formatted s",
            color = if (secondsLeft > 10) Color(0xFF757575) else Color.Red,
            style = if (secondsLeft > 10 )MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
        )
    }

}






