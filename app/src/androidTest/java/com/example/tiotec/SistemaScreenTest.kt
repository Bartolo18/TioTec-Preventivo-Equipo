package com.example.tiotec

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.tiotec.data.model.*
import com.example.tiotec.repository.SoporteRepository
import com.example.tiotec.ui.screens.SistemaScreen
import com.example.tiotec.viewmodel.SistemaViewModel
import org.junit.Rule
import org.junit.Test

class SistemaScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun consultaLaVersionSeleccionadaYMuestraElSoporte() {
        val repo = object : SoporteRepository {
            override suspend fun consultar(sistema: SistemaConsulta) = listOf(
                VersionSistema("14", "Android 14", "2023-10-04", false, null, true))
        }
        lateinit var vm: SistemaViewModel
        compose.runOnUiThread { vm = SistemaViewModel(repo) }
        compose.setContent { MaterialTheme { SistemaScreen(vm) } }
        compose.onNodeWithText("Consultar soporte").assertIsNotEnabled()
        compose.onNodeWithTag("selector_version").performClick()
        compose.onNodeWithText("Android 14").performClick()
        compose.onNodeWithText("Consultar soporte").performScrollTo().performClick()
        compose.onNodeWithText("Soporte vigente").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Lanzamiento: 04/10/2023").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fin de soporte: No informada").performScrollTo().assertIsDisplayed()
    }
}
