package com.sham.neopad.misc


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager


class OrientationSensorManager(
    context: Context,
    private val onOrientationChanged: (azimuth: Float, pitch: Float, roll: Float) -> Unit
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var isUsingRotationVector = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR -> {
                    if (isUsingRotationVector) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)

                        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                        val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                        onOrientationChanged(azimuth, pitch, roll)
                    }
                }

                // 2. Fallback: Accelerometer + Magnetic Field
                Sensor.TYPE_ACCELEROMETER -> {
                    if (!isUsingRotationVector) {
                        System.arraycopy(event.values, 0, mGravity, 0, 3)
                        calculateFallbackOrientation()
                    }
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    if (!isUsingRotationVector) {
                        System.arraycopy(event.values, 0, mGeomagnetic, 0, 3)
                        calculateFallbackOrientation()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        private fun calculateFallbackOrientation() {
            // R (Rotation Matrix) is the matrix that transforms vector from sensor coordinate system to world coordinate system
            val success = SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mGeomagnetic)

            if (success) {
                // orientationAngles[0] = Azimuth (Z-axis)
                // orientationAngles[1] = Pitch (X-axis)
                // orientationAngles[2] = Roll (Y-axis)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                // Convert radians to degrees
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                onOrientationChanged(azimuth, pitch, roll)
            }
        }
    }

    fun start() {
        // 1. Try Rotation Vector (best)
        var sensorRV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensorRV == null) {
            sensorRV = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        }

        if (sensorRV != null) {
            // Use the rotation vector sensor
            isUsingRotationVector = true
            sensorManager.registerListener(sensorListener, sensorRV, SensorManager.SENSOR_DELAY_GAME)
        } else {
            // 2. Fallback: Use Accelerometer and Magnetic Field
            isUsingRotationVector = false

            val sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            if (sensorAccel != null && sensorMag != null) {
                sensorManager.registerListener(sensorListener, sensorAccel, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(sensorListener, sensorMag, SensorManager.SENSOR_DELAY_GAME)
            } else {
                // If neither combination is available, log or notify failure
                // In a real app, you might provide a visual message to the user here.
                println("Error: Required orientation sensors not available on this device.")
            }
        }
    }

    /**
     * Stops listening to all registered sensors.
     */
    fun stop() {
        sensorManager.unregisterListener(sensorListener)
    }
}
