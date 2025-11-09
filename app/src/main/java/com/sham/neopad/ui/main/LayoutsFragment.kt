package com.sham.neopad.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Rowing
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.MainViewModelEvent
import com.sham.neopad.ui.dialogs.LayoutCreatorDialog
import com.sham.neopad.viewmodel.MainViewModel

@Composable
fun LayoutFragment(
    mvm: MainViewModel = viewModel()
) {
    val defaultLayouts = mvm.myLayouts.filter { !it.isCustom }
    val customLayouts = mvm.myLayouts.filter { it.isCustom }

    Box(modifier = Modifier.fillMaxSize()) {
        if (mvm.isPortrait) {
            PortraitLayoutView(mvm, defaultLayouts, customLayouts)
        } else {
            LandscapeLayoutView(mvm, defaultLayouts, customLayouts)
        }

        if (mvm.showLayoutCreator) {
            LayoutCreatorDialog(mvm)
        }
    }
}

@Composable
private fun PortraitLayoutView(
    mvm: MainViewModel,
    defaultLayouts: List<ControllerLayoutData>,
    customLayouts: List<ControllerLayoutData>
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Title
        Text(
            text = "Neo Pad",
            fontSize = 34.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD73131),
            lineHeight = 58.sp,
            modifier = Modifier.padding(top = 21.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Scrollable Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            DefaultLayoutsSection(mvm,defaultLayouts)

            HorizontalDivider(modifier = Modifier.padding(top = 15.dp, bottom = 10.dp), thickness = 1.dp)

            CustomLayoutsSection(mvm, customLayouts)
        }
    }
}


@Composable
private fun LandscapeLayoutView(
    mvm: MainViewModel,
    defaultLayouts: List<ControllerLayoutData>,
    customLayouts: List<ControllerLayoutData>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Top Title - Smaller in landscape
        Text(
            text = "Neo Pad",
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD73131),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 1.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main 2-column content with independent scrolling
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top= 5.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- LEFT: Default Layouts ----------
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Default Layouts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp, bottom = 4.dp)
                    )
                }

                items(defaultLayouts.size) { index ->
                    val item = defaultLayouts[index]
                    SimpleLayoutItem(
                        l = item,
                        onClick = { mvm.openViewer(item) }
                    )
                }
            }

            // ---------- RIGHT: Custom Layouts ----------
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "My Layouts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Cyan,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp, end = 20.dp)
                    )
                }

                items(customLayouts.size) { index ->
                    val item = customLayouts[index]
                    SimpleLayoutItem(
                        l = item,
                        onClick = { mvm.openViewer(item) },
                        onDelete = { mvm.emitEvent(MainViewModelEvent.DeleteLayout(item)) },
                        onEdit = { mvm.openEditMode(item) }
                    )
                }
            }
        }
    }
}



@Composable
private fun DefaultLayoutsSection(
    mvm: MainViewModel,
    defaultLayouts: List<ControllerLayoutData>
) {
    // Header with optional Add button
    var showPreview by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Default Layouts",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 20.dp)
        )

        // Add Layout button
        IconButton(
            onClick = {
                showPreview = !showPreview
            },
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp)
        ) {
            Icon(
                imageVector = if (!showPreview) Icons.Default.Preview else Icons.Default.HideImage,
                contentDescription = "SHow preview",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Layouts List
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        defaultLayouts.forEach { item ->
            if (!showPreview) {
                SimpleLayoutItem(
                    l = item,
                    onClick = { mvm.openViewer(item) }
                )
            } else {
                ExtendedLayoutItem(
                    l = item,
                    onClick = {  mvm.openViewer(item) },
                    preview = {
                        PreviewLayout(item , modifier = Modifier.fillMaxSize())
                    }
                )
            }

        }
    }
}

@Composable
private fun CustomLayoutsSection(
    mvm: MainViewModel,
    customLayouts: List<ControllerLayoutData>
) {
    // Custom Layouts Section Header
    var showPreview by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .then(
                if (mvm.isPortrait) Modifier
                else Modifier.verticalScroll(rememberScrollState())
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "My Layouts",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Cyan,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 20.dp)
        )

        IconButton(
            onClick = {
                showPreview = !showPreview
            },
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp)
        ) {
            Icon(
                imageVector = if (!showPreview) Icons.Default.Preview else Icons.Default.HideImage,
                contentDescription = "SHow preview",
                modifier = Modifier.size(24.dp)
            )
        }

        IconButton(
            onClick = { mvm.showLayoutCreator = true },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Gamepad,
                contentDescription = "Add a new layout",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Custom Layouts List
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        customLayouts.forEach { item ->
            if (!showPreview) {
                SimpleLayoutItem(
                    l = item,
                    onClick = { mvm.openViewer(item) },
                    onDelete = { mvm.emitEvent(MainViewModelEvent.DeleteLayout(item)) },
                    onEdit = { mvm.openEditMode(item) }
                )
            }else {
                ExtendedLayoutItem(
                    l = item,
                    onClick = {  mvm.openViewer(item) },
                    onDelete = { mvm.emitEvent(MainViewModelEvent.DeleteLayout(item)) },
                    onEdit = { mvm.openEditMode(item) },
                    preview = {
                        PreviewLayout(item , modifier = Modifier.fillMaxSize())
                    }
                )
            }

        }
    }
}


@Composable
fun SimpleLayoutItem(
    l: ControllerLayoutData,
    onClick: () -> Unit,
    onDelete: (() -> Unit) = {},
    onEdit: () -> Unit = {}
) {
    val layoutTypeIcon = when (l.controllerType) {
        ControllerType.PS4 -> painterResource(com.sham.neopad.R.drawable.logo_ps4)
        ControllerType.XBOX -> painterResource(com.sham.neopad.R.drawable.logo_xbox)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = layoutTypeIcon,
                    contentDescription = "Layout Type",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = l.layoutName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(12.dp))
                if (l.isRotationLayout()) {
                    Icon(
                        Icons.Default.ScreenRotation,
                        contentDescription = "Layout Type",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (l.isCustom) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Layout",
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Layout",
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ExtendedLayoutItem(
    l: ControllerLayoutData,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    preview: @Composable () -> Unit, // composable slot for LayoutViewer
) {
    val layoutTypeIcon = when(l.controllerType) {
        ControllerType.PS4 -> painterResource(com.sham.neopad.R.drawable.logo_ps4)
        ControllerType.XBOX -> painterResource(com.sham.neopad.R.drawable.logo_xbox)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // ---- Layout Preview Section (16:9 landscape aspect) ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f) // <— maintain landscape proportion
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                preview()
            }

            // ---- Info Section ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter  = layoutTypeIcon,
                        contentDescription = "Layout Type",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = l.layoutName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (l.isRotationLayout()) {
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Rotation Layout"
                        )
                    }
                }

                if (l.isCustom) {
                    Row {
                        IconButton(onClick = { onEdit?.invoke() }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Layout",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDelete?.invoke() }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Layout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun LayoutsDashboardScreenStateful() {
    Surface {
        LayoutFragment()
    }
}