# ScreenShotTest — Screenshot Testing Demo Project

## Цель проекта
Учебный проект для демонстрации скриншот-тестирования на Android с Jetpack Compose и Roborazzi.

## Стек
- **AGP**: 9.0.1 (использует новый синтаксис `compileSdk { version = release(36) }`)
- **Kotlin**: 2.0.21 (встроен в AGP 9 — `kotlin-android` плагин НЕ применяется отдельно)
- **Jetpack Compose**: BOM 2025.05.00
- **Screenshot testing**: Roborazzi 1.40.0 (без Gradle-плагина — несовместим с AGP 9)
- **Test runner**: Robolectric 4.14.1 (JVM, без эмулятора)
- **Java**: 17 (на машине нет Java 11)

## Важные решения и почему

### Kotlin плагин НЕ применяется явно
AGP 9.0.1 включает Kotlin поддержку внутри себя. Если применить `kotlin-android` отдельно — ошибка `Cannot add extension with name 'kotlin'`.

### Roborazzi без Gradle-плагина
`io.github.takahirom.roborazzi` плагин версии 1.40.0 несовместим с AGP 9 (использует удалённый `TestedExtension`). Используем только библиотеки. Вместо `recordRoborazziDebug` / `verifyRoborazziDebug` — системные свойства:

```kotlin
// app/build.gradle.kts
testOptions {
    unitTests {
        isIncludeAndroidResources = true
        all { test ->
            test.systemProperty("roborazzi.test.record",
                project.findProperty("roborazzi.test.record") ?: "true")
            test.systemProperty("roborazzi.test.verify",
                project.findProperty("roborazzi.test.verify") ?: "false")
        }
    }
}
```

### kotlin { jvmToolchain(17) } вместо kotlinOptions
`kotlinOptions {}` удалён в AGP 9. Конфигурация JVM через `kotlin { jvmToolchain(17) }` внутри `android {}`.

## Структура
```
app/src/
  main/java/com/way/screenshottest/
    MainActivity.kt
    ui/theme/          Color.kt, Type.kt, Theme.kt
    ui/components/     ProfileCard.kt, TaskItem.kt
  test/java/com/way/screenshottest/screenshot/
    ProfileCardScreenshotTest.kt   (5 тестов)
    TaskItemScreenshotTest.kt      (5 тестов)
  test/screenshots/                (10 PNG эталонов)
```

## Команды

```bash
# Запись/обновление эталонных скриншотов (режим по умолчанию)
./gradlew testDebugUnitTest

# Верификация — поймать незапланированные изменения UI
./gradlew testDebugUnitTest -Proborazzi.test.record=false -Proborazzi.test.verify=true

# Сборка APK
./gradlew assembleDebug
```

## Сценарий демо
1. Показать PNG файлы в `app/src/test/screenshots/`
2. Изменить `StatusOnline` в `Color.kt` с зелёного на другой цвет
3. Запустить верификацию — тест упадёт с diff-изображением
4. Объяснить ценность: скриншот-тесты ловят неожиданные UI-регрессии
