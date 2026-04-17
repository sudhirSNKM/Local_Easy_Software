package com.sudhir.localeasy1.data

data class Banner(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val ownerId: String = "",
    val isApproved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
