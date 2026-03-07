package com.example.movizapp.retrofit

data class TvShowDetails(
    val id: Int,
    val name: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val first_air_date: String?,
    val vote_average: Double,
    val vote_count: Int,
    val tagline: String?,
    val genres: List<Genre>,
    val seasons: List<Season>,
    val number_of_seasons: Int,
    val number_of_episodes: Int,
    val status: String?,
    val spoken_languages: List<SpokenLanguage>,
    val production_companies: List<ProductionCompany>
)

data class Season(
    val id: Int,
    val season_number: Int,
    val name: String,
    val episode_count: Int,
    val poster_path: String?,
    val overview: String?
)
