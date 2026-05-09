package com.deepseek.rentease.data

import android.util.Log
import com.deepseek.rentease.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createBooking(booking: Booking): Result<String> {
        return try {
            val bookingId = java.util.UUID.randomUUID().toString()
            val bookingWithId = booking.copy(id = bookingId)
            
            if (bookingWithId.landlordId.isBlank()) {
                throw Exception("Cannot create booking: Property owner ID is missing")
            }
            
            firestore.collection("bookings").document(bookingId).set(bookingWithId).await()
            android.util.Log.d("BookingRepository", "Booking created successfully: $bookingId for landlord: ${bookingWithId.landlordId}")
            Result.success(bookingId)
        } catch (e: Exception) {
            android.util.Log.e("BookingRepository", "Error creating booking", e)
            Result.failure(e)
        }
    }

    suspend fun getMyBookings(): List<Booking> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("tenantId", userId)
                .get()
                .await()
            snapshot.toObjects(Booking::class.java)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error fetching my bookings", e)
            emptyList()
        }
    }

    suspend fun getReceivedBookings(): List<Booking> {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("BookingRepository", "No current user UID found for received bookings")
            return emptyList()
        }
        Log.d("BookingRepository", "Fetching received bookings for landlordId: $userId")
        return try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("landlordId", userId)
                .get()
                .await()
            Log.d("BookingRepository", "Found ${snapshot.size()} received bookings")
            snapshot.toObjects(Booking::class.java)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error fetching received bookings", e)
            emptyList()
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String) {
        try {
            firestore.collection("bookings").document(bookingId)
                .update("status", status)
                .await()
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error updating booking status", e)
        }
    }

    suspend fun markReceivedBookingsAsViewed() {
        val userId = auth.currentUser?.uid ?: return
        try {
            val unreadBookings = firestore.collection("bookings")
                .whereEqualTo("landlordId", userId)
                .whereEqualTo("viewedByLandlord", false)
                .get()
                .await()

            if (!unreadBookings.isEmpty) {
                val batch = firestore.batch()
                for (doc in unreadBookings.documents) {
                    batch.update(doc.reference, "viewedByLandlord", true)
                }
                batch.commit().await()
                Log.d("BookingRepository", "Marked ${unreadBookings.size()} bookings as viewed")
            }
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error marking bookings as viewed", e)
        }
    }
}
