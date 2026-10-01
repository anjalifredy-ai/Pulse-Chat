package com.pulsechat.app.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulsechat.app.R

@Composable
fun PhoneAuthScreen(
    onCodeSent: (verificationId: String, phone: String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(state.otpSent, state.verificationId) {
        if (state.otpSent && state.verificationId != null) {
            onCodeSent(state.verificationId!!, state.phoneNumber)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = countryCode,
            onValueChange = { countryCode = it },
            label = { Text("Country code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() } },
            label = { Text(stringResource(R.string.phone_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        state.error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val full = "$countryCode$phone"
                    if (activity != null && phone.length >= 7) {
                        viewModel.sendOtp(full, activity)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phone.length >= 7
            ) {
                Text(stringResource(R.string.continue_btn))
            }
        }
    }
}
