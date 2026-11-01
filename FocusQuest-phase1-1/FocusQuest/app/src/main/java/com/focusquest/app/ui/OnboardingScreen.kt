package com.focusquest.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusquest.app.data.Settings
import com.focusquest.app.data.TaskEntity
import com.focusquest.app.ui.theme.Gold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val LAST_STEP = 3

@Composable
fun OnboardingScreen(onFinish: (Settings, List<TaskEntity>) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var start by remember { mutableIntStateOf(15 * 60) }
    var end by remember { mutableIntStateOf(22 * 60) }
    var brk by remember { mutableIntStateOf(120) }
    val tasks = remember { mutableStateListOf<TaskEntity>() }

    val draft = Settings(name.trim(), start, end, brk)
    val total = draft.totalMinutes
    val maxBreak = ((total - 15) / 15) * 15
    LaunchedEffect(total) { if (brk > maxBreak) brk = maxBreak.coerceAtLeast(0) }

    val net = draft.netMinutes
    val remaining = net - tasks.sumOf { it.minutes }
    val canNext = when (step) {
        0 -> name.isNotBlank()
        1 -> true
        2 -> net > 0
        else -> net > 0 && remaining == 0
    }

    Column(Modifier.systemBarsPadding().padding(20.dp)) {
        LinearProgressIndicator(
            progress = { (step + 1) / (LAST_STEP + 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                if (targetState > initialState)
                    (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                else
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            },
            label = "step"
        ) { s ->
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (s) {
                    0 -> StepName(name) { name = it }
                    1 -> StepWindow(start, end, { start = it }, { end = it })
                    2 -> StepBreak(total, maxBreak, brk, net) { brk = it }
                    else -> StepTasks(net, remaining, tasks)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) { Text("السابق") }
            }
            Button(
                onClick = {
                    if (step < LAST_STEP) step++ else onFinish(draft.copy(onboarded = true), tasks.toList())
                },
                enabled = canNext,
                modifier = Modifier.weight(2f)
            ) { Text(if (step < LAST_STEP) "التالي" else "ابدأ الرحلة 🚀") }
        }
    }
}

@Composable
private fun Title(text: String, sub: String? = null) {
    Text(text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    if (sub != null) {
        Spacer(Modifier.height(6.dp))
        Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun StepName(name: String, onChange: (String) -> Unit) {
    Title("أهلًا بك 👋", "خلّينا نبدأ... شو اسمك؟")
    OutlinedTextField(
        value = name,
        onValueChange = onChange,
        label = { Text("اسمك") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StepWindow(start: Int, end: Int, onStart: (Int) -> Unit, onEnd: (Int) -> Unit) {
    var pick by remember { mutableIntStateOf(0) } // 0 لا شيء، 1 البداية، 2 النهاية
    Title("وقت التركيز ⏰", "من أي ساعة إلى أي ساعة تقدر تركّز؟")
    TimeCard("من", fmtTime(start)) { pick = 1 }
    Spacer(Modifier.height(12.dp))
    TimeCard("إلى", fmtTime(end)) { pick = 2 }
    if (pick != 0) {
        TimeDialog(
            initial = if (pick == 1) start else end,
            onDismiss = { pick = 0 },
            onConfirm = { if (pick == 1) onStart(it) else onEnd(it); pick = 0 }
        )
    }
}

@Composable
private fun TimeCard(label: String, value: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(initial: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val state = rememberTimePickerState(initial / 60, initial % 60, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton({ onConfirm(state.hour * 60 + state.minute) }) { Text("تم") } },
        dismissButton = { TextButton(onDismiss) { Text("إلغاء") } },
        text = { TimePicker(state = state) }
    )
}

@Composable
private fun StepBreak(total: Int, maxBreak: Int, brk: Int, net: Int, onChange: (Int) -> Unit) {
    Title("وقت الراحة ☕", "كم تحتاج راحة (حمام، وضوء، صلاة...) داخل وقت التركيز؟")
    Text("إجمالي الوقت: ${fmtDur(total)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(12.dp))
    Text("الراحة: ${fmtDur(brk)}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    if (maxBreak > 0) {
        Slider(
            value = brk.toFloat(),
            onValueChange = { onChange(((it / 15).roundToInt() * 15).coerceIn(0, maxBreak)) },
            valueRange = 0f..maxBreak.toFloat(),
            steps = (maxBreak / 15 - 1).coerceAtLeast(0)
        )
    }
    Spacer(Modifier.height(16.dp))
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ساعات التركيز الصافية", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(fmtDur(net), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gold)
        }
    }
}

@Composable
private fun StepTasks(net: Int, remaining: Int, tasks: MutableList<TaskEntity>) {
    var title by remember { mutableStateOf("") }
    var dur by remember { mutableIntStateOf(60) }
    var apps by remember { mutableStateOf(setOf<String>()) }
    var showApps by remember { mutableStateOf(false) }
    val used = net - remaining

    Title("مهامك 📝", "وزّع ${fmtDur(net)} على مهامك (مثال: دراسة 5 ساعات)")
    LinearProgressIndicator(
        progress = { if (net > 0) (used / net.toFloat()).coerceIn(0f, 1f) else 0f },
        modifier = Modifier.fillMaxWidth().height(10.dp),
        color = Gold,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
    Spacer(Modifier.height(8.dp))
    Text(
        when {
            remaining > 0 -> "باقي ${fmtDur(remaining)} بدون مهام"
            remaining == 0 -> "ممتاز! غطّيت كل الوقت ✅"
            else -> "تجاوزت الوقت بـ ${fmtDur(-remaining)}، احذف أو عدّل مهمة"
        },
        color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))

    tasks.forEachIndexed { i, t ->
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t.title, fontWeight = FontWeight.Bold)
                    val n = if (t.allowedApps.isBlank()) 0 else t.allowedApps.split(",").size
                    Text(
                        fmtDur(t.minutes) + if (n > 0) " • $n تطبيق مسموح" else "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton({ tasks.removeAt(i) }) { Icon(Icons.Default.Delete, contentDescription = "حذف") }
            }
        }
    }

    if (remaining > 0) {
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("اسم المهمة") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton({ dur = (dur - 15).coerceAtLeast(5) }) { Text("−", fontSize = 22.sp) }
                    Text(
                        fmtDur(dur.coerceAtMost(remaining)),
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    FilledTonalIconButton({ dur = (dur + 15).coerceAtMost(remaining) }) { Text("+", fontSize = 22.sp) }
                }
                TextButton({ dur = remaining }) { Text("استخدم الوقت المتبقي كله") }
                OutlinedButton({ showApps = true }, Modifier.fillMaxWidth()) {
                    Text(if (apps.isEmpty()) "تطبيقات مسموحة أثناء المهمة (اختياري)" else "التطبيقات المسموحة: ${apps.size}")
                }
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = {
                        tasks.add(TaskEntity(title = title.trim(), minutes = dur.coerceAtMost(remaining), allowedApps = apps.joinToString(",")))
                        title = ""; apps = emptySet()
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("إضافة المهمة") }
            }
        }
    }

    if (showApps) {
        AppsDialog(apps, onDone = { apps = it; showApps = false }, onDismiss = { showApps = false })
    }
}

@Composable
private fun AppsDialog(selected: Set<String>, onDone: (Set<String>) -> Unit, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val apps by produceState(emptyList<AppInfo>()) {
        value = withContext(Dispatchers.Default) { loadApps(ctx) }
    }
    var sel by remember { mutableStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("التطبيقات المسموحة") },
        text = {
            LazyColumn(Modifier.heightIn(max = 380.dp)) {
                items(apps, key = { it.pkg }) { a ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { sel = if (a.pkg in sel) sel - a.pkg else sel + a.pkg }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = a.pkg in sel, onCheckedChange = null)
                        Spacer(Modifier.width(12.dp))
                        Text(a.label)
                    }
                }
            }
        },
        confirmButton = { TextButton({ onDone(sel) }) { Text("تم") } },
        dismissButton = { TextButton({ onDone(emptySet()) }) { Text("تخطي") } }
    )
}
