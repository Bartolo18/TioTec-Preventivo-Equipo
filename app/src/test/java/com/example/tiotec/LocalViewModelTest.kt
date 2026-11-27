package com.example.tiotec
import android.app.Application
import android.net.Uri
import com.example.tiotec.data.*
import com.example.tiotec.model.*
import com.example.tiotec.viewmodel.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class LocalViewModelTest {
    private val dispatcher=StandardTestDispatcher()
    private class Store:RevisionStore {
        val lista=MutableStateFlow<List<Revision>>(emptyList())
        var fallo=false
        override fun observar()=lista
        override suspend fun buscar(id:Long)=lista.value.firstOrNull{it.id==id}
        override suspend fun guardar(revision:Revision,nueva:Boolean):Long {
            if(fallo) error("Error de prueba")
            val id=if(nueva) (lista.value.maxOfOrNull{it.id}?:0)+1 else revision.id
            lista.value=lista.value.filterNot{it.id==id}+revision.copy(id=id)
            return id
        }
        override suspend fun eliminar(revision:Revision) {
            if(fallo) error("Error de prueba")
            lista.value=lista.value.filterNot{it.id==revision.id}
        }
        override suspend fun marcarAccion(id:Long,bit:Int,hecha:Boolean) {
            if(fallo) error("Error de prueba")
            lista.value=lista.value.map{if(it.id==id) it.copy(accionesHechas=if(hecha)it.accionesHechas or bit else it.accionesHechas and bit.inv()) else it}
        }
    }
    private class Fotos:FotoStore {
        var valida=true
        val borradas=mutableListOf<String>()
        override fun nuevaCaptura():String=error("Sin cámara de prueba")
        override fun importar(uri:Uri)="/foto/nueva.jpg"
        override fun existe(path:String)=valida
        override fun borrarFoto(path:String){borradas+=path}
        override fun borrarCaptura(uri:String){}
    }
    private lateinit var store:Store
    private lateinit var fotos:Fotos
    private lateinit var vm:TioTecViewModel
    @Before fun iniciar(){
        Dispatchers.setMain(dispatcher)
        store=Store();fotos=Fotos();vm=TioTecViewModel(Application(),store,fotos,dispatcher)
    }
    @After fun terminar(){Dispatchers.resetMain()}
    private fun registro(id:Long=1,foto:String="/foto/uno.jpg",respuestas:List<String> = listOf("No","Sí","No sé","Sí"),acciones:Int=0)=Revision(
        id=id,nombre="Luffy",dispositivo="Notebook",sistema="Windows",experiencia="Básico",
        foto=foto,respaldo=respuestas[0],actualizacion=respuestas[1],autenticacion=respuestas[2],recuperacion=respuestas[3],accionesHechas=acciones)
    private fun completar(){
        vm.editar(registro(id=0));vm.ficticio(true);vm.diasRevision(30)
    }
    @Test fun validaCamposMientrasSeEscribeYNoGuardaVacio()=runTest(dispatcher){
        assertFalse(vm.guardar());assertTrue(vm.estado.value.errores.hayErrores)
        vm.nombre("Luffy");vm.dispositivo("Notebook");vm.sistema("Windows");vm.experiencia("Básico")
        vm.ficticio(true);vm.diasRevision(30)
        for(i in 0..3)vm.respuesta(i,"Sí")
        vm.respuesta(-1,"No");vm.respuesta(4,"No")
        assertNull(vm.estado.value.errores.nombre)
        assertNotNull(vm.estado.value.errores.foto)
        vm.nueva();assertFalse(vm.estado.value.validarMientrasEscribe)
    }
    @Test fun guardaNuevoYNormalizaCampos()=runTest(dispatcher){
        completar();vm.nombre("  Luffy  ");assertTrue(vm.guardar());advanceUntilIdle()
        assertEquals("Luffy",store.lista.value.single().nombre)
        assertEquals(1L,vm.estado.value.seleccionada)
        assertEquals(Formulario(),vm.estado.value.formulario)
        assertFalse(vm.estado.value.ocupado)
    }
    @Test fun fotoInexistenteYFalloDePersistenciaNoPierdenFormulario()=runTest(dispatcher){
        completar();fotos.valida=false;assertFalse(vm.guardar())
        assertNotNull(vm.estado.value.errores.foto)
        fotos.valida=true;store.fallo=true
        assertFalse(vm.guardar());assertFalse(vm.estado.value.ocupado)
        assertTrue(vm.estado.value.avisoError);assertEquals("Luffy",vm.estado.value.formulario.nombre)
    }
    @Test fun editarConservaFechaYAccionesNoModificadas()=runTest(dispatcher){
        val anterior=registro(acciones=5);store.lista.value=listOf(anterior);advanceUntilIdle()
        vm.editar(anterior);vm.respuesta(0,"Sí");vm.nombre("Zoro")
        assertTrue(vm.guardar());advanceUntilIdle()
        val guardada=store.lista.value.single()
        assertEquals(anterior.fecha,guardada.fecha);assertEquals(4,guardada.accionesHechas)
        assertEquals("Zoro",guardada.nombre)
    }
    @Test fun editarActualizaFotoYLiberaAnterior()=runTest(dispatcher){
        val anterior=registro();store.lista.value=listOf(anterior);advanceUntilIdle()
        vm.editar(anterior.copy(foto="/foto/dos.jpg"));assertTrue(vm.guardar());advanceUntilIdle()
        assertTrue("/foto/uno.jpg" in fotos.borradas)
    }
    @Test fun editarRegistroEliminadoNoLoRecrea()=runTest(dispatcher){
        vm.editar(registro(99));assertFalse(vm.guardar());assertTrue(store.lista.value.isEmpty())
    }
    @Test fun seleccionAccionesEliminacionYErrores()=runTest(dispatcher){
        store.lista.value=listOf(registro(),registro(2));advanceUntilIdle()
        vm.seleccionar(2);assertEquals(2L,vm.estado.value.seleccionada)
        vm.marcarAccion(registro(2),0,true);advanceUntilIdle()
        assertEquals(1,store.lista.value.last().accionesHechas)
        vm.marcarAccion(registro(2),0,false);advanceUntilIdle()
        assertEquals(0,store.lista.value.last().accionesHechas)
        vm.marcarAccion(registro(),-1,true)
        store.fallo=true;vm.marcarAccion(registro(),0,true);advanceUntilIdle()
        assertTrue(vm.estado.value.avisoError)
        vm.eliminar(registro());advanceUntilIdle();assertEquals(2,store.lista.value.size)
        store.fallo=false;vm.editar(registro());vm.eliminar(registro());advanceUntilIdle()
        assertEquals(1,store.lista.value.size);assertEquals(0L,vm.estado.value.formulario.id)
        assertTrue("/foto/uno.jpg" in fotos.borradas)
    }
    @Test fun avisosYCancelacionesDeCamaraYGaleria()=runTest(dispatcher){
        vm.avisar("Primero");val primero=vm.estado.value.avisoId
        vm.avisar("Segundo");vm.limpiarAviso(primero);assertEquals("Segundo",vm.estado.value.aviso)
        vm.limpiarAviso(vm.estado.value.avisoId);assertEquals("",vm.estado.value.aviso)
        assertNull(vm.prepararCamara());assertTrue(vm.estado.value.avisoError)
        vm.resultadoCamara(null,false);assertTrue(vm.estado.value.avisoError)
        vm.resultadoGaleria(null);assertTrue(vm.estado.value.aviso.contains("cancelada"))
        completar();vm.nueva();advanceUntilIdle();assertTrue("/foto/uno.jpg" in fotos.borradas)
    }
}

