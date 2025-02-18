// File: workers/WeatherWorker.kt
package com.example.composetutorialoulu.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.composetutorialoulu.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val apiUrl =
            "https://api.open-meteo.com/v1/forecast?latitude=65.0121&longitude=25.4681&current_weather=true"
        return withContext(Dispatchers.IO) {
            try {
                Log.d("WeatherWorker", "Consultando la API: $apiUrl")
                val response = URL(apiUrl).readText()
                val jsonObj = JSONObject(response)
                val currentWeather = jsonObj.getJSONObject("current_weather")
                val temperature = currentWeather.getDouble("temperature")
                val windSpeed = currentWeather.getDouble("windspeed")

                val title = "Weather Update for Oulu"
                val message = "Temperature: $temperature °C, Wind Speed: $windSpeed km/h"

                NotificationHelper.sendNotification(applicationContext, title, message)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.retry()
            }
        }
    }
}
