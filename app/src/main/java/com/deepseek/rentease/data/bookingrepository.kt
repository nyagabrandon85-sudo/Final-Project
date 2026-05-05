package com.deepseek.rentease.data

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
            firestore.collection("bookings").document(bookingId).set(bookingWithId).await()
            Result.success(bookingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyBookings(): List<Booking> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("tenantId", userId)
                .orderBy("requestedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Booking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getReceivedBookings(): List<Booking> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("landlordId", userId)
                .orderBy("requestedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Booking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String) {
        firestore.collection("bookings").document(bookingId)
            .update("status", status)
            .await()
    }
}


