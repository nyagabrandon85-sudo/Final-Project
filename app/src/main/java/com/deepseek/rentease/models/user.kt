package com.deepseek.rentease.models

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "tenant", // "tenant" or "landlord"
    val createdAt: Long = System.currentTimeMillis()
)


