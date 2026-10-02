package com.pulsechat.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.ui.navigation.Routes
import com.pulsechat.app.ui.theme.PulseBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            delay(800)
            val route = when {
                state.isAuthenticated && state.hasProfile -> Routes.HOME
                state.isAuthenticated -> Routes.PROFILE_SETUP
                else -> Routes.LOGIN
            }
            onNavigate(route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pulse",
            color = PulseBlue,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
