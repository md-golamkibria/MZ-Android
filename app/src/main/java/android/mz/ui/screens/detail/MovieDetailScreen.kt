package android.mz.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import android.mz.ui.components.CompactMovieCard
import android.mz.ui.components.LoadingIndicator

@Composable
fun MovieDetailScreen(
    movie: Movie,
    moviesViewModel: MoviesViewModel,
    onBackClick: () -> Unit,
    onPersonClick: (Int) -> Unit,
    onRelatedMovieClick: (Movie) -> Unit,
    currentUserId: String? = null
) {
    val movieDetail by moviesViewModel.movieDetail.collectAsState()
    val movieCredits by moviesViewModel.movieCredits.collectAsState()
    val similarMovies by moviesViewModel.similarMovies.collectAsState()
    val recommendedMovies by moviesViewModel.recommendedMovies.collectAsState()
    val favoriteMovies by moviesViewModel.favoriteMovies.collectAsState()
    val isLoadingDetails by moviesViewModel.isLoadingDetails.collectAsState()
    
    val isFavorite = favoriteMovies.any { it.id == movie.id }
    
    // Load movie details when screen appears
    LaunchedEffect(movie.id) {
        moviesViewModel.fetchMovieDetails(movie.id)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoadingDetails) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                text = "Loading movie details..."
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Backdrop and Header
                MovieHeader(
                    movie = movie,
                    movieDetail = movieDetail,
                    isFavorite = isFavorite,
                    onBackClick = onBackClick,
                    onFavoriteClick = {
                        if (isFavorite) {
                            moviesViewModel.removeFromFavorites(movie, currentUserId)
                        } else {
                            moviesViewModel.addToFavorites(movie, currentUserId)
                        }
                    }
                )
                
                // Movie Info
                MovieInfo(
                    movie = movie,
                    movieDetail = movieDetail
                )
                
                // Cast Section
                movieCredits?.let { credits ->
                    if (credits.cast.isNotEmpty()) {
                        CastSection(
                            cast = credits.cast,
                            onCastClick = onPersonClick
                        )
                    }
                }
                
                // Crew Section
                movieCredits?.let { credits ->
                    if (credits.crew.isNotEmpty()) {
                        CrewSection(
                            crew = credits.crew,
                            onCrewClick = onPersonClick
                        )
                    }
                }
                
                // Similar Movies Section
                if (similarMovies.isNotEmpty()) {
                    RelatedMoviesSection(
                        title = "Similar Movies",
                        movies = similarMovies,
                        favoriteMovies = favoriteMovies,
                        onMovieClick = onRelatedMovieClick,
                        onFavoriteClick = { relatedMovie ->
                            if (favoriteMovies.any { it.id == relatedMovie.id }) {
                                moviesViewModel.removeFromFavorites(relatedMovie, currentUserId)
                            } else {
                                moviesViewModel.addToFavorites(relatedMovie, currentUserId)
                            }
                        }
                    )
                }
                
                // Recommended Movies Section
                if (recommendedMovies.isNotEmpty()) {
                    RelatedMoviesSection(
                        title = "Recommended Movies",
                        movies = recommendedMovies,
                        favoriteMovies = favoriteMovies,
                        onMovieClick = onRelatedMovieClick,
                        onFavoriteClick = { relatedMovie ->
                            if (favoriteMovies.any { it.id == relatedMovie.id }) {
                                moviesViewModel.removeFromFavorites(relatedMovie)
                            } else {
                                moviesViewModel.addToFavorites(relatedMovie)
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
fun MovieHeader(
    movie: Movie,
    movieDetail: MovieDetail?,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        // Backdrop Image
        AsyncImage(
            model = movie.backdropURL,
            contentDescription = movie.title,
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
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 200f
                    )
                )
        )
        
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
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
            
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        }
        
        // Movie Title and Rating
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = movie.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = String.format("%.1f", movie.voteAverage),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Release Year
                Text(
                    text = movie.releaseDate.take(4),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                
                // Runtime (if available)
                movieDetail?.runtime?.let { runtime ->
                    Text(
                        text = "${runtime}min",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MovieInfo(
    movie: Movie,
    movieDetail: MovieDetail?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Genres
        movieDetail?.genres?.let { genres ->
            if (genres.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(genres) { genre ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = genre.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Tagline
        movieDetail?.tagline?.let { tagline ->
            if (tagline.isNotEmpty()) {
                Text(
                    text = "\"$tagline\"",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        
        // Overview
        Text(
            text = "Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = movie.overview,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun CastSection(
    cast: List<Cast>,
    onCastClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Cast",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(cast.take(10)) { castMember ->
                CastMemberCard(
                    cast = castMember,
                    onClick = { onCastClick(castMember.id) }
                )
            }
        }
    }
}

@Composable
fun CastMemberCard(
    cast: Cast,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = cast.profileURL,
                contentDescription = cast.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = cast.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = cast.character,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CrewSection(
    crew: List<Crew>,
    onCrewClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Crew",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Show key crew members (Director, Producer, etc.)
        val keyCrew = crew.filter { 
            it.job in listOf("Director", "Producer", "Executive Producer", "Screenplay", "Writer")
        }.take(6)
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keyCrew.forEach { crewMember ->
                CrewMemberRow(
                    crew = crewMember,
                    onClick = { onCrewClick(crewMember.id) }
                )
            }
        }
    }
}

@Composable
fun CrewMemberRow(
    crew: Crew,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = crew.job,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.width(120.dp)
        )
        
        Text(
            text = crew.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RelatedMoviesSection(
    title: String,
    movies: List<Movie>,
    favoriteMovies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(movies.take(10)) { movie ->
                CompactMovieCard(
                    movie = movie,
                    isFavorite = favoriteMovies.any { it.id == movie.id },
                    onMovieClick = onMovieClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
    }
}
