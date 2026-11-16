package cn.sangsheng.calendar.ui.week

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import cn.sangsheng.calendar.ui.cards.BigCard
import java.time.LocalDate

@Composable
fun WeekScreen(
    today: LocalDate,
    selected: LocalDate,
    events: List<CalendarEvent>,
    weekStart: WeekStart,
    showLunar: Boolean,
    showFestival: Boolean,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onEventClick: (CalendarEvent) -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    val week = remember(selected, weekStart) { DateX.weekDatesOf(selected, weekStart) }
    val dayEvents = events.filter { it.date == selected }.sortedBy { it.timeText ?: "99:99" }

    Column(modifier.fillMaxSize()) {
        Text(
            text = "${week.first().monthValue}月",
            style = MaterialTheme.typography.displayLarge,
            color = cs.onSurface,
            modifier = Modifier.padding(start = 22.dp, top = 8.dp, bottom = 4.dp),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = cs.surfaceContainerLow,
                ) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp)) {
                        week.forEach { date ->
                            DayPill(
                                date = date,
                                isToday = date == today,
                                isSelected = date == selected,
                                hasEvent = events.any { it.date == date },
                                showLunar = showLunar,
                                showFestival = showFestival,
                                onClick = { onSelect(date) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            item {
                BigCard(container = cs.secondaryContainer) {
                    val lunar = LunarEngine.of(selected)
                    Text(
                        text = "${selected.monthValue}月${selected.dayOfMonth}日 · ${DateX.weekdayFullName(selected)}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSecondaryContainer,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${lunar.lunarFullName} · ${lunar.ganZhiFull}",
                        fontSize = 13.sp,
                        color = cs.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
            item {
                BigCard(container = cs.surfaceContainerHigh) {
                    Text(
                        text = "当日日程",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurface,
                    )
                    Spacer(Modifier.height(12.dp))
                    if (dayEvents.isEmpty()) {
                        Text(
                            text = "这一天还没有安排",
                            fontSize = 14.sp,
                            color = cs.onSurfaceVariant,
                        )
                    } else {
                        dayEvents.forEach { event ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onEventClick(event) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(6.dp).background(cs.primary, CircleShape))
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    text = event.timeText ?: "全天",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = cs.primary,
                                )
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    text = event.title,
                                    fontSize = 15.sp,
                                    color = cs.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPill(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvent: Boolean,
    showLunar: Boolean,
    showFestival: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val lunar = remember(date) { LunarEngine.of(date) }
    val weekend = DateX.isWeekend(date)

    val container by animateColorAsState(
        targetValue = when {
            isSelected -> cs.primary
            isToday -> cs.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(220),
        label = "pillContainer",
    )

    val numberColor = when {
        isSelected -> cs.onPrimary
        isToday -> cs.onPrimaryContainer
        weekend -> cs.primary
        else -> cs.onSurface
    }

    val label = when {
        showFestival && showLunar -> lunar.cellLabel
        showFestival -> if (lunar.cellLabel != lunar.lunarOnlyLabel) lunar.cellLabel else ""
        showLunar -> lunar.lunarOnlyLabel
        else -> ""
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = DateX.weekdayFullName(date).removePrefix("周"),
            fontSize = 11.sp,
            color = if (isSelected) cs.onPrimary.copy(alpha = 0.8f) else cs.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = numberColor,
        )
        Text(
            text = label,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = if (isSelected) cs.onPrimary.copy(alpha = 0.85f) else cs.onSurfaceVariant,
        )
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(3.dp)
                .background(
                    if (hasEvent) (if (isSelected) cs.onPrimary else cs.primary) else Color.Transparent,
                    CircleShape,
                )
        )
    }
}