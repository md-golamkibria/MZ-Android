package android.mz.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.mz.data.api.TMDBService
import android.mz.data.models.*
import android.mz.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoviesViewModel(application: Application) : AndroidViewModel(application) {
    private val tmdbService = TMDBService.getInstance()
    private val favoritesRepository = FavoritesRepository.getInstance(application)
    
    // Popular movies
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies.asStateFlow()
    
    // Search results
    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()
    
    // Genres
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()
    
    // Discover movies
    private val _discoverMovies = MutableStateFlow<List<Movie>>(emptyList())
    val discoverMovies: StateFlow<List<Movie>> = _discoverMovies.asStateFlow()
    
    // Movie details
    private val _movieDetail = MutableStateFlow<MovieDetail?>(null)
    val movieDetail: StateFlow<MovieDetail?> = _movieDetail.asStateFlow()
    
    // Movie credits
    private val _movieCredits = MutableStateFlow<CreditsResponse?>(null)
    val movieCredits: StateFlow<CreditsResponse?> = _movieCredits.asStateFlow()
    
    // Similar movies
    private val _similarMovies = MutableStateFlow<List<Movie>>(emptyList())
    val similarMovies: StateFlow<List<Movie>> = _similarMovies.asStateFlow()
    
    // Recommended movies
    private val _recommendedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val recommendedMovies: StateFlow<List<Movie>> = _recommendedMovies.asStateFlow()
    
    // Person details
    private val _personDetail = MutableStateFlow<PersonDetail?>(null)
    val personDetail: StateFlow<PersonDetail?> = _personDetail.asStateFlow()
    
    // Person movie credits
    private val _personMovieCredits = MutableStateFlow<PersonMovieCredits?>(null)
    val personMovieCredits: StateFlow<PersonMovieCredits?> = _personMovieCredits.asStateFlow()
    
    // Loading states
    private val _isLoadingPopular = MutableStateFlow(false)
    val isLoadingPopular: StateFlow<Boolean> = _isLoadingPopular.asStateFlow()
    
    private val _isLoadingSearch = MutableStateFlow(false)
    val isLoadingSearch: StateFlow<Boolean> = _isLoadingSearch.asStateFlow()
    
    private val _isLoadingDiscover = MutableStateFlow(false)
    val isLoadingDiscover: StateFlow<Boolean> = _isLoadingDiscover.asStateFlow()
    
    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()
    
    // Favorites
    val favoriteMovies: StateFlow<List<Movie>> = favoritesRepository.favoriteMovies
    val isFavoritesLoading: StateFlow<Boolean> = favoritesRepository.isLoading
    val favoritesError: StateFlow<String> = favoritesRepository.errorMessage
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        fetchPopularMovies()
        fetchGenres()
    }
    
    fun fetchPopularMovies() {
        viewModelScope.launch {
            _isLoadingPopular.value = true
            try {
                val movies = tmdbService.fetchPopularMovies()
                _popularMovies.value = movies
            } finally {
                _isLoadingPopular.value = false
            }
        }
    }
    
    fun searchMovies(query: String) {
        viewModelScope.launch {
            _isLoadingSearch.value = true
            try {
                val movies = tmdbService.searchMovies(query)
                _searchResults.value = movies
            } finally {
                _isLoadingSearch.value = false
            }
        }
    }
    
    fun fetchGenres() {
        viewModelScope.launch {
            try {
                val genreList = tmdbService.fetchGenres()
                _genres.value = genreList
            } catch (e: Exception) {
                // Handle error silently for genres
            }
        }
    }
    
    fun discoverMovies(genreId: Int? = null, minRating: Double? = null, withPerson: String? = null) {
        viewModelScope.launch {
            _isLoadingDiscover.value = true
            try {
                val movies = tmdbService.discoverMovies(genreId, minRating, withPerson)
                _discoverMovies.value = movies
            } finally {
                _isLoadingDiscover.value = false
            }
        }
    }
    
    fun fetchMovieDetails(id: Int) {
        viewModelScope.launch {
            _isLoadingDetails.value = true
            try {
                val details = tmdbService.fetchMovieDetails(id)
                _movieDetail.value = details
                
                // Also fetch credits and related movies
                val credits = tmdbService.fetchMovieCredits(id)
                _movieCredits.value = credits
                
                val similar = tmdbService.fetchSimilarMovies(id)
                _similarMovies.value = similar
                
                val recommended = tmdbService.fetchRecommendedMovies(id)
                _recommendedMovies.value = recommended
            } finally {
                _isLoadingDetails.value = false
            }
        }
    }
    
    fun fetchPersonDetails(id: Int) {
        viewModelScope.launch {
            try {
                val details = tmdbService.fetchPersonDetails(id)
                _personDetail.value = details
                
                val credits = tmdbService.fetchPersonMovieCredits(id)
                _personMovieCredits.value = credits
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun addToFavorites(movie: Movie, userId: String? = null) {
        favoritesRepository.addToFavorites(movie, userId)
    }
    
    fun removeFromFavorites(movie: Movie, userId: String? = null) {
        favoritesRepository.removeFromFavorites(movie, userId)
    }
    
    fun isFavorite(movie: Movie): Boolean {
        return favoritesRepository.isFavorite(movie)
    }
    
    fun syncFavoritesWithBackend(userId: String) {
        viewModelScope.launch {
            favoritesRepository.syncWithBackend(userId)
        }
    }
    
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
    
    fun clearMovieDetails() {
        _movieDetail.value = null
        _movieCredits.value = null
        _similarMovies.value = emptyList()
        _recommendedMovies.value = emptyList()
    }
    
    fun clearPersonDetails() {
        _personDetail.value = null
        _personMovieCredits.value = null
    }
}
