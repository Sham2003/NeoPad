package com.sham.neopad.ui.main


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sham.neopad.viewmodel.MainViewModel

// Navigation destinations
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Layouts : Screen("layouts", "Layouts", Icons.Filled.Dashboard)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun MainScreen(
    mvm: MainViewModel
) {
    val navController = rememberNavController()
    val navigationItems = listOf(
        Screen.Home,
        Screen.Layouts,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navigationItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == screen.route
                    } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) Color.Red else Color.Gray
                            )
                        },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(mvm)
            }
            composable(Screen.Layouts.route) {
                LayoutFragment(mvm)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(mvm)
            }
        }
    }
}

@Preview(
    device = "spec:width=1080px,height=2400px,dpi=480,orientation=landscape",
    name = "Realme 8s"
)
@Composable
fun MainScreenPreview() {
    val mvm = MainViewModel.EMPTY
    mvm.isPortrait = false
    Surface(modifier = Modifier.padding(top = 10.dp).fillMaxSize()) {
        MainScreen(mvm)
    }
}


