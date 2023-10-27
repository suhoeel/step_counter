package com.step_count_app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Sensor.TYPE_STEP_DETECTOR : 걸음 수마다 1씩 리턴
 * @link https://developer.android.com/reference/android/hardware/Sensor#TYPE_STEP_DETECTOR
 */
class StepCounter(
    context: Context,
    private val sensorEventListener: SensorEventListener
) {
    private val sensorManager: SensorManager
    private val stepSensor: Sensor

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    fun registerStepCounter() {
        sensorManager.registerListener(
            sensorEventListener,
            stepSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    fun unregisterStepCounter() {
        sensorManager.unregisterListener(sensorEventListener, stepSensor)
    }
}