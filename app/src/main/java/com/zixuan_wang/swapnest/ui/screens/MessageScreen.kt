package com.zixuan_wang.swapnest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zixuan_wang.swapnest.model.User
import com.zixuan_wang.swapnest.repository.FirestoreChatRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    val id: String,
    val senderName: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0
)

@Composable
fun MessageScreen(
    currentUser: User?,
    onChatClick: (String) -> Unit,
    onShowHUD: (String) -> Unit
) {
    val repo = remember { FirestoreChatRepository() }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    val uid = currentUser?.uid

    LaunchedEffect(uid) {
        if (uid == null) {
            messages = emptyList()
            return@LaunchedEffect
        }
        repo.observeChatsForUser(uid).collect { chats ->
            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            val mapped = chats.map { chat ->
                val partnerId = chat.participants.firstOrNull { it != uid }.orEmpty()
                val name = if (partnerId.isBlank()) "用户" else repo.getUserName(partnerId).ifBlank { "用户" }
                Message(
                    id = chat.id,
                    senderName = name,
                    lastMessage = chat.lastMessage,
                    time = if (chat.updatedAt == 0L) "" else dateFormat.format(Date(chat.updatedAt)),
                    unreadCount = 0
                )
            }
            messages = mapped
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Reduced top padding for header area
        Spacer(modifier = Modifier.height(12.dp))
        
        // Message Page Header - Reduced padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp) // Reduced vertical padding
        ) {
            Text(
                text = "消息",
                style = MaterialTheme.typography.titleLarge, // Slightly smaller than headline
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        
        LazyColumn {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    onClick = {
                        if (uid == null) {
                            onShowHUD("请先登录")
                        } else {
                            onChatClick(message.id)
                        }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (message.unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Red,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = message.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
