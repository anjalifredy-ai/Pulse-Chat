package com.pulsechat.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulsechat.app.ui.auth.AuthViewModel
import com.pulsechat.app.ui.auth.OtpScreen
import com.pulsechat.app.ui.auth.PhoneAuthScreen
import com.pulsechat.app.ui.auth.ProfileSetupScreen
import com.pulsechat.app.ui.auth.SplashScreen
import com.pulsechat.app.ui.chat.ChatScreen
import com.pulsechat.app.ui.contacts.NewChatScreen
import com.pulsechat.app.ui.home.HomeScreen
import com.pulsechat.app.data.repository.ChatRepository
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PulseNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    val startDestination = when {
        authState.isLoading -> Routes.SPLASH
        authState.isAuthenticated && authState.hasProfile -> Routes.HOME
        authState.isAuthenticated && !authState.hasProfile -> Routes.PROFILE_SETUP
        else -> Routes.PHONE_AUTH
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

        composable(Routes.PHONE_AUTH) {
            PhoneAuthScreen(
                onCodeSent = { verificationId, phone ->
                    navController.navigate(Routes.otp(verificationId, phone))
                }
            )
        }

        composable(
            route = Routes.OTP,
            arguments = listOf(
                navArgument("verificationId") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                verificationId = verificationId,
                phoneNumber = phoneNumber,
                onVerified = {
                    navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(Routes.PHONE_AUTH) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
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
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.NEW_CHAT) {
            val chatRepo: ChatRepository = androidx.hilt.navigation.HiltViewModelFactory
                .let { /* use ViewModel below */ null } ?: return@composable
            // Use a small ViewModel-free approach via callback
            NewChatScreen(
                onBack = { navController.popBackStack() },
                onUserSelected = { userId ->
                    // Conversation creation handled in a dedicated small helper screen flow
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("start_chat_user", userId)
                    navController.popBackStack()
                },
                onNewGroup = {
                    // Group creation UI can be expanded later
                    navController.popBackStack()
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
                onOpenContact = { userId ->
                    navController.navigate(Routes.contactInfo(userId))
                }
            )
        }
    }
}
