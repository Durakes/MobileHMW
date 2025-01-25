package com.example.composetutorialoulu

import SampleData
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.composetutorialoulu.ui.theme.ComposeTutorialOuluTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

data class Message(val author: String, val body: String)

object Routes{
    const val MAIN_VIEW = "main_view"
    const val SECOND_VIEW = "second_view"
}

@Composable
fun Conversation(messages: List<Message>){
    LazyColumn {
        items(messages){ message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewConversation(){
    ComposeTutorialOuluTheme {
        Conversation(SampleData.conversationSample)
    }
}

@Composable
fun MessageCard(msg: Message) {
    // FOR PADDING
    Row(modifier = Modifier.padding(all = 8.dp)){
        Image(
            painter = painterResource(R.drawable.profile_picture),
            contentDescription = "Contact profile picture",
            // DP for Image size and clip for shape
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )

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
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
                )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ){
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // Check state
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
        Surface {
            MessageCard(
                msg = Message("Lexi", "Hey, take a look at Jetpack Compose, it's great!")
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
    val messages = SampleData.conversationSample
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN_VIEW
    ){
        composable(Routes.MAIN_VIEW){
            MainView(navController, messages)
        }
        composable(Routes.SECOND_VIEW){
            SecondView(navController)
        }
    }

}

@Composable
fun MainView(navController: NavHostController, messages: List<Message>) {
    Column {
        IconButton({ navController.navigate(Routes.SECOND_VIEW) },
            Icons.Default.Settings,
            Alignment.CenterEnd,
            5.dp)
        Conversation(messages)
    }
}

@Composable
fun SecondView(navController: NavHostController) {
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
                    .background(Color.Gray),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.profile_picture),
                    contentDescription = "Contact profile picture",
                    // DP for Image size and clip for shape
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )

            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "User Name",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))
            // TODO Later
            OutlinedTextField(
                value = "",
                onValueChange = {}, // Not implemented yet
                label = { Text("Enter new name") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}