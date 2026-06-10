package com.way.screenshottest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.way.screenshottest.ui.theme.PriorityHigh
import com.way.screenshottest.ui.theme.PriorityLow
import com.way.screenshottest.ui.theme.PriorityMedium
import com.way.screenshottest.ui.theme.ScreenShotTestTheme
import com.way.screenshottest.ui.theme.StatusOnline

enum class Priority { High, Medium, Low }

data class TaskData(
    val title: String,
    val dueDate: String,
    val priority: Priority,
    val isDone: Boolean,
)

@Composable
fun TaskItem(
    task: TaskData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            PriorityBadge(priority = task.priority)
        }
    }
}

@Composable
private fun PriorityBadge(priority: Priority) {
    val (color, label) = when (priority) {
        Priority.High -> PriorityHigh to "HIGH"
        Priority.Medium -> PriorityMedium to "MED"
        Priority.Low -> PriorityLow to "LOW"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = StatusOnline,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemHighPriorityPreview() {
    ScreenShotTestTheme {
        TaskItem(
            task = TaskData(
                title = "Implement screenshot tests",
                dueDate = "Jun 10, 2026",
                priority = Priority.High,
                isDone = false,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskItemDonePreview() {
    ScreenShotTestTheme {
        TaskItem(
            task = TaskData(
                title = "Setup Roborazzi",
                dueDate = "Jun 5, 2026",
                priority = Priority.Medium,
                isDone = true,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
