package com.example.movizapp.Repository

import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.RetrofitInstance

class Repository {
    //fetching data from online API
    suspend fun getPopularMovies(apiKey: String,page:Int): List<Movie> {
        return RetrofitInstance.api.getPopularMovies(apiKey, page ).results

    }
}