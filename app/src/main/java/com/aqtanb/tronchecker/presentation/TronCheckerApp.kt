package com.aqtanb.tronchecker.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.aqtanb.tronchecker.presentation.navigation.TronCheckerNavHost

@Composable
fun TronCheckerApp() {
    TronCheckerNavHost(
        navController = rememberNavController()
    )
}