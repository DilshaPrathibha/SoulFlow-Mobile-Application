package com.example.SoulFlow.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager as AndroidSensorManager
import kotlin.math.sqrt

/**
 * Manages device sensors for shake detection
 */
class SensorManager(private val context: Context) : SensorEventListener {
    
    private val sensorManager: AndroidSensorManager? = context.getSystemService(Context.SENSOR_SERVICE) as? AndroidSensorManager
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var onShakeDetected: (() -> Unit)? = null
    
    // Shake detection variables
    private var lastUpdate = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val shakeThreshold = 800 // Adjust this value to make shake detection more or less sensitive
    
    companion object {
        private const val SHAKE_DETECTION_TIME_LAPSE = 100
    }
    
    fun setOnShakeDetectedListener(listener: () -> Unit) {
        onShakeDetected = listener
    }
    
    fun startListening() {
        // Register accelerometer for shake detection
        accelerometerSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, AndroidSensorManager.SENSOR_DELAY_GAME)
        }
    }
    
    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(it)
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used for accelerometer
    }
    
    private fun handleAccelerometer(event: SensorEvent) {
        // Ensure we have valid sensor data
        if (event.values.size < 3) return
        val currentTime = System.currentTimeMillis()
        
        if ((currentTime - lastUpdate) > SHAKE_DETECTION_TIME_LAPSE) {
            val timeDiff = currentTime - lastUpdate
            lastUpdate = currentTime
            
            val x = event.values.getOrNull(0) ?: 0f
            val y = event.values.getOrNull(1) ?: 0f
            val z = event.values.getOrNull(2) ?: 0f
            
            // Calculate speed with proper type handling and division by zero protection
            val speed = if (timeDiff > 0) {
                val deltaX = (x - lastX).toDouble()
                val deltaY = (y - lastY).toDouble()
                val deltaZ = (z - lastZ).toDouble()
                sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) / timeDiff * 10000
            } else {
                0.0
            }
            
            if (speed > shakeThreshold) {
                onShakeDetected?.invoke()
            }
            
            lastX = x
            lastY = y
            lastZ = z
        }
    }
    
    fun isAccelerometerAvailable(): Boolean {
        return accelerometerSensor != null
    }
}