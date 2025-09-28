package com.example.movizapp.Repository

import android.content.Context
import com.example.movizapp.retrofit.Movie
import com.example.movizapp.retrofit.RetrofitInstance
import com.example.movizapp.room.MovieDAO
import com.example.movizapp.room.MoviesDb

//repository contains all methods to fetch data from online and offline
class Repository(context: Context) {
    //fetching data from online API
    suspend fun getPopularMovies(apiKey: String,page:Int): List<Movie> {
        return RetrofitInstance.api.getPopularMovies(apiKey, page ).results

    }
       //fetching data from offline database
    private val db= MoviesDb.getInstance(context)
    private val movieDao: MovieDAO=db.movieDao
    suspend fun moviesFromDB(): List<Movie> {
        return movieDao.getAllMovieSInDB()
    }
    suspend fun insertMoviesIntoDB(movies: List<Movie>) {
        return movieDao.insertMoviesList(movies)
    }
    suspend fun  insertMovieIntoDB(movie: Movie){
        return movieDao.insert(movie)
    }

}
