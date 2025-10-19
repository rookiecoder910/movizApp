package com.example.movizapp.retrofit



import com.example.movizapp.retrofit.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
//used to fetch data from online API
interface ApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): MovieResponse
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int=1
    ): MovieResponse
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): MovieDetails


}
