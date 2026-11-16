package cn.sangsheng.calendar.ui.day

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.sangsheng.calendar.core.DateX
import cn.sangsheng.calendar.data.CalendarEvent
import cn.sangsheng.calendar.lunar.LunarEngine
import cn.sangsheng.calendar.lunar.OfficialHolidays
import cn.sangsheng.calendar.ui.cards.BigCard
import java.time.LocalDate

@Composable
fun DayScreen(
    today: LocalDate,
    selected: LocalDate,
    events: List<CalendarEvent>,
    showLunar: Boolean,
    showFestival: Boolean,
    modifier: Modifier = Modifier,
    onEventClick: (CalendarEvent) -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    val lunar = remember(selected) { LunarEngine.of(selected) }
    val dayEvents = events.filter { it.date == selected }.sortedBy { it.timeText ?: "99:99" }
    val holidayName = OfficialHolidays.holidayName(selected)
    val holidayDay = OfficialHolidays.holidayDayIndex(selected)
    val isMakeupWorkday = OfficialHolidays.isMakeupWorkday(selected)
    val offset = DateX.daysBetween(today, selected)
    val relative = when {
        offset == 0L -> "今天"
        offset == 1L -> "明天"
        offset == -1L -> "昨天"
        offset > 0 -> "${offset}天后"
        else -> "${-offset}天前"
    }

    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = selected.dayOfMonth.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = cs.onSurface,
                )
                Text(
                    text = "  ${selected.monthValue}月",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = when {
                        isMakeupWorkday -> "调休上班 · $relative"
                        holidayName != null -> "${holidayName}假期 ${holidayDay}天 · $relative"
                        else -> relative
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isMakeupWorkday) cs.error else cs.primary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            Text(
                text = buildString {
                    append("${selected.year}年 · ${DateX.weekdayFullName(selected)}")
                    if (showLunar) append(" · ${lunar.lunarFullName}")
                },
                fontSize = 14.sp,
                color = cs.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showLunar) {
                item {
                    BigCard(container = cs.secondaryContainer) {
                        Text(
                            text = lunar.lunarFullName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onSecondaryContainer,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${lunar.ganZhiYear}${lunar.shengXiao}年 ${lunar.ganZhiMonth}月 ${lunar.ganZhiDay}日",
                            fontSize = 14.sp,
                            color = cs.onSecondaryContainer.copy(alpha = 0.8f),
                        )
                        if (showFestival) {
                            val tags = listOfNotNull(lunar.jieQi, lunar.festival)
                            if (tags.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    tags.forEach { tag ->
                                        Box(
                                            Modifier
                                                .background(
                                                    cs.onSecondaryContainer.copy(alpha = 0.12f),
                                                    RoundedCornerShape(50),
                                                )
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = cs.onSecondaryContainer,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (showFestival) {
                val tags = listOfNotNull(lunar.jieQi, lunar.festival)
                if (tags.isNotEmpty()) {
                    item {
                        BigCard(container = cs.tertiaryContainer) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                tags.forEach { tag ->
                                    Text(
                                        text = tag,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cs.onTertiaryContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                BigCard(container = cs.surfaceContainerHigh) {
                    Text(
                        text = "日程",
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
                                    .padding(vertical = 9.dp),
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
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        fontSize = 16.sp,
                                        color = cs.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (event.note.isNotBlank()) {
                                        Text(
                                            text = event.note,
                                            fontSize = 12.sp,
                                            color = cs.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}