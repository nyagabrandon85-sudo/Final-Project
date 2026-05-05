package com.deepseek.rentease.models

data class Property(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val location: String = "",
    val propertyType: String = "", // apartment, house, studio, villa
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val imageUrls: List<String> = emptyList(),
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val isAvailable: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
