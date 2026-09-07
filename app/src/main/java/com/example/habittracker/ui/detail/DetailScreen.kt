package com.example.habittracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.ui.HabitFormDialog
import com.example.habittracker.ui.toComposeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    val habit = state.habit

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(habit?.name ?: "Habit") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = { showEdit = true }, enabled = habit != null) {
                        Text("Edit")
                    }
                    TextButton(onClick = { showDeleteConfirm = true }, enabled = habit != null) {
                        Text("Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (habit == null) {
                Text("Habit not found", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = state.currentStreak.toString() + " day streak",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Target " + habit.targetPerWeek + " days per week",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text("Last 30 days", style = MaterialTheme.typography.titleMedium)
            HistoryGrid(
                start = viewModel.historyStart,
                days = DetailViewModel.HISTORY_DAYS.toInt(),
                checkedDates = state.checkedDates,
                accent = habit.colorHex.toComposeColor()
            )
        }
    }

    if (showEdit && habit != null) {
        HabitFormDialog(
            title = "Edit habit",
            confirmLabel = "Save",
            initialName = habit.name,
            initialTargetPerWeek = habit.targetPerWeek,
            initialColorHex = habit.colorHex,
            onDismiss = { showEdit = false },
            onConfirm = { name, target, colorHex ->
                viewModel.updateHabit(name, target, colorHex)
                showEdit = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete habit?") },
            text = { Text("Its check-in history is deleted with it.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteHabit(onDeleted = onBack)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HistoryGrid(
    start: LocalDate,
    days: Int,
    checkedDates: Set<LocalDate>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val dates = remember(start, days) {
        (0 until days).map { start.plusDays(it.toLong()) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(dates, key = { it.toString() }) { date ->
            DayCell(date = date, checked = date in checkedDates, accent = accent)
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    checked: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = date.format(DAY_LABEL),
            style = MaterialTheme.typography.labelSmall
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (checked) accent else MaterialTheme.colorScheme.surfaceVariant
                )
        )
    }
}

private val DAY_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M")
