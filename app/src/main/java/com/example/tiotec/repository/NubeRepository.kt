package com.example.tiotec.repository

import com.example.tiotec.model.*
import com.example.tiotec.network.*
import retrofit2.HttpException

interface NubeRepository {
    suspend fun listar(): List<RevisionNube>
    suspend fun buscar(id: Long): RevisionNube
    suspend fun guardar(id: Long?, datos: DatosNube): RevisionNube
    suspend fun eliminar(id: Long)
}
class NubeRepositoryRemoto(private val api: RevisionesApi = Servicios.revisiones) : NubeRepository {
    override suspend fun listar() = api.listar()
    override suspend fun buscar(id: Long) = api.buscar(id)
    override suspend fun guardar(id: Long?, datos: DatosNube): RevisionNube {
        require(datos.errores().isEmpty()) { "Revisa el formulario." }
        return if (id == null) api.crear(datos.normalizados()) else api.actualizar(id, datos.normalizados())
    }
    override suspend fun eliminar(id: Long) {
        val respuesta = api.eliminar(id)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
    }
}
interface AvisosRepository { suspend fun listar(): List<AvisoSeguridad> }
class AvisosRepositoryRemoto(private val api: AvisosApi = Servicios.avisos) : AvisosRepository {
    override suspend fun listar() = api.listar()
}

