package com.deepseek.rentease.models

data class Booking(
    val id: String = "",
    val propertyId: String = "",
    val propertyTitle: String = "",
    val tenantId: String = "",
    val tenantName: String = "",
    val tenantPhone: String = "",
    val landlordId: String = "",
    val landlordName: String = "",
    val landlordPhone: String = "",
    val message: String = "",
    val status: String = "pending", // pending, approved, rejected
    val viewedByLandlord: Boolean = false,
    val requestedAt: Long = System.currentTimeMillis()
)
