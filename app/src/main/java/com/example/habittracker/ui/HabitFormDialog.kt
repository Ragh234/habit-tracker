package com.example.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.habittracker.data.local.HabitEntity

/** Shared by the add flow on the list and the edit flow on the detail screen. */
@Composable
fun HabitFormDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialTargetPerWeek: Int = 7,
    initialColorHex: String = HabitEntity.DEFAULT_COLOR_HEX,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetPerWeek: Int, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var target by remember { mutableStateOf(initialTargetPerWeek.toString()) }
    var colorHex by remember { mutableStateOf(initialColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { input -> target = input.filter { it.isDigit() }.take(1) },
                    label = { Text("Days per week") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Colour")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HABIT_COLORS.forEach { swatch ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(swatch.toComposeColor())
                                .border(
                                    width = if (swatch == colorHex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = swatch }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, target.toIntOrNull()?.coerceIn(1, 7) ?: 7, colorHex) },
                enabled = name.isNotBlank()
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

val HABIT_COLORS = listOf("#4CAF50", "#2196F3", "#F44336", "#FF9800", "#9C27B0")

/** Falls back to the default rather than throwing if a stored value is ever malformed. */
fun String.toComposeColor(): Color = try {
    Color(android.graphics.Color.parseColor(this))
} catch (_: IllegalArgumentException) {
    Color(android.graphics.Color.parseColor(HabitEntity.DEFAULT_COLOR_HEX))
}
