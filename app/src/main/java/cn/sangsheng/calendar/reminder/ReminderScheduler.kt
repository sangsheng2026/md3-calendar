package cn.sangsheng.calendar.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import cn.sangsheng.calendar.data.CalendarEvent
import cn.sangsheng.calendar.data.CalendarRepository
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 用 AlarmManager 做单次提醒。
 * 没有精确闹钟权限时自动降级成 setAndAllowWhileIdle（大约准，不崩）。
 */
object ReminderScheduler {

    const val EXTRA_TITLE = "event_title"
    const val EXTRA_NOTE = "event_note"
    const val EXTRA_TIME = "event_time"

    private const val TAG = "ReminderScheduler"

    fun reminderAt(event: CalendarEvent): LocalDateTime? {
        val time = event.timeOrNull() ?: return null
        return LocalDateTime.of(event.date, time).minusMinutes(event.remindMinutesBefore.toLong())
    }

    /** 有改动时调用：先取消旧的，需要的话重新排 */
    fun sync(context: Context, event: CalendarEvent) {
        cancel(context, event.id)
        if (!event.remindEnabled) return
        val at = reminderAt(event) ?: return
        if (!at.isAfter(LocalDateTime.now())) return

        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = buildPendingIntent(context, event)

        runCatching {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
            }
        }.onFailure {
            Log.w(TAG, "排提醒失败：${event.title}", it)
        }
    }

    fun cancel(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /** 开机后把未来所有提醒重新排一遍（闹钟不跨重启保留） */
    fun rescheduleAll(context: Context) {
        val repo = CalendarRepository.get(context)
        repo.events.filter { it.remindEnabled }.forEach { sync(context, it) }
    }

    private fun buildPendingIntent(context: Context, event: CalendarEvent): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, event.title)
            putExtra(EXTRA_NOTE, event.note)
            putExtra(EXTRA_TIME, event.timeText.orEmpty())
        }
        return PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}