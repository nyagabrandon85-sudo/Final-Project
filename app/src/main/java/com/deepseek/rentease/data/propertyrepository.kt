package com.deepseek.rentease.data


import com.deepseek.rentease.models.Property
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PropertyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getProperties(): List<Property> {
        return try {
            val snapshot = firestore.collection("properties")
                .whereEqualTo("isAvailable", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Property::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchProperties(query: String, minPrice: Double?, maxPrice: Double?, type: String?): List<Property> {
        return try {
            var firestoreQuery = firestore.collection("properties")
                .whereEqualTo("isAvailable", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)

            val results = firestoreQuery.get().await().toObjects(Property::class.java)

            results.filter { property ->
                val matchesQuery = query.isBlank() ||
                        property.title.contains(query, ignoreCase = true) ||
                        property.location.contains(query, ignoreCase = true)
                val matchesMinPrice = minPrice == null || property.price >= minPrice
                val matchesMaxPrice = maxPrice == null || property.price <= maxPrice
                val matchesType = type.isNullOrBlank() || property.propertyType == type

                matchesQuery && matchesMinPrice && matchesMaxPrice && matchesType
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPropertyById(propertyId: String): Property? {
        return try {
            val doc = firestore.collection("properties").document(propertyId).get().await()
            doc.toObject(Property::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addProperty(property: Property, imageUris: List<android.net.Uri>): Result<String> {
        return try {
            val propertyId = UUID.randomUUID().toString()
            val imageUrls = mutableListOf<String>()

            // Upload images
            imageUris.forEach { uri ->
                val ref = storage.reference.child("properties/$propertyId/${UUID.randomUUID()}")
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await()
                imageUrls.add(url.toString())
            }

            val propertyWithImages = property.copy(
                id = propertyId,
                imageUrls = imageUrls
            )

            firestore.collection("properties").document(propertyId).set(propertyWithImages).await()
            Result.success(propertyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyProperties(ownerId: String): List<Property> {
        return try {
            val snapshot = firestore.collection("properties")
                .whereEqualTo("ownerId", ownerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Property::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

