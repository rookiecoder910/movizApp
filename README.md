MovizApp 🎬

MovizApp is an Android movie browsing app built with Kotlin and Jetpack Compose. It fetches movies from an online API (like TMDB) and displays them in a modern, interactive UI. Users can view movie posters, titles, and descriptions with a read more/read less toggle for long overviews.

Features ✨

Fetches popular movies from an online API.

Displays movies in cards with poster, title, and overview.

Read more / Read less toggle for long descriptions.

Modern UI using Jetpack Compose & Material3.

Smooth animation when expanding/collapsing text.

Uses Coil for image loading.

Screenshots

(You can add screenshots of your app here)

Architecture 🏗️

MVVM Architecture

Repository → Handles API calls and data fetching.

ViewModel → Exposes movie data to UI.

UI (Compose) → Displays movies in cards and grids.

Jetpack Compose → For modern declarative UI.

Coil → For asynchronous image loading.

Material3 → Modern UI components.

Room (Optional) → For local caching of movies.

Setup Instructions ⚡

Clone the repository

git clone https://github.com/your-username/MovizApp.git
cd MovizApp


Open in Android Studio
Open the project folder in Android Studio Arctic Fox or newer.

Add API Key
Replace YOUR_API_KEY in your Repository or Retrofit setup with your TMDB API key.

Build & Run
Connect an Android device or start an emulator and run the app.

Dependencies 📦

Jetpack Compose – UI toolkit for Android.

Material3 – Modern Material Design components.

Coil – Image loading library.

Retrofit – For API calls.

Room – Local database (optional).

Kotlin Coroutines – For asynchronous calls.

Usage 🎮

Launch the app.

Browse popular movies in a scrollable list or grid layout.

Tap Read more to expand the movie overview.

Tap Read less to collapse the text.

Posters are loaded asynchronously from the API.

Future Improvements 🚀

Add search functionality.

Add favorites and watchlist using Room database.

Implement grid-only Netflix-style poster view with overlays.

Add shimmer loading placeholders for posters.

Add pagination to fetch more movies.

License 📝

This project is licensed under the MIT License – see the LICENSE
 file for details.
