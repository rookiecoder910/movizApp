package com.example.movizapp.retrofit

data class SeasonDetails(
    val id: Int,
    val season_number: Int,
    val name: String,
    val episodes: List<Episode>
)

data class Episode(
    val id: Int,
    val episode_number: Int,
    val name: String,
    val overview: String?,
    val still_path: String?,
    val air_date: String?,
    val vote_average: Double
)
