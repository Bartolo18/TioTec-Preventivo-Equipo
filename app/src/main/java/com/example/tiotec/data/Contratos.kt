package com.example.tiotec.data
import android.net.Uri
import kotlinx.coroutines.flow.Flow
interface RevisionStore {
    fun observar(): Flow<List<Revision>>
    suspend fun buscar(id: Long): Revision?
    suspend fun guardar(revision: Revision, nueva: Boolean): Long
    suspend fun eliminar(revision: Revision)
    suspend fun marcarAccion(id: Long, bit: Int, hecha: Boolean)
}
interface FotoStore {
    fun nuevaCaptura(): String
    fun importar(uri: Uri): String
    fun existe(path: String): Boolean
    fun borrarFoto(path: String)
    fun borrarCaptura(uri: String)
}

