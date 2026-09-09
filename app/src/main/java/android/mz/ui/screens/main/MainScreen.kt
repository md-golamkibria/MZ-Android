package android.mz.ui.screens.main

import android.util.Base64
import android.graphics.BitmapFactory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.mz.ui.viewmodel.AuthViewModel
import android.mz.ui.viewmodel.MoviesViewModel
import android.mz.ui.screens.detail.MovieDetailScreen
import android.mz.ui.screens.detail.PersonDetailScreen

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    moviesViewModel: MoviesViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    
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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                MainNavHost(
                    navController = navController,
                    authViewModel = authViewModel,
                    moviesViewModel = moviesViewModel,
                    onLogout = onLogout
                )
            }
            
            // Bottom navigation
            ModernBottomNavigation(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    moviesViewModel: MoviesViewModel,
    onLogout: () -> Unit
) {
    // Store selected movie and person in ViewModel to avoid JSON navigation issues
    var selectedMovieId by remember { mutableStateOf<Int?>(null) }
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                moviesViewModel = moviesViewModel,
                onMovieClick = { movie ->
                    selectedMovieId = movie.id
                    navController.navigate("movie_detail")
                }
            )
        }
        
        composable("explore") {
            ExploreScreen(
                authViewModel = authViewModel,
                moviesViewModel = moviesViewModel,
                onMovieClick = { movie ->
                    selectedMovieId = movie.id
                    navController.navigate("movie_detail")
                }
            )
        }
        
        composable("search") {
            SearchScreen(
                authViewModel = authViewModel,
                moviesViewModel = moviesViewModel,
                onMovieClick = { movie ->
                    selectedMovieId = movie.id
                    navController.navigate("movie_detail")
                }
            )
        }
        
        composable("favorites") {
            FavoritesScreen(
                authViewModel = authViewModel,
                moviesViewModel = moviesViewModel,
                onMovieClick = { movie ->
                    selectedMovieId = movie.id
                    navController.navigate("movie_detail")
                }
            )
        }
        
        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogout = onLogout
            )
        }
        
        composable("movie_detail") {
            selectedMovieId?.let { movieId ->
                // Find the movie from available movies
                val popularMovies by moviesViewModel.popularMovies.collectAsState()
                val searchResults by moviesViewModel.searchResults.collectAsState()
                val discoverMovies by moviesViewModel.discoverMovies.collectAsState()
                val favoriteMovies by moviesViewModel.favoriteMovies.collectAsState()
                
                val allMovies = popularMovies + searchResults + discoverMovies + favoriteMovies
                val movie = allMovies.find { it.id == movieId }
                
                movie?.let { selectedMovie ->
                    MovieDetailScreen(
                        movie = selectedMovie,
                        moviesViewModel = moviesViewModel,
                        onBackClick = { navController.popBackStack() },
                        onPersonClick = { personId ->
                            selectedPersonId = personId
                            navController.navigate("person_detail")
                        },
                        onRelatedMovieClick = { relatedMovie ->
                            selectedMovieId = relatedMovie.id
                            navController.navigate("movie_detail")
                        },
                        currentUserId = currentUser?.id
                    )
                }
            }
        }
        
        composable("person_detail") {
            selectedPersonId?.let { personId ->
                PersonDetailScreen(
                    personId = personId,
                    moviesViewModel = moviesViewModel,
                    onBackClick = { navController.popBackStack() },
                    onMovieClick = { movie ->
                        selectedMovieId = movie.id
                        navController.navigate("movie_detail")
                    }
                )
            }
        }
    }
}

@Composable
fun ModernBottomNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("explore", "Explore", Icons.Default.Explore),
        BottomNavItem("search", "Search", Icons.Default.Search),
        BottomNavItem("favorites", "Wishlist", Icons.Default.Favorite),
        BottomNavItem("profile", "Profile", Icons.Default.Person)
    )
    
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    ) {
                        val pic = currentUser?.profilePicture
                        if (item.route == "profile" && !pic.isNullOrEmpty()) {
                            if (pic.startsWith("data:image")) {
                                val comma = pic.indexOf(",")
                                val base64Part = if (comma != -1) pic.substring(comma + 1) else pic
                                val bytes = try { Base64.decode(base64Part, Base64.DEFAULT) } catch (_: Exception) { null }
                                val bmp = try { bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } } catch (_: Exception) { null }
                                if (bmp != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = item.label,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                coil.compose.AsyncImage(
                                    model = pic,
                                    contentDescription = item.label,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
