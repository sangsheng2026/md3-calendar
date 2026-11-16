package cn.sangsheng.calendar.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cn.sangsheng.calendar.reminder.ReminderScheduler
import cn.sangsheng.calendar.data.AppSettings
import cn.sangsheng.calendar.data.CalendarEvent
import cn.sangsheng.calendar.data.CalendarRepository
import cn.sangsheng.calendar.data.CountdownItem
import cn.sangsheng.calendar.ui.day.DayScreen
import cn.sangsheng.calendar.ui.month.MonthScreen
import cn.sangsheng.calendar.ui.nav.BottomPillNav
import cn.sangsheng.calendar.ui.nav.CalTab
import cn.sangsheng.calendar.ui.settings.SettingsScreen
import cn.sangsheng.calendar.ui.week.WeekScreen
import cn.sangsheng.calendar.ui.year.YearScreen
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

@Composable
fun CalendarApp(settings: AppSettings) {
    val context = LocalContext.current
    val repo = remember { CalendarRepository.get(context) }
    val today = remember { LocalDate.now() }

    var selectedEpoch by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val selected = remember(selectedEpoch) { LocalDate.ofEpochDay(selectedEpoch) }

    var tabIndex by rememberSaveable { mutableStateOf(CalTab.MONTH.ordinal) }
    val tab = CalTab.entries.getOrElse(tabIndex) { CalTab.MONTH }

    var showSettings by rememberSaveable { mutableStateOf(false) }
    var focusMonth by remember { mutableStateOf<YearMonth?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    var creatingEvent by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var creatingCountdown by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<CountdownItem?>(null) }

    val events = repo.events
    val countdowns = repo.countdowns

    // 首次启动请求通知权限，否则提醒弹不出来
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // 设置页是覆盖层，必须自己拦返回键，否则手势返回会直接退出 App
    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(120))
                    },
                    label = "tabSwitch",
                ) { current ->
                    when (current) {
                        CalTab.YEAR -> YearScreen(
                            today = today,
                            events = events,
                            weekStart = settings.weekStart,
                            onPickMonth = { ym ->
                                focusMonth = ym
                                tabIndex = CalTab.MONTH.ordinal
                            },
                        )

                        CalTab.MONTH -> MonthScreen(
                            today = today,
                            selected = selected,
                            events = events,
                            countdowns = countdowns,
                            weekStart = settings.weekStart,
                            showLunar = settings.showLunar,
                            showFestival = settings.showFestival,
                            onSelect = { selectedEpoch = it.toEpochDay() },
                            onOpenAdd = { creatingEvent = true },
                            onOpenMenu = { showMenu = true },
                            onEventClick = { editingEvent = it },
                            onCountdownClick = { editingCountdown = it },
                            focusMonth = focusMonth,
                            onFocusConsumed = { focusMonth = null },
                        )

                        CalTab.WEEK -> WeekScreen(
                            today = today,
                            selected = selected,
                            events = events,
                            weekStart = settings.weekStart,
                            showLunar = settings.showLunar,
                            showFestival = settings.showFestival,
                            onSelect = { selectedEpoch = it.toEpochDay() },
                            onEventClick = { editingEvent = it },
                        )

                        CalTab.DAY -> DayScreen(
                            today = today,
                            selected = selected,
                            events = events,
                            showLunar = settings.showLunar,
                            showFestival = settings.showFestival,
                            onEventClick = { editingEvent = it },
                        )
                    }
                }
            }

            BottomPillNav(
                current = tab,
                today = today,
                onSelect = { tabIndex = it.ordinal },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp),
            )

            // 设置页：从右侧滑入，盖住整个界面（含底部导航）
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInHorizontally(tween(280)) { it } + fadeIn(tween(180)),
                exit = slideOutHorizontally(tween(240)) { it } + fadeOut(tween(140)),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        SettingsScreen(
                            settings = settings,
                            onBack = { showSettings = false },
                        )
                    }
                }
            }
        }
    }

    if (creatingEvent || editingEvent != null) {
        EventEditor(
            initial = editingEvent,
            defaultDate = selected,
            onDismiss = {
                creatingEvent = false
                editingEvent = null
            },
            onSave = { event ->
                repo.upsertEvent(event)
                ReminderScheduler.sync(context, event)
                creatingEvent = false
                editingEvent = null
            },
            onDelete = editingEvent?.let { item ->
                {
                    ReminderScheduler.cancel(context, item.id)
                    repo.deleteEvent(item.id)
                }
            },
        )
    }

    if (creatingCountdown || editingCountdown != null) {
        CountdownEditor(
            initial = editingCountdown,
            defaultDate = selected,
            onDismiss = {
                creatingCountdown = false
                editingCountdown = null
            },
            onSave = { item ->
                repo.upsertCountdown(item)
                creatingCountdown = false
                editingCountdown = null
            },
            onDelete = editingCountdown?.let { item ->
                { repo.deleteCountdown(item.id) }
            },
        )
    }

    if (showMenu) {
        MenuDialog(
            onDismiss = { showMenu = false },
            onToday = {
                selectedEpoch = today.toEpochDay()
                focusMonth = YearMonth.from(today)
                showMenu = false
            },
            onAddCountdown = {
                creatingCountdown = true
                showMenu = false
            },
            onSettings = {
                showSettings = true
                showMenu = false
            },
        )
    }
}

