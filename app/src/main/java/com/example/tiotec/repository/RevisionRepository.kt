package com.example.tiotec.repository

import android.content.Context
import com.example.tiotec.data.Revision
import com.example.tiotec.data.RevisionesDb

// La pantalla y el ViewModel no necesitan conocer las consultas de Room.
class RevisionRepository(context: Context) : com.example.tiotec.data.RevisionStore {
    private val dao = RevisionesDb.obtener(context).dao()

    override fun observar() = dao.observar()
    override suspend fun buscar(id: Long) = dao.buscar(id)
    override suspend fun guardar(revision: Revision, nueva: Boolean): Long {
        if (nueva) return dao.insertar(revision)
        dao.actualizar(revision)
        return revision.id
    }
    override suspend fun eliminar(revision: Revision) = dao.eliminar(revision)
    override suspend fun marcarAccion(id: Long, bit: Int, hecha: Boolean) = dao.marcarAccion(id, bit, hecha)
}
