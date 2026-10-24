package com.example.tiotec.model

data class Formulario(
    val id: Long = 0,
    val nombre: String = "",
    val dispositivo: String = "",
    val sistema: String = "",
    val experiencia: String = "",
    val foto: String = "",
    val diasRevision: Int = 30,
    val ficticio: Boolean = false,
    val respuestas: List<String> = List(4) { "" }
)

data class Errores(
    val nombre: String? = null,
    val dispositivo: String? = null,
    val sistema: String? = null,
    val experiencia: String? = null,
    val foto: String? = null,
    val diasRevision: String? = null,
    val ficticio: String? = null,
    val respuestas: List<String?> = List(4) { null }
) {
    val hayErrores: Boolean
        get() = listOf(nombre, dispositivo, sistema, experiencia, foto, diasRevision, ficticio).any { it != null } ||
            respuestas.any { it != null }
}

data class Pregunta(val titulo: String, val texto: String, val accion: String, val consejo: String)

val preguntas = listOf(
    Pregunta("Respaldos", "¿El equipo de prueba tiene una copia de seguridad reciente?",
        "Revisar y hacer una copia de seguridad de prueba",
        "Una copia reciente ayuda a recuperar archivos si el equipo falla o se pierde."),
    Pregunta("Actualizaciones", "¿El sistema del equipo de prueba está actualizado?",
        "Revisar las actualizaciones del sistema",
        "Actualizar el sistema corrige fallas y mantiene sus protecciones al día."),
    Pregunta("Autenticación", "¿La cuenta ficticia usa verificación en dos pasos?",
        "Revisar la verificación en dos pasos",
        "Un segundo paso dificulta el acceso si alguien conoce la contraseña."),
    Pregunta("Recuperación", "¿La cuenta ficticia tiene un método de recuperación?",
        "Revisar la recuperación de la cuenta",
        "Un método de recuperación vigente permite volver a entrar si pierdes el acceso.")
)

val experiencias = listOf("Básico", "Medio", "Avanzado")
val respuestasValidas = listOf("Sí", "No", "No sé")
