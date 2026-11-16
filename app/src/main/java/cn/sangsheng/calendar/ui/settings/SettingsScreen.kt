package cn.sangsheng.calendar.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.sangsheng.calendar.R
import cn.sangsheng.calendar.data.AppSettings
import cn.sangsheng.calendar.data.ThemeMode
import cn.sangsheng.calendar.data.WeekStart

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "返回",
                    modifier = Modifier.size(24.dp),
                    tint = cs.onSurface,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineMedium,
                color = cs.onSurface,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "外观", container = cs.surfaceContainerLow) {
                    Text(
                        text = "主题",
                        fontSize = 15.sp,
                        color = cs.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    SegmentedRow(
                        options = listOf(
                            ThemeMode.SYSTEM to "跟随系统",
                            ThemeMode.LIGHT to "浅色",
                            ThemeMode.DARK to "深色",
                        ),
                        selected = settings.themeMode,
                        onSelect = { settings.updateThemeMode(it) },
                    )
                    Spacer(Modifier.height(16.dp))
                    SwitchRow(
                        title = "跟随壁纸取色",
                        subtitle = "莫奈取色：跟随壁纸生成整套配色",
                        checked = settings.useWallpaperColor,
                        onCheckedChange = { settings.updateWallpaperColor(it) },
                    )
                }
            }

            item {
                SettingsCard(title = "日历显示", container = cs.surfaceContainerLow) {
                    Text(
                        text = "每周起始日",
                        fontSize = 15.sp,
                        color = cs.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    SegmentedRow(
                        options = listOf(
                            WeekStart.MONDAY to "周一",
                            WeekStart.SUNDAY to "周日",
                        ),
                        selected = settings.weekStart,
                        onSelect = { settings.updateWeekStart(it) },
                    )
                    Spacer(Modifier.height(14.dp))
                    SwitchRow(
                        title = "显示农历",
                        subtitle = "日期下方显示农历日名",
                        checked = settings.showLunar,
                        onCheckedChange = { settings.updateShowLunar(it) },
                    )
                    SwitchRow(
                        title = "显示节日与节气",
                        subtitle = "如寒衣节、立冬、小雪",
                        checked = settings.showFestival,
                        onCheckedChange = { settings.updateShowFestival(it) },
                    )
                }
            }

            item {
                SettingsCard(title = "关于", container = cs.surfaceContainerLow) {
                    InfoRow("应用名称", "日历")
                    InfoRow("版本", "1.0.0")
                    InfoRow("农历数据", "lunar-java 1.7.8")
                    InfoRow("配色", "Material You 动态取色")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "日程保存在本机，不上传任何服务器。",
                        fontSize = 12.sp,
                        color = cs.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    container: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = container,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            content()
        }
    }
}

@Composable
private fun <T> SegmentedRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(cs.surfaceContainerHighest)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) cs.secondaryContainer else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) cs.onSecondaryContainer else cs.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = cs.onSurface)
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 14.sp, color = cs.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(text = value, fontSize = 14.sp, color = cs.onSurface)
    }
}