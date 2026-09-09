package android.mz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.mz.ui.viewmodel.AuthViewModel
import android.mz.ui.viewmodel.MoviesViewModel
import android.mz.ui.screens.auth.LoginScreen
import android.mz.ui.screens.main.MainScreen

@Composable
fun MovieZoneApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val moviesViewModel: MoviesViewModel = viewModel()
    
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    
    // Sync favorites with backend when user is authenticated
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            val userId = authViewModel.currentUser.value?.id
            userId?.let { id ->
                moviesViewModel.syncFavoritesWithBackend(id)
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("main") {
            MainScreen(
                authViewModel = authViewModel,
                moviesViewModel = moviesViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}
