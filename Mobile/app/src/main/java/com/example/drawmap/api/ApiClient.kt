package com.example.drawmap.api

import com.example.drawmap.config.AppConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ApiClient {
    @Volatile
    private var currentBaseUrl: String = AppConfig.Api.DEFAULT_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (AppConfig.Logging.ENABLE_HTTP_LOGGING) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(AppConfig.Api.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(AppConfig.Api.READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(AppConfig.Api.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit: Retrofit by lazy {
        createRetrofit(currentBaseUrl)
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
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
