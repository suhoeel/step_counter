package com.step_count_app

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepCounterPreference(context: Context) {

    private val prefs: SharedPreferences
    private val prefsEditor: SharedPreferences.Editor
    private var sharedPreferenceChangeListener: OnSharedPreferenceChangeListener? = null

    init {
        prefs = context.getSharedPreferences(STEP_COUNTER_PREFERENCE, Context.MODE_PRIVATE)
        prefsEditor = prefs.edit()
    }

    /**
     * 데이터 변경 리스너 등록
     */
    fun registerSharedPreferenceChangedListener(
        sharedPreferenceChangeListener: OnSharedPreferenceChangeListener
    ) {
        this.sharedPreferenceChangeListener = sharedPreferenceChangeListener
        prefs.registerOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener)
    }

    /**
     * 데이터 변경 리스너 해지
     */
    fun unregisterSharedPreferenceChangedListener() {
        prefs.unregisterOnSharedPreferenceChangeListener(this.sharedPreferenceChangeListener)
    }

    /**
     * 당일 데이터
     */
    fun getTodayCountByPreference(): Int {
        val timestamp = Timestamp(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(timestamp)
        return prefs.getInt(date, 0)
    }

    /**
     * 특정 날짜 데이터
     */
    fun getSpecificDayCountByPreference(time: Long): Int {
        val timestamp = Timestamp(time)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(timestamp)
        return prefs.getInt(date, 0)
    }

    /**
     * 특정 날짜 데이터
     * FORMAT yyyy-MM-dd
     */
    fun getSpecificDayCountByPreference(date: String): Int {
        return prefs.getInt(date, 0)
    }


    /**
     * 특정 날짜 데이터 from to
     */

    fun getSpecificDayCountByPreference(startDate: Long, endDate: Long): IntArray {
        val totalDays = ((endDate - startDate)
                / (60 * 60 * 24)).toInt() + 1 // +1 = 당일(endDate)
        val result = IntArray(totalDays)
        var date: Long
        for (i in 0 until  totalDays) {
            date = startDate + (i * (60 * 60 * 24))
            result[i] = getSpecificDayCountByPreference(date * 1000)
        }
        return result
    }

    fun getLastSystemCountByPreference(): Int {
        return prefs.getInt(STEP_COUNTER_LAST_SYSTEM_COUNT, 0)
    }

    /**
     * 특정 날짜 걸음수 + 1
     */

    /*fun plusOneStepForDay() {
        val timestamp = Timestamp(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(timestamp)
        val preCount = getSpecificDayCountByPreference(date)
        val nextCount = preCount + 1
        prefsEditor.putInt(date, nextCount).apply()
    }*/

    /**
     * 특정 날짜 걸음수 + count
     */
    fun plusCountsForDay(count: Int) {
        val timestamp = Timestamp(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(timestamp)
        val preCount = getSpecificDayCountByPreference(date)
        val nextCount = preCount + count
        prefsEditor.putInt(date, nextCount).apply()
    }

    fun setCountForDay(count: Int) {
        val timestamp = Timestamp(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(timestamp)
        val preCount = getSpecificDayCountByPreference(date)
        val nextCount = preCount + count
        prefsEditor.putInt(date, nextCount).apply()
    }

    fun setLastSystemCount(count: Int) {
        prefsEditor.putInt(STEP_COUNTER_LAST_SYSTEM_COUNT, count).apply()
    }



    fun setRebootAvailable(isAvailable: Boolean) {
        prefsEditor.putBoolean(STEP_COUNTER_REBOOT_AVAILABLE, isAvailable).apply()
    }

    fun getRebootAvailable(): Boolean {
        return prefs.getBoolean(STEP_COUNTER_REBOOT_AVAILABLE, false)
    }

    companion object {
        private const val STEP_COUNTER_PREFERENCE = "step_counter_preference"
        private const val STEP_COUNTER_LAST_SYSTEM_COUNT = "step_counter_last_system_count"
        private const val STEP_COUNTER_REBOOT_AVAILABLE = "asadfds"

        @Volatile
        private var instance: StepCounterPreference? = null

        @JvmStatic
        fun instance() = instance ?: synchronized(this) {
            instance ?: StepCounterPreference(MainApplication.getInstance()).also {
                instance = it
            }
        }
    }
}