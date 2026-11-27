package com.example.tiotec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotec.model.*
import com.example.tiotec.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException

data class NubeEstado(
    val revisiones: List<RevisionNube> = emptyList(),
    val datos: DatosNube = DatosNube(),
    val editando: Long? = null,
    val mostrarFormulario: Boolean = false,
    val ocupado: Boolean = false,
    val errores: List<String> = emptyList(),
    val mensaje: String = "",
    val fallo: Boolean = false,
    val consultado: Boolean = false
)
class NubeViewModel(private val repository: NubeRepository = NubeRepositoryRemoto()) : ViewModel() {
    private val mutable = MutableStateFlow(NubeEstado())
    val estado: StateFlow<NubeEstado> = mutable.asStateFlow()
    fun nueva() { if (!estado.value.ocupado) mutable.update { it.copy(datos = DatosNube(), editando = null, mostrarFormulario = true, errores = emptyList(), mensaje = "", fallo = false) } }
    fun cancelar() { if (!estado.value.ocupado) mutable.update { it.copy(mostrarFormulario = false, editando = null, errores = emptyList()) } }
    fun cambiar(datos: DatosNube) { if (!estado.value.ocupado) mutable.update { it.copy(datos = datos, errores = emptyList()) } }
    fun consultar() = operar {
        val lista = repository.listar()
        mutable.update { it.copy(revisiones = lista, consultado = true, mensaje = "Evaluaciones consultadas en el servidor.") }
    }
    fun editar(id: Long) = operar {
        val revision = repository.buscar(id)
        mutable.update { it.copy(datos = revision.datos(), editando = id, mostrarFormulario = true, errores = emptyList()) }
    }
    fun guardar() {
        if (estado.value.ocupado) return
        val errores = estado.value.datos.errores()
        if (errores.isNotEmpty()) { mutable.update { it.copy(errores = errores) }; return }
        val id = estado.value.editando
        val datos = estado.value.datos
        operar {
            val guardada = repository.guardar(id, datos)
            mutable.update { it.copy(
                revisiones = (it.revisiones.filterNot { r -> r.id == guardada.id } + guardada).sortedByDescending { r -> r.fecha },
                mostrarFormulario = false, editando = null, datos = DatosNube(), consultado = true,
                mensaje = if (id == null) "Evaluación creada en el servidor." else "Cambios guardados en el servidor."
            ) }
        }
    }
    fun eliminar(id: Long) = operar {
        repository.eliminar(id)
        mutable.update { it.copy(revisiones = it.revisiones.filterNot { r -> r.id == id }, mensaje = "Evaluación eliminada del servidor.") }
    }
    private fun operar(tarea: suspend () -> Unit) {
        if (estado.value.ocupado) return
        mutable.update { it.copy(ocupado = true, fallo = false, mensaje = "") }
        viewModelScope.launch {
            try { tarea() }
            catch (cancelada: CancellationException) { throw cancelada }
            catch (error: Exception) { mutable.update { it.copy(fallo = true, mensaje = mensajeError(error)) } }
            finally { mutable.update { it.copy(ocupado = false) } }
        }
    }
}
data class AvisosEstado(val avisos: List<AvisoSeguridad> = emptyList(), val ocupado: Boolean = false, val mensaje: String = "", val consultado: Boolean = false)
class AvisosViewModel(private val repository: AvisosRepository = AvisosRepositoryRemoto()) : ViewModel() {
    private val mutable = MutableStateFlow(AvisosEstado())
    val estado = mutable.asStateFlow()
    fun consultar() {
        if (estado.value.ocupado) return
        mutable.update { it.copy(ocupado = true, mensaje = "") }
        viewModelScope.launch {
            try { val avisos = repository.listar(); mutable.update { it.copy(avisos = avisos, consultado = true) } }
            catch (cancelada: CancellationException) { throw cancelada }
            catch (error: Exception) { mutable.update { it.copy(mensaje = mensajeError(error)) } }
            finally { mutable.update { it.copy(ocupado = false) } }
        }
    }
}

