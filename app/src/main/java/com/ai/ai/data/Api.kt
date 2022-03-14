package com.ai.ai.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {
    @GET(NetworkUrl.URL_MODEL_x4)
    suspend fun downloadModel4(): Response<ResponseBody>

    @GET(NetworkUrl.DOWNLOAD_V4 + "/{number}")
    suspend fun downloadLocalV4(@Path("number") number: String): Response<ResponseBody>
}
