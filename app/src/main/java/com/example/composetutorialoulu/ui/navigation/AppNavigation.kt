// ui/navigation/AppNavigation.kt
package com.example.composetutorialoulu.ui.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.example.composetutorialoulu.viewmodel.SensorViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.composetutorialoulu.data.UserRepository
import com.example.composetutorialoulu.models.Message
import com.example.composetutorialoulu.models.Routes
import com.example.composetutorialoulu.ui.components.Conversation
import com.example.composetutorialoulu.ui.components.IconButton
import com.example.composetutorialoulu.ui.screens.SplashScreen
import com.example.composetutorialoulu.viewmodel.UserViewModel
import java.io.File
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun AppNavigation(sensorViewModel: SensorViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val messages =  SampleData.conversationSample
    val userRepository = UserRepository(context)
    val viewModel = UserViewModel(userRepository)

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_SCREEN
    ) {
        composable(Routes.SPLASH_SCREEN) {
            SplashScreen(navController)
        }
        composable(Routes.MAIN_VIEW) {
            MainView(navController, messages, viewModel)
        }
        composable(Routes.SECOND_VIEW) {
            SecondView(navController, viewModel, sensorViewModel)
        }
    }
}

@Composable
fun MainView(
    navController: NavHostController,
    messages: List<Message>,
    viewModel: UserViewModel
) {
    Column {
        IconButton(
            onClick = { navController.navigate(Routes.SECOND_VIEW) },
            icon = Icons.Default.Settings,
            alignment = Alignment.CenterEnd,
            padding = 5.dp
        )
        Conversation(messages, viewModel)
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun SecondView(
    navController: NavHostController,
    viewModel: UserViewModel,
    sensorViewModel: SensorViewModel
) {
    val username by viewModel.username.collectAsState(initial = "")
    var newName by remember { mutableStateOf(username) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.saveProfileImageUri(it)
            }
        }
    )
    val profileImage by viewModel.profileImageUri.collectAsState(initial = "")

    val gyroX by sensorViewModel.gyroscopeX.collectAsState()
    val gyroY by sensorViewModel.gyroscopeY.collectAsState()
    val gyroZ by sensorViewModel.gyroscopeZ.collectAsState()

    val context = LocalContext.current

    val savedImageUriString by viewModel.cameraImageUri.collectAsState(initial = "")

    val savedImageUri = if (savedImageUriString.isNotEmpty()) savedImageUriString.toUri() else null

    fun createImageFile(context: Context): Uri {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(storageDir, "JPEG_${timestamp}.jpg")
        file.createNewFile()
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    var currentCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentCaptureUri?.let { viewModel.saveCameraImageUri(it) }
        } else {
            currentCaptureUri = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentCaptureUri = createImageFile(context)
            cameraLauncher.launch(currentCaptureUri)
        } else {
            Toast.makeText(
                context,
                "Needs permissions for camera",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    // Always resume tracking for gyroscope
    sensorViewModel.resumeTracking(context)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Add scrolling to ensure all content is visible
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        navController.navigate(Routes.MAIN_VIEW) {
                            popUpTo(Routes.MAIN_VIEW) { inclusive = true }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            Text(
                text = "User Profile",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (profileImage.isNotEmpty()){
                    Image(
                        painter = rememberAsyncImagePainter(File(profileImage)),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Enter new name") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.saveUsername(newName) }) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Gyroscope readings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Gyroscope Readings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "X-axis: ${String.format("%.4f", gyroX)} rad/s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Y-axis: ${String.format("%.4f", gyroY)} rad/s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Z-axis: ${String.format("%.4f", gyroZ)} rad/s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Captured image display
            if (savedImageUri != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Last Picture",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val imageRequest = ImageRequest.Builder(context)
                        .data(savedImageUri)
                        .crossfade(true)
                        .build()

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = imageRequest
                        ),
                        contentDescription = "Captured Image Preview",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Button(
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    )

                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        currentCaptureUri = createImageFile(context)
                        cameraLauncher.launch(currentCaptureUri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Camera Icon"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Picture")
            }
        }
    }
}