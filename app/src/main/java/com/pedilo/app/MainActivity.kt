package com.pedilo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedilo.app.data.FirebasePediloRepository
import com.pedilo.app.ui.PediloScreen
import com.pedilo.app.ui.PediloViewModel
import com.pedilo.app.ui.PediloViewModelFactory
import com.pedilo.app.ui.theme.PediloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PediloTheme {
                val viewModel: PediloViewModel = viewModel(
                    factory = PediloViewModelFactory(FirebasePediloRepository())
                )
                PediloScreen(viewModel = viewModel)
            }
        }
    }
}
