package com.example.tiotec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotec.data.model.DatosRevision
import com.example.tiotec.data.model.RevisionRemota
import com.example.tiotec.repository.ServidorRepository
import com.example.tiotec.repository.ServidorRepositoryRemoto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ServidorEstado(
    val revisiones: List<RevisionRemota> = emptyList(),
    val datos: DatosRevision = DatosRevision(),
    val editando: Long? = null,
    val mostrarFormulario: Boolean = false,
    val ocupado: Boolean = false,
    val errores: List<String> = emptyList(),
    val mensaje: String = "",
    val fallo: Boolean = false,
    val consultado: Boolean = false
)

class ServidorViewModel(
    private val repository: ServidorRepository = ServidorRepositoryRemoto()
) : ViewModel() {
    private val mutable = MutableStateFlow(ServidorEstado())
    val estado: StateFlow<ServidorEstado> = mutable.asStateFlow()

    fun nueva() {
        if (estado.value.ocupado) return
        mutable.update {
            it.copy(
                datos = DatosRevision(),
                editando = null,
                mostrarFormulario = true,
                errores = emptyList(),
                mensaje = "",
                fallo = false
            )
        }
    }

    fun cancelar() {
        if (estado.value.ocupado) return
        mutable.update {
            it.copy(mostrarFormulario = false, editando = null, errores = emptyList())
        }
    }

    fun cambiar(datos: DatosRevision) {
        if (estado.value.ocupado) return
        mutable.update { it.copy(datos = datos, errores = emptyList()) }
    }

    fun consultar() = operar {
        val lista = repository.listar()
        mutable.update {
            it.copy(
                revisiones = lista,
                consultado = true,
                mensaje = "Evaluaciones consultadas en el servidor."
            )
        }
    }

    fun editar(id: Long) = operar {
        val revision = repository.buscar(id)
        mutable.update {
            it.copy(
                datos = revision.datos(),
                editando = id,
                mostrarFormulario = true,
                errores = emptyList()
            )
        }
    }

    fun guardar() {
        if (estado.value.ocupado) return

        val errores = estado.value.datos.errores()
        if (errores.isNotEmpty()) {
            mutable.update { it.copy(errores = errores) }
            return
        }

        val id = estado.value.editando
        val datos = estado.value.datos
        operar {
            val guardada = repository.guardar(id, datos)
            mutable.update { actual ->
                val lista = actual.revisiones.filterNot { it.id == guardada.id } + guardada
                actual.copy(
                    revisiones = lista.sortedByDescending { it.fecha },
                    mostrarFormulario = false,
                    editando = null,
                    datos = DatosRevision(),
                    consultado = true,
                    mensaje = if (id == null) {
                        "Evaluación creada en el servidor."
                    } else {
                        "Cambios guardados en el servidor."
                    }
                )
            }
        }
    }

    fun eliminar(id: Long) = operar {
        repository.eliminar(id)
        mutable.update {
            it.copy(
                revisiones = it.revisiones.filterNot { revision -> revision.id == id },
                mensaje = "Evaluación eliminada del servidor."
            )
        }
    }

    private fun operar(tarea: suspend () -> Unit) {
        if (estado.value.ocupado) return
        mutable.update { it.copy(ocupado = true, fallo = false, mensaje = "") }

        viewModelScope.launch {
            try {
                tarea()
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (error: Exception) {
                mutable.update { it.copy(fallo = true, mensaje = mensajeError(error)) }
            } finally {
                mutable.update { it.copy(ocupado = false) }
            }
        }
    }
}

fun mensajeError(error: Exception): String = when (error) {
    is HttpException -> when (error.code()) {
        400 -> "El servidor rechazó los datos. Revisa el formulario."
        404 -> "La evaluación ya no existe. Vuelve a consultar."
        403, 429 -> "El servicio limitó las consultas. Intenta más tarde."
        else -> "El servidor no pudo completar la operación (${error.code()})."
    }
    is IOException -> "No hay conexión con el servicio. Comprueba que esté iniciado y vuelve a intentar."
    else -> "No se pudo completar la operación. Intenta nuevamente."
}
