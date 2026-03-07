package com.example.movizapp.retrofit

import kotlinx.serialization.SerialName

data class TvShowResponse(
    val page: Int,
    val results: List<TvShow>,
    @SerialName("total_pages")
    val total_pages: Int,
    val total_results: Int
)
