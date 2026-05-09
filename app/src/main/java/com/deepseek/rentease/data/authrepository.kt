package com.deepseek.rentease.data

import com.deepseek.rentease.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String, phone: String, role: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!

            val userData = User(
                uid = user.uid,
                email = email,
                name = name,
                phone = phone,
                role = role
            )

            firestore.collection("users").document(user.uid).set(userData).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(): String? {
        val uid = currentUser?.uid ?: run {
            android.util.Log.e("AuthRepository", "No current user UID found")
            return null
        }
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val role = doc.getString("role")
            android.util.Log.d("AuthRepository", "Fetched role for $uid: $role")
            role
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error fetching user role", e)
            null
        }
    }

    suspend fun getUserData(): User? {
        val uid = currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}

