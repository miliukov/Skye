# 🌤️ Skye – Weather App

A clean, modern Android weather app with GPS location, city search with autocomplete, and hourly forecast.

> ⚠️ **This project is currently in active development.** Features may be incomplete or subject to change.

<p align="center">
  <img src="https://github.com/user-attachments/assets/933c48fa-c552-4482-b544-71fba6bd11ef" width="600" alt="Skye App Preview" />
</p>

---

## 🛠️ Tech Stack

![Android](https://img.shields.io/badge/Android%2011+-34A853?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-34A853?style=for-the-badge&logo=google&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white)
![Room](https://img.shields.io/badge/Room-FF6F00?style=for-the-badge&logo=android&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![OpenWeatherMap](https://img.shields.io/badge/OpenWeatherMap-EB6E4B?style=for-the-badge&logo=openweathermap&logoColor=white)

---

## ✨ Features

### ✅ Done
- [x] Main screen UI
- [x] Parallel API requests
- [x] Clean Architecture
- [x] GPS location detection
- [x] Hourly forecast carousel

### 🚧 In Progress
- [ ] City search with autocomplete
- [ ] Saved cities screen

### 📋 Planned
- [ ] Saved cities with Room
- [ ] Dark / Light theme support
- [ ] Onboarding screen
- [ ] Widget for home screen
- [ ] Settings (units, language)

---

## 🏗️ Architecture

Skye follows **Clean Architecture** with **MVVM** and a strict separation of layers:

```
presentation/   → Jetpack Compose UI, ViewModel, UiState
domain/         → Use Cases, Repository interfaces, Domain models
data/           → Retrofit API, Room Database, Repository implementations, Mappers
```
---

## 🔌 API

Powered by [OpenWeatherMap](https://openweathermap.org/api):

- **Current Weather** — `/data/2.5/weather`
- **5 Day Forecast** — `/data/2.5/forecast`
- **Geocoding** — `/geo/1.0/direct`

---

## 🚀 Getting Started

1. Clone the repo
2. Get a free API key from [openweathermap.org](https://openweathermap.org/appid)
3. Add to your `local.properties`:
   
   ```
   WEATHER_API_KEY=your_api_key_here
   ```
4. Build and run on Android 11+ (API 30)

---

## 📁 Project Structure

```
dev.dmil.skye/
├── data/
│   ├── dto/            # API response models
│   ├── local/          # Room database, DAO, entities
│   ├── mapper/         # DTO → Domain mappers
│   ├── remote/         # Retrofit API interface
│   └── repository/     # Repository implementations
├── di/                 # Hilt modules
├── domain/
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
└── presentation/
    ├── screen/         # Compose screens
    ├── state/          # UI state classes
    ├── ui/theme/       # Material3 theme
    └── viewmodel/      # ViewModels
```
