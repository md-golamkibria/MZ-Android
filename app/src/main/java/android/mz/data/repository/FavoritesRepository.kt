@file:Suppress("SpellCheckingInspection")
package android.mz.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.mz.data.api.APIService
import android.mz.data.models.Movie
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FavoritesRepository(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: FavoritesRepository? = null
        
        fun getInstance(context: Context): FavoritesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoritesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("MovieZonePrefs", Context.MODE_PRIVATE)
    private val apiService = APIService.getInstance()
    private val gson = Gson()
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val favoritesKey = "FavoriteMovies"
    
    private val _favoriteMovies = MutableStateFlow<List<Movie>>(emptyList())
    val favoriteMovies: StateFlow<List<Movie>> = _favoriteMovies.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    init {
        loadFavorites()
        Log.d("FavoritesRepository", "Initialized - Count: ${_favoriteMovies.value.size}")
        Log.d("FavoritesRepository", "Will sync with real backend when user logs in")
    }
    
    private fun loadFavorites() {
        try {
            val favoritesJson = sharedPreferences.getString(favoritesKey, null)
            if (favoritesJson != null) {
                val type = object : TypeToken<List<Movie>>() {}.type
                val movies = gson.fromJson<List<Movie>>(favoritesJson, type)
                _favoriteMovies.value = movies
                Log.d("FavoritesRepository", "Loaded ${movies.size} favorite movies from storage")
            } else {
                // Load sample movies for first time users
                addSampleMoviesForTesting()
            }
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error loading favorites", e)
            _favoriteMovies.value = emptyList()
        }
    }
    
    private fun addSampleMoviesForTesting() {
        // Add movies that match the web wishlist - based on the iOS app
        val webWishlistMovies = listOf(
            Movie(
                id = 278,
                title = "The Shawshank Redemption",
                overview = "Framed in the 1940s for the double murder of his wife and her lover, upstanding banker Andy Dufresne begins a new life at the Shawshank prison.",
                posterPath = "/9cqNxx0GxF0bflyCy3FpPiy3BXg.jpg",
                backdropPath = "/kXfqcdQKsToO0OUXHcrrNCHDBzO.jpg",
                releaseDate = "1994-09-23",
                voteAverage = 8.7,
                genreIds = listOf(18, 80),
                adult = false,
                originalLanguage = "en",
                originalTitle = "The Shawshank Redemption",
                popularity = 95.0,
                video = false,
                voteCount = 26000
            ),
            Movie(
                id = 420818,
                title = "The Lion King",
                overview = "Simba idolizes his father, King Mufasa, and takes to heart his own royal destiny.",
                posterPath = "/2bXbqYdUdNVa8VIWXVfclP2ICtT.jpg",
                backdropPath = "/1TUg5pO1VZ4B0Q1amk3OlXvlpXV.jpg",
                releaseDate = "2019-07-12",
                voteAverage = 7.1,
                genreIds = listOf(12, 16, 18, 10751),
                adult = false,
                originalLanguage = "en",
                originalTitle = "The Lion King",
                popularity = 85.0,
                video = false,
                voteCount = 8500
            ),
            Movie(
                id = 680,
                title = "Pulp Fiction",
                overview = "A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper.",
                posterPath = "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
                backdropPath = "/4cDFJr4HnXN5AdPw4AKrmLlMWdO.jpg",
                releaseDate = "1994-09-10",
                voteAverage = 8.5,
                genreIds = listOf(80, 18),
                adult = false,
                originalLanguage = "en",
                originalTitle = "Pulp Fiction",
                popularity = 75.0,
                video = false,
                voteCount = 27000
            ),
            Movie(
                id = 13,
                title = "Forrest Gump",
                overview = "A man with a low IQ has accomplished great things in his life and been present during significant historic events.",
                posterPath = "/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg",
                backdropPath = "/3h1JZGDhZ8nzxdgvkxha0qBqi05.jpg",
                releaseDate = "1994-06-23",
                voteAverage = 8.5,
                genreIds = listOf(35, 18, 10749),
                adult = false,
                originalLanguage = "en",
                originalTitle = "Forrest Gump",
                popularity = 75.0,
                video = false,
                voteCount = 26000
            )
        )
        
        _favoriteMovies.value = webWishlistMovies
        saveFavorites()
        Log.d("FavoritesRepository", "Added ${webWishlistMovies.size} movies matching web wishlist")
        webWishlistMovies.forEach { movie ->
            Log.d("FavoritesRepository", "Added: ${movie.title}")
        }
    }
    
    fun addToFavorites(movie: Movie, userId: String? = null) {
        // Add to local favorites immediately for better UX
        val currentFavorites = _favoriteMovies.value.toMutableList()
        if (!currentFavorites.any { it.id == movie.id }) {
            currentFavorites.add(movie)
            _favoriteMovies.value = currentFavorites
            saveFavorites()
            Log.d("FavoritesRepository", "Added ${movie.title} to local favorites")
        }
        
        // Sync with backend if user is logged in
        userId?.let { id ->
            syncAddToBackend(movie, id)
        }
    }
    
    fun removeFromFavorites(movie: Movie, userId: String? = null) {
        // Remove from local favorites immediately
        val currentFavorites = _favoriteMovies.value.toMutableList()
        currentFavorites.removeAll { it.id == movie.id }
        _favoriteMovies.value = currentFavorites
        saveFavorites()
        Log.d("FavoritesRepository", "Removed ${movie.title} from local favorites")
        
        // Sync with backend if user is logged in
        userId?.let { id ->
            syncRemoveFromBackend(movie.id, id)
        }
    }
    
    suspend fun syncWithBackend(userId: String) {
        Log.d("FavoritesRepository", "Starting sync with backend for user: $userId")
        _isLoading.value = true
        _errorMessage.value = ""
        
        try {
            val response = apiService.getWishlist(userId)
            val items = response.wishlist ?: emptyList()
            if (response.success) {
                Log.d("FavoritesRepository", "Received ${items.size} movies from backend")
                // Merge backend items with current local favorites to avoid losing recent local additions
                val merged = (items + _favoriteMovies.value)
                    .distinctBy { it.id }
                _favoriteMovies.value = merged
                _isLoading.value = false
                saveFavorites()
            } else {
                _errorMessage.value = response.message ?: "Failed to sync favorites"
                _isLoading.value = false
                Log.e("FavoritesRepository", "Sync failed: ${response.message}")
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Failed to sync favorites"
            _isLoading.value = false
            Log.e("FavoritesRepository", "Sync error", e)
        }
    }
    
    private fun syncAddToBackend(movie: Movie, userId: String) {
        ioScope.launch {
            try {
                Log.d("FavoritesRepository", "Sync add to backend: ${movie.title}")
                val resp = apiService.addToWishlist(movie, userId)
                if (!resp.success) {
                    Log.e("FavoritesRepository", "Backend add failed: ${resp.message}")
                    _errorMessage.value = resp.message ?: "Failed to add to wishlist on server"
                } else {
                    Log.d("FavoritesRepository", "Backend add success. Items: ${resp.items?.size ?: 0}")
                }
            } catch (e: Exception) {
                Log.e("FavoritesRepository", "Add sync error", e)
                _errorMessage.value = e.message ?: "Failed to add to wishlist on server"
            }
        }
    }

    private fun syncRemoveFromBackend(movieId: Int, userId: String) {
        ioScope.launch {
            try {
                Log.d("FavoritesRepository", "Sync remove from backend: ID $movieId")
                val resp = apiService.removeFromWishlist(movieId, userId)
                if (!resp.success) {
                    Log.e("FavoritesRepository", "Backend remove failed: ${resp.message}")
                    _errorMessage.value = resp.message ?: "Failed to remove from wishlist on server"
                } else {
                    Log.d("FavoritesRepository", "Backend remove success. Items: ${resp.items?.size ?: 0}")
                }
            } catch (e: Exception) {
                Log.e("FavoritesRepository", "Remove sync error", e)
                _errorMessage.value = e.message ?: "Failed to remove from wishlist on server"
            }
        }
    }
    
    fun isFavorite(movie: Movie): Boolean {
        return _favoriteMovies.value.any { it.id == movie.id }
    }
    
    private fun saveFavorites() {
        try {
            val favoritesJson = gson.toJson(_favoriteMovies.value)
            sharedPreferences.edit {
                putString(favoritesKey, favoritesJson)
            }
            Log.d("FavoritesRepository", "Favorites saved to SharedPreferences")
        } catch (e: Exception) {
            Log.e("FavoritesRepository", "Error saving favorites", e)
        }
    }
    
    @Suppress("unused")
    fun clearError() {
        _errorMessage.value = ""
    }
}
