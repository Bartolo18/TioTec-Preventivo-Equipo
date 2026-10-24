package com.example.tiotec.repository

import android.content.Context
import com.example.tiotec.data.Revision
import com.example.tiotec.data.RevisionesDb

// La pantalla y el ViewModel no necesitan conocer las consultas de Room.
class RevisionRepository(context: Context) {
    private val dao = RevisionesDb.obtener(context).dao()

    fun observar() = dao.observar()
    suspend fun buscar(id: Long) = dao.buscar(id)
    suspend fun guardar(revision: Revision, nueva: Boolean): Long {
        if (nueva) return dao.insertar(revision)
        dao.actualizar(revision)
        return revision.id
    }
    suspend fun eliminar(revision: Revision) = dao.eliminar(revision)
    suspend fun marcarAccion(id: Long, bit: Int, hecha: Boolean) = dao.marcarAccion(id, bit, hecha)
}
