package cn.sangsheng.calendar.ui.month

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.sangsheng.calendar.R
import cn.sangsheng.calendar.core.DateX
import cn.sangsheng.calendar.data.CalendarEvent
import cn.sangsheng.calendar.data.CountdownItem
import cn.sangsheng.calendar.data.WeekStart
import cn.sangsheng.calendar.ui.cards.CardDeck
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private val BASE_MONTH: YearMonth = YearMonth.of(1900, 1)
private const val PAGE_COUNT = 2400

@Composable
fun MonthScreen(
    today: LocalDate,
    selected: LocalDate,
    events: List<CalendarEvent>,
    countdowns: List<CountdownItem>,
    weekStart: WeekStart,
    showLunar: Boolean,
    showFestival: Boolean,
    onSelect: (LocalDate) -> Unit,
    onOpenAdd: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
    onEventClick: (CalendarEvent) -> Unit = {},
    onCountdownClick: (CountdownItem) -> Unit = {},
    focusMonth: YearMonth? = null,
    onFocusConsumed: () -> Unit = {},
) {
    val cs = MaterialTheme.colorScheme
    val todayPage = remember(today) {
        ChronoUnit.MONTHS.between(BASE_MONTH, YearMonth.from(today)).toInt().coerceIn(0, PAGE_COUNT - 1)
    }
    val pagerState = rememberPagerState(initialPage = todayPage, pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val currentMonth by remember {
        derivedStateOf { BASE_MONTH.plusMonths(pagerState.currentPage.toLong()) }
    }

    LaunchedEffect(pagerState.settledPage) {
        val ym = BASE_MONTH.plusMonths(pagerState.settledPage.toLong())
        if (YearMonth.from(selected) != ym) {
            onSelect(if (YearMonth.from(today) == ym) today else ym.atDay(1))
        }
    }

    LaunchedEffect(focusMonth) {
        if (focusMonth != null) {
            val page = ChronoUnit.MONTHS.between(BASE_MONTH, focusMonth).toInt()
                .coerceIn(0, PAGE_COUNT - 1)
            pagerState.scrollToPage(page)
            onFocusConsumed()
        }
    }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 10.dp, top = 8.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = currentMonth.monthValue,
                transitionSpec = {
                    val forward = targetState > initialState
                    val direction = if (forward) 1 else -1
                    (slideInHorizontally(tween(260)) { full -> full / 3 * direction } +
                        fadeIn(tween(200))) togetherWith
                        (slideOutHorizontally(tween(260)) { full -> -full / 3 * direction } +
                            fadeOut(tween(120))) using SizeTransform(clip = false)
                },
                label = "monthTitle",
            ) { monthValue ->
                Text(
                    text = "${monthValue}月",
                    style = MaterialTheme.typography.displayLarge,
                    color = cs.onSurface,
                    modifier = Modifier.clickable {
                        scope.launch { pagerState.animateScrollToPage(todayPage) }
                    },
                )
            }

            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenAdd) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "新建事件",
                    modifier = Modifier.size(25.dp),
                    tint = cs.onSurface,
                )
            }
            IconButton(onClick = onOpenMenu) {
                Icon(
                    painter = painterResource(R.drawable.ic_more),
                    contentDescription = "更多",
                    modifier = Modifier.size(23.dp),
                    tint = cs.onSurface,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 150.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = cs.surfaceContainerLow,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        WeekdayHeader(weekStart = weekStart)
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(CELL_HEIGHT * GRID_ROWS),
                            pageSpacing = 0.dp,
                        ) { page ->
                            MonthGrid(
                                ym = BASE_MONTH.plusMonths(page.toLong()),
                                today = today,
                                selected = selected,
                                events = events,
                                weekStart = weekStart,
                                showLunar = showLunar,
                                showFestival = showFestival,
                                onSelect = onSelect,
                            )
                        }
                    }
                }
            }
            item {
                Box(Modifier.padding(top = 4.dp)) {
                    CardDeck(
                        today = today,
                        selected = selected,
                        events = events,
                        countdowns = countdowns,
                        onEventClick = onEventClick,
                        onCountdownClick = onCountdownClick,
                    )
                }
            }
        }
    }
}