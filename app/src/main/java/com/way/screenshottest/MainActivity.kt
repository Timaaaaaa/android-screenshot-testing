package com.way.screenshottest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.way.screenshottest.ui.components.Priority
import com.way.screenshottest.ui.components.ProfileCard
import com.way.screenshottest.ui.components.ProfileData
import com.way.screenshottest.ui.components.TaskData
import com.way.screenshottest.ui.components.TaskItem
import com.way.screenshottest.ui.components.UserStatus
import com.way.screenshottest.ui.theme.ScreenShotTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenShotTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Screenshot Testing Demo",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
        )

        ProfileCard(
            profile = ProfileData(
                name = "Алибек Т.",
                role = "Android Developer",
                status = UserStatus.Online,
                followers = 342,
                following = 128,
            ),
        )

        Text(
            text = "Tasks",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        )

        TaskItem(
            task = TaskData(
                title = "Implement screenshot tests",
                dueDate = "Jun 10, 2026",
                priority = Priority.High,
                isDone = false,
            ),
        )
        TaskItem(
            task = TaskData(
                title = "Setup Roborazzi",
                dueDate = "Jun 5, 2026",
                priority = Priority.Medium,
                isDone = true,
            ),
        )
        TaskItem(
            task = TaskData(
                title = "Write documentation",
                dueDate = "Jun 15, 2026",
                priority = Priority.Low,
                isDone = false,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoScreenPreview() {
    ScreenShotTestTheme {
        DemoScreen()
    }
}
