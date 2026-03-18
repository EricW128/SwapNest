package com.zixuan_wang.swapnest.model

data class ChatThread(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val updatedAt: Long = 0L
)

