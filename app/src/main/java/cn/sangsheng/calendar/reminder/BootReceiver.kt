package cn.sangsheng.calendar.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 重启后闹钟全部丢失，这里重新排一遍 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            ReminderScheduler.rescheduleAll(context)
        }
    }
}