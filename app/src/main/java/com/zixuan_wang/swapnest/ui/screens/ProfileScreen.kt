package com.zixuan_wang.swapnest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zixuan_wang.swapnest.viewmodel.AuthViewModel
import com.zixuan_wang.swapnest.viewmodel.ItemViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    itemViewModel: ItemViewModel,
    onLogout: () -> Unit,
    onActionClick: (String) -> Unit
) {
    val user by authViewModel.currentUser.collectAsState()
    val requestedItemIds by itemViewModel.requestedItemIds.collectAsState()
    val favoritedItemIds by itemViewModel.favoritedItemIds.collectAsState()
    val allItems by itemViewModel.allItems.collectAsState()
    val myItemsCount = remember(user?.uid, allItems) {
        val uid = user?.uid
        if (uid.isNullOrBlank()) 0 else allItems.count { it.ownerId == uid }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Reduced top padding for header area
        Spacer(modifier = Modifier.height(12.dp))
        
        // Header Section - More compact
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 0.5.dp // Reduced shadow
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp), // Reduced top padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.padding(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = user?.name ?: "未登录用户",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email ?: "example@swapnest.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Xianyu style Stats/Action Grid
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileQuickAction(label = "我发布的", count = myItemsCount.toString(), icon = "📤", onClick = { onActionClick("我发布的") })
                ProfileQuickAction(label = "我收到的", count = requestedItemIds.size.toString(), icon = "📥", onClick = { onActionClick("我收到的") })
                ProfileQuickAction(label = "我交换的", count = "0", icon = "🔄", onClick = { onActionClick("我交换的") })
                ProfileQuickAction(label = "我收藏的", count = favoritedItemIds.size.toString(), icon = "⭐", onClick = { onActionClick("我收藏的") })
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Action List
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column {
                ProfileMenuItem(label = "地址管理", icon = "📍", onClick = { onActionClick("地址管理") })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFF0F0F0))
                ProfileMenuItem(label = "实名认证", icon = "🆔", onClick = { onActionClick("实名认证") })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFF0F0F0))
                ProfileMenuItem(label = "我的钱包", icon = "💰", onClick = { onActionClick("我的钱包") })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color(0xFFF0F0F0))
                ProfileMenuItem(label = "设置", icon = "⚙️", onClick = { onActionClick("设置") })
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .height(44.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
        ) {
            Text(text = "退出登录", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ProfileQuickAction(label: String, count: String, icon: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun ProfileMenuItem(label: String, icon: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF9F9F9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.KeyboardArrowRight, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp),
                tint = Color.LightGray
            )
        }
    }
}
