package com.zixuan_wang.swapnest.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.zixuan_wang.swapnest.model.ChatMessage
import com.zixuan_wang.swapnest.model.ChatThread
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeChatsForUser(uid: String): Flow<List<ChatThread>> = callbackFlow {
        val query = firestore.collection("chats")
            .whereArrayContains("participants", uid)

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) return@addSnapshotListener
            val chats = snapshot.documents.mapNotNull { doc ->
                val participants = doc.get("participants") as? List<*> ?: emptyList<Any>()
                ChatThread(
                    id = doc.id,
                    participants = participants.filterIsInstance<String>(),
                    lastMessage = doc.getString("lastMessage") ?: "",
                    updatedAt = doc.getLong("updatedAt") ?: 0L
                )
            }.sortedByDescending { it.updatedAt }
            trySend(chats)
        }

        awaitClose { registration.remove() }
    }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val query = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")

        val registration = query.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) return@addSnapshotListener
            val messages = snapshot.documents.mapNotNull { doc ->
                val text = doc.getString("text") ?: return@mapNotNull null
                val senderId = doc.getString("senderId") ?: ""
                val timestamp = doc.getLong("timestamp") ?: 0L
                ChatMessage(
                    id = doc.id,
                    senderId = senderId,
                    text = text,
                    timestamp = timestamp
                )
            }
            trySend(messages)
        }

        awaitClose { registration.remove() }
    }

    suspend fun ensureChat(chatId: String, participants: List<String>) {
        val doc = firestore.collection("chats").document(chatId)
        doc.set(
            mapOf(
                "participants" to participants,
                "updatedAt" to System.currentTimeMillis()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun getParticipants(chatId: String): List<String> {
        val snap = firestore.collection("chats").document(chatId).get().await()
        val participants = snap.get("participants") as? List<*> ?: emptyList<Any>()
        return participants.filterIsInstance<String>()
    }

    suspend fun sendMessage(chatId: String, senderId: String, text: String) {
        val now = System.currentTimeMillis()
        val chatDoc = firestore.collection("chats").document(chatId)
        val msgRef = chatDoc.collection("messages").document()
        msgRef.set(
            mapOf(
                "senderId" to senderId,
                "text" to text,
                "timestamp" to now
            )
        ).await()
        chatDoc.set(
            mapOf(
                "lastMessage" to text,
                "updatedAt" to now
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    suspend fun getUserName(uid: String): String {
        val snap = firestore.collection("users").document(uid).get().await()
        return snap.getString("name") ?: ""
    }
}
