package com.ai.ai.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface Api {
    @GET(NetworkUrl.URL_MODEL_x4)
    suspend fun downloadModel4(): Response<ResponseBody>
}
