package com.videoapi.app.data

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api_live.php")
    suspend fun getLive(
        @Query("user_id") userId: String,
        @Query("ch") ch: String,
        @Query("after") after: Long
    ): JsonElement

    @GET("api_old_live.php")
    suspend fun getOldList(
        @Query("list") list: Int = 1
    ): JsonElement

    @GET("dataview.json")
    suspend fun getDataView(): JsonElement
}
