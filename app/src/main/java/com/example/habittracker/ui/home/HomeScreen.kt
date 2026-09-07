package com.example.habittracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.data.HabitSummary
import com.example.habittracker.ui.HabitFormDialog
import com.example.habittracker.ui.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onHabitClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val habits by viewModel.summaries.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Habits") },
                actions = {
                    TextButton(onClick = onSettingsClick) { Text("Settings") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New habit") },
                icon = {},
                onClick = { showAddDialog = true }
            )
        }
    ) { innerPadding ->
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No habits yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits, key = { it.habit.id }) { summary ->
                    HabitRow(
                        summary = summary,
                        onClick = { onHabitClick(summary.habit.id) },
                        onCheckedChange = { checked ->
                            viewModel.setChecked(summary.habit.id, checked)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        HabitFormDialog(
            title = "New habit",
            confirmLabel = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, target, colorHex ->
                viewModel.addHabit(name, target, colorHex)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HabitRow(
    summary: HabitSummary,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(summary.habit.colorHex.toComposeColor())
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(summary.habit.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = streakLabel(summary.currentStreak) +
                        "  ·  " + summary.habit.targetPerWeek + "x per week",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Checkbox(
                checked = summary.doneToday,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

private fun streakLabel(streak: Int): String = when (streak) {
    0 -> "No streak"
    1 -> "1 day streak"
    else -> streak.toString() + " day streak"
}
