package com.step_count_app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * 데이터 변경 리스너 등록
 *
 *
 */


class StepCounterService : Service() {

    private var currentCount = 0

    private val manager = MainApplication.getInstance()
        .getSystemService(NOTIFICATION_SERVICE) as NotificationManager


    var notification: Notification? = null

    /**
     * lastSystemStepCount가 currentStepCount 값보다 작으면 reboot으로 인한 초기화임을 인지하고
     * lastSystemStepCount을 0으로 세팅함과 동시에 currentStepCount값을 당일 데이터로 추가시킨다.
     *
     */

    private val stepCounter =
        StepCounter(MainApplication.getInstance(), object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val currentStepCount = event.values[0].toInt()

                val lastSystemCount =
                    StepCounterPreference.instance().getLastSystemCountByPreference()

                if(lastSystemCount == 0) {
                    StepCounterPreference.instance().setLastSystemCount(currentStepCount)
                    return
                }

                val plusCount = currentStepCount - lastSystemCount

                if (plusCount > 0) {
                    StepCounterPreference.instance().plusCountsForDay(plusCount)
                }

                currentCount = StepCounterPreference.instance().getTodayCountByPreference()

                Toast.makeText(
                    MainApplication.getInstance(),
                    "$currentStepCount",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TEST", "TEST $currentStepCount")


                StepCounterPreference.instance().setLastSystemCount(currentStepCount)


                notification = notiBuilder()
                manager.notify(100, notification)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        })

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        currentCount = StepCounterPreference.instance().getTodayCountByPreference()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "test",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "test"
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            val manager = MainApplication.getInstance()
                .getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            // id must not be 0
            startForeground(100, notiBuilder())
        }


        return START_STICKY
    }

    override fun onCreate() {
        stepCounter.registerStepCounter()
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d("TEST", "onDestroy")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        stepCounter.unregisterStepCounter()
        super.onDestroy()
    }

    private val notiIntent: PendingIntent
        get() {
            val notificationIntent = Intent(this, MainActivity::class.java)
            return PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }


    private fun notiBuilder(): Notification {
        return NotificationCompat.Builder(MainApplication.getInstance(), CHANNEL_ID)
            .setContentTitle("올웨이즈 만보기")
            .setContentText("currentCount : $currentCount")
            .setContentIntent(notiIntent)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    /**
     * 종종 앱 입장 시 포그라운드 서비스를 RESTART 해주세요.
     * 차후 메모리릭 현상으로 종종 종료된다면 onTrimMemory 를 사용해 주세요.
     * Service can be removed or detached when memory is leaked
     */


    override fun onLowMemory() {
        Log.d("TEST", "onLowMemory")
        super.onLowMemory()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        private const val CHANNEL_ID = "step_count_noti"
    }
}