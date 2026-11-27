package com.example.tiotec

import com.example.tiotec.data.model.*
import com.example.tiotec.data.remote.SoporteApi
import com.example.tiotec.repository.*
import com.example.tiotec.viewmodel.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody
import org.junit.*
import org.junit.Assert.*
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SistemaTest {
    private val dispatcher = StandardTestDispatcher()
    private val vigente = VersionSistema("14", "14 'Upside Down Cake'", "2023-10-04", false, null, true)
    private val finalizada = VersionSistema("13", "13 'Tiramisu'", "2022-08-15", true, "2026-03-02", false)
    @Before fun iniciar() { Dispatchers.setMain(dispatcher) }
    @After fun terminar() { Dispatchers.resetMain() }

    private class Falso(var versiones: List<VersionSistema>) : SoporteRepository {
        var fallo: Exception? = null
        var llamadas = 0
        var solicitado: SistemaConsulta? = null
        override suspend fun consultar(sistema: SistemaConsulta): List<VersionSistema> {
            llamadas++; solicitado = sistema
            fallo?.let { throw it }
            return versiones
        }
    }

    @Test fun presentaEstadosSinInventarFechas() {
        assertEquals("Soporte vigente", vigente.estadoSoporte())
        assertEquals("Soporte finalizado", finalizada.estadoSoporte())
        assertTrue(finalizada.orientacion().contains("fabricante"))
        assertTrue(vigente.orientacion().contains("configuración"))
        val incierta = VersionSistema("8", " ")
        assertEquals("8", incierta.titulo())
        assertTrue(incierta.estadoSoporte().contains("Sin información"))
        assertTrue(incierta.orientacion().contains("fuente"))
        assertEquals("Soporte no finalizado", incierta.copy(isEol = false).estadoSoporte())
        assertTrue(incierta.copy(isEol = false).orientacion().contains("actualizaciones"))
        assertEquals("02/03/2026", fechaSoporte("2026-03-02"))
        for (fecha in listOf(null, "", "desconocida", "2026-02-30"))
            assertEquals("No informada", fechaSoporte(fecha))
    }

    @Test fun interpretaElFormatoRealDeLaApiConCamposOpcionales() {
        // Dos registros del catálogo Android consultado el 19-11-2026 y un caso incompleto de prueba.
        val json = """{"schema_version":"1.2.1","result":{"name":"android","releases":[
            {"name":"14","label":"14 'Upside Down Cake'","releaseDate":"2023-10-04","isEol":false,"eolFrom":null,"isMaintained":true},
            {"name":"13","label":"13 'Tiramisu'","releaseDate":"2022-08-15","isEol":true,"eolFrom":"2026-03-02","isMaintained":false},
            {"name":"sin-fecha","campoNuevo":"ignorado"}]}}"""
        val versiones = Gson().fromJson(json, RespuestaSoporte::class.java).result!!.releases!!
        assertEquals("Soporte vigente", versiones[0].estadoSoporte())
        assertEquals("Soporte finalizado", versiones[1].estadoSoporte())
        assertEquals("02/03/2026", fechaSoporte(versiones[1].eolFrom))
        assertTrue(versiones[2].estadoSoporte().contains("Sin información"))
        assertEquals("No informada", fechaSoporte(versiones[2].releaseDate))
    }

    @Test fun repositorioUsaProductoSeleccionadoYRechazaRespuestasIncompletas() = runTest {
        var producto = ""
        var respuesta = RespuestaSoporte(ProductoSoporte(listOf(vigente, vigente, VersionSistema(""))))
        val repo = SoporteRepositoryRemoto(object : SoporteApi {
            override suspend fun consultar(nombre: String): RespuestaSoporte { producto = nombre; return respuesta }
        })
        assertEquals(listOf(vigente), repo.consultar(SistemaConsulta.ANDROID))
        assertEquals("android", producto)
        repo.consultar(SistemaConsulta.WINDOWS); assertEquals("windows", producto)
        assertEquals("https://endoflife.date/windows", SistemaConsulta.WINDOWS.enlace)
        respuesta = RespuestaSoporte(ProductoSoporte(emptyList()))
        assertTrue(repo.consultar(SistemaConsulta.ANDROID).isEmpty())
        for (incompleta in listOf(RespuestaSoporte(), RespuestaSoporte(ProductoSoporte()))) {
            respuesta = incompleta
            try { repo.consultar(SistemaConsulta.ANDROID); fail("Debe rechazar una respuesta incompleta") }
            catch (error: IOException) { assertTrue(error.message!!.contains("incompleta")) }
        }
    }

    @Test fun exigeVersionYReiniciaElResultadoAlCambiarDeSistema() = runTest(dispatcher) {
        val repo = Falso(listOf(vigente, finalizada)); val vm = SistemaViewModel(repo)
        vm.consultar(); advanceUntilIdle()
        vm.verSoporte(); assertTrue(vm.estado.value.mensaje.contains("Selecciona"))
        vm.seleccionarVersion("inexistente"); assertEquals("", vm.estado.value.versionSeleccionada)
        vm.seleccionarVersion("13"); vm.verSoporte(); assertEquals(finalizada, vm.estado.value.resultado)
        vm.seleccionarVersion("14"); assertNull(vm.estado.value.resultado)
        vm.verSoporte(); assertEquals(vigente, vm.estado.value.resultado)
        vm.cambiarSistema(SistemaConsulta.ANDROID); assertEquals(vigente, vm.estado.value.resultado)
        vm.cambiarSistema(SistemaConsulta.WINDOWS)
        assertNull(vm.estado.value.resultado); assertTrue(vm.estado.value.versiones.isEmpty())
        assertFalse(vm.estado.value.consultado); assertEquals("", vm.estado.value.versionSeleccionada)
        vm.consultar(); advanceUntilIdle(); assertEquals(SistemaConsulta.WINDOWS, repo.solicitado)
    }

    @Test fun impideConsultasSimultaneasYSeleccionMientrasCarga() = runTest(dispatcher) {
        val repo = Falso(listOf(vigente)); val vm = SistemaViewModel(repo)
        vm.consultar(); assertTrue(vm.estado.value.ocupado)
        vm.consultar(); vm.cambiarSistema(SistemaConsulta.WINDOWS)
        vm.seleccionarVersion("14"); vm.verSoporte()
        advanceUntilIdle()
        assertEquals(1, repo.llamadas); assertEquals(SistemaConsulta.ANDROID, vm.estado.value.sistema)
        assertEquals("", vm.estado.value.versionSeleccionada); assertNull(vm.estado.value.resultado)
    }

    @Test fun permiteReintentarYEvitaMostrarResultadosAnterioresTrasUnFallo() = runTest(dispatcher) {
        val repo = Falso(listOf(vigente)); val vm = SistemaViewModel(repo)
        vm.consultar(); advanceUntilIdle(); vm.seleccionarVersion("14"); vm.verSoporte()
        repo.fallo = IOException(); vm.consultar(); advanceUntilIdle()
        assertNull(vm.estado.value.resultado); assertTrue(vm.estado.value.versiones.isEmpty())
        assertTrue(vm.estado.value.mensaje.contains("internet")); assertFalse(vm.estado.value.ocupado)
        repo.fallo = null; repo.versiones = emptyList(); vm.consultar(); advanceUntilIdle()
        assertTrue(vm.estado.value.consultado); assertTrue(vm.estado.value.mensaje.contains("no informó"))
        repo.versiones = listOf(finalizada); vm.consultar(); advanceUntilIdle()
        assertEquals(listOf(finalizada), vm.estado.value.versiones); assertEquals("", vm.estado.value.mensaje)
    }

    @Test fun presentaErroresHttpYErroresInesperadosSinAtribuirlosAlServidorLocal() = runTest(dispatcher) {
        val repo = Falso(emptyList()); val vm = SistemaViewModel(repo)
        for (codigo in listOf(403, 429, 404, 500)) {
            repo.fallo = HttpException(Response.error<Unit>(codigo, ResponseBody.create(null, "{}")))
            vm.consultar(); advanceUntilIdle()
            assertTrue(vm.estado.value.mensaje.contains(if (codigo in listOf(403,429)) "limitó" else "disponible"))
            assertFalse(vm.estado.value.ocupado)
        }
        repo.fallo = IllegalStateException(); vm.consultar(); advanceUntilIdle()
        assertTrue(vm.estado.value.mensaje.contains("consultar el soporte"))
    }

    @Test fun propagaCancelacionSinMostrarUnFallo() = runTest(dispatcher) {
        val repo = Falso(emptyList()); repo.fallo = CancellationException()
        val vm = SistemaViewModel(repo); vm.consultar(); advanceUntilIdle()
        assertFalse(vm.estado.value.ocupado); assertEquals("", vm.estado.value.mensaje)
    }
}
