package com.zixuan_wang.swapnest.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.zixuan_wang.swapnest.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val u = firebaseAuth.currentUser
        _currentUser.value =
            if (u == null) {
                null
            } else {
                User(
                    uid = u.uid,
                    name = u.displayName ?: "",
                    email = u.email ?: ""
                )
            }
    }

    init {
        auth.addAuthStateListener(authListener)
        authListener.onAuthStateChanged(auth)
    }

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun register(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: return
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        firestore.collection("users").document(user.uid).set(
            mapOf(
                "uid" to user.uid,
                "name" to name,
                "email" to (user.email ?: email)
            )
        ).await()
    }

    override suspend fun logout() {
        auth.signOut()
    }
}

