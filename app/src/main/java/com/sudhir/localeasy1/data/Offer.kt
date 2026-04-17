package com.sudhir.localeasy1.data

data class Offer(
    val id: String = "",
    val businessId: String = "",
    val title: String = "",
    val description: String = "",
    val discount: Int = 0, // percentage
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
