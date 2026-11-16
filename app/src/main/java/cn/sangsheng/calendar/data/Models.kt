package cn.sangsheng.calendar.data

import java.time.LocalDate
import java.time.LocalTime

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: LocalDate,
    val timeText: String? = null,
    val note: String = "",
    val colorIndex: Int = 0,
    val remindEnabled: Boolean = false,
    /** 提前多少分钟提醒，0 = 准时 */
    val remindMinutesBefore: Int = 10,
) {
    /** 解析 HH:mm 形式的时间文本；解析不出来返回 null */
    fun timeOrNull(): LocalTime? {
        val raw = timeText?.trim() ?: return null
        val parts = raw.split(":", "：")
        if (parts.size != 2) return null
        val hour = parts[0].trim().toIntOrNull() ?: return null
        val minute = parts[1].trim().toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime.of(hour, minute)
    }
}

data class CountdownItem(
    val id: String,
    val title: String,
    val target: LocalDate,
    val note: String = "",
)