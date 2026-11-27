package com.example.tiotec
import com.example.tiotec.model.*
import com.example.tiotec.repository.*
import com.example.tiotec.viewmodel.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NubeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val datos = DatosNube("Luffy","Notebook","Windows","Básico",30,listOf("No","Sí","No sé","Sí"))
    private fun revision(id: Long = 1) = RevisionNube(id, id, datos.nombre, datos.dispositivo, datos.sistema, datos.experiencia,30,datos.respuestas,0)
    @Before fun iniciar() { Dispatchers.setMain(dispatcher) }
    @After fun terminar() { Dispatchers.resetMain() }
    private inner class Falso : NubeRepository {
        var lista = listOf(revision())
        var fallo: Exception? = null
        var llamadas = 0
        override suspend fun listar(): List<RevisionNube> { llamadas++; fallo?.let { throw it }; return lista }
        override suspend fun buscar(id: Long): RevisionNube { fallo?.let { throw it }; return lista.first { it.id == id } }
        override suspend fun guardar(id: Long?, datos: DatosNube): RevisionNube { fallo?.let { throw it }; return revision(id ?: 2).copy(nombre = datos.nombre) }
        override suspend fun eliminar(id: Long) { fallo?.let { throw it }; lista = lista.filterNot { it.id == id } }
    }
    @Test fun consultaEditarCrearYEliminar() = runTest(dispatcher) {
        val repo = Falso(); val vm = NubeViewModel(repo)
        vm.consultar(); assertTrue(vm.estado.value.ocupado); advanceUntilIdle()
        assertEquals(1, vm.estado.value.revisiones.size)
        vm.editar(1); advanceUntilIdle(); assertEquals(1L, vm.estado.value.editando)
        vm.cambiar(datos.copy(nombre = "Nami")); vm.guardar(); advanceUntilIdle()
        assertEquals("Nami", vm.estado.value.revisiones.single().nombre)
        assertFalse(vm.estado.value.mostrarFormulario)
        vm.nueva(); vm.cambiar(datos); vm.guardar(); advanceUntilIdle()
        assertEquals(2,vm.estado.value.revisiones.size)
        vm.eliminar(1); advanceUntilIdle(); assertEquals(2L,vm.estado.value.revisiones.single().id)
        vm.nueva(); vm.cancelar(); assertFalse(vm.estado.value.mostrarFormulario)
    }
    @Test fun impideGuardarInvalidoYOperacionesSimultaneas() = runTest(dispatcher) {
        val repo=Falso();val vm=NubeViewModel(repo)
        vm.nueva();vm.guardar();assertTrue(vm.estado.value.errores.isNotEmpty())
        vm.consultar();vm.consultar();vm.nueva();vm.cambiar(datos);vm.cancelar();vm.guardar();vm.eliminar(1)
        advanceUntilIdle();assertEquals(1,repo.llamadas);assertEquals(DatosNube(),vm.estado.value.datos)
    }
    @Test fun erroresConservanDatosYPermitenReintentar() = runTest(dispatcher) {
        val repo=Falso();val vm=NubeViewModel(repo)
        vm.consultar();advanceUntilIdle()
        repo.fallo=IOException();vm.consultar();advanceUntilIdle()
        assertTrue(vm.estado.value.fallo);assertEquals(1,vm.estado.value.revisiones.size)
        vm.nueva();vm.cambiar(datos);vm.guardar();advanceUntilIdle()
        assertTrue(vm.estado.value.mostrarFormulario);assertEquals(datos,vm.estado.value.datos)
        vm.editar(1);advanceUntilIdle();assertTrue(vm.estado.value.fallo)
        vm.eliminar(1);advanceUntilIdle();assertEquals(1,vm.estado.value.revisiones.size)
        repo.fallo=null;vm.guardar();advanceUntilIdle();assertFalse(vm.estado.value.fallo)
    }
    @Test fun cancelacionNoSePresentaComoFalloDeRed() = runTest(dispatcher) {
        val repo=Falso();repo.fallo=CancellationException()
        val vm=NubeViewModel(repo);vm.consultar();advanceUntilIdle()
        assertFalse(vm.estado.value.ocupado);assertFalse(vm.estado.value.fallo)
    }
    @Test fun mensajesDistinguenProblemasDeConexionYHttp() {
        assertTrue(mensajeError(IOException()).contains("conexión"))
        for(code in listOf(400,403,404,429,500)) {
            val respuesta = Response.error<Unit>(code,ResponseBody.create(null,"{}"))
            assertTrue(mensajeError(HttpException(respuesta)).isNotBlank())
        }
        assertTrue(mensajeError(IllegalStateException()).contains("operación"))
    }
    @Test fun apiExternaManejaExitoErrorVacioYCancelacion() = runTest(dispatcher) {
        var fallo:Exception?=null;var llamadas=0
        val aviso=AvisoSeguridad("GHSA-prueba","Aviso de prueba","high","https://github.com/advisories/GHSA-prueba","2026-01-01")
        var respuesta=listOf(aviso)
        val repo=object:AvisosRepository{override suspend fun listar():List<AvisoSeguridad>{llamadas++;fallo?.let{throw it};return respuesta}}
        val vm=AvisosViewModel(repo);vm.consultar();vm.consultar();advanceUntilIdle()
        assertEquals(1,llamadas);assertEquals(aviso,vm.estado.value.avisos.single())
        fallo=IOException();vm.consultar();advanceUntilIdle();assertTrue(vm.estado.value.mensaje.isNotBlank())
        assertEquals(1,vm.estado.value.avisos.size)
        fallo=null;respuesta=emptyList();vm.consultar();advanceUntilIdle();assertTrue(vm.estado.value.avisos.isEmpty())
        fallo=CancellationException();vm.consultar();advanceUntilIdle();assertFalse(vm.estado.value.ocupado)
    }
}

