package android.mz.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.mz.ui.viewmodel.AuthViewModel
import android.mz.ui.viewmodel.MoviesViewModel
import android.mz.ui.components.MovieCard
import android.mz.ui.components.LoadingIndicator

@Composable
fun FavoritesScreen(
    authViewModel: AuthViewModel,
    moviesViewModel: MoviesViewModel,
    onMovieClick: (android.mz.data.models.Movie) -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val favoriteMovies by moviesViewModel.favoriteMovies.collectAsState()
    val isFavoritesLoading by moviesViewModel.isFavoritesLoading.collectAsState()
    val favoritesError by moviesViewModel.favoritesError.collectAsState()
    
    // Sync favorites when screen appears
    LaunchedEffect(currentUser) {
        android.util.Log.d("FavoritesScreen", "=== FAVORITES SCREEN LOADED ===")
        android.util.Log.d("FavoritesScreen", "Current User: $currentUser")
        android.util.Log.d("FavoritesScreen", "User ID: ${currentUser?.id}")
        android.util.Log.d("FavoritesScreen", "Username: ${currentUser?.username}")
        android.util.Log.d("FavoritesScreen", "Profile Picture: ${currentUser?.profilePicture}")
        
        currentUser?.id?.let { userId ->
            android.util.Log.d("FavoritesScreen", "Syncing favorites for user: $userId")
            moviesViewModel.syncFavoritesWithBackend(userId)
        } ?: run {
            android.util.Log.e("FavoritesScreen", "No user ID found - cannot sync favorites")
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp) // Space for bottom navigation
    ) {
        // Header
        WishlistHeader(wishlistCount = favoriteMovies.size)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content
        when {
            isFavoritesLoading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    text = "Loading wishlist..."
                )
            }
            favoritesError.isNotEmpty() -> {
                ErrorMessage(
                    message = favoritesError,
                    onRetry = {
                        currentUser?.id?.let { userId ->
                            moviesViewModel.syncFavoritesWithBackend(userId)
                        }
                    }
                )
            }
            favoriteMovies.isEmpty() -> {
                EmptyWishlist()
            }
            else -> {
                WishlistGrid(
                    movies = favoriteMovies,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = { movie ->
                        val userId = currentUser?.id
                        moviesViewModel.removeFromFavorites(movie, userId)
                    }
                )
            }
        }
    }
}

@Composable
fun WishlistHeader(wishlistCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Wishlist",
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "My Wishlist",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Text(
            text = "$wishlistCount movies",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun WishlistGrid(
    movies: List<android.mz.data.models.Movie>,
    onMovieClick: (android.mz.data.models.Movie) -> Unit,
    onFavoriteClick: (android.mz.data.models.Movie) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp), // Fixed height for grid
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies) { movie ->
            MovieCard(
                movie = movie,
                isFavorite = true, // All movies in favorites are favorites
                onMovieClick = onMovieClick,
                onFavoriteClick = onFavoriteClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EmptyWishlist() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "No wishlist items",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your wishlist is empty",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Start adding movies to your wishlist by tapping the heart icon",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error Loading Wishlist",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
            ) {
                Text(
                    text = "Retry",
                    color = Color.White
                )
            }
        }
    }
}
