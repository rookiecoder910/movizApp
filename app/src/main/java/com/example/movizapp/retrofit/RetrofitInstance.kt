package com.example.movizapp.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//retrofit instance to make network requests
//object defines singleton
object RetrofitInstance {
    private const val Base_Url="https://api.themoviedb.org/3/"

    // 1. Configure the OkHttpClient with extended timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Time to establish the connection
        .readTimeout(30, TimeUnit.SECONDS)    // Time to wait for data transfer
        .build()

    val api: ApiService by lazy {
        //retrofit builder to build and create a retrofit object
        val retrofit = Retrofit.Builder()
            .baseUrl(Base_Url)
            // 2. Pass the configured client to Retrofit
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)

    }
}
