// ui/navigation/AppNavigation.kt
package com.example.composetutorialoulu.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.composetutorialoulu.data.UserRepository
import com.example.composetutorialoulu.models.Message
import com.example.composetutorialoulu.models.Routes
import com.example.composetutorialoulu.ui.components.Conversation
import com.example.composetutorialoulu.ui.components.IconButton
import com.example.composetutorialoulu.viewmodel.UserViewModel
import java.io.File

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val messages =  SampleData.conversationSample
    val userRepository = UserRepository(context)
    val viewModel = UserViewModel(userRepository)

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_VIEW
    ) {
        composable(Routes.MAIN_VIEW) {
            MainView(navController, messages, viewModel)
        }
        composable(Routes.SECOND_VIEW) {
            SecondView(navController, viewModel)
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

@Composable
fun SecondView(
    navController: NavHostController,
    viewModel: UserViewModel
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = {
                    navController.navigate(Routes.MAIN_VIEW) {
                        popUpTo(Routes.MAIN_VIEW) { inclusive = true }
                    }
                },
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                alignment = Alignment.CenterStart,
                padding = 0.dp
            )

            Spacer(modifier = Modifier.height(16.dp))
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
        }
    }
}
