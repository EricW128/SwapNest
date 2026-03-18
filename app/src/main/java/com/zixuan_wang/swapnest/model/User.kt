package com.zixuan_wang.swapnest.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val itemsOwned: List<String> = emptyList()
)
