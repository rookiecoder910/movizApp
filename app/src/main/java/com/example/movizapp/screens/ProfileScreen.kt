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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    // State for the editable user name, using a placeholder based on your game
    var userName by remember { mutableStateOf("Pixelpholio User") }
    // Flag to track if the data has been changed since the last save
    var isEditing by remember { mutableStateOf(false) }

    // State for the profile photo (mocking two options for demonstration)
    var photoIndex by remember { mutableStateOf(0) }
    val photoUrls = remember {
        listOf(
            "https://placehold.co/150x150/007AFF/FFFFFF?text=PP", // Pixelpholio Placeholder 1
            "https://placehold.co/150x150/FF4500/FFFFFF?text=MZ" // MovizApp Placeholder 2
        )
    }

    // State for showing the saved confirmation message
    var showSavedMessage by remember { mutableStateOf(false) }

    // Logic to auto-hide the saved message
    LaunchedEffect(showSavedMessage) {
        if (showSavedMessage) {
            delay(2000)
            showSavedMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- Profile Picture Section ---
                Box(
                    modifier = Modifier
                        .size(150.dp)
                ) {
                    // Profile Image
                    AsyncImage(
                        model = photoUrls[photoIndex],
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    // Overlay FAB to change photo
                    FloatingActionButton(
                        onClick = {
                            // Mocking photo change and setting the editing flag
                            photoIndex = (photoIndex + 1) % photoUrls.size
                            isEditing = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(40.dp),
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Change Photo",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // --- Name Input Section ---
                OutlinedTextField(
                    value = userName,
                    onValueChange = { newValue ->
                        // Only update isEditing if the new value is different from the initial default
                        if (newValue != "Pixelpholio User") {
                            isEditing = true
                        }
                        userName = newValue
                    },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(48.dp))

                // --- Save Button ---
                Button(
                    onClick = {
                        // In a real app, you would call a ViewModel function here to persist the changes.
                        // viewModel.saveProfile(userName, photoUrls[photoIndex])
                        isEditing = false // Reset editing flag after saving
                        showSavedMessage = true // Show confirmation
                    },
                    enabled = isEditing, // Button is only enabled if changes have been made
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Save", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Profile", fontWeight = FontWeight.Bold)
                }

                // --- Saved Message Confirmation ---
                if (showSavedMessage) {
                    Spacer(Modifier.height(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            "Profile saved successfully!",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    )
}
