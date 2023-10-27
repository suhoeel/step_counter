package com.step_count_app

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class BackupWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val url = URL("url")
        val urlConnection = url.openConnection() as HttpURLConnection
        try {
            val stream: InputStream = BufferedInputStream(urlConnection.inputStream)
            val res = stream.bufferedReader().use { it.readText() }
            Log.d("TEST", "res Code ${urlConnection.responseCode}")
            Log.d("TEST", "res Data $res")
            if(urlConnection.responseCode != 200) {
                return Result.retry()
            }
        } finally {
            urlConnection.disconnect()
        }

        return Result.success()
    }

}