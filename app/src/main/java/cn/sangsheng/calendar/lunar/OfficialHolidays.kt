package cn.sangsheng.calendar.lunar

import java.time.LocalDate

/**
 * 中国法定节假日与调休安排。
 *
 * 数据来源：国务院办公厅《关于2026年部分节假日安排的通知》
 * （国办发明电〔2025〕7号，2025-11-04 发布）。
 *
 * ⚠️ 这是逐年发布的数据，每年 11 月左右出新一年的通知，
 * 到时候只需要在 SPANS / MAKEUP_WORKDAYS 里补上新一年即可。
 */
object OfficialHolidays {

    data class Span(val name: String, val start: LocalDate, val end: LocalDate) {
        fun contains(date: LocalDate): Boolean =
            !date.isBefore(start) && !date.isAfter(end)
    }

    private val SPANS: List<Span> = listOf(
        // 2026
        Span("元旦", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3)),
        Span("春节", LocalDate.of(2026, 2, 15), LocalDate.of(2026, 2, 23)),
        Span("清明节", LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 6)),
        Span("劳动节", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5)),
        Span("端午节", LocalDate.of(2026, 6, 19), LocalDate.of(2026, 6, 21)),
        Span("中秋节", LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 27)),
        Span("国庆节", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 7)),
    )

    /** 周末但需要上班的调休日 */
    private val MAKEUP_WORKDAYS: Set<LocalDate> = setOf(
        // 2026
        LocalDate.of(2026, 1, 4),    // 元旦调休
        LocalDate.of(2026, 2, 14),   // 春节调休
        LocalDate.of(2026, 2, 28),   // 春节调休
        LocalDate.of(2026, 5, 9),    // 劳动节调休
        LocalDate.of(2026, 9, 20),   // 国庆调休
        LocalDate.of(2026, 10, 10),  // 国庆调休
    )

    /** 这一天落在哪段假期里；没有则返回 null */
    fun holidayOf(date: LocalDate): Span? = SPANS.firstOrNull { it.contains(date) }

    fun holidayName(date: LocalDate): String? = holidayOf(date)?.name

    /** 是否调休上班日（周末被拉去上班） */
    fun isMakeupWorkday(date: LocalDate): Boolean = MAKEUP_WORKDAYS.contains(date)

    /** 是否是真正的休息日：在假期区间内，或者是周末且不调休 */
    fun isRestDay(date: LocalDate): Boolean {
        if (isMakeupWorkday(date)) return false
        if (holidayOf(date) != null) return true
        val dow = date.dayOfWeek.value
        return dow == 6 || dow == 7
    }

    /** 假期第几天（从 1 开始）；不在假期返回 0 */
    fun holidayDayIndex(date: LocalDate): Int {
        val span = holidayOf(date) ?: return 0
        return (span.start.toEpochDay()..span.end.toEpochDay())
            .indexOf(date.toEpochDay()) + 1
    }
}