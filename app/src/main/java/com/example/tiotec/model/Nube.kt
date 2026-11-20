package com.example.tiotec.model

data class DatosNube(
    val nombre: String = "",
    val dispositivo: String = "",
    val sistema: String = "",
    val experiencia: String = "Básico",
    val diasRevision: Int = 30,
    val respuestas: List<String> = List(4) { "" },
    val accionesHechas: Int = 0
) {
    fun errores(): List<String> = buildList {
        if (nombre.trim().length !in 2..40) add("Nombre ficticio: entre 2 y 40 caracteres.")
        if (dispositivo.trim().length !in 2..50) add("Dispositivo: entre 2 y 50 caracteres.")
        if (sistema.trim().length !in 2..30) add("Sistema: entre 2 y 30 caracteres.")
        if (experiencia !in experiencias) add("Selecciona un nivel válido.")
        if (diasRevision !in 7..90) add("La próxima revisión debe ser entre 7 y 90 días.")
        if (respuestas.size != 4 || respuestas.any { it !in respuestasValidas }) add("Responde las cuatro preguntas.")
        if (accionesHechas !in 0..15) add("El avance de acciones no es válido.")
    }
    fun normalizados() = copy(nombre = nombre.trim(), dispositivo = dispositivo.trim(), sistema = sistema.trim())
}
data class RevisionNube(
    val id: Long,
    val fecha: Long,
    val nombre: String,
    val dispositivo: String,
    val sistema: String,
    val experiencia: String,
    val diasRevision: Int,
    val respuestas: List<String>,
    val accionesHechas: Int
) {
    fun datos() = DatosNube(nombre, dispositivo, sistema, experiencia, diasRevision, respuestas, accionesHechas)
}
data class AvisoSeguridad(
    val ghsa_id: String,
    val summary: String,
    val severity: String,
    val html_url: String,
    val published_at: String
)

