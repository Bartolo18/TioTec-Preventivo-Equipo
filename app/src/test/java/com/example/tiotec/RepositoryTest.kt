package com.example.tiotec
import com.example.tiotec.data.model.*
import com.example.tiotec.data.remote.*
import com.example.tiotec.repository.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Response
import retrofit2.HttpException
import okhttp3.ResponseBody

class RepositoryTest {
    private val datos=DatosRevision(" Luffy "," Notebook "," Windows ","Básico",30,List(4){"Sí"})
    @Test fun repositorioUsaLosEndpointsCorrectosYPropagaErrores()=runTest{
        var metodo="";var eliminacionCorrecta=true
        val registro=RevisionRemota(1,1,"Luffy","Notebook","Windows","Básico",30,List(4){"Sí"},0)
        val api=object:RevisionesApi {
            override suspend fun listar():List<RevisionRemota>{metodo="GET";return listOf(registro)}
            override suspend fun buscar(id:Long):RevisionRemota{metodo="GET/$id";return registro}
            override suspend fun crear(datos:DatosRevision):RevisionRemota{metodo="POST";assertEquals("Luffy",datos.nombre);return registro}
            override suspend fun actualizar(id:Long,datos:DatosRevision):RevisionRemota{metodo="PUT/$id";return registro}
            override suspend fun eliminar(id:Long):Response<Unit>{metodo="DELETE/$id";return if(eliminacionCorrecta)Response.success(Unit)else Response.error(500,ResponseBody.create(null,"{}"))}
        }
        val repo=ServidorRepositoryRemoto(api)
        assertEquals(1,repo.listar().size);assertEquals("GET",metodo)
        assertEquals(1L,repo.buscar(1).id);assertEquals("GET/1",metodo)
        repo.guardar(null,datos);assertEquals("POST",metodo)
        repo.guardar(1,datos);assertEquals("PUT/1",metodo)
        repo.eliminar(1);assertEquals("DELETE/1",metodo)
        eliminacionCorrecta=false
        try{repo.eliminar(1);fail()}catch(e:HttpException){assertEquals(500,e.code())}
        try{repo.guardar(null,DatosRevision());fail()}catch(e:IllegalArgumentException){assertTrue(e.message!!.contains("formulario"))}
    }
}
