package com.example.tiotec

import com.example.tiotec.data.model.*
import com.example.tiotec.repository.ServidorRepository
import com.example.tiotec.viewmodel.ServidorViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ServidorViewModelKotest : StringSpec({
    // ViewModel usa Main; las pruebas controlan su cola sin esperas reales.
    val dispatcher = StandardTestDispatcher()
    val datos = DatosRevision("Luffy", "Notebook", "Windows", respuestas = List(4) { "Sí" })
    val registro = RevisionRemota(1, 1, "Luffy", "Notebook", "Windows", "Básico", 30,
        List(4) { "Sí" }, 0)
    beforeTest { Dispatchers.setMain(dispatcher) }
    afterTest { Dispatchers.resetMain() }

    "consultar pasa de carga a una lista visible" {
        runTest(dispatcher) {
            val repo = mockk<ServidorRepository>()
            coEvery { repo.listar() } returns listOf(registro)
            val vm = ServidorViewModel(repo)
            vm.consultar()
            vm.estado.value.ocupado shouldBe true
            advanceUntilIdle()
            vm.estado.value.revisiones shouldContainExactly listOf(registro)
            vm.estado.value.ocupado shouldBe false
            coVerify(exactly = 1) { repo.listar() }
        }
    }

    "una consulta repetida durante la carga no duplica solicitudes" {
        runTest(dispatcher) {
            val repo = mockk<ServidorRepository>()
            coEvery { repo.listar() } returns emptyList()
            val vm = ServidorViewModel(repo)
            vm.consultar()
            vm.consultar()
            advanceUntilIdle()
            coVerify(exactly = 1) { repo.listar() }
        }
    }

    "un fallo al guardar conserva el formulario y permite reintentar" {
        runTest(dispatcher) {
            val repo = mockk<ServidorRepository>()
            coEvery { repo.guardar(null, datos) } throws IOException()
            val vm = ServidorViewModel(repo)
            vm.nueva()
            vm.cambiar(datos)
            vm.guardar()
            advanceUntilIdle()
            vm.estado.value.fallo shouldBe true
            vm.estado.value.datos shouldBe datos
            vm.estado.value.mostrarFormulario shouldBe true

            coEvery { repo.guardar(null, datos) } returns registro
            vm.guardar()
            advanceUntilIdle()
            vm.estado.value.fallo shouldBe false
            vm.estado.value.mostrarFormulario shouldBe false
            vm.estado.value.revisiones shouldContainExactly listOf(registro)
        }
    }

    "eliminar una evaluación actualiza la lista después de la respuesta" {
        runTest(dispatcher) {
            val repo = mockk<ServidorRepository>()
            coEvery { repo.listar() } returns listOf(registro)
            coEvery { repo.eliminar(1) } returns Unit
            val vm = ServidorViewModel(repo)
            vm.consultar()
            advanceUntilIdle()
            vm.eliminar(1)
            advanceUntilIdle()
            vm.estado.value.revisiones shouldBe emptyList()
            coVerify(exactly = 1) { repo.eliminar(1) }
        }
    }
})
