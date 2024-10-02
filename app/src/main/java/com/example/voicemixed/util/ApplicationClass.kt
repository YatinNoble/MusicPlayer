package com.example.voicemixed.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

class ApplicationClass : Application() {

    private var isInForeground = true

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {
                isInForeground = true
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                // Check if this was the last activity
                if (isFinishing(activity)) {
                    isInForeground = false
                }
            }

        })
    }

    fun isAppInForeground(): Boolean {
        return isInForeground
    }

    private fun isFinishing(activity: Activity): Boolean {
        return activity.isFinishing || activity.isDestroyed
    }
}