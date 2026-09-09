package android.mz.data.models

import com.google.gson.annotations.SerializedName

// API Models for backend communication
data class APIUser(
    val id: String,
    val username: String,
    val country: String?,
    @SerializedName("profileImage")
    val profileImage: String?
) {
    // Computed properties for compatibility with existing User model
    val email: String? = null // Backend doesn't return email in signin response
    val profilePicture: String? = profileImage
    val createdAt: String? = null
    
    init {
        android.util.Log.d("APIUser", "Created APIUser - profileImage: $profileImage, profilePicture: $profilePicture")
    }
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null
)

data class AuthResponse(
    val ok: Boolean,
    val error: String?,
    val user: APIUser?,
    val token: String?
) {
    // Computed properties for compatibility
    val success: Boolean get() = ok
    val message: String? get() = error
}

data class ProfileUpdateResponse(
    val success: Boolean,
    val message: String?,
    val profilePicture: String?
)

data class ProfileBackendResponse(
    val ok: Boolean,
    val error: String?,
    val user: ProfileUser?
)

data class ProfileUser(
    val id: String,
    val username: String,
    val country: String?,
    @SerializedName("profileImage")
    val profileImage: String?
)

data class DeleteAccountResponse(
    val success: Boolean,
    val message: String?
)

// Enhanced user model for local storage
data class User(
    val id: String?,
    val username: String,
    val password: String?, // Optional for API responses
    val email: String?,
    val profilePicture: String?,
    val createdAt: String?
) {
    companion object {
        fun fromAPIUser(apiUser: APIUser, password: String? = null): User {
            val sanitizedPic = apiUser.profilePicture?.trim()
            return User(
                id = apiUser.id,
                username = apiUser.username,
                password = password,
                email = apiUser.email,
                profilePicture = sanitizedPic,
                createdAt = apiUser.createdAt
            )
        }
    }
}
