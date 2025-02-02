package com.example.composetutorialoulu

import SampleData
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import com.example.composetutorialoulu.ui.theme.ComposeTutorialOuluTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserRepository(private val context: Context) {
    private val usernameKey = stringPreferencesKey("username")
    private val profileImageUriKey = stringPreferencesKey("profile_image_uri")

    val usernameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[usernameKey] ?: ""
    }

    val profileImageUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[profileImageUriKey] ?: ""
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[usernameKey] = username
        }
    }

    private suspend fun saveProfileImageUri(uri: String){
        context.dataStore.edit { prefs ->
            prefs[profileImageUriKey] = uri
        }
    }

    suspend fun saveProfileImage(uri: Uri) {
        val file = saveImageToInternalStorage(uri)
        saveProfileImageUri(file.absolutePath)
    }

    private fun saveImageToInternalStorage(uri: Uri): File {
        val file = File(context.filesDir, "profile_image.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

}

// ViewModel to manage UI state
class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _username = MutableStateFlow("Username")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _profileImageUri = MutableStateFlow("")
    val profileImageUri: StateFlow<String> = _profileImageUri.asStateFlow()

    init {
        viewModelScope.launch {
            repository.usernameFlow.collect { name ->
                _username.value = name.ifEmpty { "Username" }
            }
        }
        viewModelScope.launch {
            repository.profileImageUriFlow.collect { uri ->
                _profileImageUri.value = uri
            }
        }
    }

    fun saveUsername(username: String) {
        viewModelScope.launch {
            repository.saveUsername(username)
        }
    }

    fun saveProfileImageUri(uri: Uri){
        viewModelScope.launch {
            repository.saveProfileImage(uri)
        }
    }
}

data class Message(val author: String, val body: String)

object Routes{
    const val MAIN_VIEW = "main_view"
    const val SECOND_VIEW = "second_view"
}

@Composable
fun Conversation(messages: List<Message>, viewModel: UserViewModel){
    LazyColumn {
        items(messages){ message ->
            MessageCard(message, viewModel)
        }
    }
}

@Preview
@Composable
fun PreviewConversation(){
    ComposeTutorialOuluTheme {
        val context = LocalContext.current
        val userRepository = remember { UserRepository(context) }
        val viewModel = remember { UserViewModel(userRepository) }
        Conversation(SampleData.conversationSample, viewModel)
    }
}

@Composable
fun MessageCard(msg: Message, viewModel: UserViewModel) {
    val username by viewModel.username.collectAsState(initial = "User")
    val profileImage by viewModel.profileImageUri.collectAsState(initial = "")
    // FOR PADDING
    Row(modifier = Modifier.padding(all = 8.dp)){
        if (profileImage.isNotEmpty()){
            Image(
                painter = rememberAsyncImagePainter(File(profileImage)),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .border(1.5.dp,MaterialTheme.colorScheme.primary, CircleShape),
                tint = Color.White
            )
        }

        // Space between image and column
        Spacer(modifier = Modifier.width(8.dp))

        // State of the message
        var isExpanded by remember { mutableStateOf(false) }

        // update Color in message
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            label = "",
        )

        // Toggle when click
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = username,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
                )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ){
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    ComposeTutorialOuluTheme {
        val context = LocalContext.current
        val userRepository = remember { UserRepository(context) }
        val viewModel = remember { UserViewModel(userRepository) }
        Surface {
            MessageCard(
                msg = Message("Lexi", "Hey, take a look at Jetpack Compose, it's great!"),
                viewModel
            )
        }
    }

}

@Composable
fun IconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    alignment: Alignment,
    padding: Dp
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = padding),
        contentAlignment = alignment,
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(30.dp),
            contentPadding = PaddingValues(3.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Options",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    val context = LocalContext.current
    val messages = SampleData.conversationSample
    val userRepository = remember { UserRepository(context) }
    val viewModel = remember { UserViewModel(userRepository) }
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_VIEW
    ){
        composable(Routes.MAIN_VIEW){
            MainView(navController, messages, viewModel)
        }
        composable(Routes.SECOND_VIEW){
            SecondView(navController, viewModel)
        }
    }

}

@Composable
fun MainView(navController: NavHostController, messages: List<Message>, viewModel: UserViewModel) {
    Column {
        IconButton({ navController.navigate(Routes.SECOND_VIEW) },
            Icons.Default.Settings,
            Alignment.CenterEnd,
            5.dp)
        Conversation(messages, viewModel)
    }
}

@Composable
fun SecondView(navController: NavHostController, viewModel: UserViewModel) {
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
                0.dp
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
                }else {
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
                onValueChange = {
                    newName = it
                },
                label = { Text("Enter new name") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { viewModel.saveUsername(newName) }) {
                Text("Save")
            }
        }
    }
}