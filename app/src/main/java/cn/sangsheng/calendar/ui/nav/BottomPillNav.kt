package cn.sangsheng.calendar.ui.nav

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.sangsheng.calendar.R
import java.time.LocalDate

enum class CalTab(val label: String, @DrawableRes val icon: Int) {
    YEAR("年", R.drawable.ic_tab_year),
    MONTH("月", R.drawable.ic_tab_month),
    WEEK("周", R.drawable.ic_tab_week),
    DAY("日", R.drawable.ic_tab_day),
}

/**
 * MD3 大色块胶囊导航。选中项是一枚 secondaryContainer 实心胶囊；
 * "日"入口直接显示今天的日期数字（与常见中文日历一致），不用图标。
 */
@Composable
fun BottomPillNav(
    current: CalTab,
    today: LocalDate,
    onSelect: (CalTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalTab.entries.forEach { tab ->
                val selected = tab == current
                val container by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    label = "navContainer",
                )
                val content by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "navContent",
                )
                val width by animateDpAsState(
                    targetValue = if (selected) 82.dp else 54.dp,
                    label = "navWidth",
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.02f else 0.92f,
                    animationSpec = tween(durationMillis = 240),
                    label = "navIconScale",
                )

                Row(
                    modifier = Modifier
                        .width(width)
                        .clip(RoundedCornerShape(50))
                        .background(container)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (tab == CalTab.DAY) {
                        Box(Modifier.scale(iconScale)) {
                            DayNumberBadge(today.dayOfMonth, content)
                        }
                    } else {
                        Icon(
                            painter = painterResource(tab.icon),
                            contentDescription = tab.label,
                            modifier = Modifier.size(22.dp).scale(iconScale),
                            tint = content,
                        )
                    }
                    if (selected) {
                        Text(
                            text = tab.label,
                            color = content,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

/** 方框里写当天日期 */
@Composable
private fun DayNumberBadge(dayOfMonth: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(1.7.dp, color, RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayOfMonth.toString(),
            fontSize = if (dayOfMonth >= 10) 10.sp else 12.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}