@Composable
private fun DatePickRow(
    label: String,
    date: LocalDate,
    onPick: (LocalDate) -> Unit,
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cs.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(
                    context,
                    { _, y, m, d -> onPick(LocalDate.of(y, m + 1, d)) },
                    date.year,
                    date.monthValue - 1,
                    date.dayOfMonth,
                ).show()
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = cs.onSecondaryContainer.copy(alpha = 0.7f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${date.year}年${date.monthValue}月${date.dayOfMonth}日",
                fontSize = 15.sp,
                color = cs.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun EventEditor(
    initial: CalendarEvent?,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var time by remember { mutableStateOf(initial?.timeText ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: defaultDate) }
    var remind by remember { mutableStateOf(initial?.remindEnabled ?: false) }
    var remindBefore by remember { mutableStateOf(initial?.remindMinutesBefore ?: 10) }
    val cs = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建日程" else "编辑日程") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("时间（如 14:00，可留空）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可留空）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                DatePickRow(label = "日期", date = date, onPick = { date = it })
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("提醒", fontSize = 15.sp, color = cs.onSurface)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = remind, onCheckedChange = { remind = it })
                }
                if (remind) {
                    ReminderOptions(selected = remindBefore, onSelect = { remindBefore = it })
                    if (time.isBlank()) {
                        Text(
                            text = "填了时间才能提醒",
                            fontSize = 11.sp,
                            color = cs.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        CalendarEvent(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            date = date,
                            timeText = time.takeIf { it.isNotBlank() },
                            note = note.trim(),
                            colorIndex = initial?.colorIndex ?: 0,
                            remindEnabled = remind,
                            remindMinutesBefore = remindBefore,
                        )
                    )
                },
            ) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = { onDelete() }) {
                        Text("删除", color = cs.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        },
    )
}

@Composable
private fun CountdownEditor(
    initial: CountdownItem?,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (CountdownItem) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var target by remember { mutableStateOf(initial?.target ?: defaultDate) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    val cs = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建倒数日" else "编辑倒数日") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可留空）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                DatePickRow(label = "目标日", date = target, onPick = { target = it })
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    onSave(
                        CountdownItem(
                            id = initial?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            target = target,
                            note = note.trim(),
                        )
                    )
                },
            ) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = { onDelete() }) {
                        Text("删除", color = cs.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        },
    )
}

@Composable
private fun MenuDialog(
    onDismiss: () -> Unit,
    onToday: () -> Unit,
    onAddCountdown: () -> Unit,
    onSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("更多") },
        text = {
            Column {
                MenuRow("回到今天", onToday)
                MenuRow("新建倒数日", onAddCountdown)
                MenuRow("设置", onSettings)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}

@Composable
private fun MenuRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}
@Composable
private fun ReminderOptions(selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val options = listOf(
        0 to "准时",
        10 to "提前10分",
        30 to "提前30分",
        60 to "提前1小时",
    )
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
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isSelected) cs.onSecondaryContainer else cs.onSurfaceVariant,
                )
            }
        }
    }
}