package cn.sangsheng.calendar.ui.year

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.sangsheng.calendar.core.DateX
import cn.sangsheng.calendar.data.CalendarEvent
import cn.sangsheng.calendar.data.WeekStart
import cn.sangsheng.calendar.lunar.LunarEngine
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun YearScreen(
    today: LocalDate,
    events: List<CalendarEvent>,
    weekStart: WeekStart,
    onPickMonth: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    var year by remember { mutableIntStateOf(today.year) }
    val eventDates by remember(events) { derivedStateOf { events.map { it.date }.toHashSet() } }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 14.dp, top = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${year}年",
                style = MaterialTheme.typography.displayLarge,
                color = cs.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "今年",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = cs.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { year = today.year }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
            Text(
                text = "‹",
                fontSize = 22.sp,
                color = cs.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { year -= 1 }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
            Text(
                text = "›",
                fontSize = 22.sp,
                color = cs.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { year += 1 }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 132.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items((1..12).map { YearMonth.of(year, it) }) { ym ->
                MiniMonthCard(
                    ym = ym,
                    today = today,
                    eventDates = eventDates,
                    weekStart = weekStart,
                    onClick = { onPickMonth(ym) },
                )
            }
        }
    }
}

@Composable
private fun MiniMonthCard(
    ym: YearMonth,
    today: LocalDate,
    eventDates: Set<LocalDate>,
    weekStart: WeekStart,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val isCurrent = YearMonth.from(today) == ym
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isCurrent) cs.secondaryContainer else cs.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
            Text(
                text = DateX.monthTitle(ym),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) cs.onSecondaryContainer else cs.onSurface,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
            DateX.monthGrid(ym, weekStart).chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        val inMonth = YearMonth.from(date) == ym
                        val isToday = date == today
                        val hasEvent = date in eventDates
                        Box(
                            Modifier.weight(1f).height(18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (inMonth) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(
                                            when {
                                                isToday -> cs.primary
                                                hasEvent -> cs.primary.copy(alpha = 0.12f)
                                                else -> androidx.compose.ui.graphics.Color.Transparent
                                            },
                                            CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        maxLines = 1,
                                        softWrap = false,
                                        style = TextStyle(
                                            fontSize = 8.sp,
                                            lineHeight = 9.sp,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                            color = when {
                                                isToday -> cs.onPrimary
                                                DateX.isWeekend(date) -> cs.primary
                                                else -> cs.onSurfaceVariant
                                            },
                                        ),
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