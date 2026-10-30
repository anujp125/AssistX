package com.assistx.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.assistx.AssistApplication
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        setContent {
            val settings by viewModel.settings.collectAsState()
            val accent = MetroAccent.fromHex(settings.accentHex)
            val themeMode = when (settings.themeMode.lowercase()) {
                "light" -> ThemeMode.LIGHT
                "system" -> ThemeMode.SYSTEM
                else -> ThemeMode.DARK
            }

            AssistXTheme(accent = accent, themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudioScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onScreenResumed(this)
    }
}
