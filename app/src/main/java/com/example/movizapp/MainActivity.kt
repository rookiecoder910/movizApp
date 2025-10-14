package com.example.movizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.movizapp.Repository.Repository
import com.example.movizapp.screens.MovieScreen
import com.example.movizapp.ui.theme.MovizAppTheme
import com.example.movizapp.viewmodel.MovieViewModel
import com.example.movizapp.viewmodel.MovieViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Repository & ViewModel setup
        val repository = Repository(applicationContext)
        val viewModelFactory = MovieViewModelFactory(repository)
        val movieViewModel = ViewModelProvider(this, viewModelFactory)[MovieViewModel::class.java]

        setContent {
            MovizAppTheme {
                Scaffold(
                    topBar = { AppHeader() }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)

                    ) {

                        MovieSearchBar(
                            viewModel = movieViewModel,
                            onSearch = { query ->

                            movieViewModel.searchMovies(query)
                        })
                        Spacer(modifier = Modifier.height(16.dp))

                        if (movieViewModel.searchResults.isNotEmpty()) {
                            // Show search results in a new composable (e.g., SearchResultScreen)
                            Text("Showing results for: ${movieViewModel.searchResults.size} movies")
                            MovieScreen(viewModel = movieViewModel)
                            // You'd pass movieViewModel.searchResults here
                        } else {
                            // Show the popular movie list by default
                            MovieScreen(viewModel = movieViewModel)
                        }
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
                style = MaterialTheme.typography.headlineLarge
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

            IconButton(onClick = { /* Handle profile click */ }) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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

    SearchBar(
        query = query,
        onQueryChange = { newQuery ->
            query = newQuery
            if (query.isNotBlank()) {
                // Trigger live search as user types (optional)
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
        placeholder = { Text("Search movies...") },
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
        // ✅ Suggestions show BELOW this SearchBar
        val searchResults = viewModel.searchResults

        if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(searchResults.size) { index ->
                    val movie = searchResults[index]

                    ListItem(
                        headlineContent = { Text(movie.title) },
                        leadingContent = {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w92${movie.poster_path}",
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .size(48.dp)
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
            // Optional empty state
            Text(
                text = "No results found",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
