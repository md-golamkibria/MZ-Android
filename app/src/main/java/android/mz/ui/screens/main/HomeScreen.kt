package android.mz.ui.screens.main

import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.mz.ui.viewmodel.AuthViewModel
import android.mz.ui.viewmodel.MoviesViewModel
import android.mz.ui.components.MovieCard
import android.mz.ui.components.LoadingIndicator

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    moviesViewModel: MoviesViewModel,
    onMovieClick: (android.mz.data.models.Movie) -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val popularMovies by moviesViewModel.popularMovies.collectAsState()
    val favoriteMovies by moviesViewModel.favoriteMovies.collectAsState()
    val isLoadingPopular by moviesViewModel.isLoadingPopular.collectAsState()
    
    var showProfileDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp) // Space for bottom navigation
    ) {
        // Header Section
        HeaderSection(
            currentUser = currentUser,
            favoriteCount = favoriteMovies.size,
            movieCount = popularMovies.size,
            onProfileClick = { showProfileDialog = true }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Popular Movies Section
        if (isLoadingPopular) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                text = "Loading movies..."
            )
        } else {
            PopularMoviesSection(
                movies = popularMovies,
                favoriteMovies = favoriteMovies,
                onMovieClick = onMovieClick,
                onFavoriteClick = { movie ->
                    val userId = currentUser?.id
                    if (moviesViewModel.isFavorite(movie)) {
                        moviesViewModel.removeFromFavorites(movie, userId)
                    } else {
                        moviesViewModel.addToFavorites(movie, userId)
                    }
                }
            )
        }
    }
    
    // Profile Dialog
    if (showProfileDialog) {
        ProfileDialog(
            currentUser = currentUser,
            onDismiss = { showProfileDialog = false },
            onLogout = {
                showProfileDialog = false
                authViewModel.logout()
            }
        )
    }
}

@Composable
fun HeaderSection(
    currentUser: android.mz.data.models.User?,
    favoriteCount: Int,
    movieCount: Int,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MovieZone",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Discover the latest movies",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Profile Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                val pic = currentUser?.profilePicture
                if (!pic.isNullOrEmpty()) {
                    if (pic.startsWith("data:image")) {
                        val comma = pic.indexOf(",")
                        val base64Part = if (comma != -1) pic.substring(comma + 1) else pic
                        val bytes = try { Base64.decode(base64Part, Base64.DEFAULT) } catch (_: Exception) { null }
                        val bmp = try { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } } catch (_: Exception) { null }
                        if (bmp != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        android.util.Log.d("HomeScreen", "Profile picture URL: $pic")
                        AsyncImage(
                            model = pic,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            onError = { error ->
                                android.util.Log.e("HomeScreen", "Profile picture load error: $error")
                            },
                            onSuccess = { _ ->
                                android.util.Log.d("HomeScreen", "Profile picture loaded successfully")
                            }
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            StatItem(
                icon = Icons.Default.Movie,
                value = movieCount.toString(),
                label = "Movies"
            )
            StatItem(
                icon = Icons.Default.Favorite,
                value = favoriteCount.toString(),
                label = "Favorites"
            )
            StatItem(
                icon = Icons.Default.Star,
                value = "8.5",
                label = "Avg Rating"
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PopularMoviesSection(
    movies: List<android.mz.data.models.Movie>,
    favoriteMovies: List<android.mz.data.models.Movie>,
    onMovieClick: (android.mz.data.models.Movie) -> Unit,
    onFavoriteClick: (android.mz.data.models.Movie) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Popular Movies",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(1200.dp), // Increased height for more movies
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies.take(20)) { movie -> // Show first 20 movies
                MovieCard(
                    movie = movie,
                    isFavorite = favoriteMovies.any { it.id == movie.id },
                    onMovieClick = onMovieClick,
                    onFavoriteClick = onFavoriteClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ProfileDialog(
    currentUser: android.mz.data.models.User?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Profile",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                val profilePicture = currentUser?.profilePicture
                if (!profilePicture.isNullOrEmpty()) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Text(
                    text = "Username: ${currentUser?.username ?: "Unknown"}",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                val userEmail = currentUser?.email
                if (!userEmail.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Email: $userEmail",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onLogout) {
                Text("Logout", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        },
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
