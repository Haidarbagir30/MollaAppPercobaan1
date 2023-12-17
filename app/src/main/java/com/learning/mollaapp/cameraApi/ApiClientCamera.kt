package com.learning.mollaapp.cameraApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClientCamera {
    private const val BASE_URL = "https://translate-text-mf5fjq4ezq-et.a.run.app"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiServiceCamera: ApiServiceCamera = retrofit.create(ApiServiceCamera::class.java)
}