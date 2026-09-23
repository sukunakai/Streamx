package com.streamx.app.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.streamx.app.ui.screens.DownloadScreen
import com.streamx.app.ui.screens.HomeScreen
import com.streamx.app.ui.screens.ProfileScreen
import com.streamx.app.ui.screens.SearchScreen
import com.streamx.app.viewmodel.StreamXViewModel

@Composable
fun MainAppScreen(navController: NavController, viewModel: StreamXViewModel) {
    val bottomNavController = rememberNavController()
    val context = LocalContext.current
    var backPressTime by remember { mutableLongStateOf(0L) }
    val haptic = LocalHapticFeedback.current

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    BackHandler {
        if (currentRoute == "home") {
            val now = System.currentTimeMillis()
            if (now - backPressTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                backPressTime = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        } else {
            bottomNavController.navigate("home") {
                popUpTo(bottomNavController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple("home", Icons.Filled.Home, "Home"),
                    Triple("search", Icons.Filled.Search, "Search"),
                    Triple("downloads", Icons.Filled.Download, "Downloads"),
                    Triple("profile", Icons.Filled.Person, "Me")
                )
                items.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            bottomNavController.navigate(route) {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen(viewModel, navController) }
            composable("search") { SearchScreen() }
            composable("downloads") { DownloadScreen() }
            composable("profile") { ProfileScreen(viewModel) }
        }
    }
}
