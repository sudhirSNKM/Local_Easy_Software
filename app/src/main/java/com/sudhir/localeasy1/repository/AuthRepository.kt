package com.sudhir.localeasy1.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.UserRole
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(email: String, password: String, name: String, role: UserRole): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: ""

            // Save user profile
            val userMap = mapOf(
                "uid" to userId,
                "name" to name,
                "email" to email,
                "role" to role.name.lowercase()
            )
            db.collection("users").document(userId).set(userMap).await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(userId: String): UserRole? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val roleStr = doc.getString("role")?.lowercase()
                when (roleStr) {
                    "admin" -> UserRole.ADMIN
                    "super_admin" -> UserRole.SUPER_ADMIN
                    else -> UserRole.USER
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}
