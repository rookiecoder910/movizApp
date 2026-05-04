package com.example.movizapp.retrofit

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
//used to define the structure of the table
@Immutable
@Entity("movies_table")
data class Movie(
    @PrimaryKey
    val id:Int,

    val title: String,
    val overview: String,
    val release_date: String,
    val vote_average: Double,
    val poster_path: String,





)
