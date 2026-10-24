package com.example.tiotec.validation

import com.example.tiotec.model.Errores
import com.example.tiotec.model.Formulario
import com.example.tiotec.model.experiencias
import com.example.tiotec.model.respuestasValidas

// Reglas puras: no dependen de Compose ni de Android.
object Validacion {
    fun revisar(f: Formulario) = Errores(
        nombre = if (f.nombre.trim().length !in 2..40) "Escribe un nombre ficticio de 2 a 40 caracteres." else null,
        dispositivo = if (f.dispositivo.trim().length !in 2..50) "Describe un dispositivo de 2 a 50 caracteres." else null,
        sistema = if (f.sistema.trim().length !in 2..30) "Escribe un sistema de 2 a 30 caracteres." else null,
        experiencia = if (f.experiencia !in experiencias) "Selecciona un nivel." else null,
        foto = if (f.foto.isBlank()) "Agrega una foto del dispositivo de prueba." else null,
        diasRevision = if (f.diasRevision !in 7..90) "Elige entre 7 y 90 días." else null,
        ficticio = if (!f.ficticio) "Confirma que usas datos ficticios." else null,
        respuestas = (0..3).map { i ->
            if (f.respuestas.getOrNull(i) !in respuestasValidas) "Selecciona Sí, No o No sé." else null
        }
    )
}
