package com.example.tiotec

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.tiotec.data.model.*
import com.example.tiotec.repository.ServidorRepository
import com.example.tiotec.ui.screens.ServidorScreen
import com.example.tiotec.viewmodel.ServidorViewModel
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Guía 15, página 6: prueba instrumentada de Compose con Rule y JUnit 4.
// El repositorio simulado permite comprobar la pantalla sin un servidor real.
class ServidorScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private class RepositorioPrueba : ServidorRepository {
        val registro = RevisionRemota(1, 1, "Luffy", "Notebook", "Windows", "Básico", 30,
            List(4) { "Sí" }, 0)
        private var lista = listOf(registro)
        var sinConexion = false
        override suspend fun listar(): List<RevisionRemota> {
            if (sinConexion) throw IOException()
            return lista
        }
        override suspend fun buscar(id: Long) = lista.single { it.id == id }
        override suspend fun guardar(id: Long?, datos: DatosRevision): RevisionRemota {
            val guardada = registro.copy(id = id ?: 2, nombre = datos.nombre)
            lista = lista.filterNot { it.id == guardada.id } + guardada
            return guardada
        }
        override suspend fun eliminar(id: Long) { lista = lista.filterNot { it.id == id } }
    }

    private fun abrir(repo: RepositorioPrueba = RepositorioPrueba()): ServidorViewModel {
        lateinit var vm: ServidorViewModel
        compose.runOnUiThread { vm = ServidorViewModel(repo) }
        compose.setContent { MaterialTheme { ServidorScreen(vm) } }
        compose.waitForIdle()
        return vm
    }

    @Test fun muestraLaEvaluacionRecibidaDelRepositorio() {
        abrir()
        compose.onNodeWithText("#1 · Luffy").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Notebook · Windows").assertIsDisplayed()
    }

    @Test fun guardarVacioMuestraLaValidacionEnPantalla() {
        abrir()
        compose.onNodeWithText("Nueva evaluación").performClick()
        compose.onNodeWithText("Guardar en servidor").performScrollTo().performClick()
        compose.onNodeWithText("Nombre ficticio: entre 2 y 40 caracteres.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test fun editarYConfirmarEliminacionActualizaLaListaVisible() {
        val vm = abrir()
        compose.onNodeWithText("Editar").performScrollTo().performClick()
        compose.onNode(hasSetTextAction() and hasText("Nombre ficticio"))
            .performScrollTo().performTextReplacement("Nami")
        compose.onNodeWithText("Guardar en servidor").performScrollTo().performClick()
        compose.onNodeWithText("#1 · Nami").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Eliminar").performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("#1 · Nami").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Eliminar").performScrollTo().performClick()
        compose.onNode(hasText("Eliminar") and hasAnyAncestor(isDialog())).performClick()
        compose.waitUntil { vm.estado.value.revisiones.isEmpty() }
        compose.onNodeWithText("Todavía no hay evaluaciones en el servidor.")
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("#1 · Nami").assertDoesNotExist()
    }

    @Test fun sinConexionMuestraUnErrorYPermiteReintentar() {
        val repo = RepositorioPrueba().apply { sinConexion = true }
        abrir(repo)
        compose.onNodeWithText("No hay conexión con el servicio.", substring = true).assertIsDisplayed()
        compose.runOnUiThread { repo.sinConexion = false }
        compose.onNodeWithText("Actualizar").performClick()
        compose.onNodeWithText("#1 · Luffy").performScrollTo().assertIsDisplayed()
    }
}
