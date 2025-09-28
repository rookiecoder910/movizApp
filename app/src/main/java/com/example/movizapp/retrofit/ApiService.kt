package com.example.movizapp.retrofit



import com.example.movizapp.retrofit.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query
//used to fetch data from online API
interface ApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): MovieResponse
}
