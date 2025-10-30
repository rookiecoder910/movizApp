package com.example.movizapp.retrofit

import androidx.room.PrimaryKey

data class MovieDetails(
    @PrimaryKey
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String,
    val runtime: Int?,
    val vote_average: Double,
    val vote_count: Int,
    val tagline: String?,
    val genres: List<Genre>,
    val spoken_languages: List<SpokenLanguage>,
    val production_companies: List<ProductionCompany>,
    val production_countries: List<ProductionCountry>,
    val status: String?,
    val budget: Long?,
    val revenue: Long?
)
data class Genre(
    val id: Int, val name: String
)
data class SpokenLanguage(
    val iso_639_1: String,
    val name: String
)
data class ProductionCompany(
    val id: Int, val name: String,
    val logo_path: String?,
    val origin_country: String
)
data class ProductionCountry(
    val iso_3166_1: String,
    val name: String
)



