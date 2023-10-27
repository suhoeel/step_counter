package com.step_count_app;

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.config.ReactFeatureFlags
import com.facebook.react.shell.MainReactPackage
import com.facebook.soloader.SoLoader
import com.stepCounter.StepCounterPacakge
import java.util.Arrays
import java.util.concurrent.TimeUnit

class MainApplication : Application(), ReactApplication, androidx.work.Configuration.Provider {

    private val mReactNativeHost: ReactNativeHost = object : ReactNativeHost(this) {

        override fun getUseDeveloperSupport(): Boolean {
            return BuildConfig.DEBUG
        }

        override fun getPackages(): List<ReactPackage> {
            return Arrays.asList(
                MainReactPackage(),
                StepCounterPacakge(),
            )
        }

        override fun getJSMainModuleName(): String {
            return "index"
        }
    }

    override fun getReactNativeHost(): ReactNativeHost {
        return mReactNativeHost
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        // If you opted-in for the New Architecture, we enable the TurboModule system
        ReactFeatureFlags.useTurboModules = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        SoLoader.init(this,  /* native exopackage */false)
        ReactNativeFlipper.initializeFlipper(this, reactNativeHost.reactInstanceManager)


    }

    companion object {

        private var application: MainApplication? = null

        @JvmStatic
        fun getInstance(): Context {
            return application as Context
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}