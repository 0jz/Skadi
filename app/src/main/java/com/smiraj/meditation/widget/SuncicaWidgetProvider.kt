package com.smiraj.meditation.widget

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.smiraj.meditation.MainActivity
import com.smiraj.meditation.R
import com.smiraj.meditation.emergency.EmergencyContact
import com.smiraj.meditation.emergency.EmergencySms

class SuncicaWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SEND_EMERGENCY_SMS) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                runCatching { EmergencySms.sendToEmergencyContact() }
            } else {
                context.startActivity(smsComposerIntent())
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.suncica_widget).apply {
            setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))
            setOnClickPendingIntent(R.id.widget_sos, emergencySmsIntent(context))
            setOnClickPendingIntent(R.id.widget_support, dialIntent(context, "0800100600", 600))
        }
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            10,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun dialIntent(context: Context, number: String, requestCode: Int): PendingIntent {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun emergencySmsIntent(context: Context): PendingIntent {
        val intent = Intent(context, SuncicaWidgetProvider::class.java).apply {
            action = ACTION_SEND_EMERGENCY_SMS
        }
        return PendingIntent.getBroadcast(
            context,
            1120,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun smsComposerIntent(): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${EmergencyContact.PHONE}")).apply {
            putExtra("sms_body", EmergencyContact.MESSAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    companion object {
        private const val ACTION_SEND_EMERGENCY_SMS = "com.smiraj.meditation.SEND_EMERGENCY_SMS"
    }
}
