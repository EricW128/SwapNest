package com.zixuan_wang.swapnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zixuan_wang.swapnest.repository.FirebaseAuthRepository
import com.zixuan_wang.swapnest.repository.FirestoreItemRepository
import com.zixuan_wang.swapnest.ui.screens.*
import com.zixuan_wang.swapnest.ui.theme.SwapNestTheme
import com.zixuan_wang.swapnest.viewmodel.AuthViewModel
import com.zixuan_wang.swapnest.viewmodel.ItemViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            SwapNestTheme {
                val backgroundColor = MaterialTheme.colorScheme.background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    SwapNestApp()
                }
            }
        }
    }
}

@Composable
fun CustomHUD(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.7f),
                contentColor = Color.White,
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color.White
                )
            }
        }
        
        LaunchedEffect(isVisible) {
            if (isVisible) {
                delay(2000)
                onDismiss()
            }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapNestApp() {
    val rootNavController = rememberNavController()
    
    var hudMessage by remember { mutableStateOf("") }
    var hudVisible by remember { mutableStateOf(false) }

    fun showHUD(msg: String) {
        hudMessage = msg
        hudVisible = true
    }

    val itemRepo = remember { FirestoreItemRepository() }
    val authRepo = remember { FirebaseAuthRepository() }
    val itemViewModel = remember { ItemViewModel(itemRepo) }
    val authViewModel = remember { AuthViewModel(authRepo) }
    val currentUser by authViewModel.currentUser.collectAsState()
    val rootNavBackStackEntry by rootNavController.currentBackStackEntryAsState()
    val rootRoute = rootNavBackStackEntry?.destination?.route

    LaunchedEffect(currentUser?.uid, rootRoute) {
        if (currentUser == null) {
            if (rootRoute == "bootstrap") {
                rootNavController.navigate("login") {
                    popUpTo(0)
                    launchSingleTop = true
                }
            } else if (rootRoute != null && rootRoute != "login" && rootRoute != "register") {
                rootNavController.navigate("login") {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
        } else {
            if (rootRoute == "login" || rootRoute == "register" || rootRoute == "bootstrap") {
                rootNavController.navigate("main") {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = rootNavController,
            startDestination = "bootstrap",
            enterTransition = { 
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            exitTransition = { 
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            popEnterTransition = { 
                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
            popExitTransition = { 
                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            }
        ) {
            composable(
                "bootstrap",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }
            composable("login", 
                enterTransition = { EnterTransition.None }, 
                exitTransition = { ExitTransition.None }
            ) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { 
                        rootNavController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onRegisterClick = { rootNavController.navigate("register") },
                    onShowHUD = { showHUD(it) }
                )
            }
            composable("register", 
                enterTransition = { EnterTransition.None }, 
                exitTransition = { ExitTransition.None }
            ) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        rootNavController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBack = { rootNavController.popBackStack() },
                    onShowHUD = { showHUD(it) }
                )
            }
            
            // Main Route includes BottomBar - This ensures the bottom bar slides with the screens
            composable("main",
                enterTransition = { EnterTransition.None },
                exitTransition = {
                    if (targetState.destination.route == "add_item") {
                        ExitTransition.None
                    } else {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    }
                },
                popEnterTransition = {
                    if (initialState.destination.route == "add_item") {
                        EnterTransition.None
                    } else {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        )
                    }
                }
            ) {
                MainContent(
                    itemViewModel = itemViewModel,
                    authViewModel = authViewModel,
                    rootNavController = rootNavController,
                    showHUD = { showHUD(it) }
                )
            }

            // Detail Routes - No BottomBar here, they will slide over/out with MainContent
            composable(
                route = "item_detail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                ItemDetailScreen(
                    itemId = itemId,
                    viewModel = itemViewModel,
                    onBack = { rootNavController.popBackStack() },
                    onContactSeller = { chatId -> 
                        rootNavController.navigate("chat_detail/$chatId") {
                            launchSingleTop = true
                        }
                    },
                    onShowSnackbar = { showHUD(it) }
                )
            }
            composable(
                "chat_detail/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val currentUser by authViewModel.currentUser.collectAsState()
                ChatDetailScreen(
                    chatId = chatId,
                    currentUser = currentUser,
                    onBack = { rootNavController.popBackStack() },
                    onShowHUD = { showHUD(it) }
                )
            }
            composable(
                "add_item",
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(300)
                    )
                },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(300)
                    )
                }
            ) {
                val addItemUser by authViewModel.currentUser.collectAsState()
                AddItemScreen(
                    viewModel = itemViewModel,
                    currentUser = addItemUser,
                    onSuccess = { 
                        rootNavController.popBackStack()
                        showHUD("发布成功！")
                    },
                    onBack = { rootNavController.popBackStack() },
                    onShowHUD = { showHUD(it) }
                )
            }
            composable("requested_items") {
                RequestedItemsScreen(
                    viewModel = itemViewModel,
                    onBack = { rootNavController.popBackStack() },
                    onItemClick = { itemId -> rootNavController.navigate("item_detail/$itemId") }
                )
            }
            composable(
                "profile_action/{title}",
                arguments = listOf(navArgument("title") { type = NavType.StringType })
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                ProfilePlaceholderScreen(
                    title = title,
                    onBack = { rootNavController.popBackStack() },
                    onItemClick = { itemId -> rootNavController.navigate("item_detail/$itemId") }
                )
            }
        }

        CustomHUD(
            message = hudMessage,
            isVisible = hudVisible,
            onDismiss = { hudVisible = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    itemViewModel: ItemViewModel,
    authViewModel: AuthViewModel,
    rootNavController: androidx.navigation.NavHostController,
    showHUD: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            val themeColor = MaterialTheme.colorScheme.primary

            fun navigateTo(route: String) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                CompositionLocalProvider(LocalIndication provides NoIndication) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val feedSelected = currentDestination?.route == "feed"
                        val nearbySelected = currentDestination?.route == "nearby"
                        val messagesSelected = currentDestination?.route == "messages"
                        val profileSelected = currentDestination?.route == "profile"

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = NoIndication
                                ) { navigateTo("feed") },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (feedSelected) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home",
                                tint = if (feedSelected) themeColor else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "首页",
                                fontSize = 10.sp,
                                color = if (feedSelected) themeColor else Color.Gray
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = NoIndication
                                ) { navigateTo("nearby") },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (nearbySelected) Icons.Default.LocationOn else Icons.Outlined.LocationOn,
                                contentDescription = "Nearby",
                                tint = if (nearbySelected) themeColor else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "附近",
                                fontSize = 10.sp,
                                color = if (nearbySelected) themeColor else Color.Gray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = NoIndication
                                ) { rootNavController.navigate("add_item") },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = themeColor,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = NoIndication
                                ) { navigateTo("messages") },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (messagesSelected) Icons.Default.Email else Icons.Outlined.Email,
                                contentDescription = "Messages",
                                tint = if (messagesSelected) themeColor else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "消息",
                                fontSize = 10.sp,
                                color = if (messagesSelected) themeColor else Color.Gray
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = NoIndication
                                ) { navigateTo("profile") },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                if (profileSelected) Icons.Default.Person else Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = if (profileSelected) themeColor else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "我的",
                                fontSize = 10.sp,
                                color = if (profileSelected) themeColor else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "feed",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable("feed") {
                    FeedScreen(
                        viewModel = itemViewModel,
                        onItemClick = { itemId -> rootNavController.navigate("item_detail/$itemId") }
                    )
                }
                composable("nearby") {
                    NearbyScreen(
                        viewModel = itemViewModel,
                        onItemClick = { itemId: String -> rootNavController.navigate("item_detail/$itemId") }
                    )
                }
                composable("messages") {
                    val currentUser by authViewModel.currentUser.collectAsState()
                    MessageScreen(
                        currentUser = currentUser,
                        onChatClick = { chatId -> rootNavController.navigate("chat_detail/$chatId") },
                        onShowHUD = { showHUD(it) }
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        itemViewModel = itemViewModel,
                        onLogout = { 
                            rootNavController.navigate("login") { 
                                popUpTo(0) { inclusive = true } 
                            } 
                        },
                        onActionClick = { action ->
                            if (action == "我收到的") {
                                rootNavController.navigate("requested_items")
                            } else if (action == "我收藏的") {
                                rootNavController.navigate("favorite_items")
                            } else if (action == "我发布的") {
                                rootNavController.navigate("my_items")
                            } else {
                                rootNavController.navigate("profile_action/$action")
                            }
                        }
                    )
                }
            }
        }
    }
}
