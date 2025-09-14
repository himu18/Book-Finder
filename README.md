# Book Finder

A native Android app in Kotlin using Jetpack Compose to search books and view details via the Open Library API. Implements MVVM with Clean Architecture, Retrofit/OkHttp, Room, Hilt DI, Paging, StateFlow, Glide Compose, pull-to-refresh, shimmer, and animated details.

## Setup Instructions
- Android Studio Ladybug or newer
- JDK 17
- Clone and open the project in Android Studio
- Build & run:
```bash
./gradlew assembleDebug
```
- Min SDK 24, Target/Compile SDK 36

## Architecture (MVVM + Clean)
- domain: `model`, `repository` interface, `usecase`
- data: `remote` (Retrofit, DTOs), `local` (Room), `repository` implementation, mappers
- di: Hilt modules (Retrofit/OkHttp, Room, Repository)
- ui: Compose screens, ViewModels (Hilt), navigation
- State: Kotlin `StateFlow`, `PagingData`

Flow:
- Search query -> ViewModel -> Repository -> Retrofit PagingSource -> UI renders list.
- Details -> repository fetches work details; favorites stored via Room and observed as Flow.

## Networking & Pagination
- Base URL `https://openlibrary.org/`
- Endpoints:
  - Search: `/search.json?title={query}&limit=20&page={page}`
  - Details: `/works/{work_id}.json`
  - Covers: `https://covers.openlibrary.org/b/id/{cover_id}-M.jpg`
- Paging via `Pager(PagingConfig(20))` with a custom `PagingSource`.
- OkHttp logging interceptor enabled (BASIC).

## Database (Room/SQLite)
- Table `books` fields: `id`, `title`, `author`, `cover_url`, `publish_year`, `description`, `is_saved`, `created_at`.
- DAO: upsert, deleteById, observeAll, isSaved.

## UI
- Search Screen: search bar, grid results (title, author, thumbnail), pull-to-refresh, shimmer, infinite scroll.
- Details Screen: cover with rotation animation, title/author/year/description, favorite toggle.
- Glide Compose for images.

## Testing
- Unit: `DetailsViewModelTest` with fake repository
- API: `OpenLibraryApiTest` using MockWebServer
- DB: `RoomTest` using in-memory Room

## Cross-Platform Discussion
- The core domain models, repository interface, and use cases can be shared via Kotlin Multiplatform.
- Replace Android-specific layers (Compose UI, Room, Hilt) with platform counterparts: SwiftUI + KMM storage/DI on iOS.

## Known Limitations
- Details endpoint enriches minimal book data; not all fields are exhaustively mapped.
- Error messaging is minimal; production app should provide retry and richer states.
- No offline cache for search results beyond favorites.
