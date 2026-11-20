package com.example.tiotec.network

import android.os.Build

import com.example.tiotec.BuildConfig
import com.example.tiotec.model.RevisionNube
import com.example.tiotec.model.DatosNube
import com.example.tiotec.model.AvisoSeguridad
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface RevisionesApi {
    @GET("api/revisiones") suspend fun listar(): List<RevisionNube>
    @GET("api/revisiones/{id}") suspend fun buscar(@Path("id") id: Long): RevisionNube
    @POST("api/revisiones") suspend fun crear(@Body datos: DatosNube): RevisionNube
    @PUT("api/revisiones/{id}") suspend fun actualizar(@Path("id") id: Long, @Body datos: DatosNube): RevisionNube
    @DELETE("api/revisiones/{id}") suspend fun eliminar(@Path("id") id: Long): retrofit2.Response<Unit>
}

interface AvisosApi {
    @Headers("Accept: application/vnd.github+json", "User-Agent: TioTec-Preventivo")
    @GET("advisories")
    suspend fun listar(@Query("per_page") limite: Int = 6, @Query("ecosystem") ecosistema: String = "maven"): List<AvisoSeguridad>
}

object Servicios {
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS).build()
    private fun retrofit(url: String) = Retrofit.Builder().baseUrl(url).client(cliente)
        .addConverterFactory(GsonConverterFactory.create()).build()
    val revisiones: RevisionesApi by lazy {
        val emulador = Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish"
        val base = if (emulador) "http://10.0.2.2:8080/" else BuildConfig.BACKEND_URL
        retrofit(base).create(RevisionesApi::class.java)
    }
    val avisos: AvisosApi by lazy { retrofit("https://api.github.com/").create(AvisosApi::class.java) }
}

