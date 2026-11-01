package com.focusquest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.focusquest.app.ui.MainScreen
import com.focusquest.app.ui.OnboardingScreen
import com.focusquest.app.ui.theme.FocusTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusTheme {
                val settings by vm.settings.collectAsState()
                val tasks by vm.tasks.collectAsState()
                val current = settings
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        current == null -> Box(Modifier.fillMaxSize())
                        !current.onboarded -> OnboardingScreen(onFinish = vm::finishOnboarding)
                        else -> MainScreen(current, tasks)
                    }
                }
            }
        }
    }
}
