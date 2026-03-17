package com.example.drawmap.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    // В эмуляторе Android localhost компьютера доступен по адресу 10.0.2.2
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val routeApiService: RouteApiService by lazy {
        retrofit.create(RouteApiService::class.java)
    }

    val heatMapApiService: HeatMapApiService by lazy {
        retrofit.create(HeatMapApiService::class.java)
    }
}
