package com.example.tiotec
import com.example.tiotec.model.*
import com.example.tiotec.network.*
import com.example.tiotec.repository.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Response
import retrofit2.HttpException
import okhttp3.ResponseBody

class RepositoryTest {
    private val datos=DatosNube(" Luffy "," Notebook "," Windows ","Básico",30,List(4){"Sí"})
    @Test fun repositorioUsaLosEndpointsCorrectosYPropagaErrores()=runTest{
        var metodo="";var eliminacionCorrecta=true
        val registro=RevisionNube(1,1,"Luffy","Notebook","Windows","Básico",30,List(4){"Sí"},0)
        val api=object:RevisionesApi {
            override suspend fun listar():List<RevisionNube>{metodo="GET";return listOf(registro)}
            override suspend fun buscar(id:Long):RevisionNube{metodo="GET/$id";return registro}
            override suspend fun crear(datos:DatosNube):RevisionNube{metodo="POST";assertEquals("Luffy",datos.nombre);return registro}
            override suspend fun actualizar(id:Long,datos:DatosNube):RevisionNube{metodo="PUT/$id";return registro}
            override suspend fun eliminar(id:Long):Response<Unit>{metodo="DELETE/$id";return if(eliminacionCorrecta)Response.success(Unit)else Response.error(500,ResponseBody.create(null,"{}"))}
        }
        val repo=NubeRepositoryRemoto(api)
        assertEquals(1,repo.listar().size);assertEquals("GET",metodo)
        assertEquals(1L,repo.buscar(1).id);assertEquals("GET/1",metodo)
        repo.guardar(null,datos);assertEquals("POST",metodo)
        repo.guardar(1,datos);assertEquals("PUT/1",metodo)
        repo.eliminar(1);assertEquals("DELETE/1",metodo)
        eliminacionCorrecta=false
        try{repo.eliminar(1);fail()}catch(e:HttpException){assertEquals(500,e.code())}
        try{repo.guardar(null,DatosNube());fail()}catch(e:IllegalArgumentException){assertTrue(e.message!!.contains("formulario"))}
    }
    @Test fun avisosSeSolicitanAApiExterna()=runTest{
        var llamadas=0
        val repo=AvisosRepositoryRemoto(object:AvisosApi{
            override suspend fun listar(limite:Int,ecosistema:String):List<AvisoSeguridad>{
                llamadas++;assertEquals(6,limite);assertEquals("maven",ecosistema);return emptyList()
            }
        })
        assertTrue(repo.listar().isEmpty());assertEquals(1,llamadas)
    }
}

