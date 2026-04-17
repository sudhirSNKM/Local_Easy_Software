package com.sudhir.localeasy1.data

data class Place(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val address: String
)

val samplePlaces = listOf(
    Place("1", "Central Park", "Park", 4.8, "Manhattan, NY"),
    Place("2", "Joe's Coffee", "Cafe", 4.5, "Greenwich Village, NY"),
    Place("3", "The Met Museum", "Museum", 4.9, "Upper East Side, NY"),
    Place("4", "Pizza Palace", "Restaurant", 4.2, "Brooklyn, NY"),
    Place("5", "City Library", "Library", 4.6, "5th Ave, NY")
)
