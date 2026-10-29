package org.agentos.poc.collector

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import org.agentos.poc.collector.model.EventNotification
import org.agentos.poc.privacy.SensitiveFilter

class ContextNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "AgentNotification"
        private var notificationListener: ((EventNotification) -> Unit)? = null

        fun setOnNotificationListener(listener: ((EventNotification) -> Unit)?) {
            notificationListener = listener
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        // Check blacklist (banking / crypto / passwords)
        if (SensitiveFilter.shouldBlockPackage(packageName)) {
            return
        }

        val extras = sbn.notification.extras ?: return
        val rawTitle = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val rawText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (rawTitle.isBlank() && rawText.isBlank()) return

        // Sanitize with PII Regex & sensitive filters
        val sanitizedTitle = SensitiveFilter.filterNodeText(rawTitle, false)
        val sanitizedText = SensitiveFilter.filterNodeText(rawText, false)

        val appName = try {
            val pm = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val event = EventNotification(
            id = "${packageName}_${sbn.id}_${sbn.postTime}",
            timestampMs = sbn.postTime,
            packageName = packageName,
            appName = appName,
            title = sanitizedTitle,
            text = sanitizedText,
            category = sbn.notification.category
        )

        Log.d(TAG, "Notification captured from $appName: $sanitizedTitle")
        notificationListener?.invoke(event)
    }
}

