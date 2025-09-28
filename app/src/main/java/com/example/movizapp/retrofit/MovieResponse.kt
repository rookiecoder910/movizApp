package com.example.movizapp.retrofit

import kotlinx.serialization.SerialName
//used to define the structure of the table
data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
@SerialName("total_pages")
    val totalpages: Int,
    val total_results: Int

)

