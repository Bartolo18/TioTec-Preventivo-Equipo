package com.example.tiotec.repository

import com.example.tiotec.data.model.DatosRevision
import com.example.tiotec.data.model.RevisionRemota
import com.example.tiotec.data.remote.RetrofitInstance
import com.example.tiotec.data.remote.RevisionesApi
import retrofit2.HttpException

interface ServidorRepository {
    suspend fun listar(): List<RevisionRemota>
    suspend fun buscar(id: Long): RevisionRemota
    suspend fun guardar(id: Long?, datos: DatosRevision): RevisionRemota
    suspend fun eliminar(id: Long)
}
class ServidorRepositoryRemoto(
    private val api: RevisionesApi = RetrofitInstance.revisiones
) : ServidorRepository {
    override suspend fun listar(): List<RevisionRemota> {
        return api.listar()
    }

    override suspend fun buscar(id: Long): RevisionRemota {
        return api.buscar(id)
    }

    override suspend fun guardar(id: Long?, datos: DatosRevision): RevisionRemota {
        require(datos.errores().isEmpty()) { "Revisa el formulario." }

        val formulario = datos.normalizados()
        return if (id == null) {
            api.crear(formulario)
        } else {
            api.actualizar(id, formulario)
        }
    }

    override suspend fun eliminar(id: Long) {
        val respuesta = api.eliminar(id)
        if (!respuesta.isSuccessful) {
            throw HttpException(respuesta)
        }
    }
}
