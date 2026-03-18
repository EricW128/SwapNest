package com.zixuan_wang.swapnest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.zixuan_wang.swapnest.viewmodel.ItemViewModel

@Composable
fun NearbyScreen(
    viewModel: ItemViewModel,
    onItemClick: (String) -> Unit
) {
    val items by viewModel.items.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
        }
    }

    val shanghai = LatLng(31.2304, 121.4737)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(shanghai, 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            filteredItems.forEach { item ->
                Marker(
                    state = MarkerState(position = LatLng(item.latitude, item.longitude)),
                    title = item.title,
                    snippet = "${if (item.type == "Donation") "赠送" else "交换"} - ${item.category}",
                    onClick = {
                        onItemClick(item.id)
                        true
                    }
                )
            }
        }

        // Search Bar Overlay - More compact
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp) // Reduced padding
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("搜索附近的闲置...", color = Color.LightGray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}
