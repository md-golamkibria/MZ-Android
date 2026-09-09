package android.mz.data.models

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    val results: List<Movie>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("genre_ids")
    val genreIds: List<Int>,
    val adult: Boolean,
    @SerializedName("original_language")
    val originalLanguage: String,
    @SerializedName("original_title")
    val originalTitle: String,
    val popularity: Double,
    val video: Boolean,
    @SerializedName("vote_count")
    val voteCount: Int
) {
    val posterURL: String
        get() = "https://image.tmdb.org/t/p/w500${posterPath ?: ""}"
    
    val backdropURL: String
        get() = "https://image.tmdb.org/t/p/w1280${backdropPath ?: ""}"
}

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("vote_average")
    val voteAverage: Double,
    val genres: List<Genre>,
    val runtime: Int?,
    val tagline: String?,
    val status: String,
    val budget: Int,
    val revenue: Int,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionCompany>
) {
    val posterURL: String
        get() = "https://image.tmdb.org/t/p/w500${posterPath ?: ""}"
    
    val backdropURL: String
        get() = "https://image.tmdb.org/t/p/w1280${backdropPath ?: ""}"
}

data class Genre(
    val id: Int,
    val name: String
)

data class GenreResponse(
    val genres: List<Genre>
)

data class ProductionCompany(
    val id: Int,
    val name: String,
    @SerializedName("logo_path")
    val logoPath: String?,
    @SerializedName("origin_country")
    val originCountry: String
)

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path")
    val profilePath: String?,
    val order: Int
) {
    val profileURL: String
        get() = "https://image.tmdb.org/t/p/w200${profilePath ?: ""}"
}

data class Crew(
    val id: Int,
    val name: String,
    val job: String,
    val department: String,
    @SerializedName("profile_path")
    val profilePath: String?
)

// Wishlist API Response (matching iOS app structure)
data class WishlistResponse(
    val ok: Boolean,
    val error: String?,
    val items: List<Movie>?
) {
    // Computed properties for compatibility
    val success: Boolean get() = ok
    val message: String? get() = error
    val wishlist: List<Movie>? get() = items
}

data class CreditsResponse(
    val cast: List<Cast>,
    val crew: List<Crew>
)

// Person search and details
data class PersonSearchResponse(
    val results: List<Person>
)

data class Person(
    val id: Int,
    val name: String
)

data class PersonDetail(
    val id: Int,
    val name: String,
    val biography: String?,
    val birthday: String?,
    val deathday: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    @SerializedName("profile_path")
    val profilePath: String?,
    @SerializedName("known_for_department")
    val knownForDepartment: String?
)

data class PersonMovieCredits(
    val cast: List<MovieCredit>,
    val crew: List<MovieCredit>
)

data class MovieCredit(
    val id: Int,
    val title: String?,
    @SerializedName("original_title")
    val originalTitle: String?,
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val popularity: Double,
    @SerializedName("genre_ids")
    val genreIds: List<Int>,
    val adult: Boolean,
    @SerializedName("original_language")
    val originalLanguage: String,
    val video: Boolean,
    val character: String?,
    val job: String?,
    val department: String?
)
