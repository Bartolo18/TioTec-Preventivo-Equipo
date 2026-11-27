package com.example.tiotec.data.remote

import com.example.tiotec.data.model.DatosRevision
import com.example.tiotec.data.model.RespuestaSoporte
import com.example.tiotec.data.model.RevisionRemota
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RevisionesApi {
    @GET("api/revisiones")
    suspend fun listar(): List<RevisionRemota>

    @GET("api/revisiones/{id}")
    suspend fun buscar(@Path("id") id: Long): RevisionRemota

    @POST("api/revisiones")
    suspend fun crear(@Body datos: DatosRevision): RevisionRemota

    @PUT("api/revisiones/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body datos: DatosRevision
    ): RevisionRemota

    @DELETE("api/revisiones/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}

interface SoporteApi {
    @GET("api/v1/products/{producto}/")
    suspend fun consultar(@Path("producto") producto: String): RespuestaSoporte
}
