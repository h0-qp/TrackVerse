package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.viewmodel.AuthViewModel

import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun ProfileScreen(navController: NavController? = null, viewModel: AuthViewModel = viewModel()) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BlueHighlight)
                return@Column
            }

            if (user != null) {
                // Logged in UI
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .border(2.dp, BorderStroke, CircleShape)
                ) {
                    AsyncImage(
                        model = user?.photoUrl ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${user?.displayName}",
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = user?.displayName ?: stringResource(R.string.guest_user),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = user?.email ?: stringResource(R.string.anonymous),
                    fontSize = 14.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.signOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(stringResource(R.string.sign_out), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                // Logged out UI
                Text(stringResource(R.string.app_name), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BlueHighlight, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.sign_in_to_sync), fontSize = 16.sp, color = TextTertiary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearError() },
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.clearError() },
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.signInWithEmail(email, password) },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.login_register_email), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.or), fontSize = 14.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { launcher.launch(viewModel.getGoogleSignInIntent()) },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueHighlight, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.sign_in_google), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { viewModel.signInAsGuest() }) {
                    Text(stringResource(R.string.continue_guest), color = TextSecondary, fontSize = 14.sp)
                }

                if (error != null) {
                    Text(text = error!!, color = ErrorColor, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
        
        // Settings Gear Icon
        if (user != null) {
            IconButton(
                onClick = { navController?.navigate("settings") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary
                )
            }
        }
    }
}
