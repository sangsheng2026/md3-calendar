package cn.sangsheng.calendar.ui.month

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.sangsheng.calendar.core.DateX
import cn.sangsheng.calendar.data.CalendarEvent
import cn.sangsheng.calendar.data.WeekStart
import cn.sangsheng.calendar.lunar.LunarEngine
import cn.sangsheng.calendar.lunar.OfficialHolidays
import java.time.LocalDate
import java.time.YearMonth

val CELL_HEIGHT = 56.dp
val GRID_ROWS = 6

@Composable
fun WeekdayHeader(
    weekStart: WeekStart,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val labels = remember(weekStart) { DateX.weekdayLabels(weekStart) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (DateX.isWeekendColumn(index, weekStart)) {
                        cs.primary.copy(alpha = 0.85f)
                    } else {
                        cs.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
fun MonthGrid(
    ym: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    events: List<CalendarEvent>,
    weekStart: WeekStart,
    showLunar: Boolean,
    showFestival: Boolean,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = remember(ym, weekStart) { DateX.monthGrid(ym, weekStart) }
    val eventDates by remember(events) { derivedStateOf { events.map { it.date }.toHashSet() } }

    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        days.chunked(7).forEachIndexed { _, week ->
            Row(Modifier.fillMaxWidth().height(CELL_HEIGHT)) {
                week.forEachIndexed { index, date ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DayCell(
                            date = date,
                            inMonth = YearMonth.from(date) == ym,
                            isWeekend = DateX.isWeekendColumn(index, weekStart),
                            isToday = date == today,
                            isSelected = date == selected,
                            hasEvent = date in eventDates,
                            showLunar = showLunar,
                            showFestival = showFestival,
                            onClick = { onSelect(date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isWeekend: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvent: Boolean,
    showLunar: Boolean,
    showFestival: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val lunar = remember(date) { LunarEngine.of(date) }

    val targetContainer = when {
        isSelected -> cs.primary
        isToday -> cs.primaryContainer
        else -> Color.Transparent
    }
    val container by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(durationMillis = 220),
        label = "cellContainer",
    )

    val numberColor = when {
        isSelected -> cs.onPrimary
        isToday -> cs.onPrimaryContainer
        !inMonth -> (if (isWeekend) cs.primary else cs.onSurface).copy(alpha = 0.3f)
        isWeekend -> cs.primary
        else -> cs.onSurface
    }

    val labelColor = when {
        isSelected -> cs.onPrimary.copy(alpha = 0.85f)
        isToday -> cs.onPrimaryContainer.copy(alpha = 0.8f)
        !inMonth -> (if (isWeekend) cs.primary else cs.onSurfaceVariant).copy(alpha = 0.3f)
        isWeekend -> cs.primary.copy(alpha = 0.8f)
        else -> cs.onSurfaceVariant
    }

    val dotColor = when {
        isSelected -> cs.onPrimary
        isToday -> cs.onPrimaryContainer
        else -> cs.primary
    }

    // 调休上班日优先标"班"
    val isMakeupWorkday = OfficialHolidays.isMakeupWorkday(date)

    // 小字内容：农历 / 节日节气 两个开关自由组合
    val rawLabel = when {
        showFestival && showLunar -> lunar.cellLabel
        showFestival -> if (lunar.cellLabel != lunar.lunarOnlyLabel) lunar.cellLabel else ""
        showLunar -> lunar.lunarOnlyLabel
        else -> ""
    }
    val label = if (isMakeupWorkday) "班" else rawLabel
    val finalLabelColor = if (isMakeupWorkday) cs.error else labelColor

    Column(
        modifier = Modifier
            .widthIn(min = 42.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 21.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = numberColor,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = finalLabelColor,
            fontWeight = if (isMakeupWorkday) FontWeight.SemiBold else FontWeight.Normal,
        )
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(3.dp)
                .background(if (hasEvent) dotColor else Color.Transparent, CircleShape)
        )
    }
}