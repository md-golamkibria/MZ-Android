package android.mz.data.api

import android.mz.data.models.*
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// TMDB API Interface
interface TMDBAPI {
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("api_key") apiKey: String): Response<MovieResponse>
    
    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Response<MovieDetail>
    
    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Response<CreditsResponse>
    
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<MovieResponse>
    
    @GET("genre/movie/list")
    suspend fun getGenres(@Query("api_key") apiKey: String): Response<GenreResponse>
    
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") genreId: Int? = null,
        @Query("vote_average.gte") minRating: Double? = null,
        @Query("with_people") withPeople: Int? = null
    ): Response<MovieResponse>
    
    @GET("search/person")
    suspend fun searchPerson(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<PersonSearchResponse>
    
    @GET("movie/{id}/similar")
    suspend fun getSimilarMovies(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Response<MovieResponse>
    
    @GET("movie/{id}/recommendations")
    suspend fun getRecommendedMovies(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Response<MovieResponse>
    
    @GET("person/{id}")
    suspend fun getPersonDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Response<PersonDetail>
    
    @GET("person/{id}/movie_credits")
    suspend fun getPersonMovieCredits(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Response<PersonMovieCredits>
}

// TMDB Service Implementation
class TMDBService {
    companion object {
        @Volatile
        private var INSTANCE: TMDBService? = null
        
        fun getInstance(): TMDBService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TMDBService().also { INSTANCE = it }
            }
        }
    }
    
    private val apiKey = "397abbabeb0c47f060cf654d072c0c15"
    private val baseURL = "https://api.themoviedb.org/3/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseURL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(TMDBAPI::class.java)
    
    suspend fun fetchPopularMovies(): List<Movie> {
        return try {
            val response = api.getPopularMovies(apiKey)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("TMDBService", "Failed to fetch popular movies: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching popular movies", e)
            emptyList()
        }
    }
    
    suspend fun fetchMovieDetails(id: Int): MovieDetail? {
        return try {
            val response = api.getMovieDetails(id, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("TMDBService", "Failed to fetch movie details: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching movie details", e)
            null
        }
    }
    
    suspend fun fetchMovieCredits(id: Int): CreditsResponse? {
        return try {
            val response = api.getMovieCredits(id, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("TMDBService", "Failed to fetch movie credits: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching movie credits", e)
            null
        }
    }
    
    suspend fun searchMovies(query: String): List<Movie> {
        return try {
            if (query.isEmpty()) return emptyList()
            
            val response = api.searchMovies(apiKey, query)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("TMDBService", "Failed to search movies: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error searching movies", e)
            emptyList()
        }
    }
    
    suspend fun fetchGenres(): List<Genre> {
        return try {
            val response = api.getGenres(apiKey)
            if (response.isSuccessful) {
                response.body()?.genres ?: emptyList()
            } else {
                Log.e("TMDBService", "Failed to fetch genres: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching genres", e)
            emptyList()
        }
    }
    
    suspend fun discoverMovies(
        genreId: Int? = null,
        minRating: Double? = null,
        withPerson: String? = null
    ): List<Movie> {
        return try {
            // If person name is provided, search for person first and get their ID
            val personId = if (!withPerson.isNullOrEmpty()) {
                findPersonId(withPerson)
            } else null
            
            val response = api.discoverMovies(
                apiKey = apiKey,
                genreId = genreId,
                minRating = minRating,
                withPeople = personId
            )
            
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("TMDBService", "Failed to discover movies: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error discovering movies", e)
            emptyList()
        }
    }
    
    suspend fun findPersonId(name: String): Int? {
        return try {
            if (name.isEmpty()) return null
            
            val response = api.searchPerson(apiKey, name)
            if (response.isSuccessful) {
                response.body()?.results?.firstOrNull()?.id
            } else {
                Log.e("TMDBService", "Failed to find person: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error finding person", e)
            null
        }
    }
    
    suspend fun searchPerson(name: String): List<Movie> {
        return try {
            if (name.isEmpty()) return emptyList()
            
            // First find the person ID
            val personId = findPersonId(name) ?: return emptyList()
            
            // Then discover movies with that person
            discoverMovies(withPerson = name)
        } catch (e: Exception) {
            Log.e("TMDBService", "Error searching person", e)
            emptyList()
        }
    }
    
    // MARK: - Related Movies
    suspend fun fetchSimilarMovies(id: Int): List<Movie> {
        return try {
            val response = api.getSimilarMovies(id, apiKey)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("TMDBService", "Failed to fetch similar movies: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching similar movies", e)
            emptyList()
        }
    }
    
    suspend fun fetchRecommendedMovies(id: Int): List<Movie> {
        return try {
            val response = api.getRecommendedMovies(id, apiKey)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("TMDBService", "Failed to fetch recommended movies: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching recommended movies", e)
            emptyList()
        }
    }
    
    // MARK: - Person Details
    suspend fun fetchPersonDetails(id: Int): PersonDetail? {
        return try {
            val response = api.getPersonDetails(id, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("TMDBService", "Failed to fetch person details: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching person details", e)
            null
        }
    }
    
    suspend fun fetchPersonMovieCredits(id: Int): PersonMovieCredits? {
        return try {
            val response = api.getPersonMovieCredits(id, apiKey)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("TMDBService", "Failed to fetch person movie credits: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TMDBService", "Error fetching person movie credits", e)
            null
        }
    }
}
