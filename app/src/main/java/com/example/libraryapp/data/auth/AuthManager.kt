package com.example.libraryapp.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = withContext(Dispatchers.IO) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }
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
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    suspend fun tryAutoLogin(): Boolean {
        if (auth.currentUser != null) return true
        return suspendCancellableCoroutine { cont ->
            lateinit var listener: FirebaseAuth.AuthStateListener
            listener = FirebaseAuth.AuthStateListener { fa ->
                auth.removeAuthStateListener(listener)
                if (cont.isActive) {
                    cont.resume(fa.currentUser != null)
                }
            }
            auth.addAuthStateListener(listener)
            cont.invokeOnCancellation {
                auth.removeAuthStateListener(listener)
            }
        }
    }
}
