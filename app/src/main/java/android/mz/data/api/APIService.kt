package android.mz.data.api

import android.mz.data.models.*
import android.util.Log
import kotlinx.coroutines.delay
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
// API Interface
interface MovieZoneAPI {
    @POST("users/signin")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("users/signup")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @GET("wishlist")
    suspend fun getWishlist(): Response<WishlistResponse>
    @POST("wishlist")
    suspend fun addToWishlist(@Body request: Map<String, Movie>): Response<WishlistResponse>
    
    @DELETE("wishlist")
    suspend fun removeFromWishlist(@Query("id") movieId: Int): Response<WishlistResponse>
    
    @PUT("users/profile")
    suspend fun uploadProfilePicture(@Body request: Map<String, String>): Response<ProfileBackendResponse>
    @GET("users/profile")
    suspend fun getProfile(): Response<ProfileBackendResponse>
    
    @DELETE("users/profile")
    suspend fun deleteAccount(@Body request: Map<String, String>): Response<DeleteAccountResponse>
}

// Cookie Jar for session management - specifically for JWT token cookie
class SimpleCookieJar : CookieJar {
    private val cookies = mutableMapOf<String, List<Cookie>>()
    
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        Log.d("CookieJar", "=== SAVING COOKIES ===")
        Log.d("CookieJar", "Host: ${url.host}")
        Log.d("CookieJar", "Cookie count: ${cookies.size}")
        cookies.forEach { cookie ->
            Log.d("CookieJar", "Cookie: ${cookie.name} = ${cookie.value.take(20)}...")
            Log.d("CookieJar", "  Domain: ${cookie.domain}")
            Log.d("CookieJar", "  Path: ${cookie.path}")
            Log.d("CookieJar", "  HttpOnly: ${cookie.httpOnly}")
            Log.d("CookieJar", "  Secure: ${cookie.secure}")
        }
        this.cookies[url.host] = cookies
    }
    
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val hostCookies = cookies[url.host] ?: emptyList()
        Log.d("CookieJar", "=== LOADING COOKIES ===")
        Log.d("CookieJar", "Host: ${url.host}")
        Log.d("CookieJar", "Cookie count: ${hostCookies.size}")
        hostCookies.forEach { cookie ->
            Log.d("CookieJar", "Loading cookie: ${cookie.name}")
        }
        return hostCookies
    }
}

