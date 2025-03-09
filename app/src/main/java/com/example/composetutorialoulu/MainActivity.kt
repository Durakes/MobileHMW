// MainActivity.kt
package com.example.composetutorialoulu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.composetutorialoulu.notifications.NotificationHelper
import com.example.composetutorialoulu.ui.navigation.AppNavigation
import com.example.composetutorialoulu.viewmodel.SensorViewModel
import com.example.composetutorialoulu.workers.WeatherWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val sensorViewModel: SensorViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)

        checkPermissions()

        val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_work",
            ExistingPeriodicWorkPolicy.KEEP,
            weatherWorkRequest
        )

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<WeatherWorker>()
            .setInitialDelay(20, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)

        setContent {
            AppNavigation(sensorViewModel = sensorViewModel)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
            NotificationHelper.sendNotification(
                this,
                "Notificaciones activadas",
                "Las notificaciones han sido activadas correctamente."
            )
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Permiso de notificaciones (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Permiso de reconocimiento de actividad (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // Lanzar solicitud combinada
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
