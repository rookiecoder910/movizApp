package com.example.movizapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.movizapp.auth.AuthViewModel
import com.example.movizapp.ui.theme.DarkBackground
import com.example.movizapp.ui.theme.DarkCard
import com.example.movizapp.ui.theme.NetflixRed
import com.example.movizapp.ui.theme.TextGrey

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    // If already signed in, go back
    LaunchedEffect(authState) {
        if (authState != null) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Text(
            text = "MOVIZ",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = NetflixRed
            )
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Sign in to sync your watchlist\nacross all your devices",
            style = MaterialTheme.typography.bodyLarge,
            color = TextGrey,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        // Google Sign-In Button
        Button(
            onClick = { authViewModel.signInWithGoogle(context) },
            enabled = !authViewModel.isSigningIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            if (authViewModel.isSigningIn) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = DarkBackground,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text("Signing in...", color = DarkBackground, fontWeight = FontWeight.SemiBold)
            } else {
                Text(
                    text = "G",
                    color = Color(0xFF4285F4),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Continue with Google",
                    color = DarkBackground,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Error message
        authViewModel.signInError?.let { error ->
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFFEF5350),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Skip for now", color = TextGrey)
        }
    }
}
