# 🎬 MovizApp

<div align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" />
</div>

<div align="center">
  <img src="https://img.shields.io/github/stars/your-username/MovizApp?style=social" alt="GitHub Repo stars" />
  <img src="https://img.shields.io/github/forks/your-username/MovizApp?style=social" alt="GitHub forks" />
  <img src="https://img.shields.io/github/issues/your-username/MovizApp" alt="GitHub issues" />
  <img src="https://img.shields.io/github/license/your-username/MovizApp" alt="GitHub license" />
</div>

---

## 🚀 About

**MovizApp** is an **Android movie and TV show browsing & streaming app** built completely with **Kotlin & Jetpack Compose**.  

It fetches popular Movies and TV Shows from the TMDB API, presenting them in a highly polished, **Netflix-inspired dark theme**. Additionally, MovizApp provides a built-in player using **VidKing**, ensuring an uninterrupted cinematic experience by featuring highly-effective ad and popup blocking via WebView.

---

## ✨ Features

- 🍿 **Browse Movies & TV Shows:** Discover popular, top-rated, and trending content powered by the TMDB API.
- 🎨 **Netflix-Inspired Dark UI:** A professional and immersive streaming-service visual aesthetic with perfectly tailored design tokens.
- 🎬 **Horizontal Poster Carousels:** Browse vast collections effortlessly on the home screen with snappy scrollable carousels.
- 🔍 **Dedicated Search Screen:** Instantly find your favorite movies and shows with a beautiful grid layout.
- ▶️ **Integrated VidKing Player:** Stream content directly inside the app. Features intelligent, aggressive ad and popup blocking in the WebView player.
- 📺 **Detailed Information:** View full overviews, cast, and seasons with smooth **Read more / Read less** toggle animations.
- 👤 **Profile Screen:** A centralized hub with consistent dark theme styling.

---

## 📸 Screenshots

<img width="338" height="764" alt="image" src="https://github.com/user-attachments/assets/061e1fc5-08ac-421a-8b00-8ea4ddac3cf1" />

*Browse movies and shows in a clean, modern layout*

*(Note: We recommend replacing the screenshot above with the newest Netflix-styled UI snapshots!)*

---

## 🏗 Architecture & Tech Stack

Developed strictly adhering to the **MVVM (Model-View-ViewModel)** architectural pattern to ensure robust data management, clean UI separation, and unit-testability.

**Tech Stack & Libraries Used:**  
- **UI:** Jetpack Compose, Material3, Accompanist FlowLayout, Lottie Compose
- **Network:** Retrofit2, Gson
- **Image Loading:** Coil
- **Asynchrony:** Kotlin Coroutines & Flow
- **Local Storage:** Room Database
- **Navigation:** Jetpack Navigation Compose
- **Web & Video:** `androidx.webkit` (WebView for VidKing ad-blocking)

---

## ⚡ Setup Instructions

1. **Clone the repository**
```bash
git clone https://github.com/your-username/MovizApp.git
cd MovizApp
```

2. **Open in Android Studio**
- Launch Android Studio and select `Open an Existing Project`.
- Navigate to the cloned `MovizApp` directory and select it.

3. **Configure API Keys** (If required by the project)
- You may need to provide a TMDB API Key.
- Generate an API read access token / key at [The Movie Database (TMDB)](https://www.themoviedb.org/).
- Insert the key into your Retrofit `ApiService` interceptor or wherever API keys are required (e.g., `local.properties`).

4. **Build and Run**
- Click `Sync Project with Gradle Files`.
- Hit `Run` (`Shift + F10`) to deploy MovizApp on your emulator or a physical Android device (Min SDK 29 / Android 10).
