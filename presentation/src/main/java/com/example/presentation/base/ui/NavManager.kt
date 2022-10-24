package com.example.presentation.base.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.central.ui.CentralScreen
import com.example.presentation.peripheral.ui.PeripheralScreen
import com.google.accompanist.insets.ProvideWindowInsets

@Composable
fun NavManager() {
    val navController = rememberNavController()

    ProvideWindowInsets {
        NavHost(navController = navController, startDestination = NavScreen.Central.route) {
            composable(NavScreen.Central.route) {
                CentralScreen(viewModel = hiltViewModel(), goToPeripheral = {
                        navController.navigate(NavScreen.Peripheral.route)
                    }
                )
            }
            composable(
                route = NavScreen.Peripheral.route,
            ) {
                PeripheralScreen(viewModel = hiltViewModel(), goToCentral = {
                    navController.navigate(NavScreen.Central.route)
                })
            }
        }
    }
}

sealed class NavScreen(val route: String) {

    object Central : NavScreen("Central")

    object Peripheral : NavScreen("Peripheral")
}