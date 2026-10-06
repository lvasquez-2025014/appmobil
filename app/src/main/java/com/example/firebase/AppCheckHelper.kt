package com.example.firebase

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

private const val TAG = "AppCheckHelper"

fun configureAppCheck(activity: Activity, intent: Intent?) {
    runCatching {
        val debugToken = intent?.getStringExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")
        if (!debugToken.isNullOrEmpty()) {
            System.setProperty("firebase.appcheck.debug.token", debugToken)
        }
        val factory = DebugAppCheckProviderFactory.getInstance()
        Firebase.appCheck.installAppCheckProviderFactory(factory)
        Log.i(TAG, "App Check installed successfully")
    }.onFailure { e ->
        Log.w(TAG, "App Check initialization warning", e)
    }
}
