// Screenshot tests for TaskItem component
package com.way.screenshottest.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.way.screenshottest.ui.components.Priority
import com.way.screenshottest.ui.components.TaskData
import com.way.screenshottest.ui.components.TaskItem
import com.way.screenshottest.ui.theme.ScreenShotTestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class TaskItemScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Тест 1: Задача с высоким приоритетом — не выполнена
    @Test
    fun taskItem_highPriority_pending() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
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
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/task_high_priority_pending.png"
        )
    }

    // Тест 2: Задача выполнена — текст зачеркнут
    @Test
    fun taskItem_done() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
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
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/task_done.png"
        )
    }

    // Тест 3: Задача с низким приоритетом — проверяем синий бейдж
    @Test
    fun taskItem_lowPriority() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
                TaskItem(
                    task = TaskData(
                        title = "Write documentation",
                        dueDate = "Jun 20, 2026",
                        priority = Priority.Low,
                        isDone = false,
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/task_low_priority.png"
        )
    }

    // Тест 4: Список задач с разными приоритетами
    @Test
    fun taskList_allPriorities() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TaskItem(
                        task = TaskData("Критическая задача", "Jun 10, 2026", Priority.High, false)
                    )
                    TaskItem(
                        task = TaskData("Обычная задача", "Jun 15, 2026", Priority.Medium, false)
                    )
                    TaskItem(
                        task = TaskData("Маловажная задача", "Jun 20, 2026", Priority.Low, false)
                    )
                    TaskItem(
                        task = TaskData("Выполненная задача", "Jun 5, 2026", Priority.Medium, true)
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/task_list_all_priorities.png"
        )
    }

    // Тест 4: Темная тема
    @Test
    fun taskItem_darkTheme() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = true) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TaskItem(
                        task = TaskData("Критическая задача", "Jun 10, 2026", Priority.High, false)
                    )
                    TaskItem(
                        task = TaskData("Выполненная задача", "Jun 5, 2026", Priority.Medium, true)
                    )
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/task_item_dark.png"
        )
    }
}
