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
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            when {
                email.isEmpty() -> {
                    _loginState.value = LoginState.Error("Email cannot be empty")
                }
                password.isEmpty() -> {
                    _loginState.value = LoginState.Error("Password cannot be empty")
                }
                password.length < 6 -> {
                    _loginState.value = LoginState.Error("Password must be at least 6 characters")
                }
                else -> {
                    authManager.login(email, password)
                        .onSuccess {
                            _loginState.value = LoginState.Success
                        }
                        .onFailure {
                            _loginState.value = LoginState.Error(it.message ?: "Login failed")
                        }
                }
            }
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}