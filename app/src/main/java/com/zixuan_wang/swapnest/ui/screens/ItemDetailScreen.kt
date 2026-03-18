package com.zixuan_wang.swapnest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.zixuan_wang.swapnest.model.Item
import com.zixuan_wang.swapnest.repository.FirestoreChatRepository
import com.zixuan_wang.swapnest.viewmodel.ItemViewModel
import kotlinx.coroutines.launch

@Immutable
private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return object : DelegatableNode, Modifier.Node(), DrawModifierNode {
            override fun ContentDrawScope.draw() {
                drawContent()
            }
        }
    }

    override fun hashCode(): Int = -1
    override fun equals(other: Any?): Boolean = other === this
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    viewModel: ItemViewModel,
    onBack: () -> Unit,
    onContactSeller: (String) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val item = remember(itemId) { viewModel.getItem(itemId) }
    val requestedItemIds by viewModel.requestedItemIds.collectAsState()
    val claimedItemIds by viewModel.claimedItemIds.collectAsState()
    val favoritedItemIds by viewModel.favoritedItemIds.collectAsState()
    val scope = rememberCoroutineScope()
    val chatRepo = remember { FirestoreChatRepository() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        if (item != null) {
            val isDonation = item.type == "Donation"
            val isRequested = !isDonation && requestedItemIds.contains(itemId)
            val isClaimed = isDonation && claimedItemIds.contains(itemId)
            val isFavorited = favoritedItemIds.contains(itemId)

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Back Button Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, top = 8.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack, 
                                    contentDescription = "Back", 
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                
                    Column(modifier = Modifier.padding(16.dp)) {
                        // User Info Row - Show both name and location clearly
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.ownerName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "上海·静安",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "刚刚发布",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                            
                            Button(
                                onClick = { onShowSnackbar("关注成功") },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("关注", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                lineHeight = 28.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                color = Color(0xFF333333),
                                lineHeight = 24.sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Details Tag Row
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DetailTag(label = item.category)
                            DetailTag(label = item.condition)
                            DetailTag(label = if (item.type == "Donation") "免费捐赠" else "闲置交换")
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))

                        // Mock Comment Section
                        Text(
                            text = "全部留言 (2)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        CommentItem(name = "李华", content = "请问还在吗？", time = "2小时前")
                        CommentItem(name = "王五", content = "成色看起来不错，想要！", time = "5小时前")
                        
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
                    }
                }

                // Fixed Bottom Bar
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    color = Color.White
                ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .navigationBarsPadding(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = NoIndication
                                    ) { viewModel.toggleFavorite(itemId) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorited) Color(0xFFFFC107) else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Button(
                                onClick = { 
                                    if (isDonation) {
                                        if (!isClaimed) {
                                            viewModel.claimItem(itemId)
                                        }
                                    } else {
                                        if (!isRequested) {
                                            viewModel.requestItem(itemId)
                                        }
                                    }
                                    val me = FirebaseAuth.getInstance().currentUser?.uid
                                    if (me.isNullOrBlank()) {
                                        onShowSnackbar("请先登录")
                                        return@Button
                                    }
                                    val seller = item.ownerId
                                    if (seller.isBlank()) {
                                        onShowSnackbar("无法发起私聊（缺少卖家信息）")
                                        return@Button
                                    }
                                    val chatId = listOf(me, seller).sorted().joinToString("_") + "_" + itemId
                                    scope.launch {
                                        runCatching {
                                            chatRepo.ensureChat(chatId, listOf(me, seller))
                                        }.onFailure {
                                            onShowSnackbar(it.message ?: "创建会话失败")
                                        }.onSuccess {
                                            onContactSeller(chatId)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDonation) {
                                        if (isClaimed) Color.Gray else MaterialTheme.colorScheme.primary
                                    } else {
                                        if (isRequested) Color.Gray else MaterialTheme.colorScheme.primary
                                    }
                                ),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (isDonation) {
                                        if (isClaimed) "已申领" else "立即申领"
                                    } else {
                                        if (isRequested) "已发起交换" else "发起交换"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                }
            }
        }
    }
}

@Composable
fun DetailTag(label: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Text(
            text = "#$label",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CommentItem(name: String, content: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.Person, 
                contentDescription = null, 
                modifier = Modifier.padding(6.dp), 
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
            Text(text = time, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
        }
    }
}
