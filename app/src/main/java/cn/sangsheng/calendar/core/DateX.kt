package cn.sangsheng.calendar.core

import cn.sangsheng.calendar.data.WeekStart
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

object DateX {

    private val MONDAY_LABELS = listOf("一", "二", "三", "四", "五", "六", "日")
    private val SUNDAY_LABELS = listOf("日", "一", "二", "三", "四", "五", "六")

    fun weekdayLabels(start: WeekStart): List<String> =
        if (start == WeekStart.MONDAY) MONDAY_LABELS else SUNDAY_LABELS

    /** 该列是不是周末（无论一周从哪天开始，周六周日永远是周末） */
    fun isWeekendColumn(index: Int, start: WeekStart): Boolean {
        val labels = weekdayLabels(start)
        return labels.getOrNull(index) == "六" || labels.getOrNull(index) == "日"
    }

    /** 一周之始的偏移量 */
    private fun offsetOf(date: LocalDate, start: WeekStart): Long =
        if (start == WeekStart.MONDAY) ((date.dayOfWeek.value + 6) % 7).toLong()
        else (date.dayOfWeek.value % 7).toLong()

    fun weekDatesOf(date: LocalDate, start: WeekStart = WeekStart.MONDAY): List<LocalDate> {
        val first = date.minusDays(offsetOf(date, start))
        return (0 until 7).map { first.plusDays(it.toLong()) }
    }

    /** 6 行 × 7 列固定 42 格，含上月/下月补位 */
    fun monthGrid(ym: YearMonth, start: WeekStart = WeekStart.MONDAY): List<LocalDate> {
        val first = ym.atDay(1)
        val begin = first.minusDays(offsetOf(first, start))
        return (0 until 42).map { begin.plusDays(it.toLong()) }
    }

    fun isWeekend(date: LocalDate): Boolean =
        date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    fun monthTitle(ym: YearMonth): String = "${ym.monthValue}月"

    fun weekdayFullName(date: LocalDate): String =
        listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[date.dayOfWeek.value - 1]
}