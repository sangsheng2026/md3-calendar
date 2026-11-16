package cn.sangsheng.calendar.lunar

import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.time.LocalDate

/**
 * 单日农历信息。
 * cellLabel 是月历格子里那行小字，优先级：
 * 节气 > 农历节日（自建表 + 库补充如除夕）> 公历节日（自建表）> 移动节日 > 农历日名
 */
data class LunarDayInfo(
    val lunarMonthName: String,
    val lunarDayName: String,
    val lunarFullName: String,
    val cellLabel: String,
    /** 只含农历的标签（初一显示月名），关闭节日显示时用 */
    val lunarOnlyLabel: String,
    val jieQi: String?,
    val festival: String?,
    val ganZhiYear: String,
    val ganZhiMonth: String,
    val ganZhiDay: String,
    val ganZhiFull: String,
    val shengXiao: String,
)

object LunarEngine {

    private val cache = object : LinkedHashMap<LocalDate, LunarDayInfo>(1024, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<LocalDate, LunarDayInfo>?): Boolean =
            size > 5000
    }

    @Synchronized
    fun of(date: LocalDate): LunarDayInfo {
        cache[date]?.let { return it }
        val info = compute(date)
        cache[date] = info
        return info
    }

    private fun compute(date: LocalDate): LunarDayInfo {
        val solar = Solar.fromYmd(date.year, date.monthValue, date.dayOfMonth)
        val lunar: Lunar = solar.getLunar()

        val isLeap = lunar.getMonth() < 0
        val monthAbs = Math.abs(lunar.getMonth())
        val monthName = (if (isLeap) "闰" else "") + lunar.getMonthInChinese() + "月"
        val dayName = lunar.getDayInChinese()
        val lunarFullName = monthName + dayName

        val jieQi = lunar.getJieQi()?.takeIf { it.isNotBlank() }

        // 自建中国节日表优先；库里的农历节日只作补充（能补上"除夕"）
        val ownLunar = HolidayTable.lunarFixed(monthAbs, lunar.getDay())
        val libLunar = lunar.getFestivals()?.firstOrNull()?.takeIf { it.isNotBlank() }
        val ownSolar = HolidayTable.solarFixed(date)
        val moving = HolidayTable.moving(date)

        val festival = ownLunar ?: libLunar ?: ownSolar ?: moving

        // 初一显示月名（传统历书习惯），其余显示日名
        val fallback = if (dayName == "初一") monthName else dayName
        val cellLabel = jieQi ?: festival ?: fallback

        val gzYear = lunar.getYearInGanZhi()
        val gzMonth = lunar.getMonthInGanZhi()
        val gzDay = lunar.getDayInGanZhi()

        return LunarDayInfo(
            lunarMonthName = monthName,
            lunarDayName = dayName,
            lunarFullName = lunarFullName,
            cellLabel = cellLabel,
            lunarOnlyLabel = fallback,
            jieQi = jieQi,
            festival = festival,
            ganZhiYear = gzYear,
            ganZhiMonth = gzMonth,
            ganZhiDay = gzDay,
            ganZhiFull = "${gzYear}年 ${gzMonth}月 ${gzDay}日",
            shengXiao = lunar.getYearShengXiao(),
        )
    }

    /** 从某天开始向后找最近的节气（含当天） */
    fun nextJieQi(from: LocalDate, maxDays: Int = 40): Pair<String, Long>? {
        for (i in 0..maxDays) {
            val d = from.plusDays(i.toLong())
            val info = of(d)
            if (info.jieQi != null) return info.jieQi to i.toLong()
        }
        return null
    }
}