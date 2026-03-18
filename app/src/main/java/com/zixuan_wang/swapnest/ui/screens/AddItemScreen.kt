package com.zixuan_wang.swapnest.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zixuan_wang.swapnest.model.Item
import com.zixuan_wang.swapnest.model.User
import com.zixuan_wang.swapnest.viewmodel.ItemViewModel
import com.zixuan_wang.swapnest.viewmodel.AddItemUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    viewModel: ItemViewModel,
    currentUser: User?,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    onShowHUD: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Donation") }
    var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { selectedImages = selectedImages + it }
    }

    var showCategorySheet by remember { mutableStateOf(false) }
    var showConditionSheet by remember { mutableStateOf(false) }
    val addItemUiState by viewModel.addItemUiState.collectAsState()

    LaunchedEffect(addItemUiState) {
        when (val s = addItemUiState) {
            AddItemUiState.Success -> {
                viewModel.resetAddItemUiState()
                onSuccess()
            }
            is AddItemUiState.Error -> {
                onShowHUD(s.message)
                viewModel.resetAddItemUiState()
            }
            else -> Unit
        }
    }

    val categories = listOf("电子产品", "图书文具", "生活用品", "运动户外", "美妆个护", "其他")
    val conditions = listOf("全新", "几乎全新", "九成新", "八成新", "七成新及以下")

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            val u = currentUser ?: return@Button
                            val image = selectedImages.firstOrNull() ?: return@Button
                            if (title.isNotBlank()) {
                                val newItem = Item(
                                    title = title,
                                    description = description,
                                    category = category,
                                    condition = condition,
                                    type = type,
                                    imageUrl = "",
                                    ownerId = u.uid,
                                    ownerName = u.name
                                )
                                viewModel.addItem(newItem, image)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f) // Make it centered and not full width
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        enabled = addItemUiState != AddItemUiState.Uploading && currentUser != null && title.isNotBlank() && selectedImages.isNotEmpty()
                    ) {
                        Text(
                            if (addItemUiState == AddItemUiState.Uploading) "发布中..." else "确认发布",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Custom Top Bar Area - Reduced spacing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp, start = 4.dp, end = 4.dp), // Reduced vertical padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "发布闲置", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Image Selection Area - Red Style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp) // Adjusted vertical padding
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Image Button
                if (selectedImages.size < 9) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8F9FA))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = "Add Image", tint = Color.LightGray, modifier = Modifier.size(24.dp))
                            Text("添加图片", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }

                // Selected Images
                selectedImages.forEachIndexed { index, uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            IconButton(
                                onClick = { selectedImages = selectedImages.filterIndexed { i, _ -> i != index } },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(18.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE), modifier = Modifier.padding(horizontal = 16.dp))
            
            // Input Fields
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("物品名称 (如: 复古台灯)", color = Color.LightGray, fontSize = 16.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
                
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF0F0F0))
                
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("添加正文，说说你的物品故事吧...\n\n#成色 #来源 #转手原因", color = Color.LightGray, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Options
            PublishOptionItem(label = "添加分类", value = if (category.isEmpty()) "" else category) {
                showCategorySheet = true
            }
            PublishOptionItem(label = "成色", value = if (condition.isEmpty()) "选择成色" else condition) {
                showConditionSheet = true
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("想要的方式", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row {
                    FilterChip(
                        selected = type == "Donation",
                        onClick = { type = "Donation" },
                        label = { Text("免费领", fontSize = 12.sp) },
                        modifier = Modifier.padding(end = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = null
                    )
                    FilterChip(
                        selected = type == "Exchange",
                        onClick = { type = "Exchange" },
                        label = { Text("换个物", fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        border = null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Selection Sheets
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        "选择分类",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    categories.forEach { cat ->
                        ListItem(
                            headlineContent = { Text(cat) },
                            modifier = Modifier.clickable {
                                category = cat
                                showCategorySheet = false
                            }
                        )
                    }
                }
            }
        }

        if (showConditionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showConditionSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        "选择成色",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    conditions.forEach { cond ->
                        ListItem(
                            headlineContent = { Text(cond) },
                            modifier = Modifier.clickable {
                                condition = cond
                                showConditionSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublishOptionItem(label: String, value: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = Color.Gray, fontSize = 14.sp)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}
