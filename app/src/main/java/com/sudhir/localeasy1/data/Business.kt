package com.sudhir.localeasy1.data

data class Business(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val address: String = "",
    val approved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
