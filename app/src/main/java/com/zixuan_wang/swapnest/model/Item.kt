package com.zixuan_wang.swapnest.model

import com.google.firebase.firestore.DocumentId

data class Item(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val condition: String = "", // e.g., New, Used, etc.
    val imageUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val type: String = "Donation", // "Donation" or "Exchange"
    val timestamp: Long = System.currentTimeMillis(),
    val isAvailable: Boolean = true,
    val latitude: Double = 31.2304, // Default to Shanghai
    val longitude: Double = 121.4737
)
