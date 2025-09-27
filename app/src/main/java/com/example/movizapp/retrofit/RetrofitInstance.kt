package com.example.movizapp.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//retrofit instance to make network requests
//object defines singleton
object RetrofitInstance {
    private const val Base_Url="https://api.themoviedb.org/3/"
    val api: ApiService by lazy {
        //retrofit builder to build and create a retrofit object
        val retrofit = Retrofit.Builder()
            .baseUrl(Base_Url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)

    }


}