// API Service Implementation
class APIService {
    companion object {
        @Volatile
        private var INSTANCE: APIService? = null
        
        fun getInstance(): APIService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: APIService().also { INSTANCE = it }
            }
        }
    }
    
    private val baseURL = "https://moviezoneonline.vercel.app/api/"
    private val useMockAuth = false // Use real backend authentication
    private val useMockWishlist = false // Use real backend wishlist
    
    private val cookieJar = SimpleCookieJar()
    private val gson = com.google.gson.Gson()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "MovieZone-Android/1.0")
            
            // Log all outgoing headers
            Log.d("APIService", "=== REQUEST HEADERS ===")
            Log.d("APIService", "URL: ${original.url}")
            Log.d("APIService", "Method: ${original.method}")
            original.headers.forEach { (name, value) ->
                Log.d("APIService", "Header: $name = $value")
            }
            
            val request = requestBuilder.build()
            Log.d("APIService", "=== FINAL REQUEST HEADERS ===")
            request.headers.forEach { (name, value) ->
                Log.d("APIService", "Final Header: $name = $value")
            }
            
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseURL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(MovieZoneAPI::class.java)
    
    // MARK: - Authentication APIs
    
    suspend fun login(username: String, password: String): AuthResponse {
        Log.d("APIService", "Login called - useMockAuth: $useMockAuth")
        
        if (useMockAuth) {
            Log.d("APIService", "Using mock authentication")
            return mockLogin(username, password)
        }
        
        return try {
            val request = LoginRequest(username, password)
            Log.d("APIService", "=== LOGIN ATTEMPT ===")
            Log.d("APIService", "URL: ${baseURL}users/signin")
            Log.d("APIService", "Username: $username")
            Log.d("APIService", "Request: $request")
            
            val response = api.login(request)
            
            Log.d("APIService", "=== RESPONSE RECEIVED ===")
            Log.d("APIService", "Status Code: ${response.code()}")
            Log.d("APIService", "Is Successful: ${response.isSuccessful}")
            Log.d("APIService", "Headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("APIService", "=== LOGIN SUCCESS ===")
                Log.d("APIService", "Response Body: $responseBody")
                Log.d("APIService", "User ID: ${responseBody?.user?.id}")
                Log.d("APIService", "Username: ${responseBody?.user?.username}")
                Log.d("APIService", "Profile Image: ${responseBody?.user?.profileImage}")
                Log.d("APIService", "Country: ${responseBody?.user?.country}")
                responseBody ?: throw APIException("Empty response body")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("APIService", "=== LOGIN FAILED ===")
                Log.e("APIService", "Status: ${response.code()}")
                Log.e("APIService", "Error Body: $errorBody")
                Log.e("APIService", "Response Headers: ${response.headers()}")
                Log.e("APIService", "Request URL: ${response.raw().request.url}")
                Log.e("APIService", "Request Method: ${response.raw().request.method}")
                Log.e("APIService", "Request Headers: ${response.raw().request.headers}")
                
                // Parse error response if it's JSON
                try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, AuthResponse::class.java)
                    Log.e("APIService", "Parsed Error: ${errorResponse.error}")
                } catch (e: Exception) {
                    Log.e("APIService", "Could not parse error response: ${e.message}")
                }
                
                // Check if response is HTML (indicating wrong endpoint)
                if (errorBody?.contains("<!doctype html", ignoreCase = true) == true) {
                    throw APIException("Backend API endpoints not available. Server returned HTML instead of JSON.")
                }
                
                // Try to parse error response
                val errorResponse = try {
                    if (errorBody?.isNotEmpty() == true) {
                        gson.fromJson(errorBody, AuthResponse::class.java)
                    } else null
                } catch (e: Exception) {
                    null
                }
                
                if (errorResponse != null) {
                    Log.d("APIService", "Parsed error response: $errorResponse")
                    errorResponse
                } else {
                    AuthResponse(
                        ok = false,
                        error = "Login failed with status ${response.code()}",
                        user = null,
                        token = null
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("APIService", "=== LOGIN EXCEPTION ===")
            Log.e("APIService", "Exception: ${e.javaClass.simpleName}")
            Log.e("APIService", "Message: ${e.message}")
            Log.e("APIService", "Stack trace:", e)
            
            // Return error response
            AuthResponse(
                ok = false,
                error = e.message ?: "Network error occurred",
                user = null,
                token = null
            )
        }
    }
    
    suspend fun register(username: String, password: String, email: String? = null): AuthResponse {
        if (useMockAuth) {
            return mockRegister(username, password, email)
        }
        
        return try {
            val request = RegisterRequest(username, password, email)
            val response = api.register(request)
            
            if (response.isSuccessful) {
                response.body() ?: throw APIException("Empty response body")
            } else {
                throw APIException("Registration failed with status ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("APIService", "Register error", e)
            throw APIException(e.message ?: "Registration failed")
        }
    }
    
    // MARK: - Wishlist APIs
    
    suspend fun addToWishlist(movie: Movie, userId: String): WishlistResponse {
        return try {
            val requestBody = mapOf("movie" to movie)
            Log.d("APIService", "Adding movie to wishlist: ${movie.title}")
            
            val response = api.addToWishlist(requestBody)
            
            Log.d("APIService", "Add wishlist response status: ${response.code()}")
            
            if (response.isSuccessful) {
                response.body() ?: throw APIException("Empty response body")
            } else {
                throw APIException("Failed to add to wishlist")
            }
        } catch (e: Exception) {
            Log.e("APIService", "Add to wishlist error", e)
            throw APIException(e.message ?: "Failed to add to wishlist")
        }
    }
    
    suspend fun removeFromWishlist(movieId: Int, userId: String): WishlistResponse {
        return try {
            Log.d("APIService", "Removing movie from wishlist: ID $movieId")
            
            val response = api.removeFromWishlist(movieId)
            
            Log.d("APIService", "Remove wishlist response status: ${response.code()}")
            
            if (response.isSuccessful) {
                response.body() ?: throw APIException("Empty response body")
            } else if (response.code() == 404) {
                throw APIException("Movie not found in wishlist")
            } else {
                throw APIException("Failed to remove from wishlist")
            }
        } catch (e: Exception) {
            Log.e("APIService", "Remove from wishlist error", e)
            throw APIException(e.message ?: "Failed to remove from wishlist")
        }
    }
    
    suspend fun getWishlist(userId: String): WishlistResponse {
        if (useMockWishlist) {
            return mockGetWishlist()
        }
        
        return try {
            Log.d("APIService", "=== FETCHING WISHLIST ===")
            Log.d("APIService", "URL: ${baseURL}wishlist")
            Log.d("APIService", "User ID: $userId")
            
            val response = api.getWishlist()
            
            Log.d("APIService", "=== WISHLIST RESPONSE ===")
            Log.d("APIService", "Status Code: ${response.code()}")
            Log.d("APIService", "Headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("APIService", "Response Body: $responseBody")
                responseBody ?: throw APIException("Empty response body")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("APIService", "=== WISHLIST ERROR ===")
                Log.e("APIService", "Status: ${response.code()}")
                Log.e("APIService", "Error Body: $errorBody")
                
                // Check if it's an authentication error
                if (response.code() == 401) {
                    throw APIException("Authentication required. Please login again.")
                } else if (response.code() == 403) {
                    throw APIException("Access forbidden. Check your permissions.")
                } else {
                    throw APIException("Failed to get wishlist (Status: ${response.code()})")
                }
            }
        } catch (e: Exception) {
            Log.e("APIService", "=== WISHLIST EXCEPTION ===")
            Log.e("APIService", "Exception: ${e.javaClass.simpleName}")
            Log.e("APIService", "Message: ${e.message}")
            Log.e("APIService", "Stack trace:", e)
            throw APIException(e.message ?: "Failed to get wishlist")
        }
    }
    
    // MARK: - Profile APIs
    suspend fun fetchProfile(): ProfileBackendResponse {
        return try {
            Log.d("APIService", "Fetching profile")
            val response = api.getProfile()
            Log.d("APIService", "Profile fetch status: ${response.code()}")
            if (response.isSuccessful) {
                response.body() ?: throw APIException("Empty response body")
            } else {
                throw APIException("Failed to fetch profile (Status: ${response.code()})")
            }
        } catch (e: Exception) {
            Log.e("APIService", "Fetch profile error", e)
            throw APIException(e.message ?: "Failed to fetch profile")
        }
    }
    
    suspend fun uploadProfilePicture(imageBase64: String, userId: String): ProfileUpdateResponse {
        return try {
            val dataURL = "data:image/jpeg;base64,$imageBase64"
            val requestBody = mapOf("profileImage" to dataURL)
            
            Log.d("APIService", "Uploading profile picture")
            
            val response = api.uploadProfilePicture(requestBody)
            
            Log.d("APIService", "Profile upload response status: ${response.code()}")
            
            if (response.isSuccessful) {
                val backendResponse = response.body() ?: throw APIException("Empty response body")
                
                // Convert to expected format
                ProfileUpdateResponse(
                    success = backendResponse.ok,
                    message = backendResponse.error,
                    profilePicture = backendResponse.user?.profileImage
                )
            } else {
                throw APIException("Failed to upload profile picture")
            }
        } catch (e: Exception) {
            Log.e("APIService", "Upload profile picture error", e)
            throw APIException(e.message ?: "Failed to upload profile picture")
        }
    }
    
    suspend fun deleteAccount(userId: String): DeleteAccountResponse {
        return try {
            val requestBody = mapOf("userId" to userId)
            val response = api.deleteAccount(requestBody)
            
            if (response.isSuccessful) {
                response.body() ?: throw APIException("Empty response body")
            } else {
                throw APIException("Failed to delete account")
            }
        } catch (e: Exception) {
            Log.e("APIService", "Delete account error", e)
            throw APIException(e.message ?: "Failed to delete account")
        }
    }
    
    // MARK: - Mock Functions (for testing without backend)
    
    private suspend fun mockLogin(username: String, password: String): AuthResponse {
        // Simulate network delay
        delay(1000)
        
        // Accept any non-empty username/password for demo
        if (username.isNotEmpty() && password.isNotEmpty()) {
            // Create profile picture that matches web app's style
            val profilePictureURL = "https://ui-avatars.com/api/?name=$username&size=150&background=6366f1&color=ffffff&bold=true&format=png&rounded=true"
            val mockUser = APIUser(
                id = "mock_user_123",
                username = username,
                country = null,
                profileImage = profilePictureURL
            )
            
            Log.d("APIService", "Mock login - Profile picture URL: $profilePictureURL")
            
            return AuthResponse(
                ok = true,
                error = null,
                user = mockUser,
                token = "mock_token_123"
            )
        } else {
            throw APIException("Invalid username or password")
        }
    }
    
    private suspend fun mockGetWishlist(): WishlistResponse {
        // Simulate network delay
        delay(1000)
        
        // Create some mock favorite movies
        val mockMovies = listOf(
            Movie(
                id = 550,
                title = "Fight Club",
                overview = "A ticking-time-bomb insomniac and a slippery soap salesman channel primal male aggression into a shocking new form of therapy.",
                posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
                backdropPath = "/52AfXWuXCHn3UjD17rBruA9f5qb.jpg",
                releaseDate = "1999-10-15",
                voteAverage = 8.4,
                voteCount = 26280,
                genreIds = listOf(18, 53, 35),
                adult = false,
                originalLanguage = "en",
                originalTitle = "Fight Club",
                popularity = 61.416,
                video = false
            ),
            Movie(
                id = 13,
                title = "Forrest Gump",
                overview = "A man with a low IQ has accomplished great things in his life and been present during significant historic events—in each case, far exceeding what anyone imagined he could do.",
                posterPath = "/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg",
                backdropPath = "/3h1JZGDhZ8nzxdgvkxha0qBqi05.jpg",
                releaseDate = "1994-06-23",
                voteAverage = 8.5,
                voteCount = 24947,
                genreIds = listOf(35, 18, 10749),
                adult = false,
                originalLanguage = "en",
                originalTitle = "Forrest Gump",
                popularity = 48.307,
                video = false
            ),
            Movie(
                id = 680,
                title = "Pulp Fiction",
                overview = "A burger-loving hit man, his philosophical partner, a drug-addled gangster's moll and a washed-up boxer converge in this sprawling, comedic crime caper.",
                posterPath = "/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
                backdropPath = "/4cDFJr4HnXN5AdPw4AKrmLlMWdO.jpg",
                releaseDate = "1994-09-10",
                voteAverage = 8.5,
                voteCount = 24947,
                genreIds = listOf(80, 18),
                adult = false,
                originalLanguage = "en",
                originalTitle = "Pulp Fiction",
                popularity = 65.25,
                video = false
            )
        )
        
        Log.d("APIService", "Mock wishlist - returning ${mockMovies.size} movies")
        
        return WishlistResponse(
            ok = true,
            error = null,
            items = mockMovies
        )
    }
    
    private suspend fun mockRegister(username: String, password: String, email: String?): AuthResponse {
        // Simulate network delay
        delay(1000)
        
        // Accept any non-empty username/password for demo
        if (username.isNotEmpty() && password.isNotEmpty()) {
            val mockUser = APIUser(
                id = "mock_user_${(100..999).random()}",
                username = username,
                country = null,
                profileImage = null
            )
            
            return AuthResponse(
                ok = true,
                error = null,
                user = mockUser,
                token = "mock_token_${(100..999).random()}"
            )
        } else {
            throw APIException("Invalid registration data")
        }
    }
}

// Custom exception for API errors
class APIException(message: String) : Exception(message)
