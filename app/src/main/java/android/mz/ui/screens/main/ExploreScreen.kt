package android.mz.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.mz.ui.viewmodel.MoviesViewModel
import android.mz.ui.viewmodel.AuthViewModel
import android.mz.ui.components.MovieCard
import android.mz.ui.components.LoadingIndicator

@Composable
fun ExploreScreen(
    authViewModel: AuthViewModel,
    moviesViewModel: MoviesViewModel,
    onMovieClick: (android.mz.data.models.Movie) -> Unit = {}
) {
    val genres by moviesViewModel.genres.collectAsState()
    val discoverMovies by moviesViewModel.discoverMovies.collectAsState()
    val favoriteMovies by moviesViewModel.favoriteMovies.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoadingDiscover by moviesViewModel.isLoadingDiscover.collectAsState()
    
    var selectedGenre by remember { mutableStateOf<android.mz.data.models.Genre?>(null) }
    var selectedRating by remember { mutableStateOf<Double?>(null) }
    var personName by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    
    // Load discover movies when filters change
    LaunchedEffect(selectedGenre, selectedRating, personName) {
        moviesViewModel.discoverMovies(
            genreId = selectedGenre?.id,
            minRating = selectedRating,
            withPerson = personName.ifBlank { null }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp) // Space for bottom navigation
    ) {
        // Header
        ExploreHeader(
            onFilterClick = { showFilters = !showFilters }
        )
        
        // Filters Section
        if (showFilters) {
            FiltersSection(
                genres = genres,
                selectedGenre = selectedGenre,
                selectedRating = selectedRating,
                personName = personName,
                onPersonNameChanged = { personName = it },
                onGenreSelected = { selectedGenre = it },
                onRatingSelected = { selectedRating = it },
                onClearFilters = {
                    selectedGenre = null
                    selectedRating = null
                    personName = ""
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Movies Grid
        if (isLoadingDiscover) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                text = "Discovering movies..."
            )
        } else {
            DiscoverMoviesGrid(
                movies = discoverMovies,
                favoriteMovies = favoriteMovies,
                onMovieClick = onMovieClick,
                onFavoriteClick = { movie ->
                    val userId = currentUser?.id
                    if (favoriteMovies.any { it.id == movie.id }) {
                        moviesViewModel.removeFromFavorites(movie, userId)
                    } else {
                        moviesViewModel.addToFavorites(movie, userId)
                    }
                }
            )
        }
    }
}

@Composable
fun ExploreHeader(
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Explore",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Discover movies by genre and rating",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filters",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FiltersSection(
    genres: List<android.mz.data.models.Genre>,
    selectedGenre: android.mz.data.models.Genre?,
    selectedRating: Double?,
    personName: String,
    onPersonNameChanged: (String) -> Unit,
    onGenreSelected: (android.mz.data.models.Genre?) -> Unit,
    onRatingSelected: (Double?) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Clear All",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actor/Director
            Text(
                text = "Actor/Director",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = personName,
                onValueChange = onPersonNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(text = "Type a name (e.g., Tom Hanks)", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                )
            )

            // Genres
            Text(
                text = "Genres",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(genres) { genre ->
                    GenreChip(
                        genre = genre,
                        isSelected = selectedGenre?.id == genre.id,
                        onClick = {
                            onGenreSelected(if (selectedGenre?.id == genre.id) null else genre)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rating
            Text(
                text = "Minimum Rating",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                val ratings = listOf(4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
                items(ratings) { rating ->
                    RatingChip(
                        rating = rating,
                        isSelected = selectedRating == rating,
                        onClick = {
                            onRatingSelected(if (selectedRating == rating) null else rating)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GenreChip(
    genre: android.mz.data.models.Genre,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = genre.name,
                color = if (isSelected) Color.Black else Color.White
            )
        },
        selected = isSelected,
        enabled = true,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color.White,
            containerColor = Color.Transparent,
            selectedLabelColor = Color.Black,
            labelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = Color.White.copy(alpha = 0.5f),
            selectedBorderColor = Color.White
        )
    )
}

@Composable
fun RatingChip(
    rating: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = "${rating.toInt()}+",
                color = if (isSelected) Color.Black else Color.White
            )
        },
        selected = isSelected,
        enabled = true,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color.White,
            containerColor = Color.Transparent,
            selectedLabelColor = Color.Black,
            labelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = Color.White.copy(alpha = 0.5f),
            selectedBorderColor = Color.White
        )
    )
}

@Composable
fun DiscoverMoviesGrid(
    movies: List<android.mz.data.models.Movie>,
    favoriteMovies: List<android.mz.data.models.Movie>,
    onMovieClick: (android.mz.data.models.Movie) -> Unit,
    onFavoriteClick: (android.mz.data.models.Movie) -> Unit
) {
    if (movies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No movies found\nTry adjusting your filters",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
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
                    isFavorite = favoriteMovies.any { it.id == movie.id },
                    onMovieClick = onMovieClick,
                    onFavoriteClick = onFavoriteClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
