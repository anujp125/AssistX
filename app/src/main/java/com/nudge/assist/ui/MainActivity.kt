package com.nudge.assist.ui

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
import com.nudge.assist.AssistApplication
import com.nudge.assist.ui.theme.AssistXTheme

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
            AssistXTheme {
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
