package com.sudhir.localeasy1.data

data class Booking(
    val id: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val businessId: String = "",
    val serviceName: String = "",
    val time: Long = 0L,
    val status: String = "pending", // pending, confirmed, completed, cancelled
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
