package com.deepseek.rentease.data

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.deepseek.rentease.models.Property
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PropertyRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getProperties(): List<Property> {
        return try {
            val snapshot = firestore.collection("properties")
                .get()
                .await()
            snapshot.toObjects(Property::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchProperties(query: String, minPrice: Double?, maxPrice: Double?, type: String?): List<Property> {
        return try {
            val results = firestore.collection("properties")
                .get()
                .await()
                .toObjects(Property::class.java)

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

    suspend fun addProperty(property: Property, imageUris: List<Uri>): Result<String> {
        return try {
            val propertyId = UUID.randomUUID().toString()
            val imageUrls = mutableListOf<String>()

            // Upload images to Cloudinary
            imageUris.forEach { uri ->
                val url = uploadToCloudinary(uri)
                imageUrls.add(url)
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

    private suspend fun uploadToCloudinary(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .unsigned("rentEase uploads") // Your preset name
            .option("folder", "properties")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as String
                    continuation.resume(url)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    suspend fun getMyProperties(ownerId: String): List<Property> {
        return try {
            val snapshot = firestore.collection("properties")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            snapshot.toObjects(Property::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateProperty(property: Property): Result<Unit> {
        return try {
            firestore.collection("properties").document(property.id).set(property).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProperty(propertyId: String): Result<Unit> {
        return try {
            firestore.collection("properties").document(propertyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
