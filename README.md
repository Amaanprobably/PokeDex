# Pokedex Android App 📱

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)

A modern, robust Android application that displays Pokémon data using the PokéAPI via GraphQL. This project was built to demonstrate proficiency in modern Android development, focusing on Clean Architecture, offline-first capabilities, complex state management, and advanced UI transitions.

## 🚀 Features

* **Offline-First Support:** Browse Pokémon seamlessly without an internet connection using local Room database caching.
* **Infinite Scrolling:** Smoothly loads data in paginated chunks of 40 using the Paging 3 library.
* **Background Synchronization:** Local search functionality powered by a background `SyncWorker` that silently fetches and updates the database.
* **Advanced UI Transitions:** Features beautiful Shared Element Transitions in Jetpack Compose for a fluid, 60fps flow between the list and detail screens.

## 🛠 Tech Stack

* **UI:** Jetpack Compose (Material 3)
* **Architecture:** Modern Android Architecture (MVVM + Repository Pattern) - utilizing Dependency Inversion for clear separation of concerns.
* **Database & Caching:** Room Persistence Library
* **Pagination:** Paging 3 (`RemoteMediator` & `RemoteKeys`)
* **Networking:** Retrofit with GraphQL
* **Dependency Injection:** Koin
* **Asynchronous Programming:** Kotlin Coroutines & Flows 
* **Image Loading:** Coil (with Shared Element support)
* **Background Tasks:** WorkManager

## 🧠 Technical Challenges & Solutions

### 👻 The "Ghost Data" Pagination Conflict
> **The Problem:** I wanted a lightning-fast local search, so I built a background `SyncWorker` to cache 1,000 basic Pokémon entries. However, this completely broke my main list's pagination. Paging 3 calculates its next offset based on database size. Seeing 1,000 items (which I call "ghost data" since they lacked full details), the paginator assumed the list was fully loaded and permanently stuck the infinite scroll at page one.

**The Fix:** I implemented a **RemoteKeys** architecture. By storing explicit API offsets in a dedicated `remote_keys` table, the pagination logic became 100% independent of the main data table. The paginator now smoothly ignores the background search data and fetches the correct next page every time.


### 🪶 Keeping the App Lean (4-10MB) & Fast
> **The Challenge:** Downloading 1,000 full Pokémon profiles at launch would bloat the app size and kill performance.

**The Fix:** I designed an independent, lightweight search mechanism. The background sync only pulls the bare minimum (names and sprite URLs) into the database. The heavy data is only fetched on-demand when a user clicks a specific Pokémon. This keeps the final APK size incredibly small (under 10MB) without compromising the user experience.


### 🐛 Squashing UI Glitches (Double-Taps & Recompositions)
> **The Challenge:** In Compose, users could tap two Pokémon simultaneously, causing the app stack multiple detail screens on top of each other or have glitchy animation. Additionally, the list was doing too much work redrawing items during scrolls.

**The Fix:** - **Safe Navigation:** I eliminated the multi-touch navigation bug by intercepting clicks and checking the `NavController`'s `Lifecycle.State.RESUMED` status before triggering any transitions.
* **Compose Optimization:** To optimize the UI, I assigned unique `key` parameters to the Compose list items, drastically reducing unnecessary recompositions and keeping the scrolling buttery smooth.
## 📸 Screenshots

<img src="https://github.com/user-attachments/assets/3de5851e-1792-45df-844e-f41891a9d437" width="30%" />
&nbsp; &nbsp; &nbsp; &nbsp;
<img src="https://github.com/user-attachments/assets/e5554614-98f3-4436-902f-a040d1f14b0c" width="30%" />
&nbsp; &nbsp; &nbsp; &nbsp;
<img src="https://github.com/user-attachments/assets/e24a752d-53c6-44dd-bd6a-1ffc51712e49" width="30%" />

## 🏗 Setup & Installation

1. Clone the repository:
   ```bash
   git clone [https://github.com/Amaanprobably/PokeDex.git](https://github.com/Amaanprobably/PokeDex.git)
   
2. Open the project in Android Studio (Ladybug or newer recommended).

3. Sync the Gradle files.

4. Build and run the app on an Android Emulator or physical device.
