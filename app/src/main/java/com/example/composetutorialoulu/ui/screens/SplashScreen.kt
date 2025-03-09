package com.example.composetutorialoulu.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.composetutorialoulu.R
import com.example.composetutorialoulu.models.Routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    // Animation states
    val scale = remember { Animatable(0.0f) }
    val alpha = remember { Animatable(0f) }
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // First animation: Scale up and fade in
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 800,
                easing = EaseOutBack
            )
        )

        // Second animation: Fade in text
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = LinearEasing
            )
        )

        // Third animation: Rotate the logo slightly
        rotationAngle.animateTo(
            targetValue = 360f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseInOutQuad
            )
        )

        // Delay before navigating to main screen
        delay(800)
        navController.navigate(Routes.MAIN_VIEW) {
            popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo - Replace R.drawable.app_logo with your actual logo resource
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale.value)
                    .fillMaxWidth(0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "Mobile Computing Final Project",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alpha.value)
                    .padding(top = 16.dp)
            )

            // App Tagline
            Text(
                text = "Welcome to the app",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alpha.value)
                    .padding(top = 8.dp)
            )
        }
    }
}