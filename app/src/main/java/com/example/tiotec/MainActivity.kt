package com.example.tiotec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.example.tiotec.ui.AppTioTec
import com.example.tiotec.viewmodel.TioTecViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val ancho = calculateWindowSizeClass(this).widthSizeClass
            MaterialTheme {
                val vm: TioTecViewModel = viewModel()
                AppTioTec(vm, ancho)
            }
        }
    }
}
