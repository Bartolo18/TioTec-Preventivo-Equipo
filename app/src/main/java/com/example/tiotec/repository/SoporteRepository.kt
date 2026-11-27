package com.example.tiotec.repository

import com.example.tiotec.data.model.SistemaConsulta
import com.example.tiotec.data.model.VersionSistema
import com.example.tiotec.data.remote.RetrofitInstance
import com.example.tiotec.data.remote.SoporteApi
import java.io.IOException

interface SoporteRepository {
    suspend fun consultar(sistema: SistemaConsulta): List<VersionSistema>
}

class SoporteRepositoryRemoto(private val api: SoporteApi = RetrofitInstance.soporte) : SoporteRepository {
    override suspend fun consultar(sistema: SistemaConsulta): List<VersionSistema> {
        val versiones = api.consultar(sistema.producto).result?.releases
            ?: throw IOException("Respuesta incompleta del catálogo de versiones.")
        return versiones.filter { it.name.isNotBlank() }.distinctBy { it.name }
    }
}
