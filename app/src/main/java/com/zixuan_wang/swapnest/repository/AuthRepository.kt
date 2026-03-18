package com.zixuan_wang.swapnest.repository

import com.zixuan_wang.swapnest.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(email: String, password: String)
    suspend fun register(name: String, email: String, password: String)
    suspend fun logout()
}

class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(
        User(uid = "user1", name = "Eric Wang", email = "ericwang@example.com")
    )
    override val currentUser: StateFlow<User?> = _currentUser

    override suspend fun login(email: String, password: String) {
        _currentUser.value = User(uid = "user1", name = "Eric Wang", email = email)
    }

    override suspend fun register(name: String, email: String, password: String) {
        _currentUser.value = User(uid = "user1", name = name, email = email)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
