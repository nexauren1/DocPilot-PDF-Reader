package com.nexauren.docpilot.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Analytics is opt-in. No document names, PDF content, text, or account email are logged.
 */
object DocPilotAnalytics {
    private const val PREFS_NAME = "docpilot_privacy"
    private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ANALYTICS_ENABLED, false)

    fun applySavedConsent(context: Context) {
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(isEnabled(context))
    }

    fun setConsent(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ANALYTICS_ENABLED, enabled)
            .apply()

        val analytics = FirebaseAnalytics.getInstance(context)
        analytics.setAnalyticsCollectionEnabled(enabled)
        if (enabled) {
            analytics.logEvent("analytics_consent_granted", null)
        }
    }

    fun logEvent(context: Context, name: String, parameters: Bundle? = null) {
        if (!isEnabled(context)) return
        FirebaseAnalytics.getInstance(context).logEvent(name, parameters)
    }

    fun logScreenView(context: Context, screenName: String) {
        val parameters = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "DocPilotApp")
        }
        logEvent(context, FirebaseAnalytics.Event.SCREEN_VIEW, parameters)
    }
}
