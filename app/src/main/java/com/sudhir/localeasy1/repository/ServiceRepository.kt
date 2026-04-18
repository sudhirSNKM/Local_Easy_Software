package com.sudhir.localeasy1.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sudhir.localeasy1.data.Service
import kotlinx.coroutines.tasks.await

class ServiceRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

    private suspend fun getApprovedBusinessIds(): Set<String> {
        val businesses = db.collection("businesses")
            .whereEqualTo("approved", true)
            .get().await()
        return businesses.documents.map { it.id }.toSet()
    }

    suspend fun getAllServices(): List<Service> {
        return try {
            val approvedBusinessIds = getApprovedBusinessIds()
            val snapshot = db.collection("services").get().await()
            snapshot.documents.map { doc ->
                Service(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    duration = doc.getString("duration") ?: "",
                    category = doc.getString("category") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    timings = doc.get("timings") as? List<String> ?: emptyList(),
                    notes = doc.getString("notes") ?: ""
                )
            }.filter { it.businessId in approvedBusinessIds }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getServicesByCategory(category: String): List<Service> {
        return try {
            val approvedBusinessIds = getApprovedBusinessIds()
            val snapshot = db.collection("services")
                .whereEqualTo("category", category)
                .get().await()
            snapshot.documents.map { doc ->
                Service(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    duration = doc.getString("duration") ?: "",
                    category = doc.getString("category") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    timings = doc.get("timings") as? List<String> ?: emptyList(),
                    notes = doc.getString("notes") ?: ""
                )
            }.filter { it.businessId in approvedBusinessIds }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addService(service: Service): Result<String> {
        return try {
            val docRef = db.collection("services").document()
            val serviceWithId = service.copy(id = docRef.id)
            docRef.set(serviceWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServicesByBusinessId(businessId: String): List<Service> {
        return try {
            val snapshot = db.collection("services")
                .whereEqualTo("businessId", businessId)
                .get().await()
            snapshot.documents.map { doc ->
                Service(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    price = doc.getLong("price")?.toInt() ?: 0,
                    duration = doc.getString("duration") ?: "",
                    category = doc.getString("category") ?: "",
                    businessId = doc.getString("businessId") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    timings = doc.get("timings") as? List<String> ?: emptyList(),
                    notes = doc.getString("notes") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
