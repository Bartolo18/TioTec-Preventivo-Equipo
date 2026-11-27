package com.example.tiotec.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class SistemaConsulta(val etiqueta: String, val producto: String) {
    ANDROID("Android", "android"), WINDOWS("Windows", "windows");
    val enlace: String get() = "https://endoflife.date/$producto"
}

data class RespuestaSoporte(val result: ProductoSoporte? = null)
data class ProductoSoporte(val releases: List<VersionSistema>? = null)
data class VersionSistema(
    val name: String = "",
    val label: String? = null,
    val releaseDate: String? = null,
    val isEol: Boolean? = null,
    val eolFrom: String? = null,
    val isMaintained: Boolean? = null
) {
    fun titulo(): String = label?.takeIf { it.isNotBlank() } ?: name
    fun estadoSoporte(): String = when {
        isEol == true -> "Soporte finalizado"
        isMaintained == true -> "Soporte vigente"
        isEol == false -> "Soporte no finalizado"
        else -> "Sin información confirmada de soporte"
    }
    fun orientacion(): String = when {
        isEol == true -> "Consulta con el fabricante una actualización compatible o una alternativa con soporte."
        isMaintained == true || isEol == false -> "Revisa las actualizaciones disponibles en la configuración de tu equipo."
        else -> "Consulta el detalle de la fuente y la información de tu fabricante."
    }
}

fun fechaSoporte(fecha: String?): String = try {
    LocalDate.parse(fecha).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
} catch (_: Exception) { "No informada" }
