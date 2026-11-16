package cn.sangsheng.calendar.ui.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import cn.sangsheng.calendar.data.CountdownItem
import cn.sangsheng.calendar.lunar.LunarEngine
import java.time.LocalDate

/** MD3 大色块卡片外壳，可选点击（用于打开编辑） */
@Composable
fun BigCard(
    container: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier.fillMaxWidth()
    Surface(
        modifier = if (onClick != null) base.clip(RoundedCornerShape(28.dp)).clickable(onClick = onClick) else base,
        shape = RoundedCornerShape(28.dp),
        color = container,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            content = content,
        )
    }
}

@Composable
fun CardDeck(
    today: LocalDate,
    selected: LocalDate,
    events: List<CalendarEvent>,
    countdowns: List<CountdownItem>,
    modifier: Modifier = Modifier,
    onEventClick: (CalendarEvent) -> Unit = {},
    onCountdownClick: (CountdownItem) -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    AnimatedVisibility(
        visible = appeared,
        enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { full -> full / 10 },
    ) {
        Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // ① 倒数日 / 纪念日 —— 点一下即可编辑或删除
            val nearest = countdowns.minByOrNull {
                kotlin.math.abs(DateX.daysBetween(today, it.target))
            }
            if (nearest != null) {
                val days = DateX.daysBetween(today, nearest.target)
                val dayText = when {
                    days > 0 -> "${days}天后"
                    days == 0L -> "就是今天"
                    else -> "已过去${-days}天"
                }
                BigCard(
                    container = cs.primaryContainer,
                    onClick = { onCountdownClick(nearest) },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(cs.onPrimaryContainer, CircleShape))
                        Spacer(Modifier.width(9.dp))
                        Text(
                            text = nearest.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = " · $dayText",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.onPrimaryContainer.copy(alpha = 0.75f),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "倒数日 | " + nearest.note.ifBlank {
                            "${nearest.target.year}年${nearest.target.monthValue}月${nearest.target.dayOfMonth}日"
                        },
                        fontSize = 14.sp,
                        color = cs.onPrimaryContainer.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // ② 选中日的农历 / 干支
            val lunar = LunarEngine.of(selected)
            BigCard(container = cs.secondaryContainer) {
                Text(
                    text = lunar.lunarFullName,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSecondaryContainer,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${lunar.ganZhiYear}${lunar.shengXiao}年 ${lunar.ganZhiMonth}月 ${lunar.ganZhiDay}日",
                    fontSize = 15.sp,
                    color = cs.onSecondaryContainer.copy(alpha = 0.8f),
                )
            }

            // ③ 下一个节气 / 节日倒计时
            val nextJieQi = LunarEngine.nextJieQi(today)
            if (nextJieQi != null) {
                val (name, offset) = nextJieQi
                val date = today.plusDays(offset)
                BigCard(container = cs.tertiaryContainer) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onTertiaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (offset == 0L) "今天" else "${offset}天",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onTertiaryContainer.copy(alpha = 0.55f),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${date.monthValue}月${date.dayOfMonth}日 | ${DateX.weekdayFullName(date)}",
                        fontSize = 14.sp,
                        color = cs.onTertiaryContainer.copy(alpha = 0.7f),
                    )
                }
            }

            // ④ 选中日的日程（点条目编辑）
            val dayEvents = events.filter { it.date == selected }.sortedBy { it.timeText ?: "99:99" }
            if (dayEvents.isNotEmpty()) {
                val palette = listOf(cs.primary, cs.tertiary, cs.secondary, cs.error, cs.primary.copy(alpha = 0.55f))
                BigCard(container = cs.surfaceContainerHigh) {
                    Text(
                        text = "${selected.monthValue}月${selected.dayOfMonth}日 · ${DateX.weekdayFullName(selected)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurface,
                    )
                    Spacer(Modifier.height(14.dp))
                    dayEvents.forEachIndexed { index, event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onEventClick(event) }
                                .padding(
                                    top = if (index == 0) 2.dp else 10.dp,
                                    bottom = if (index == dayEvents.lastIndex) 2.dp else 10.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .width(4.dp)
                                    .height(38.dp)
                                    .background(
                                        palette[event.colorIndex % palette.size],
                                        RoundedCornerShape(2.dp),
                                    )
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = cs.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (event.note.isNotBlank() || event.timeText != null) {
                                    Text(
                                        text = listOfNotNull(event.timeText, event.note.takeIf { it.isNotBlank() })
                                            .joinToString(" · "),
                                        fontSize = 12.sp,
                                        color = cs.onSurfaceVariant,
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
}