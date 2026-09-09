package android.mz.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.mz.data.api.APIService
import android.mz.data.models.User
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthenticationRepository(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: AuthenticationRepository? = null
        
        fun getInstance(context: Context): AuthenticationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthenticationRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("MovieZonePrefs", Context.MODE_PRIVATE)
    private val apiService = APIService.getInstance()
    private val gson = Gson()
    
    private val currentUserKey = "CurrentUser"
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    
    init {
        // Load existing user data if available
        Log.d("AuthRepository", "Initializing authentication repository")
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        try {
            val userJson = sharedPreferences.getString(currentUserKey, null)
            if (userJson != null) {
                val user = gson.fromJson(userJson, User::class.java)
                _currentUser.value = user
                _isAuthenticated.value = true
                Log.d("AuthRepository", "Loaded existing user: ${user.username}")
            } else {
                _isAuthenticated.value = false
                _currentUser.value = null
                Log.d("AuthRepository", "No existing user found")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error loading current user", e)
            _isAuthenticated.value = false
            _currentUser.value = null
        }
    }
    
    suspend fun register(username: String, password: String, email: String? = null): Boolean {
        _isLoading.value = true
        _errorMessage.value = ""
        
        return try {
            val response = apiService.register(username, password, email)
            
            if (response.success && response.user != null) {
                val user = User.fromAPIUser(response.user, password)
                _currentUser.value = user
                _isAuthenticated.value = true
                _isLoading.value = false
                saveCurrentUser(user)
                Log.d("AuthRepository", "Registration successful for user: ${user.username}")
                true
            } else {
                _errorMessage.value = response.message ?: "Registration failed"
                _isLoading.value = false
                Log.e("AuthRepository", "Registration failed: ${response.message}")
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Registration failed"
            _isLoading.value = false
            Log.e("AuthRepository", "Registration error", e)
            false
        }
    }
    
    suspend fun login(username: String, password: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = ""
        
        return try {
            val response = apiService.login(username, password)
            
            if (response.success && response.user != null) {
                var user = User.fromAPIUser(response.user, password)
                Log.d("AuthRepository", "Login successful - User: ${user.username}")
                Log.d("AuthRepository", "API User profileImage: ${response.user.profileImage ?: "null"}")
                Log.d("AuthRepository", "User profilePicture(before): ${user.profilePicture ?: "null"}")

                // Immediately try to fetch the latest profile from backend (has profileImage)
                try {
                    val profileResp = apiService.fetchProfile()
                    val serverPic = profileResp.user?.profileImage
                    Log.d("AuthRepository", "Fetched profile image from server: ${serverPic ?: "null"}")
                    if (!serverPic.isNullOrEmpty()) {
                        user = user.copy(profilePicture = serverPic)
                    }
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Could not refresh profile after login: ${e.message}")
                }

                _currentUser.value = user
                _isAuthenticated.value = true
                _isLoading.value = false
                saveCurrentUser(user)
                
                true
            } else {
                _errorMessage.value = response.message ?: "Login failed"
                _isLoading.value = false
                Log.e("AuthRepository", "Login failed: ${response.message}")
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Login failed"
            _isLoading.value = false
            Log.e("AuthRepository", "Login error", e)
            false
        }
    }
    
    fun logout() {
        _currentUser.value = null
        _isAuthenticated.value = false
        _errorMessage.value = ""
        sharedPreferences.edit().remove(currentUserKey).apply()
        Log.d("AuthRepository", "User logged out")
    }
    
    fun updateProfilePicture(profilePictureURL: String) {
        val user = _currentUser.value ?: return
        
        Log.d("AuthRepository", "Updating profile picture to: $profilePictureURL")
        
        // Create updated user with new profile picture
        val updatedUser = User(
            id = user.id,
            username = user.username,
            password = user.password,
            email = user.email,
            profilePicture = profilePictureURL,
            createdAt = user.createdAt
        )
        
        _currentUser.value = updatedUser
        saveCurrentUser(updatedUser)
        Log.d("AuthRepository", "Profile picture updated successfully")
    }
    
    suspend fun refreshProfile(): Boolean {
        val user = _currentUser.value ?: return false
        return try {
            val profileResp = apiService.fetchProfile()
            val serverPic = profileResp.user?.profileImage
            val serverCountry = profileResp.user?.country
            Log.d("AuthRepository", "Refresh profile: image=${serverPic ?: "null"}, country=${serverCountry ?: "null"}")
            val updated = user.copy(
                profilePicture = serverPic ?: user.profilePicture
            )
            _currentUser.value = updated
            saveCurrentUser(updated)
            true
        } catch (e: Exception) {
            Log.w("AuthRepository", "refreshProfile failed: ${e.message}")
            false
        }
    }
    
    suspend fun deleteAccount(): Boolean {
        val userId = _currentUser.value?.id ?: return false
        
        _isLoading.value = true
        _errorMessage.value = ""
        
        return try {
            val response = apiService.deleteAccount(userId)
            
            if (response.success) {
                logout()
                _isLoading.value = false
                Log.d("AuthRepository", "Account deleted successfully")
                true
            } else {
                _errorMessage.value = response.message ?: "Failed to delete account"
                _isLoading.value = false
                Log.e("AuthRepository", "Account deletion failed: ${response.message}")
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Failed to delete account"
            _isLoading.value = false
            Log.e("AuthRepository", "Account deletion error", e)
            false
        }
    }
    
    private fun saveCurrentUser(user: User) {
        try {
            val userJson = gson.toJson(user)
            sharedPreferences.edit().putString(currentUserKey, userJson).apply()
            Log.d("AuthRepository", "User saved to SharedPreferences")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error saving user", e)
        }
    }
    
    fun clearError() {
        _errorMessage.value = ""
    }
}
