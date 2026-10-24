package com.example.tiotec.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiotec.data.FotoStorage
import com.example.tiotec.data.Revision
import com.example.tiotec.model.Errores
import com.example.tiotec.model.Formulario
import com.example.tiotec.repository.RevisionRepository
import com.example.tiotec.validation.Validacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PantallaEstado(
    val formulario: Formulario = Formulario(),
    val errores: Errores = Errores(),
    val validarMientrasEscribe: Boolean = false,
    val revisiones: List<Revision> = emptyList(),
    val seleccionada: Long? = null,
    val ocupado: Boolean = false,
    val aviso: String = "",
    val avisoError: Boolean = false,
    val avisoId: Int = 0
)

class TioTecViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RevisionRepository(application)
    private val fotos = FotoStorage(application)
    private var versionFormulario = 0
    private val mutableEstado = MutableStateFlow(PantallaEstado())
    val estado: StateFlow<PantallaEstado> = mutableEstado

    init {
        viewModelScope.launch {
            repositorio.observar().collect { lista ->
                mutableEstado.update { actual ->
                    actual.copy(
                        revisiones = lista,
                        seleccionada = actual.seleccionada?.takeIf { id -> lista.any { it.id == id } }
                            ?: lista.firstOrNull()?.id
                    )
                }
            }
        }
    }

    private fun cambiarFormulario(nuevo: Formulario, mostrar: (Errores, Errores) -> Errores) {
        mutableEstado.update { actual ->
            val revisados = Validacion.revisar(nuevo)
            actual.copy(
                formulario = nuevo,
                errores = if (actual.validarMientrasEscribe) revisados else mostrar(actual.errores, revisados)
            )
        }
    }

    fun nombre(valor: String) = cambiarFormulario(estado.value.formulario.copy(nombre = valor)) { anterior, nuevo ->
        anterior.copy(nombre = nuevo.nombre)
    }
    fun dispositivo(valor: String) = cambiarFormulario(estado.value.formulario.copy(dispositivo = valor)) { anterior, nuevo ->
        anterior.copy(dispositivo = nuevo.dispositivo)
    }
    fun sistema(valor: String) = cambiarFormulario(estado.value.formulario.copy(sistema = valor)) { anterior, nuevo ->
        anterior.copy(sistema = nuevo.sistema)
    }
    fun experiencia(valor: String) = cambiarFormulario(estado.value.formulario.copy(experiencia = valor)) { anterior, nuevo ->
        anterior.copy(experiencia = nuevo.experiencia)
    }
    fun diasRevision(valor: Int) = cambiarFormulario(estado.value.formulario.copy(diasRevision = valor)) { anterior, nuevo ->
        anterior.copy(diasRevision = nuevo.diasRevision)
    }
    fun ficticio(valor: Boolean) = cambiarFormulario(estado.value.formulario.copy(ficticio = valor)) { anterior, nuevo ->
        anterior.copy(ficticio = nuevo.ficticio)
    }
    fun respuesta(indice: Int, valor: String) {
        val lista = estado.value.formulario.respuestas.toMutableList()
        if (indice in lista.indices) {
            lista[indice] = valor
            cambiarFormulario(estado.value.formulario.copy(respuestas = lista)) { anterior, nuevo ->
                val errores = anterior.respuestas.toMutableList()
                errores[indice] = nuevo.respuestas[indice]
                anterior.copy(respuestas = errores)
            }
        }
    }

    fun nueva() {
        liberarFotoSinGuardar()
        versionFormulario++
        mutableEstado.update { it.copy(formulario = Formulario(), errores = Errores(), validarMientrasEscribe = false) }
    }

    fun editar(revision: Revision) {
        liberarFotoSinGuardar()
        versionFormulario++
        mutableEstado.update {
            it.copy(
                formulario = Formulario(
                    id = revision.id, nombre = revision.nombre, dispositivo = revision.dispositivo,
                    sistema = revision.sistema, experiencia = revision.experiencia, foto = revision.foto,
                    diasRevision = revision.diasRevision,
                    ficticio = true, respuestas = revision.respuestas
                ),
                errores = Errores(), validarMientrasEscribe = false
            )
        }
    }

    private fun liberarFotoSinGuardar() {
        val actual = estado.value
        val ruta = actual.formulario.foto
        if (ruta.isNotBlank() && actual.revisiones.none { it.foto == ruta }) {
            viewModelScope.launch(Dispatchers.IO) { fotos.borrarFoto(ruta) }
        }
    }

    fun seleccionar(id: Long) { mutableEstado.update { it.copy(seleccionada = id) } }

    fun avisar(texto: String, error: Boolean = false) {
        mutableEstado.update { it.copy(aviso = texto, avisoError = error, avisoId = it.avisoId + 1) }
    }

    fun limpiarAviso(id: Int) {
        mutableEstado.update { if (it.avisoId == id) it.copy(aviso = "") else it }
    }

    suspend fun guardar(): Boolean {
        if (estado.value.ocupado) return false
        val form = estado.value.formulario
        val errores = Validacion.revisar(form)
        if (errores.hayErrores || !fotos.existe(form.foto)) {
            mutableEstado.update {
                it.copy(errores = if (fotos.existe(form.foto)) errores else errores.copy(foto = "Selecciona una foto válida."),
                    validarMientrasEscribe = true)
            }
            avisar("Revisa los campos marcados.", true)
            return false
        }
        mutableEstado.update { it.copy(ocupado = true) }
        return try {
            val id = withContext(Dispatchers.IO) {
                val anterior = if (form.id == 0L) null else repositorio.buscar(form.id)
                if (form.id != 0L && anterior == null) error("La revisión ya no existe.")
                val revision = Revision(
                    id = form.id, fecha = anterior?.fecha ?: System.currentTimeMillis(),
                    nombre = form.nombre.trim(), dispositivo = form.dispositivo.trim(),
                    sistema = form.sistema.trim(), experiencia = form.experiencia, foto = form.foto,
                    diasRevision = form.diasRevision,
                    respaldo = form.respuestas[0], actualizacion = form.respuestas[1],
                    autenticacion = form.respuestas[2], recuperacion = form.respuestas[3],
                    accionesHechas = (0..3).fold(0) { mascara, i ->
                        if (anterior != null && form.respuestas[i] != "Sí" &&
                            form.respuestas[i] == anterior.respuestas[i] &&
                            anterior.accionesHechas and (1 shl i) != 0
                        ) mascara or (1 shl i) else mascara
                    }
                )
                val guardado = repositorio.guardar(revision, anterior == null)
                if (anterior != null && anterior.foto != form.foto) fotos.borrarFoto(anterior.foto)
                guardado
            }
            mutableEstado.update {
                it.copy(formulario = Formulario(), errores = Errores(), validarMientrasEscribe = false,
                    seleccionada = id, ocupado = false)
            }
            versionFormulario++
            avisar("Evaluación guardada.")
            true
        } catch (_: Exception) {
            mutableEstado.update { it.copy(ocupado = false) }
            avisar("No se pudo guardar. Intenta de nuevo.", true)
            false
        }
    }

    fun eliminar(revision: Revision) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repositorio.eliminar(revision)
                    fotos.borrarFoto(revision.foto)
                }
                if (estado.value.formulario.id == revision.id) nueva()
                avisar("Evaluación eliminada.")
            } catch (_: Exception) { avisar("No se pudo eliminar.", true) }
        }
    }

    fun marcarAccion(revision: Revision, indice: Int, hecha: Boolean) {
        if (indice !in 0..3) return
        val bit = 1 shl indice
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { repositorio.marcarAccion(revision.id, bit, hecha) } }
            catch (_: Exception) { avisar("No se pudo guardar el avance.", true) }
        }
    }

    fun prepararCamara(): Uri? = try {
        Uri.parse(fotos.nuevaCaptura())
    } catch (_: Exception) {
        avisar("No se pudo abrir la cámara.", true)
        null
    }

    fun errorCamara(uri: Uri) {
        fotos.borrarCaptura(uri.toString())
        avisar("No hay una cámara disponible.", true)
    }

    fun resultadoCamara(uri: Uri?, exito: Boolean) {
        if (uri == null) {
            avisar("No se pudo recuperar la captura. Intenta otra vez.", true)
            return
        }
        if (exito) importarFoto(uri, true) else {
            fotos.borrarCaptura(uri.toString())
            avisar("Captura cancelada.")
        }
    }

    fun resultadoGaleria(uri: Uri?) {
        if (uri == null) avisar("Selección cancelada.") else importarFoto(uri, false)
    }

    private fun importarFoto(uri: Uri, esCaptura: Boolean) {
        val version = versionFormulario
        viewModelScope.launch {
            mutableEstado.update { it.copy(ocupado = true) }
            try {
                val ruta = withContext(Dispatchers.IO) { fotos.importar(uri) }
                if (version != versionFormulario) {
                    withContext(Dispatchers.IO) { fotos.borrarFoto(ruta) }
                    avisar("La foto se descartó porque cambiaste de formulario.", true)
                    return@launch
                }
                val anterior = estado.value.formulario.foto
                val guardada = estado.value.revisiones.any { it.foto == anterior }
                cambiarFormulario(estado.value.formulario.copy(foto = ruta)) { anteriorError, nuevoError ->
                    anteriorError.copy(foto = nuevoError.foto)
                }
                if (!guardada && anterior.isNotBlank()) withContext(Dispatchers.IO) { fotos.borrarFoto(anterior) }
                avisar("Foto agregada.")
            } catch (_: Exception) { avisar("No se pudo leer la imagen. Elige otra foto.", true) }
            finally {
                if (esCaptura) fotos.borrarCaptura(uri.toString())
                mutableEstado.update { it.copy(ocupado = false) }
            }
        }
    }
}
