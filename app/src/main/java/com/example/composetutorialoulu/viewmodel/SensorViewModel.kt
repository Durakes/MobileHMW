package com.example.composetutorialoulu.viewmodel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorViewModel : ViewModel(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var gyroscopeSensor: Sensor? = null

    // StateFlow to hold gyroscope values
    private val _gyroscopeX = MutableStateFlow(0f)
    val gyroscopeX: StateFlow<Float> = _gyroscopeX

    private val _gyroscopeY = MutableStateFlow(0f)
    val gyroscopeY: StateFlow<Float> = _gyroscopeY

    private val _gyroscopeZ = MutableStateFlow(0f)
    val gyroscopeZ: StateFlow<Float> = _gyroscopeZ

    fun resumeTracking(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        gyroscopeSensor?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun pauseTracking() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            _gyroscopeX.value = event.values[0]
            _gyroscopeY.value = event.values[1]
            _gyroscopeZ.value = event.values[2]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onCleared() {
        super.onCleared()
        pauseTracking()
    }
}