# 🎬 MovizApp

![GitHub Repo stars](https://img.shields.io/github/stars/your-username/MovizApp?style=social)
![GitHub forks](https://img.shields.io/github/forks/your-username/MovizApp?style=social)
![GitHub issues](https://img.shields.io/github/issues/your-username/MovizApp)
![GitHub license](https://img.shields.io/github/license/your-username/MovizApp)

---

## 🚀 About

**MovizApp** is an **Android movie browsing app** built with **Kotlin & Jetpack Compose**.  
It fetches popular movies from TMDB API and displays them in **beautiful, interactive movie cards**.  
Users can expand/collapse movie descriptions using a **Read more / Read less** toggle, providing a clean and modern UI.

---

## ✨ Features

- Browse **popular movies** from an online API (TMDB).  
- **Modern card UI** with movie posters, titles, and descriptions.  
- **Read more / Read less** toggle for long movie overviews.  
- **Smooth animations** for expanding/collapsing text.  
- Built with **Jetpack Compose & Material3**.  
- **Coil** for asynchronous image loading.

---

## 📸 Screenshots

<img width="338" height="764" alt="image" src="https://github.com/user-attachments/assets/061e1fc5-08ac-421a-8b00-8ea4ddac3cf1" />


*Browse movies in a clean card layout*


---

## 🏗 Architecture

**MVVM Pattern**  
- **Repository** → Handles API calls and data fetching.  
- **ViewModel** → Exposes movie data to the UI.  
- **UI (Jetpack Compose)** → Displays movie cards and grids.  

**Libraries Used:**  
- Jetpack Compose & Material3  
- Coil (image loading)  
- Retrofit (API calls)  
- Kotlin Coroutines  
- Room (optional, for local caching)

---

## ⚡ Setup Instructions

1. **Clone the repository**
```bash
git clone https://github.com/your-username/MovizApp.git
cd MovizApp
