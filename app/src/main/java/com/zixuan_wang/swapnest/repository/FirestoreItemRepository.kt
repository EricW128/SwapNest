package com.zixuan_wang.swapnest.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.zixuan_wang.swapnest.model.Item
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreItemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ItemRepository {
    private val itemsCollection = firestore.collection("items")
    private val legacyItemsCollection = firestore.collection("item")

    private fun normalizeItem(docId: String, raw: Item): Item {
        val type = when (raw.type) {
            "捐赠", "赠送", "免费领" -> "Donation"
            "交换", "可交换" -> "Exchange"
            else -> raw.type
        }
        val imageUrl =
            if (raw.imageUrl.isBlank()) "https://picsum.photos/seed/${docId}/400/600" else raw.imageUrl

        return raw.copy(
            id = if (raw.id.isNotBlank()) raw.id else docId,
            type = type,
            imageUrl = imageUrl
        )
    }

    override fun getItems(): Flow<List<Item>> = callbackFlow {
        var latestNew: List<Item> = emptyList()
        var latestLegacy: List<Item> = emptyList()

        fun emitMerged() {
            val merged = (latestNew + latestLegacy)
                .distinctBy { it.id }
                .sortedByDescending { it.timestamp }
            trySend(merged)
        }

        val registrations = mutableListOf<ListenerRegistration>()

        registrations += itemsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                latestNew = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Item::class.java)?.let { normalizeItem(doc.id, it) }
                }.orEmpty()
                emitMerged()
            }

        registrations += legacyItemsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                latestLegacy = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Item::class.java)?.let { normalizeItem(doc.id, it) }
                }.orEmpty()
                emitMerged()
            }

        awaitClose { registrations.forEach { it.remove() } }
    }

    override suspend fun addItem(item: Item, imageUri: Uri?): Item {
        val user = auth.currentUser
        val ownerId = item.ownerId.ifBlank { user?.uid ?: "anonymous" }
        val ownerName = item.ownerName.ifBlank { user?.displayName ?: "" }

        val doc = itemsCollection.document()
        val placeholderUrl =
            item.imageUrl.ifBlank { "https://picsum.photos/seed/${System.currentTimeMillis()}/400/600" }

        val baseItem = item.copy(
            id = doc.id,
            ownerId = if (item.ownerId.isNotBlank()) item.ownerId else ownerId,
            ownerName = if (item.ownerName.isNotBlank()) item.ownerName else ownerName,
            imageUrl = placeholderUrl,
            timestamp = System.currentTimeMillis()
        )

        doc.set(baseItem).await()

        if (imageUri == null) return baseItem

        val uploadedUrl = runCatching {
            val path = "items/$ownerId/${doc.id}_${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(path)
            ref.putFile(imageUri).await()
            var lastError: Exception? = null
            repeat(3) { attempt ->
                try {
                    return@runCatching ref.downloadUrl.await().toString()
                } catch (e: Exception) {
                    lastError = e
                    if (attempt < 2) delay(200)
                }
            }
            throw IllegalStateException(
                "图片上传后获取链接失败（Storage 对象不存在/权限异常）",
                lastError
            )
        }.getOrNull()

        if (uploadedUrl.isNullOrBlank()) return baseItem

        runCatching {
            doc.update("imageUrl", uploadedUrl).await()
        }

        return baseItem.copy(imageUrl = uploadedUrl)
    }

    override suspend fun getItemById(id: String): Item? {
        val snap = itemsCollection.document(id).get().await()
        return snap.toObject(Item::class.java)?.copy(id = snap.id)
    }
}
