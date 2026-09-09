package android.mz.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.mz.data.models.*
import android.mz.ui.viewmodel.MoviesViewModel
import android.mz.ui.components.LoadingIndicator

@Composable
fun PersonDetailScreen(
    personId: Int,
    moviesViewModel: MoviesViewModel,
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val personDetail by moviesViewModel.personDetail.collectAsState()
    val personMovieCredits by moviesViewModel.personMovieCredits.collectAsState()
    val favoriteMovies by moviesViewModel.favoriteMovies.collectAsState()
    
    // Load person details when screen appears
    LaunchedEffect(personId) {
        moviesViewModel.fetchPersonDetails(personId)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        Color(0xFF1A1A1A),
                        Color.Black
                    )
                )
            )
    ) {
        if (personDetail == null) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                text = "Loading person details..."
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                PersonHeader(
                    personDetail = personDetail!!,
                    onBackClick = onBackClick
                )
                
                // Biography
                PersonBiography(
                    personDetail = personDetail!!
                )
                
                // Filmography
                personMovieCredits?.let { credits ->
                    PersonFilmography(
                        credits = credits,
                        favoriteMovies = favoriteMovies,
                        personType = personDetail!!.knownForDepartment ?: "Acting",
                        onMovieClick = onMovieClick,
                        onFavoriteClick = { movie ->
                            if (favoriteMovies.any { it.id == movie.id }) {
                                moviesViewModel.removeFromFavorites(movie)
                            } else {
                                moviesViewModel.addToFavorites(movie)
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PersonHeader(
    personDetail: PersonDetail,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Background with person's profile image
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${personDetail.profilePath ?: ""}",
            contentDescription = personDetail.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 100f
                    )
                )
        )
        
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        // Person Info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = personDetail.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Known for department
                personDetail.knownForDepartment?.let { department ->
                    Text(
                        text = department,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
                
                // Birth year
                personDetail.birthday?.let { birthday ->
                    Text(
                        text = "Born ${birthday.take(4)}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
            }
            
            // Place of birth
            personDetail.placeOfBirth?.let { place ->
                Text(
                    text = place,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PersonBiography(
    personDetail: PersonDetail
) {
    personDetail.biography?.let { biography ->
        if (biography.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Biography",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = biography,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun PersonFilmography(
    credits: PersonMovieCredits,
    favoriteMovies: List<Movie>,
    personType: String,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filmography",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Combine cast and crew credits, prioritizing cast for actors
        val allCredits = if (personType == "Acting") {
            credits.cast + credits.crew
        } else {
            credits.crew + credits.cast
        }
        
        // Sort by popularity and filter out movies without posters
        val sortedCredits = allCredits
            .filter { it.posterPath != null }
            .sortedByDescending { it.popularity }
            .take(20)
        
        if (sortedCredits.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(600.dp), // Fixed height
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedCredits) { credit ->
                    MovieCreditCard(
                        credit = credit,
                        personType = personType,
                        isFavorite = favoriteMovies.any { it.id == credit.id },
                        onMovieClick = {
                            // Convert MovieCredit to Movie for navigation
                            val movie = convertCreditToMovie(credit)
                            onMovieClick(movie)
                        },
                        onFavoriteClick = {
                            val movie = convertCreditToMovie(credit)
                            onFavoriteClick(movie)
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No filmography available",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MovieCreditCard(
    credit: MovieCredit,
    personType: String,
    isFavorite: Boolean,
    onMovieClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onMovieClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Movie poster
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${credit.posterPath ?: ""}",
                contentDescription = credit.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f
                        )
                    )
            )
            
            // Favorite button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            
            // Movie info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = credit.title ?: credit.originalTitle ?: "Unknown",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Show character or job
                val role = if (personType == "Acting") {
                    credit.character
                } else {
                    credit.job
                }
                
                role?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Release year
                credit.releaseDate?.let { date ->
                    Text(
                        text = date.take(4),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

// Helper function to convert MovieCredit to Movie
fun convertCreditToMovie(credit: MovieCredit): Movie {
    return Movie(
        id = credit.id,
        title = credit.title ?: credit.originalTitle ?: "Unknown",
        overview = credit.overview ?: "",
        posterPath = credit.posterPath,
        backdropPath = credit.backdropPath,
        releaseDate = credit.releaseDate ?: "",
        voteAverage = credit.voteAverage,
        genreIds = credit.genreIds,
        adult = credit.adult,
        originalLanguage = credit.originalLanguage,
        originalTitle = credit.originalTitle ?: credit.title ?: "Unknown",
        popularity = credit.popularity,
        video = credit.video,
        voteCount = credit.voteCount
    )
}
