package com.example.tiotec

import com.example.tiotec.data.model.*
import com.example.tiotec.validation.Validacion
import org.junit.Assert.*
import org.junit.Test

class ValidacionTest {
    private val valido = Formulario(nombre = "Luffy", dispositivo = "Notebook", sistema = "Windows 11",
        experiencia = "Básico", foto = "/fotos/prueba.jpg", ficticio = true,
        respuestas = listOf("Sí", "No", "No sé", "Sí"))
    @Test fun aceptaFormularioCompleto() {
        assertFalse(Validacion.revisar(valido).hayErrores)
        assertFalse(Validacion.revisar(valido.copy(diasRevision = 7)).hayErrores)
        assertFalse(Validacion.revisar(valido.copy(diasRevision = 90)).hayErrores)
    }
    @Test fun detectaCadaCampoYLimites() {
        val invalidos = listOf(valido.copy(nombre = " "), valido.copy(nombre = "a".repeat(41)),
            valido.copy(dispositivo = "x"), valido.copy(dispositivo = "x".repeat(51)),
            valido.copy(sistema = ""), valido.copy(sistema = "x".repeat(31)),
            valido.copy(experiencia = "Otro"), valido.copy(foto = " "),
            valido.copy(diasRevision = 6), valido.copy(diasRevision = 91),
            valido.copy(ficticio = false), valido.copy(respuestas = emptyList()),
            valido.copy(respuestas = listOf("Sí", "Tal vez", "No", "No")))
        invalidos.forEach { assertTrue(Validacion.revisar(it).hayErrores) }
        assertTrue(Validacion.revisar(Formulario()).respuestas.all { it != null })
    }
    @Test fun validaNubeSinDependerDeAndroid() {
        val datos = DatosRevision(" Luffy ", " Notebook ", " Windows ", "Básico", 30, listOf("Sí","No","No sé","Sí"))
        assertTrue(datos.errores().isEmpty())
        assertEquals("Luffy", datos.normalizados().nombre)
        val invalidos = listOf(datos.copy(nombre = "x"), datos.copy(dispositivo = ""),
            datos.copy(sistema = ""), datos.copy(experiencia = "Otro"), datos.copy(diasRevision = 6),
            datos.copy(respuestas = emptyList()), datos.copy(respuestas = List(4) { "Otro" }),
            datos.copy(accionesHechas = -1), datos.copy(accionesHechas = 16))
        invalidos.forEach { assertTrue(it.errores().isNotEmpty()) }
        val r = RevisionRemota(1,1,datos.nombre,datos.dispositivo,datos.sistema,datos.experiencia,datos.diasRevision,datos.respuestas,0)
        assertEquals(datos, r.datos())
    }
}

