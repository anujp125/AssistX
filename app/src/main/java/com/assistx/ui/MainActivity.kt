package com.assistx.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.assistx.AssistApplication
import com.assistx.ui.components.AssistXSplashScreen
import com.assistx.ui.theme.AssistXTheme
import com.assistx.ui.theme.MetroAccent
import com.assistx.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as AssistApplication
                return StudioViewModel(
                    stateCoordinator = app.stateCoordinator,
                    runtimeController = app.runtimeController,
                    permissionAdapter = app.overlayPermissionAdapter,
                    accessibilityAdapter = app.accessibilityAdapter,
                    diagnosticRecorder = app.diagnosticRecorder,
                    userPreferences = app.overlayPreferences
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 12+ (API 31+) Hand off immediately to Compose animated splash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }

        setContent {
            val settings by viewModel.settings.collectAsState()
            val accent = MetroAccent.fromHex(settings.accentHex)
            val themeMode = when (settings.themeMode.lowercase()) {
                "light" -> ThemeMode.LIGHT
                "system" -> ThemeMode.SYSTEM
                else -> ThemeMode.DARK
            }

            var isSplashVisible by rememberSaveable { mutableStateOf(true) }

            AssistXTheme(accent = accent, themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        StudioScreen(viewModel = viewModel)

                        if (isSplashVisible) {
                            AssistXSplashScreen(
                                accentColor = accent.color,
                                onAnimationComplete = {
                                    isSplashVisible = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onScreenResumed(this)
    }
}
