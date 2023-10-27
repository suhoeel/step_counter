package com.stepCounter

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.step_count_app.BackupWorker
import com.step_count_app.StepCounterPreference
import com.step_count_app.StepCounterService
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class StepCounterModule(val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    var test = false

    override fun getName() = "StepCounterModule"

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private val periodicWorkRequest =
        PeriodicWorkRequestBuilder<BackupWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

    @ReactMethod
    fun getData(from: Double, to: Double, promise: Promise) {

        if (test) {
            reactContext.stopService(Intent(reactContext, StepCounterService::class.java))
            test = false
            StepCounterPreference.instance().setRebootAvailable(false)
        } else {
            if (!isStepCounterAvailable()) return
            val intent = Intent(reactContext, StepCounterService::class.java)

            if (!foregroundServiceRunning()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    reactContext.startForegroundService(intent)
                } else {
                    reactContext.startService(intent)
                }
            }
            test = true
            StepCounterPreference.instance().setRebootAvailable(true)
        }

        /*val countArray = StepCounterPreference.instance()
            .getSpecificDayCountByPreference(from.toLong() / 1000, to.toLong() / 1000)

        val countAll = countArray.sum()

        val data = Arguments.createMap().apply {
            putInt("count", countAll)
        }


        promise.resolve(data)*/
    }

    /**
     *
     *  꼭 Permission 2개 승인 이후 테스트 해주세요.
     *
     */
    @ReactMethod
    fun start(from: Double) {

        val today = Timestamp(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(today)

        StepCounterPreference.instance()
            .registerSharedPreferenceChangedListener { sharedPreferences, key ->
                if (key == todayDate) {
                    val count = StepCounterPreference.instance().getTodayCountByPreference()
                    val data = ChangedStepCountData(count, today.time.toDouble())
                    changedStepCountCall(data)
                }
            }


        WorkManager.getInstance(reactContext).enqueueUniquePeriodicWork(
            "test",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

    }

    /**
     * getRunningServices() Deprecated 아님. 써드파티 앱에서 실행 외 정상 동작.
     */
    private fun foregroundServiceRunning(): Boolean {
        val activityManager =
            reactContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (StepCounterService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    @ReactMethod
    fun stop() {
//        reactContext.stopService(Intent(reactContext, StepCounterService::class.java))


        StepCounterPreference.instance().unregisterSharedPreferenceChangedListener()
        WorkManager.getInstance(reactContext).cancelAllWork()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission() {
        val currentActivity = reactContext.currentActivity ?: return

        if (ContextCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {

            currentActivity.requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                101
            )

            /*if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.POST_NOTIFICATIONS)) {

            }*/
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestActivityRecognitionPermission() {
        val currentActivity = reactContext.currentActivity ?: return

        if (ContextCompat.checkSelfPermission(
                reactContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            currentActivity.requestPermissions(
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                102
            )

            /*if (ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.ACTIVITY_RECOGNITION)) {

            }*/
        }
    }

    private fun changedStepCountCall(data: ChangedStepCountData) {
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("changed_step_count", data.getParam())
    }

    fun isStepCounterAvailable(): Boolean {
        val sensorManager = reactContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        return (stepCounterSensor != null)
    }
}

data class ChangedStepCountData(val count: Int, val date: Double) {
    fun getParam() = WritableNativeMap().apply {
        putInt("count", count)
        putDouble("date", date)
    }
}