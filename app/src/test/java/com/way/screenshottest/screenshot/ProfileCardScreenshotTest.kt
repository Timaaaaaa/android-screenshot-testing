package com.way.screenshottest.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.way.screenshottest.ui.components.ProfileCard
import com.way.screenshottest.ui.components.ProfileData
import com.way.screenshottest.ui.components.UserStatus
import com.way.screenshottest.ui.theme.ScreenShotTestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot

// Тесты запускаются на JVM без эмулятора — благодаря Robolectric
// Скриншоты сохраняются в: app/build/outputs/roborazzi/
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ProfileCardScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Тест 1: Карточка пользователя — онлайн статус, светлая тема
    @Test
    fun profileCard_online_lightTheme() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
                ProfileCard(
                    profile = ProfileData(
                        name = "Алибек Т.",
                        role = "Android Developer",
                        status = UserStatus.Online,
                        followers = 342,
                        following = 128,
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/profile_card_online_light.png"
        )
    }

    // Тест 2: Занят — статус Busy
    @Test
    fun profileCard_busy_lightTheme() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
                ProfileCard(
                    profile = ProfileData(
                        name = "Алибек Т.",
                        role = "Android Developer",
                        status = UserStatus.Busy,
                        followers = 342,
                        following = 128,
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/profile_card_busy_light.png"
        )
    }

    // Тест 3: Офлайн статус
    @Test
    fun profileCard_offline_lightTheme() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
                ProfileCard(
                    profile = ProfileData(
                        name = "Алибек Т.",
                        role = "Android Developer",
                        status = UserStatus.Offline,
                        followers = 342,
                        following = 128,
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/profile_card_offline_light.png"
        )
    }

    // Тест 4: Длинное имя — проверяем что текст не обрезается странно
    @Test
    fun profileCard_longName() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = false) {
                ProfileCard(
                    profile = ProfileData(
                        name = "Александр Константинопольский",
                        role = "Senior Android & Kotlin Multiplatform Developer",
                        status = UserStatus.Online,
                        followers = 12_400,
                        following = 980,
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/profile_card_long_name.png"
        )
    }

    // Тест 5: Темная тема
    @Test
    fun profileCard_online_darkTheme() {
        composeTestRule.setContent {
            ScreenShotTestTheme(darkTheme = true) {
                ProfileCard(
                    profile = ProfileData(
                        name = "Алибек Т.",
                        role = "Android Developer",
                        status = UserStatus.Online,
                        followers = 342,
                        following = 128,
                    ),
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/profile_card_online_dark.png"
        )
    }
}
