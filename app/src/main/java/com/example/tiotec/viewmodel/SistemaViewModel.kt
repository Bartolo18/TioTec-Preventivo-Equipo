package com.example.tiotec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotec.data.model.*
import com.example.tiotec.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class SistemaEstado(
    val sistema: SistemaConsulta = SistemaConsulta.ANDROID,
    val versiones: List<VersionSistema> = emptyList(),
    val versionSeleccionada: String = "",
    val resultado: VersionSistema? = null,
    val ocupado: Boolean = false,
    val consultado: Boolean = false,
    val mensaje: String = ""
)

class SistemaViewModel(private val repository: SoporteRepository = SoporteRepositoryRemoto()) : ViewModel() {
    private val mutable = MutableStateFlow(SistemaEstado())
    val estado = mutable.asStateFlow()

    fun cambiarSistema(sistema: SistemaConsulta) {
        if (estado.value.ocupado || sistema == estado.value.sistema) return
        mutable.value = SistemaEstado(sistema = sistema)
    }
    fun seleccionarVersion(nombre: String) {
        if (estado.value.ocupado) return
        if (estado.value.versiones.none { it.name == nombre }) return
        mutable.update { it.copy(versionSeleccionada = nombre, resultado = null, mensaje = "") }
    }
    fun verSoporte() {
        if (estado.value.ocupado) return
        val version = estado.value.versiones.find { it.name == estado.value.versionSeleccionada }
        mutable.update { it.copy(resultado = version,
            mensaje = if (version == null) "Selecciona una versión para consultar su soporte." else "") }
    }
    fun consultar() {
        if (estado.value.ocupado) return
        val sistema = estado.value.sistema
        mutable.update { it.copy(ocupado = true, versiones = emptyList(), versionSeleccionada = "",
            resultado = null, consultado = false, mensaje = "") }
        viewModelScope.launch {
            try {
                val versiones = repository.consultar(sistema)
                mutable.update { it.copy(versiones = versiones, consultado = true,
                    mensaje = if (versiones.isEmpty()) "La fuente no informó versiones para este sistema." else "") }
            } catch (cancelada: CancellationException) { throw cancelada }
            catch (error: Exception) {
                val mensaje = when (error) {
                    is HttpException -> if (error.code() == 429 || error.code() == 403)
                        "La fuente limitó las consultas. Vuelve a intentar más tarde."
                        else "La fuente no está disponible en este momento. Vuelve a intentar."
                    is IOException -> "No se pudieron obtener las versiones. Revisa internet y vuelve a intentar."
                    else -> "No se pudo consultar el soporte. Vuelve a intentar."
                }
                mutable.update { it.copy(mensaje = mensaje) }
            } finally { mutable.update { it.copy(ocupado = false) } }
        }
    }
}
