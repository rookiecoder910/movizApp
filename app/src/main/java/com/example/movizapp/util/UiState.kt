package com.example.movizapp.util

/**
 * Represents the state of a UI-bound data operation.
 * Use this to drive loading, success, and error states in Composable screens.
 */
sealed class UiState<out T> {
    /** Data is currently being fetched. */
    object Loading : UiState<Nothing>()

    /** Data was fetched successfully. */
    data class Success<T>(val data: T) : UiState<T>()

    /** An error occurred. Optionally includes a retry action. */
    data class Error(
        val message: String,
        val retry: (() -> Unit)? = null
    ) : UiState<Nothing>()
}
