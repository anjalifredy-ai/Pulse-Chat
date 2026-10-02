package com.pulsechat.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulsechat.app.ui.auth.AuthViewModel
import com.pulsechat.app.ui.auth.LoginScreen
import com.pulsechat.app.ui.auth.ProfileSetupScreen
import com.pulsechat.app.ui.auth.SplashScreen
import com.pulsechat.app.ui.call.ActiveCallScreen
import com.pulsechat.app.ui.call.OutgoingCallScreen
import com.pulsechat.app.ui.chat.ChatScreen
import com.pulsechat.app.ui.contacts.NewChatScreen
import com.pulsechat.app.ui.contacts.NewChatViewModel
import com.pulsechat.app.ui.groups.CreateGroupScreen
import com.pulsechat.app.ui.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun PulseNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.isAuthenticated, authState.isLoading) {
        if (!authState.isLoading && !authState.isAuthenticated) {
            val current = navController.currentDestination?.route
            if (current != Routes.LOGIN && current != Routes.SPLASH) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val startDestination = when {
        authState.isLoading -> Routes.SPLASH
        authState.isAuthenticated && authState.hasProfile -> Routes.HOME
        authState.isAuthenticated && !authState.hasProfile -> Routes.PROFILE_SETUP
        else -> Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onAuthenticated = { hasProfile ->
                    val dest = if (hasProfile) Routes.HOME else Routes.PROFILE_SETUP
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenChat = { conversationId ->
                    navController.navigate(Routes.chat(conversationId))
                },
                onNewChat = { navController.navigate(Routes.NEW_CHAT) },
                onSettings = { }
            )
        }

        composable(Routes.NEW_CHAT) {
            val viewModel: NewChatViewModel = hiltViewModel()
            val scope = rememberCoroutineScope()
            NewChatScreen(
                onBack = { navController.popBackStack() },
                onUserSelected = { userId ->
                    scope.launch {
                        val result = viewModel.startChatWith(userId)
                        result.onSuccess { convId ->
                            navController.navigate(Routes.chat(convId)) {
                                popUpTo(Routes.HOME)
                            }
                        }
                    }
                },
                onNewGroup = { navController.navigate(Routes.NEW_GROUP) },
                onMessageYourself = {
                    scope.launch {
                        val result = viewModel.startSelfChat()
                        result.onSuccess { convId ->
                            navController.navigate(Routes.chat(convId)) {
                                popUpTo(Routes.HOME)
                            }
                        }
                    }
                }
            )
        }

        composable(Routes.NEW_GROUP) {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onCreated = { convId ->
                    navController.navigate(Routes.chat(convId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() },
                onOpenContact = { },
                onVoiceCall = {
                    navController.navigate("outgoing_call/voice/$conversationId")
                },
                onVideoCall = {
                    navController.navigate("outgoing_call/video/$conversationId")
                }
            )
        }

        composable(
            route = "outgoing_call/{type}/{conversationId}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("conversationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "voice"
            OutgoingCallScreen(
                name = authState.displayName.ifBlank { "Pulse Contact" },
                photoUrl = authState.photoUrl,
                isVideo = type == "video",
                onEnd = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CALL,
            arguments = listOf(navArgument("callId") { type = NavType.StringType })
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            ActiveCallScreen(
                callId = callId,
                onEnd = { navController.popBackStack() }
            )
        }
    }
}
