package com.sudhir.localeasy1.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Booking
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun createBooking(booking: Booking): Result<String> {
        return try {
            val docRef = db.collection("bookings").document()
            val bookingWithId = booking.copy(id = docRef.id, createdAt = System.currentTimeMillis())
            docRef.set(bookingWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isSlotAvailable(businessId: String, time: Long): Boolean {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("businessId", businessId)
                .whereEqualTo("time", time)
                .get().await()
            
            // Check if any booking for this slot is NOT cancelled
            val activeBookings = snapshot.documents.filter { doc ->
                val status = doc.getString("status") ?: "pending"
                status != "cancelled"
            }
            activeBookings.isEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get().await()
            snapshot.documents.map { doc ->
                Booking(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    serviceId = doc.getString("serviceId") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    serviceName = doc.getString("serviceName") ?: "",
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
                .get().await()
            snapshot.documents.map { doc ->
                Booking(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    serviceId = doc.getString("serviceId") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    serviceName = doc.getString("serviceName") ?: "",
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
