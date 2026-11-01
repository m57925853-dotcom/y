package com.focusquest.app.ui

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusquest.app.data.Settings
import com.focusquest.app.data.TaskEntity
import com.focusquest.app.ui.theme.Gold
import kotlinx.coroutines.launch

@Composable
fun MainScreen(settings: Settings, tasks: List<TaskEntity>) {
    val pager = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val items = listOf(
        "المهام" to Icons.AutoMirrored.Filled.List,
        "التعديل" to Icons.Default.Edit,
        "حسابي" to Icons.Default.Person
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = pager.currentPage == i,
                        onClick = { scope.launch { pager.animateScrollToPage(i) } },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { pad ->
        HorizontalPager(pager, Modifier.padding(pad).fillMaxSize()) { page ->
            when (page) {
                0 -> TasksPage(settings, tasks)
                1 -> Placeholder("تعديل المهام ووقت التركيز لكل يوم\n(المرحلة 2)")
                else -> ProfilePage(settings)
            }
        }
    }
}

@Composable
private fun TasksPage(settings: Settings, tasks: List<TaskEntity>) {
    Column(Modifier.padding(20.dp)) {
        Text("مهام اليوم", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "التركيز من ${fmtTime(settings.startMin)} إلى ${fmtTime(settings.endMin)}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tasks, key = { it.id }) { t ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(18.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(fmtDur(t.minutes), color = Gold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePage(settings: Settings) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.size(96.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(settings.name.take(1), fontSize = 40.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(12.dp))
        Text(settings.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("المستوى 1", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("XP", "0", Modifier.weight(1f))
            StatCard("العملات 🪙", "0", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Text("المتجر ونظام النقاط وساعات الإنجاز (المرحلة 4)", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Gold)
        }
    }
}

@Composable
private fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
