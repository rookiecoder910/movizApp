package com.example.movizapp.retrofit

import kotlinx.serialization.SerialName

data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
@SerialName("total_pages")
    val totalpages: Int,
    val total_results: Int

)

