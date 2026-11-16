package cn.sangsheng.calendar.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: return
        val note = intent.getStringExtra(ReminderScheduler.EXTRA_NOTE).orEmpty()
        val time = intent.getStringExtra(ReminderScheduler.EXTRA_TIME).orEmpty()
        NotificationHelper.showEventReminder(context, title, note, time)
    }
}