package cn.sangsheng.calendar.lunar

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 中文日历节日表。
 * 只收中国用户关心的节日，刻意不用 lunar-java 的公历节日表
 * （那张表里 11/1 是"万圣节"、10/31 是"万圣节前"，会污染月历）。
 */
object HolidayTable {

    /** 农历固定节日：月 * 100 + 日（月取绝对值，闰月按同月算） */
    private val LUNAR_FIXED: Map<Int, String> = mapOf(
        101 to "春节",
        115 to "元宵节",
        202 to "龙抬头",
        505 to "端午节",
        707 to "七夕节",
        715 to "中元节",
        815 to "中秋节",
        909 to "重阳节",
        1001 to "寒衣节",
        1015 to "下元节",
        1208 to "腊八节",
        1223 to "小年",
    )

    /** 公历固定节日：月 * 100 + 日 */
    private val SOLAR_FIXED: Map<Int, String> = mapOf(
        101 to "元旦",
        214 to "情人节",
        308 to "妇女节",
        312 to "植树节",
        401 to "愚人节",
        501 to "劳动节",
        504 to "青年节",
        601 to "儿童节",
        701 to "建党节",
        801 to "建军节",
        910 to "教师节",
        1001 to "国庆节",
        1031 to "万圣夜",
        1108 to "记者节",
        1213 to "国家公祭日",
        1224 to "平安夜",
        1225 to "圣诞节",
    )

    fun lunarFixed(monthAbs: Int, day: Int): String? = LUNAR_FIXED[monthAbs * 100 + day]

    fun solarFixed(date: LocalDate): String? = SOLAR_FIXED[date.monthValue * 100 + date.dayOfMonth]

    /** 移动节日：母亲节 / 父亲节 / 感恩节 */
    fun moving(date: LocalDate): String? {
        val m = date.monthValue
        val dom = date.dayOfMonth
        val dow = date.dayOfWeek
        return when {
            m == 5 && dow == DayOfWeek.SUNDAY && dom in 8..14 -> "母亲节"
            m == 6 && dow == DayOfWeek.SUNDAY && dom in 15..21 -> "父亲节"
            m == 11 && dow == DayOfWeek.THURSDAY && dom in 22..28 -> "感恩节"
            else -> null
        }
    }
}