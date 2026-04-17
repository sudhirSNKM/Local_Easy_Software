package com.sudhir.localeasy1.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Booking
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun createBooking(booking: Booking): Result<String> {
        return try {
            val docRef = db.collection("bookings").document()
            val bookingWithId = booking.copy(id = docRef.id)
            docRef.set(bookingWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("time")
                .get().await()
            snapshot.documents.map { doc ->
                Booking(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    serviceId = doc.getString("serviceId") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    time = doc.getLong("time") ?: 0L,
                    status = doc.getString("status") ?: "pending",
                    notes = doc.getString("notes") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBusinessBookings(businessId: String): List<Booking> {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("businessId", businessId)
                .orderBy("time")
                .get().await()
            snapshot.documents.map { doc ->
                Booking(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    serviceId = doc.getString("serviceId") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    time = doc.getLong("time") ?: 0L,
                    status = doc.getString("status") ?: "pending",
                    notes = doc.getString("notes") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> {
        return try {
            db.collection("bookings").document(bookingId)
                .update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
