package cn.sangsheng.calendar.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/**
 * 一期：SharedPreferences + JSON 的轻量实现。
 * 二期换成 Room 时只需替换本类，UI 侧接口不变。
 */
class CalendarRepository private constructor(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("calendar_data", Context.MODE_PRIVATE)

    val events: SnapshotStateList<CalendarEvent> = mutableStateListOf()
    val countdowns: SnapshotStateList<CountdownItem> = mutableStateListOf()

    init {
        load()
    }

    fun eventsOn(date: LocalDate): List<CalendarEvent> =
        events.filter { it.date == date }.sortedBy { it.timeText ?: "99:99" }

    fun hasEventOn(date: LocalDate): Boolean = events.any { it.date == date }

    fun addEvent(
        title: String,
        date: LocalDate,
        timeText: String?,
        note: String = "",
        remindEnabled: Boolean = false,
        remindMinutesBefore: Int = 10,
    ) {
        if (title.isBlank()) return
        events.add(
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                date = date,
                timeText = timeText?.takeIf { it.isNotBlank() },
                note = note,
                colorIndex = events.size % 5,
                remindEnabled = remindEnabled,
                remindMinutesBefore = remindMinutesBefore,
            )
        )
        save()
    }

    /** 新增或按 id 覆盖，统一入口 */
    fun upsertEvent(event: CalendarEvent) {
        val index = events.indexOfFirst { it.id == event.id }
        if (index >= 0) events[index] = event else events.add(event)
        save()
    }

    fun upsertCountdown(item: CountdownItem) {
        val index = countdowns.indexOfFirst { it.id == item.id }
        if (index >= 0) countdowns[index] = item else countdowns.add(item)
        save()
    }

    fun updateEvent(event: CalendarEvent) {
        val index = events.indexOfFirst { it.id == event.id }
        if (index >= 0) {
            events[index] = event
            save()
        }
    }

    fun deleteEvent(id: String) {
        events.removeAll { it.id == id }
        save()
    }

    fun addCountdown(title: String, target: LocalDate, note: String = "") {
        if (title.isBlank()) return
        countdowns.add(
            CountdownItem(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                target = target,
                note = note,
            )
        )
        save()
    }

    fun updateCountdown(item: CountdownItem) {
        val index = countdowns.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            countdowns[index] = item
            save()
        }
    }

    fun deleteCountdown(id: String) {
        countdowns.removeAll { it.id == id }
        save()
    }

    private fun load() {
        val rawEvents = prefs.getString(KEY_EVENTS, null)
        if (rawEvents == null) {
            seed()
            return
        }
        runCatching {
            val arr = JSONArray(rawEvents)
            repeat(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                events.add(
                    CalendarEvent(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        date = LocalDate.ofEpochDay(o.getLong("date")),
                        timeText = o.optString("time").takeIf { it.isNotBlank() },
                        note = o.optString("note"),
                        colorIndex = o.optInt("color"),
                        remindEnabled = o.optBoolean("remind", false),
                        remindMinutesBefore = o.optInt("remindBefore", 10),
                    )
                )
            }
        }

        runCatching {
            val raw = prefs.getString(KEY_COUNTDOWNS, null) ?: return@runCatching
            val arr = JSONArray(raw)
            repeat(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                countdowns.add(
                    CountdownItem(
                        id = o.getString("id"),
                        title = o.getString("title"),
                        target = LocalDate.ofEpochDay(o.getLong("target")),
                        note = o.optString("note"),
                    )
                )
            }
        }
    }

    private fun save() {
        val eArr = JSONArray()
        events.forEach { e ->
            eArr.put(
                JSONObject()
                    .put("id", e.id)
                    .put("title", e.title)
                    .put("date", e.date.toEpochDay())
                    .put("time", e.timeText ?: "")
                    .put("note", e.note)
                    .put("color", e.colorIndex)
                    .put("remind", e.remindEnabled)
                    .put("remindBefore", e.remindMinutesBefore)
            )
        }
        val cArr = JSONArray()
        countdowns.forEach { c ->
            cArr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("title", c.title)
                    .put("target", c.target.toEpochDay())
                    .put("note", c.note)
            )
        }
        prefs.edit()
            .putString(KEY_EVENTS, eArr.toString())
            .putString(KEY_COUNTDOWNS, cArr.toString())
            .apply()
    }

    /** 首次启动的示例数据，让月历上的事件点、卡片流都有内容 */
    private fun seed() {
        val today = LocalDate.now()
        events.addAll(
            listOf(
                CalendarEvent(UUID.randomUUID().toString(), "产品评审会", today, "14:00", "会议室 B", 0, remindEnabled = true, remindMinutesBefore = 10),
                CalendarEvent(UUID.randomUUID().toString(), "健身", today, "19:30", "", 1),
                CalendarEvent(UUID.randomUUID().toString(), "交房租", today.plusDays(4), null, "", 2),
                CalendarEvent(UUID.randomUUID().toString(), "小雪 · 和朋友吃饭", today.plusDays(6), "18:00", "", 3),
            )
        )
        countdowns.add(
            CountdownItem(
                id = UUID.randomUUID().toString(),
                title = "早上好，夜之城！",
                target = LocalDate.of(2077, 1, 1),
                note = "2077年1月1日",
            )
        )
        save()
    }

    companion object {
        private const val KEY_EVENTS = "events"
        private const val KEY_COUNTDOWNS = "countdowns"

        @Volatile
        private var instance: CalendarRepository? = null

        fun get(context: Context): CalendarRepository =
            instance ?: synchronized(this) {
                instance ?: CalendarRepository(context).also { instance = it }
            }
    }
}