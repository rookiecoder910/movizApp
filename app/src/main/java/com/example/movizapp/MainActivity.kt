package com.example.movizapp

import MovizNavGraph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.movizapp.Repository.Repository
import com.example.movizapp.ui.theme.MovizAppTheme
import com.example.movizapp.viewmodel.MovieViewModel
import com.example.movizapp.viewmodel.MovieViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val repository = Repository(applicationContext)
        val viewModelFactory = MovieViewModelFactory(repository)
        val movieViewModel = ViewModelProvider(this, viewModelFactory)[MovieViewModel::class.java]

        setContent {
            MovizAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    topBar = { AppHeader() }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        //  Search bar for movies
                        MovieSearchBar(
                            viewModel = movieViewModel,


                            onSearch = { query ->
                                movieViewModel.searchMovies(query)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        //  Navigation Graph handles all screen routing
                        MovizNavGraph(
                            viewModel = movieViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    onNavigationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = "Moviz",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Navigation Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Handle Profile Click */ }) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent, // Make it transparent to blend with content
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieSearchBar(
    viewModel: MovieViewModel,
    onSearch: (String) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // --- Typing Effect Logic ---
    val placeholderTexts = listOf(
        "Search movies...",
        "Search by title..."
    )

    // This state holds the text that is currently visible in the placeholder
    var displayedText by remember { mutableStateOf(placeholderTexts[0]) }

    // This effect runs the typing animation
    LaunchedEffect(active) {
        // Only run the animation when the search bar is not active
        if (!active) {
            var currentTextIndex = 0
            while (true) {
                val targetText = placeholderTexts[currentTextIndex]


                displayedText = ""
                targetText.forEach { char ->
                    displayedText += char
                    delay(151L) // Speed of typing
                }


                delay(2000L)


                repeat(targetText.length) {
                    displayedText = displayedText.dropLast(1)
                    delay(75L) // Speed of deleting
                }


                delay(500L)


                currentTextIndex = (currentTextIndex + 1) % placeholderTexts.size
            }
        }
    }


    SearchBar(
        query = query,
        onQueryChange = { newQuery ->
            query = newQuery
            if (query.isNotBlank()) {
                viewModel.searchMovies(query)
            } else {
                viewModel.searchResults = emptyList()
            }
        },
        onSearch = {
            onSearch(query)
            active = false
        },
        active = active,
        onActiveChange = { active = it },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),


        placeholder = {
            Text(displayedText)
        },

        leadingIcon = {
            IconButton(onClick = {
                if (active) {
                    active = false
                    query = ""
                    viewModel.searchResults = emptyList()
                } else {
                    active = true
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        }
    ) {
        val searchResults = viewModel.searchResults

        if (searchResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(searchResults.size) { index ->
                    val movie = searchResults[index]
                    ListItem(
                        headlineContent = { Text(movie.title) },
                        leadingContent = {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w92${movie.poster_path}",
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .size(47.dp)
                                    .clip(MaterialTheme.shapes.small)
                            )
                        },
                        supportingContent = {
                            Text("⭐ ${String.format("%.1f", movie.vote_average)}")
                        },
                        modifier = Modifier
                            .clickable {
                                query = movie.title
                                onSearch(query)
                                active = false
                                viewModel.searchResults = emptyList()
                            }
                            .fillMaxWidth()
                    )
                }
            }
        } else if (query.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No results found",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}