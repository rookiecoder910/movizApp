package com.example.movizapp.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<FirebaseUser?> = authRepository.authStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    var isSigningIn by mutableStateOf(false)
        private set

    var signInError by mutableStateOf<String?>(null)
        private set

    fun signInWithGoogle(activityContext: Context) {
        isSigningIn = true
        signInError = null
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activityContext)
            result.fold(
                onSuccess = { /* auth state flow will update automatically */ },
                onFailure = { signInError = it.message ?: "Sign-in failed" }
            )
            isSigningIn = false
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearError() {
        signInError = null
    }
}
