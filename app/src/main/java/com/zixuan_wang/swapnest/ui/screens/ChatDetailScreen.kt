package com.zixuan_wang.swapnest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zixuan_wang.swapnest.model.User
import com.zixuan_wang.swapnest.repository.FirestoreChatRepository
import kotlinx.coroutines.launch

data class ChatBubbleMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    currentUser: User?,
    onBack: () -> Unit,
    onShowHUD: (String) -> Unit
) {
    val repo = remember { FirestoreChatRepository() }
    val uid = currentUser?.uid
    var chatPartnerName by remember { mutableStateOf("用户") }
    val scope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatBubbleMessage>>(emptyList()) }

    LaunchedEffect(chatId, uid) {
        if (uid.isNullOrBlank()) return@LaunchedEffect
        runCatching {
            val participants = repo.getParticipants(chatId)
            val partnerId = participants.firstOrNull { it != uid }.orEmpty()
            chatPartnerName = if (partnerId.isBlank()) "用户" else repo.getUserName(partnerId).ifBlank { "用户" }
        }
    }

    LaunchedEffect(chatId, uid) {
        repo.observeMessages(chatId).collect { list ->
            messages = list.map { m ->
                ChatBubbleMessage(
                    id = m.id,
                    text = m.text,
                    isFromMe = uid != null && m.senderId == uid
                )
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 4.dp, bottom = 6.dp, start = 4.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = chatPartnerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("发送消息...", fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val senderId = uid
                            if (senderId.isNullOrBlank()) {
                                onShowHUD("请先登录")
                                return@IconButton
                            }
                            val text = messageText.trim()
                            if (text.isBlank()) return@IconButton
                            messageText = ""
                            scope.launch {
                                runCatching { repo.sendMessage(chatId, senderId, text) }
                                    .onFailure { onShowHUD(it.message ?: "发送失败") }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatBubbleMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (message.isFromMe) 12.dp else 0.dp,
                bottomEnd = if (message.isFromMe) 0.dp else 12.dp
            ),
            color = if (message.isFromMe) MaterialTheme.colorScheme.primary else Color.White,
            tonalElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (message.isFromMe) Color.White else Color.Black,
                fontSize = 15.sp
            )
        }
    }
}
