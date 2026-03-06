# Pokedex Android App 📱

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)

A modern, robust Android application that displays Pokémon data using the PokéAPI via GraphQL. 


> **[⬇ Download APK](https://github.com/Amaanprobably/PokeDex/releases/download/v1.0.0/app-releasev1.0.apk)** — Install directly on any Android device (API 26+)

---

## 📸 Screenshots

<img src="https://github.com/user-attachments/assets/3de5851e-1792-45df-844e-f41891a9d437" width="30%" />
&nbsp; &nbsp; &nbsp; &nbsp;
<img src="https://github.com/user-attachments/assets/e5554614-98f3-4436-902f-a040d1f14b0c" width="30%" />
&nbsp; &nbsp; &nbsp; &nbsp;
<img src="https://github.com/user-attachments/assets/e24a752d-53c6-44dd-bd6a-1ffc51712e49" width="30%" />

---

## 🚀 Features

* **Offline-First Support:** Browse Pokémon seamlessly without an internet connection using local Room database caching.
* **Infinite Scrolling:** Smoothly loads data in paginated chunks of 40 using the Paging 3 library.
* **Background Synchronization:** Local search functionality powered by a background `SyncWorker` that silently fetches and updates the database.
* **Advanced UI Transitions:** Features beautiful Shared Element Transitions in Jetpack Compose for a fluid, 60fps flow between the list and detail screens.

---

## 🏗 Architecture Overview

<img width="70%" height="752" alt="1000238046" src="https://github.com/user-attachments/assets/a3ea2715-2861-4060-ad85-c836a772d128" />

---
The app follows MVVM with a unidirectional data flow. The UI never talks to the data layer directly — all state flows down through the ViewModel and all events flow up through it.
## 🧠 Key Engineering Decisions

### 🪶 1. Lean Data Strategy — Fetch Only What's Needed, When It's Needed

> **🚨 The Challenge:** Downloading 1,000 full Pokémon profiles at launch would bloat the app size and kill performance.

**🛠️ The Fix:** I designed an independent, lightweight search mechanism. The background sync only pulls the bare minimum (names and sprite URLs) into the database to ensure flawless searching. Full profile data (abilities, stats, types, etc) is fetched on-demand only when a user taps into a specific Pokémon. This keeps the APK under 10MB and the initial sync fast regardless of connection quality.

* **Compose Optimization:** To optimize the UI, I assigned unique `key` parameters to the Compose list items, drastically reducing unnecessary recompositions and keeping the scrolling buttery smooth.

### 🗄️ 2. Decoupled Database Architecture — Isolating Search from Paging State

> **🚨 The Challenge:** > Paging 3 relies heavily on Room's Invalidation Tracker to know when to refresh the UI. Initially, whenever the background `SyncWorker` updated the local cache or a search query was executed, Room would automatically invalidate the shared tables. This caused the main `Pager` to emit a redundant loading state, resulting in jarring "double loading" screens and interrupting the Compose UI animations.

**🛠️ The Solution:** I engineered a decoupled database architecture. By isolating the Search mechanism into its own dedicated table structure, completely separate from the main Pokemon Table (Paging Source), I severed the invalidation link. Now, background syncs and search queries update their respective data silos without triggering false invalidations on the main paged list. The result? A buttery-smooth, glitch-free UI that never loads the same data twice.

### 🧠 3. Why GraphQL Over the REST Endpoint

PokéAPI exposes both REST and GraphQL. The REST list endpoint returns full Pokémon objects — including data that's irrelevant for a paginated list view. GraphQL let me request exactly name and sprite_url for the list, reducing payload size significantly per page request. The tradeoff is added query complexity, but for a data-heavy app with pagination the bandwidth saving is worth it.

### 🧠 4. Why Koin Over Hilt

I chose Koin for this project because it has zero annotation processing overhead, which meaningfully reduces build times during development. For a solo project where iteration speed matters and the DI graph isn't enormous, Koin's runtime approach is a reasonable tradeoff. I'm aware Hilt would be the standard choice in a team or production environment.

## 🚀 Features

* **Offline-First Support:** Browse Pokémon seamlessly without an internet connection using local Room database caching.
* **Infinite Scrolling:** Smoothly loads data in paginated chunks of 40 using the Paging 3 library.
* **Background Synchronization:** Local search functionality powered by a background `SyncWorker` that silently fetches and updates the database.
* **Advanced UI Transitions:** Features beautiful Shared Element Transitions in Jetpack Compose for a fluid, 60fps flow between the list and detail screens.

## 🛠 Tech Stack

* **UI:** Jetpack Compose (Material 3)
* **Architecture:** Modern Android Architecture (MVVM + Repository Pattern) - utilizing Unidirectional Data Flow (UDF).
* **Database & Caching:** Room Persistence Library
* **Pagination:** Paging 3 (`RemoteMediator` & `RemoteKeys`)
* **Networking:** Retrofit with GraphQL
* **Dependency Injection:** Koin
* **Asynchronous Programming:** Kotlin Coroutines & Flows 
* **Image Loading:** Coil (with Shared Element support)
* **Background Tasks:** WorkManager

## 🗺️ Future Scope 
* **Battle Simulator:** A turn-based combat engine designed to replicate the mechanics and logic of the core Pokémon RPG series.

## 🏗 Setup & Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Amaanprobably/PokeDex.git
   ```
   
2. Open the project in Android Studio (Ladybug or newer recommended).

3. Sync the Gradle files.

4. Build and run the app on an Android Emulator or physical device.

## 🤝 Acknowledgments

A special thanks to the following resources and individuals:

* **UI & Visual Design Reference:** Massive thanks to [@philipplackner](https://github.com/philipplackner) for his classic 2020 Pokédex series, which served as the visual foundation and inspiration for this modern rebuild.
> All architecture, business logic, and engineering decisions are original.

* **Data Source:** [PokéAPI](https://pokeapi.co/) for providing the incredibly detailed, reliable, and free Pokémon database via GraphQL.
