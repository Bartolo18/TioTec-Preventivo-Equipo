package com.example.tiotec

import com.example.tiotec.data.model.*
import com.example.tiotec.data.remote.RevisionesApi
import com.example.tiotec.data.remote.SoporteApi
import com.example.tiotec.repository.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// Guía 15, partes 4 y 5: StringSpec y una API simulada con MockK.
// Se prueba el repositorio real inyectando su dependencia, sin acceder a internet.
class ServidorRepositoryKotest : StringSpec({
    val datos = DatosRevision(" Luffy ", " Notebook ", " Windows ",
        respuestas = List(4) { "Sí" })
    val registro = RevisionRemota(1, 1, "Luffy", "Notebook", "Windows", "Básico", 30,
        List(4) { "Sí" }, 0)

    "listar entrega las evaluaciones devueltas por GET" {
        runTest {
            val api = mockk<RevisionesApi>()
            coEvery { api.listar() } returns listOf(registro)
            ServidorRepositoryRemoto(api).listar() shouldContainExactly listOf(registro)
            coVerify(exactly = 1) { api.listar() }
        }
    }

    "crear normaliza el formulario y utiliza POST" {
        runTest {
            val api = mockk<RevisionesApi>()
            coEvery { api.crear(datos.normalizados()) } returns registro
            ServidorRepositoryRemoto(api).guardar(null, datos) shouldBe registro
            coVerify(exactly = 1) { api.crear(datos.normalizados()) }
            coVerify(exactly = 0) { api.actualizar(any(), any()) }
        }
    }

    "editar conserva el identificador y utiliza PUT" {
        runTest {
            val api = mockk<RevisionesApi>()
            coEvery { api.actualizar(1, datos.normalizados()) } returns registro
            ServidorRepositoryRemoto(api).guardar(1, datos) shouldBe registro
            coVerify(exactly = 1) { api.actualizar(1, datos.normalizados()) }
            coVerify(exactly = 0) { api.crear(any()) }
        }
    }

    "un formulario inválido no envía solicitudes a la API" {
        runTest {
            val api = mockk<RevisionesApi>()
            shouldThrow<IllegalArgumentException> {
                ServidorRepositoryRemoto(api).guardar(null, DatosRevision())
            }
            coVerify(exactly = 0) { api.crear(any()) }
        }
    }

    "eliminar comunica al ViewModel un fallo HTTP del servidor" {
        runTest {
            val api = mockk<RevisionesApi>()
            coEvery { api.eliminar(1) } returns Response.error(500, ResponseBody.create(null, "{}"))
            shouldThrow<HttpException> { ServidorRepositoryRemoto(api).eliminar(1) }.code() shouldBe 500
        }
    }

    "la API externa descarta versiones sin nombre y duplicadas" {
        runTest {
            val api = mockk<SoporteApi>()
            val version = VersionSistema(name = "14", isEol = false)
            coEvery { api.consultar("android") } returns RespuestaSoporte(
                ProductoSoporte(listOf(version, version, VersionSistema())))
            SoporteRepositoryRemoto(api).consultar(SistemaConsulta.ANDROID) shouldContainExactly listOf(version)
            coVerify(exactly = 1) { api.consultar("android") }
        }
    }

    "una respuesta externa incompleta produce un error recuperable" {
        runTest {
            val api = mockk<SoporteApi>()
            coEvery { api.consultar("windows") } returns RespuestaSoporte()
            shouldThrow<IOException> { SoporteRepositoryRemoto(api).consultar(SistemaConsulta.WINDOWS) }
        }
    }
})
