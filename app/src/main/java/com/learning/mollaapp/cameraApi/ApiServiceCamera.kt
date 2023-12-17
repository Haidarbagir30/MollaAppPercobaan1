package com.learning.mollaapp.cameraApi

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiServiceCamera {

    @Multipart
    @POST("read_image")
    fun readImage(@Part image: MultipartBody.Part): Call<ReadImageResponse>

    @POST("translate")
    fun translateText(@Query("text") text: String): Call<TranslationResponse>
}
