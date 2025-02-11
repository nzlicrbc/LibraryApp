package com.example.libraryapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryapp.data.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            when {
                email.isEmpty() -> {
                    _registerState.value = RegisterState.Error("Email cannot be empty")
                }
                password.isEmpty() -> {
                    _registerState.value = RegisterState.Error("Password cannot be empty")
                }
                else -> {
                    authManager.register(email, password)
                        .onSuccess {
                            _registerState.value = RegisterState.Success
                        }
                        .onFailure {
                            _registerState.value = RegisterState.Error(it.message ?: "Registration failed")
                        }
                }
            }
        }
    }
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}