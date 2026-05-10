package com.example.weathersnap

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weathersnap.ui.theme.WeatherSnapTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import presentation.camera.CameraScreen
import presentation.report.CreateReportScreen
import presentation.report.CreateReportViewModel
import presentation.reports.SavedReportsScreen
import presentation.reports.SavedReportsViewModel
import presentation.weather.WeatherScreen
import presentation.weather.WeatherViewModel

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherSnapTheme {
                WeatherSnapApp()
            }
        }
    }
}

@Composable
private fun WeatherSnapApp() {
    val navController = rememberNavController()
    val weatherViewModel: WeatherViewModel = hiltViewModel()
    val weatherState by weatherViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "weather",
        enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { it / 5 }) },
        exitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 5 }) },
        popEnterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { -it / 5 }) },
        popExitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { it / 5 }) }
    ) {
        composable("weather") {
            WeatherScreen(
                viewModel = weatherViewModel,
                onCreateReport = { navController.navigate("create-report") },
                onOpenReports = { navController.navigate("reports") }
            )
        }
        composable("create-report") { backStackEntry ->
            val handle = backStackEntry.savedStateHandle
            val imagePath by handle.getStateFlow("imagePath", "").collectAsState()
            val originalImageSize by handle.getStateFlow("originalImageSize", 0L).collectAsState()
            val compressedImageSize by handle.getStateFlow("compressedImageSize", 0L).collectAsState()
            val createReportViewModel: CreateReportViewModel = hiltViewModel()

            CreateReportScreen(
                weather = weatherState.weather,
                imagePath = imagePath,
                originalImageSize = originalImageSize,
                compressedImageSize = compressedImageSize,
                viewModel = createReportViewModel,
                onCapturePhoto = { navController.navigate("camera") },
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate("reports") {
                        popUpTo("weather")
                    }
                }
            )
        }
        composable("camera") {
            CameraScreen(
                onCaptured = { image ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("imagePath", image.path)
                        set("originalImageSize", image.originalSize)
                        set("compressedImageSize", image.compressedSize)
                    }
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }
        composable("reports") {
            val reportsViewModel: SavedReportsViewModel = hiltViewModel()
            SavedReportsScreen(
                viewModel = reportsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@HiltAndroidApp(Application::class)
class WeatherSnapAppApplication : Hilt_WeatherSnapAppApplication()
