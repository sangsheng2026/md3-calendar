package cn.sangsheng.calendar.reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cn.sangsheng.calendar.R

/** 通知渠道与弹出提醒 */
object NotificationHelper {

    const val CHANNEL_ID = "event_reminder"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "日程提醒",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "日程开始前的提醒通知"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showEventReminder(
        context: Context,
        title: String,
        note: String,
        time: String,
    ) {
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val body = buildString {
            if (time.isNotBlank()) append(time)
            if (note.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(note)
            }
            if (isEmpty()) append("日程提醒")
        }

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .apply { if (contentIntent != null) setContentIntent(contentIntent) }
            .build()

        manager.notify(title.hashCode(), notification)
    }
}