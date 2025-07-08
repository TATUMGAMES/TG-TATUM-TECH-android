package com.tatumgames.tatumtech.android.ui.components.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasCameraPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (!hasCameraPermission) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            hasCameraPermission = true
        }
    }

    Scaffold(
        topBar = {
            Header(text = "Scanner", onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = Color(0xFF000000)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                CameraPreviewWithControls(
                    onSwitchCamera = {
                        // TODO: Implement camera switching
                    },
                    onCapture = {
                        // TODO: Capture image and scan QR code
                        // On successful scan, update My Timeline
                    }
                )
            } else {
                Text("Camera permission required", color = Color.White)
            }
        }
    }
}

@Composable
fun CameraPreviewWithControls(
    onSwitchCamera: () -> Unit,
    onCapture: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Placeholder for camera preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera Preview", color = Color.White)
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onSwitchCamera) {
                Image(
                    painter = painterResource(id = R.drawable.forward_arrow),
                    contentDescription = "Switch Camera",
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = onCapture) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Capture",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
    // TODO: Integrate QR code scanning and image capture logic
    // On successful scan, call a callback to update My Timeline
} 
