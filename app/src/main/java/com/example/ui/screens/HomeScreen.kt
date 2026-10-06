package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNavDestination
import com.example.data.model.HabitItem
import com.example.data.model.TaskItem
import com.example.ui.components.AddHabitDialog
import com.example.ui.components.AddNoteDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatOverviewCard
import com.example.ui.theme.AppleSystemBlue
import com.example.ui.theme.AppleSystemGreen
import com.example.ui.theme.AppleSystemOrange
import com.example.ui.theme.AppleSystemPurple
import com.example.ui.viewmodel.EspacioUiState
import com.example.ui.viewmodel.EspacioViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: EspacioUiState,
    viewModel: EspacioViewModel,
    modifier: Modifier = Modifier
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    val completedTasksCount = uiState.tasks.count { it.isCompleted }
    val totalTasksCount = uiState.tasks.size
    val totalBalance = uiState.transactions.fold(0.0) { acc, tx ->
        if (tx.isIncome) acc + tx.amount else acc - tx.amount
    }
    val completedHabitsToday = uiState.habits.count { it.completedToday }
    val totalHabits = uiState.habits.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // iOS Large Title Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "LUNES, 6 DE OCTUBRE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mi Espacio",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        letterSpacing = (-0.8).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Apple Widgets (2x2 Grid)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatOverviewCard(
                    title = "Tareas",
                    value = "$completedTasksCount/$totalTasksCount",
                    subtitle = if (totalTasksCount > 0) "${(completedTasksCount * 100 / totalTasksCount)}% listas" else "0%",
                    icon = Icons.Default.Checklist,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AppleSystemBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppNavDestination.TASKS) }
                )
                StatOverviewCard(
                    title = "Hábitos",
                    value = "$completedHabitsToday/$totalHabits",
                    subtitle = "Completados hoy",
                    icon = Icons.Default.LocalFireDepartment,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AppleSystemOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppNavDestination.HABITS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatOverviewCard(
                    title = "Balance",
                    value = "$${String.format(Locale.US, "%.0f", totalBalance)}",
                    subtitle = "Saldo disponible",
                    icon = Icons.Default.AccountBalanceWallet,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AppleSystemGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppNavDestination.FINANCES) }
                )
                StatOverviewCard(
                    title = "Notas",
                    value = "${uiState.notes.size}",
                    subtitle = "Guardadas",
                    icon = Icons.Default.Description,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AppleSystemPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppNavDestination.NOTES) }
                )
            }
        }

        // Apple Quick Action Pills
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(title = "Acciones Rápidas")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                item {
                    AppleActionPill(
                        label = "✨ Gemini IA Studio",
                        isAccent = true,
                        onClick = { viewModel.navigateTo(AppNavDestination.AI_STUDIO) },
                        tag = "chip_gemini_ai_studio"
                    )
                }
                item {
                    AppleActionPill(
                        label = "+ Tarea",
                        onClick = { showAddTaskDialog = true },
                        tag = "chip_add_task"
                    )
                }
                item {
                    AppleActionPill(
                        label = "+ Hábito",
                        onClick = { showAddHabitDialog = true },
                        tag = "chip_add_habit"
                    )
                }
                item {
                    AppleActionPill(
                        label = "+ Movimiento",
                        onClick = { showAddTxDialog = true },
                        tag = "chip_add_tx"
                    )
                }
                item {
                    AppleActionPill(
                        label = "+ Nota",
                        onClick = { showAddNoteDialog = true },
                        tag = "chip_add_note"
                    )
                }
            }
        }

        // Inset Grouped: Tareas Pendientes (Apple iOS style)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Tareas de Hoy",
                actionText = "Ver todas",
                onActionClick = { viewModel.navigateTo(AppNavDestination.TASKS) }
            )
        }

        val pendingTasks = uiState.tasks.filter { !it.isCompleted }.take(4)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (pendingTasks.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AppleSystemGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AppleSystemGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Todo al día",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "No tienes tareas pendientes urgentes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column {
                        pendingTasks.forEachIndexed { index, task ->
                            AppleTaskRow(
                                task = task,
                                onToggle = { viewModel.toggleTask(task.id) }
                            )
                            if (index < pendingTasks.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Habits Carousel
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(
                title = "Hábitos en Racha",
                actionText = "Ver todos",
                onActionClick = { viewModel.navigateTo(AppNavDestination.HABITS) }
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.habits, key = { it.id }) { habit ->
                    AppleHabitCard(
                        habit = habit,
                        onToggle = { viewModel.toggleHabitToday(habit.id) }
                    )
                }
            }
        }

        // Apple Hero Banner (Gemini AI Studio)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { viewModel.navigateTo(AppNavDestination.AI_STUDIO) }
                    .testTag("gemini_studio_callout_banner"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF007AFF),
                                    Color(0xFF5856D6),
                                    Color(0xFFAF52DE)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "GEMINI MULTIMODAL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Estudio de Inteligencia Artificial",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = (-0.4).sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chatbot con roles, videos con Veo 3, música con Lyria, búsqueda en vivo y transcripción de voz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(AppNavDestination.AI_STUDIO) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AppleSystemBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Abrir Estudio IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, cat, prio -> viewModel.addTask(title, cat, prio) }
        )
    }
    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { name, days, col -> viewModel.addHabit(name, days, col) }
        )
    }
    if (showAddTxDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTxDialog = false },
            onConfirm = { t, a, i, c -> viewModel.addTransaction(t, a, i, c) }
        )
    }
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { t, c, col -> viewModel.addNote(t, c, col) }
        )
    }
}

// Apple iOS Pill Button
@Composable
fun AppleActionPill(
    label: String,
    onClick: () -> Unit,
    tag: String,
    isAccent: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isAccent) AppleSystemBlue else MaterialTheme.colorScheme.surface,
        border = if (isAccent) null else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isAccent) Color.White else MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Apple Inset Row
@Composable
fun AppleTaskRow(
    task: TaskItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("task_row_${task.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.isCompleted) AppleSystemGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = task.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = task.priority.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(task.priority.colorHex)
                )
            }
        }
    }
}

// Apple Habit Card
@Composable
fun AppleHabitCard(
    habit: HabitItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable { onToggle() }
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(habit.colorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(habit.colorHex),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AppleSystemOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${habit.streak}d",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppleSystemOrange
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${habit.completedDaysThisWeek}/${habit.targetDaysPerWeek} días",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (habit.completedToday) AppleSystemGreen else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (habit.completedToday) Color.White else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (habit.completedToday) "Listo ✓" else "Completar",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
