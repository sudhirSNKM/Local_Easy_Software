package com.sudhir.localeasy1.data

data class Service(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val duration: String = "",
    val category: String = "",
    val businessId: String = "",
    val imageUrl: String = "",
    val timings: List<String> = emptyList(),
    val notes: String = ""
)
