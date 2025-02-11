package com.example.libraryapp.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = withContext(Dispatchers.IO) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }
            saveUserCredentials(email, password)
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = withContext(Dispatchers.IO) {
                auth.signInWithEmailAndPassword(email, password).await()
            }
            saveUserCredentials(email, password)
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveUserCredentials(email: String, password: String) {
        prefs.edit()
            .putString("user_email", email)
            .putString("user_password", password)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    fun logout() {
        auth.signOut()
        prefs.edit().clear().apply()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isLoggedIn(): Boolean {
        return getCurrentUser() != null || prefs.getBoolean("is_logged_in", false)
    }

    suspend fun tryAutoLogin(): Boolean {
        if (isLoggedIn()) return true

        val email = prefs.getString("user_email", null)
        val password = prefs.getString("user_password", null)

        return if (email != null && password != null) {
            login(email, password).isSuccess
        } else false
    }
}