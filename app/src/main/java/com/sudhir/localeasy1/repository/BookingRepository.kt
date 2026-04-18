package com.sudhir.localeasy1.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Booking
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

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
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    userName = doc.getString("userName") ?: "",
                    phone = doc.getString("phone") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getBusinessBookings(businessId: String): List<Booking> {
        return try {
            android.util.Log.d("BOOKING_REPO_DEBUG", "Querying bookings for businessId: $businessId")

            val snapshot = db.collection("bookings")
                .whereEqualTo("businessId", businessId)
                .get().await()

            android.util.Log.d("BOOKING_REPO_DEBUG", "Found ${snapshot.size()} documents")

            snapshot.documents.map { doc ->
                val booking = Booking(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    serviceId = doc.getString("serviceId") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    serviceName = doc.getString("serviceName") ?: "",
                    time = doc.getLong("time") ?: 0L,
                    status = doc.getString("status") ?: "pending",
                    notes = doc.getString("notes") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    userName = doc.getString("userName") ?: "",
                    phone = doc.getString("phone") ?: ""
                )

                android.util.Log.d("BOOKING_REPO_DEBUG", "Booking: ${booking.serviceName} - ${booking.userName} - ${booking.businessId}")
                booking
            }
        } catch (e: Exception) {
            android.util.Log.e("BOOKING_REPO_DEBUG", "Error getting business bookings", e)
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

    suspend fun getBookingsForOwner(ownerId: String): List<Booking> {
        return try {
            // Get all businesses for this owner
            val businessesSnapshot = db.collection("businesses")
                .whereEqualTo("ownerId", ownerId)
                .get().await()

            val businessIds = businessesSnapshot.documents.map { it.id }

            val allBookings = mutableListOf<Booking>()

            // Get bookings for each business
            for (businessId in businessIds) {
                val snapshot = db.collection("bookings")
                    .whereEqualTo("businessId", businessId)
                    .get().await()

                val bookings = snapshot.documents.map { doc ->
                    Booking(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        serviceId = doc.getString("serviceId") ?: "",
                        businessId = doc.getString("businessId") ?: "",
                        serviceName = doc.getString("serviceName") ?: "",
                        time = doc.getLong("time") ?: 0L,
                        status = doc.getString("status") ?: "pending",
                        notes = doc.getString("notes") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        userName = doc.getString("userName") ?: "",
                        phone = doc.getString("phone") ?: ""
                    )
                }
                allBookings.addAll(bookings)
            }

            allBookings.sortedByDescending { it.time }
        } catch (e: Exception) {
            android.util.Log.e("BOOKING_REPO_DEBUG", "Error getting bookings for owner", e)
            emptyList()
        }
    }
}
