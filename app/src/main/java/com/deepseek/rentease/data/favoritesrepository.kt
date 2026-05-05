package com.deepseek.rentease.data

import com.deepseek.rentease.models.Property
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun addToFavorites(property: Property) {
        val userId = getUserId() ?: return
        firestore.collection("favorites")
            .document(userId)
            .collection("userFavorites")
            .document(property.id)
            .set(property)
            .await()
    }

    suspend fun removeFromFavorites(propertyId: String) {
        val userId = getUserId() ?: return
        firestore.collection("favorites")
            .document(userId)
            .collection("userFavorites")
            .document(propertyId)
            .delete()
            .await()
    }

    suspend fun getFavorites(): List<Property> {
        val userId = getUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("favorites")
                .document(userId)
                .collection("userFavorites")
                .get()
                .await()
            snapshot.toObjects(Property::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun isFavorite(propertyId: String): Boolean {
        val userId = getUserId() ?: return false
        return try {
            val doc = firestore.collection("favorites")
                .document(userId)
                .collection("userFavorites")
                .document(propertyId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}

