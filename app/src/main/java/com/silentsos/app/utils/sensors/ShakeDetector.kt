package com.silentsos.app.utils.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    // Configurable sensitivity (0-100, default 65)
    var sensitivity: Int = 65
        set(value) {
            field = value.coerceIn(0, 100)
        }

    // Threshold calculated from sensitivity: lower sensitivity = higher threshold
    private val shakeThreshold: Float
        get() = 800f + (100 - sensitivity) * 20f // Range: 800 - 2800

    private var shakeCount = 0
    private var lastShakeTime: Long = 0
    private val requiredShakes = 3
    private val shakeWindowMs = 2000L

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)
                ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else {
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastUpdate

        if (timeDiff < 100) return

        lastUpdate = currentTime

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val speed = sqrt(
            ((x - lastX) * (x - lastX) +
            (y - lastY) * (y - lastY) +
            (z - lastZ) * (z - lastZ)).toDouble()
        ) / timeDiff * 10000

        if (speed > shakeThreshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > shakeWindowMs) {
                shakeCount = 0
            }
            shakeCount++
            lastShakeTime = now

            if (shakeCount >= requiredShakes) {
                shakeCount = 0
                onShakeDetected()
            }
        }

        lastX = x
        lastY = y
        lastZ = z
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
