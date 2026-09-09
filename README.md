# MovieZone Android App

A modern Android movie discovery app built with Jetpack Compose, matching the iOS MovieZone app functionality with full backend integration.

## Features

### 🎬 Core Features
- **Movie Discovery**: Browse popular movies with beautiful card layouts
- **Advanced Search**: Search movies by title with real-time results
- **Genre Filtering**: Explore movies by genre and rating filters
- **Favorites/Wishlist**: Save favorite movies with backend sync
- **Movie Details**: Comprehensive movie information with cast, crew, and related movies
- **Person Profiles**: Actor and director profiles with complete filmography

### 🔐 Authentication & Profile
- **User Authentication**: Login/Register with backend integration
- **Profile Management**: Upload profile pictures, manage account
- **Persistent Login**: Stay logged in between app sessions
- **Account Deletion**: Complete account and data removal

### 🌐 Backend Integration
- **Real-time Sync**: Favorites sync with MongoDB backend
- **Profile Pictures**: Upload and sync profile images
- **Cross-platform**: Shares same backend as iOS app and web app
- **Offline-first**: Local storage with backend synchronization

## Technical Stack

### Architecture
- **MVVM Pattern**: Clean separation of concerns
- **Repository Pattern**: Data layer abstraction
- **StateFlow**: Reactive state management
- **Jetpack Compose**: Modern declarative UI

### Libraries & Dependencies
- **Jetpack Compose**: UI framework
- **Navigation Compose**: Screen navigation
- **Retrofit**: HTTP client for API calls
- **Coil**: Image loading and caching
- **Gson**: JSON serialization
- **OkHttp**: HTTP logging and cookie management
- **Material 3**: Modern Material Design components

### APIs
- **TMDB API**: Movie data, cast, crew, and images
- **Backend API**: Authentication, favorites, and profile management
- **Base URL**: `https://moviezoneonline.vercel.app/api`

## Project Structure

```
app/src/main/java/android/mz/
├── data/
│   ├── api/
│   │   ├── APIService.kt          # Backend API integration
│   │   └── TMDBService.kt         # TMDB API integration
│   ├── models/
│   │   ├── Movie.kt               # Movie data models
│   │   └── User.kt                # User and auth models
│   └── repository/
│       ├── AuthenticationRepository.kt
│       └── FavoritesRepository.kt
├── ui/
│   ├── components/
│   │   ├── MovieCard.kt           # Movie display components
│   │   └── LoadingIndicator.kt    # Loading animations
│   ├── screens/
│   │   ├── auth/
│   │   │   └── LoginScreen.kt     # Authentication UI
│   │   ├── main/
│   │   │   ├── HomeScreen.kt      # Popular movies
│   │   │   ├── ExploreScreen.kt   # Genre filtering
│   │   │   ├── SearchScreen.kt    # Movie search
│   │   │   ├── FavoritesScreen.kt # User favorites
│   │   │   └── ProfileScreen.kt   # User profile
│   │   └── detail/
│   │       ├── MovieDetailScreen.kt   # Movie details
│   │       └── PersonDetailScreen.kt  # Actor/director profiles
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt       # Authentication logic
│   │   └── MoviesViewModel.kt     # Movie data logic
│   └── theme/
│       └── Theme.kt               # App theming
└── MainActivity.kt                # App entry point
```

## Backend Integration

### Authentication Endpoints
- `POST /api/users/signin` - User login
- `POST /api/auth/register` - User registration
- `DELETE /api/profile/delete-account` - Account deletion

### Wishlist Endpoints
- `GET /api/wishlist` - Get user favorites
- `POST /api/wishlist` - Add movie to favorites
- `DELETE /api/wishlist?id={movieId}` - Remove from favorites

### Profile Endpoints
- `PUT /api/users/profile` - Upload profile picture (base64 format)

### Data Models
All API responses follow the backend format:
```json
{
  "ok": true/false,
  "error": "error message",
  "user": { ... },
  "items": [ ... ]
}
```

## Key Features Implementation

### 🎯 Navigation Flow
```
Login → Main App → Movie Details → Person Details
  ↓         ↓           ↓              ↓
Auth    Bottom Nav   Cast/Crew    Filmography
        (5 tabs)     Navigation    Navigation
```

### 🔄 State Management
- **AuthViewModel**: User authentication state
- **MoviesViewModel**: Movie data and favorites
- **Repository Pattern**: Data source abstraction
- **StateFlow**: Reactive UI updates

### 🎨 UI Components
- **MovieCard**: Reusable movie display with favorites
- **CompactMovieCard**: Smaller cards for related movies
- **LoadingIndicator**: Animated loading states
- **Modern Bottom Navigation**: Custom tab bar design

### 📱 Responsive Design
- **Grid Layouts**: Adaptive movie grids
- **Scrollable Content**: Smooth scrolling experiences
- **Loading States**: Comprehensive loading indicators
- **Error Handling**: User-friendly error messages

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0)
- Internet connection for API calls

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Run on device or emulator

### Configuration
- TMDB API key is included in `TMDBService.kt`
- Backend URL is configured in `APIService.kt`
- No additional configuration required

## Features Comparison with iOS App

| Feature | iOS App | Android App | Status |
|---------|---------|-------------|--------|
| Authentication | ✅ | ✅ | Complete |
| Movie Discovery | ✅ | ✅ | Complete |
| Search & Filters | ✅ | ✅ | Complete |
| Favorites Sync | ✅ | ✅ | Complete |
| Movie Details | ✅ | ✅ | Complete |
| Cast/Crew Navigation | ✅ | ✅ | Complete |
| Person Profiles | ✅ | ✅ | Complete |
| Profile Pictures | ✅ | ✅ | Complete |
| Account Management | ✅ | ✅ | Complete |
| Offline Support | ✅ | ✅ | Complete |

## Backend Compatibility

The Android app is fully compatible with the existing backend:
- ✅ **Same Database**: Uses MongoDB collections (users, wishlists)
- ✅ **Same Endpoints**: Identical API endpoints as iOS app
- ✅ **Cross-platform Sync**: Favorites sync between iOS, Android, and web
- ✅ **Profile Pictures**: Images sync across all platforms
- ✅ **Authentication**: JWT cookie-based auth system

## Performance Optimizations

- **Image Caching**: Coil library for efficient image loading
- **Lazy Loading**: LazyColumn/LazyRow for large lists
- **State Preservation**: Proper state management across navigation
- **Memory Management**: Efficient ViewModels and repositories
- **Network Optimization**: Request caching and error handling

## Testing

The app includes comprehensive error handling and loading states:
- Network connectivity issues
- API response errors
- Image loading failures
- Authentication failures
- Empty states and edge cases

## Future Enhancements

Potential improvements for future versions:
- Push notifications for new movie releases
- Social features (share favorites, reviews)
- Offline movie downloads
- Advanced filtering options
- Personalized recommendations
- Dark/Light theme toggle

---

**MovieZone Android** - A complete movie discovery experience with seamless backend integration and cross-platform compatibility.
