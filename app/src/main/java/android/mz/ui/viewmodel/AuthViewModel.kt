package android.mz.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.mz.data.repository.AuthenticationRepository
import android.mz.data.models.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthenticationRepository.getInstance(application)
    
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isLoading: StateFlow<Boolean> = authRepository.isLoading
    val errorMessage: StateFlow<String> = authRepository.errorMessage
    
    fun login(username: String, password: String) {
        android.util.Log.d("AuthViewModel", "Login called with username: $username")
        viewModelScope.launch {
            try {
                val result = authRepository.login(username, password)
                android.util.Log.d("AuthViewModel", "Login result: $result")
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error", e)
            }
        }
    }
    
    fun register(username: String, password: String, email: String? = null) {
        viewModelScope.launch {
            authRepository.register(username, password, email)
        }
    }
    
    fun logout() {
        authRepository.logout()
    }
    
    fun updateProfilePicture(profilePictureURL: String) {
        authRepository.updateProfilePicture(profilePictureURL)
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount()
        }
    }
    
    fun clearError() {
        authRepository.clearError()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            authRepository.refreshProfile()
        }
    }
}
