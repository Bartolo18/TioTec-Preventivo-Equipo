package com.example.tiotec.data.remote

import android.os.Build
import com.example.tiotec.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun crearRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val revisiones: RevisionesApi by lazy {
        val esEmulador = Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish"
        val url = if (esEmulador) {
            "http://10.0.2.2:8080/"
        } else {
            BuildConfig.BACKEND_URL
        }
        crearRetrofit(url).create(RevisionesApi::class.java)
    }

    val soporte: SoporteApi by lazy {
        crearRetrofit("https://endoflife.date/").create(SoporteApi::class.java)
    }
}
