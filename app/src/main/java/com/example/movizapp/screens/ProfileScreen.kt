package com.example.movizapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.movizapp.ui.theme.DarkBackground
import com.example.movizapp.ui.theme.DarkCard
import com.example.movizapp.ui.theme.DarkSurface
import com.example.movizapp.ui.theme.NetflixRed
import com.example.movizapp.ui.theme.TextGrey
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var userName by remember { mutableStateOf("Moviz User") }
    var isEditing by remember { mutableStateOf(false) }
    var photoIndex by remember { mutableStateOf(0) }
    val photoUrls = remember {
        listOf(
            "https://placehold.co/150x150/E50914/FFFFFF?text=M",
            "https://placehold.co/150x150/1a1a1a/E50914?text=MZ"
        )
    }
    var showSavedMessage by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            delay(2000)
            showSavedMessage = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Profile Picture
        Box(modifier = Modifier.size(120.dp)) {
            AsyncImage(
                model = photoUrls[photoIndex],
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(DarkCard)
            )
            FloatingActionButton(
                onClick = {
                    photoIndex = (photoIndex + 1) % photoUrls.size
                    isEditing = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp),
                containerColor = NetflixRed,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = "Change Photo",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Name Input
        OutlinedTextField(
            value = userName,
            onValueChange = { newValue ->
                if (newValue != "Moviz User") isEditing = true
                userName = newValue
            },
            label = { Text("Display Name", color = TextGrey) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NetflixRed,
                unfocusedBorderColor = DarkCard,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(Modifier.height(40.dp))

        // Save Button
        Button(
            onClick = {
                isEditing = false
                showSavedMessage = true
            },
            enabled = isEditing,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = NetflixRed,
                disabledContainerColor = DarkCard
            )
        ) {
            Icon(Icons.Default.Done, contentDescription = "Save", modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save Profile", fontWeight = FontWeight.Bold)
        }

        // Saved confirmation
        if (showSavedMessage) {
            Spacer(Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Profile saved successfully!",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}
