package com.sudhir.localeasy1.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Business
import kotlinx.coroutines.tasks.await

class BusinessRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun getApprovedBusinesses(): List<Business> {
        return try {
            val snapshot = db.collection("businesses")
                .whereEqualTo("approved", true)
                .get().await()
            snapshot.documents.map { doc ->
                Business(
                    id = doc.id,
                    ownerId = doc.getString("ownerId") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "",
                    address = doc.getString("address") ?: "",
                    approved = doc.getBoolean("approved") ?: (doc.getBoolean("isApproved") ?: false),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingBusinesses(): List<Business> {
        return try {
            val snapshot = db.collection("businesses")
                .whereEqualTo("approved", false)
                .get().await()
            snapshot.documents.map { doc ->
                Business(
                    id = doc.id,
                    ownerId = doc.getString("ownerId") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "",
                    address = doc.getString("address") ?: "",
                    approved = doc.getBoolean("approved") ?: (doc.getBoolean("isApproved") ?: false),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun approveBusiness(businessId: String): Result<Unit> {
        return try {
            db.collection("businesses").document(businessId)
                .update("approved", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectBusiness(businessId: String): Result<Unit> {
        return try {
            db.collection("businesses").document(businessId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBusiness(business: Business): Result<String> {
        return try {
            val docRef = db.collection("businesses").document()
            val businessWithId = business.copy(id = docRef.id)
            docRef.set(businessWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBusinessesByOwner(ownerId: String): List<Business> {
        return try {
            val snapshot = db.collection("businesses")
                .whereEqualTo("ownerId", ownerId)
                .get().await()
            snapshot.documents.map { doc ->
                Business(
                    id = doc.id,
                    ownerId = doc.getString("ownerId") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "",
                    address = doc.getString("address") ?: "",
                    approved = doc.getBoolean("approved") ?: (doc.getBoolean("isApproved") ?: false),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOwnerPrimaryBusiness(ownerId: String): Business? {
        return getBusinessesByOwner(ownerId).firstOrNull()
    }
}
