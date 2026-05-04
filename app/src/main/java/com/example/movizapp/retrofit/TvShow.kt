package com.example.movizapp.retrofit

import androidx.compose.runtime.Immutable

@Immutable
data class TvShow(
    val id: Int,
    val name: String,
    val overview: String,
    val first_air_date: String?,
    val vote_average: Double,
    val poster_path: String?
)
