# Скриншот-тестирование на Android с Roborazzi

## Что это и зачем

Скриншот-тест рендерит Compose-компонент, делает PNG-снимок и сравнивает его
с эталонным файлом. Если пиксели не совпадают — тест падает и показывает diff.

**Что ловят скриншот-тесты:**
- случайно изменил цвет, шрифт, отступ
- компонент "поехал" при рефакторинге
- тема/локализация сломала верстку

**Что НЕ ловят:**
- логику (для этого unit-тесты)
- взаимодействия (для этого UI-тесты)

---

## Стек этого проекта

| Библиотека | Версия | Зачем |
|---|---|---|
| Robolectric | 4.14.1 | Запускает Android-код на JVM, без эмулятора |
| Roborazzi | 1.40.0 | Делает скриншоты и сравнивает с эталоном |
| AGP | 9.0.1 | Android Gradle Plugin |
| Kotlin | 2.0.21 | Встроен в AGP 9, не подключается отдельно |

**Ключевой момент:** тесты работают на обычном JVM (как unit-тесты), не требуют
эмулятор или реальное устройство. Это делает их быстрыми и удобными для CI.

---

## Как повторить в своём проекте с нуля

### Шаг 1 — Добавить зависимости

В `gradle/libs.versions.toml` добавить версии:

```toml
[versions]
roborazzi = "1.40.0"
robolectric = "4.14.1"

[libraries]
roborazzi             = { group = "io.github.takahirom.roborazzi", name = "roborazzi",             version.ref = "roborazzi" }
roborazzi-compose     = { group = "io.github.takahirom.roborazzi", name = "roborazzi-compose",     version.ref = "roborazzi" }
roborazzi-junit-rule  = { group = "io.github.takahirom.roborazzi", name = "roborazzi-junit-rule",  version.ref = "roborazzi" }
robolectric           = { group = "org.robolectric",               name = "robolectric",           version.ref = "robolectric" }
```

> Если не используешь версионный каталог — добавить напрямую в `dependencies {}`.

### Шаг 2 — Подключить в app/build.gradle.kts

```kotlin
dependencies {
    // уже есть в проекте:
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.androidx.compose.ui.test.junit4)

    // добавить:
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
}
```

### Шаг 3 — Настроить testOptions

В `android {}` блоке `app/build.gradle.kts` добавить:

```kotlin
testOptions {
    unitTests {
        // без этого Robolectric не найдёт ресурсы (drawable, strings и т.д.)
        isIncludeAndroidResources = true

        all { test ->
            // roborazzi.test.record=true  → перезаписывает эталоны
            // roborazzi.test.verify=true  → сравнивает с эталонами, падает при расхождении
            // можно переключать флагами через -P при запуске gradle
            test.systemProperty(
                "roborazzi.test.record",
                project.findProperty("roborazzi.test.record") ?: "true"
            )
            test.systemProperty(
                "roborazzi.test.verify",
                project.findProperty("roborazzi.test.verify") ?: "false"
            )
        }
    }
}
```

По умолчанию (без флагов) тесты **записывают** эталоны. Это безопасно: при первом
запуске все PNG создаются автоматически.

### Шаг 4 — Написать тест

Создать файл в `app/src/test/java/.../screenshot/MyComponentScreenshotTest.kt`:

```kotlin
package com.example.myapp.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.example.myapp.ui.components.MyButton
import com.example.myapp.ui.theme.MyAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)      // запускаем через Robolectric (JVM)
@GraphicsMode(GraphicsMode.Mode.NATIVE)     // NATIVE нужен для Compose-рендеринга
@Config(sdk = [35])                          // версия Android SDK для симуляции
class MyButtonScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun myButton_defaultState() {
        // 1. Рендерим компонент
        composeTestRule.setContent {
            MyAppTheme {
                MyButton(text = "Click me")
            }
        }

        // 2. Делаем скриншот и сохраняем/сравниваем
        // Путь указывается относительно папки app/
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/my_button_default.png"
        )
    }
}
```

**Разбор аннотаций:**

- `@RunWith(RobolectricTestRunner::class)` — говорит JUnit использовать Robolectric
  вместо обычного JVM. Это позволяет вызывать Android API (View, Canvas и т.д.)
- `@GraphicsMode(GraphicsMode.Mode.NATIVE)` — включает нативный рендеринг Skia.
  Без него Compose не рисует правильно
- `@Config(sdk = [35])` — какую версию Android эмулировать. Лучше указывать явно,
  чтобы тесты были детерминированы на всех машинах

### Шаг 5 — Создать папку для эталонов

```
app/src/test/screenshots/   ← сюда попадут PNG-файлы
```

Папку нужно создать вручную или она создастся автоматически при первом запуске.
**Эти PNG нужно коммитить в git** — они и есть эталон.

### Шаг 6 — Запустить первый раз (создать эталоны)

```bash
./gradlew testDebugUnitTest
```

После этого в `app/src/test/screenshots/` появятся PNG-файлы.
Открой их, убедись что выглядят правильно, и сделай `git add`.

---

## Режимы работы

### Режим записи (по умолчанию)

```bash
./gradlew testDebugUnitTest
```

Что происходит: тест рендерит компонент, сохраняет PNG в `src/test/screenshots/`.
Если файл уже есть — перезаписывает его. Тест **всегда проходит**.

Когда использовать: после намеренного изменения UI (новый дизайн, новый цвет).

### Режим верификации

```bash
./gradlew testDebugUnitTest -Proborazzi.test.record=false -Proborazzi.test.verify=true
```

Что происходит: тест рендерит компонент, сравнивает пиксели с PNG в
`src/test/screenshots/`. Если есть отличия — тест **падает**.

Когда использовать: перед коммитом, в CI/CD при PR.

### Где смотреть результаты при падении

```
app/build/outputs/roborazzi/
```

Для каждого упавшего теста создаётся три файла:
- `*_compare.png` — эталон слева, новый вариант справа, diff посередине
- `*_actual.png` — что рендерит код сейчас
- исходный эталон в `src/test/screenshots/`

---

## Структура тестов в этом проекте

```
app/src/test/
├── java/com/way/screenshottest/screenshot/
│   ├── ProfileCardScreenshotTest.kt   (5 тестов)
│   └── TaskItemScreenshotTest.kt      (5 тестов)
└── screenshots/                        (10 PNG эталонов)
    ├── profile_card_online_light.png
    ├── profile_card_busy_light.png
    ├── profile_card_offline_light.png
    ├── profile_card_long_name.png
    ├── profile_card_online_dark.png
    ├── task_high_priority_pending.png
    ├── task_done.png
    ├── task_low_priority.png
    ├── task_list_all_priorities.png
    └── task_item_dark.png
```

Каждый тест покрывает отдельное состояние компонента. Типичный набор:
- светлая тема
- тёмная тема
- граничные данные (очень длинный текст, пустое поле)
- разные состояния (активный/неактивный, ошибка/успех)

---

## Сценарий демо

1. Открыть `app/src/test/screenshots/profile_card_online_light.png`
2. Найти `StatusOnline` в `app/src/main/java/.../ui/theme/Color.kt`:
   ```kotlin
   val StatusOnline = Color(0xFF4CAF50) // зелёный
   ```
3. Изменить на красный:
   ```kotlin
   val StatusOnline = Color(0xFFE53935) // красный
   ```
4. Запустить верификацию:
   ```bash
   ./gradlew testDebugUnitTest -Proborazzi.test.record=false -Proborazzi.test.verify=true
   ```
5. Тесты `profileCard_online_*` упадут. Открыть diff в
   `app/build/outputs/roborazzi/` — видно что зелёный кружок стал красным
6. Откатить изменение в `Color.kt` и запустить верификацию снова — всё зелено

---

## GitHub Actions: автозапуск при PR

Файл `.github/workflows/screenshot-tests.yml` уже создан в проекте.

### Что происходит при создании PR

1. GitHub запускает виртуальную машину Ubuntu
2. Устанавливает JDK 17 и настраивает Gradle
3. Запускает `./gradlew testDebugUnitTest` в режиме верификации
4. Если тест упал — загружает diff-картинки как артефакт (кнопка **Artifacts**
   на странице запуска в GitHub Actions)

### Содержимое workflow-файла

```yaml
name: Screenshot Tests

on:
  pull_request:
    branches: [ main, master ]   # запускается только при PR в main или master

jobs:
  screenshot-verify:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4          # скачать код из репозитория

      - uses: actions/setup-java@v4        # установить JDK 17
        with:
          java-version: '17'
          distribution: 'temurin'

      - uses: gradle/actions/setup-gradle@v3   # кешировать Gradle (ускоряет сборку)

      - run: chmod +x ./gradlew            # сделать gradlew исполняемым (Linux)

      - name: Run screenshot verification
        run: ./gradlew testDebugUnitTest -Proborazzi.test.record=false -Proborazzi.test.verify=true

      - name: Upload diffs on failure      # загружает артефакт только если тест упал
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: screenshot-diffs
          path: app/build/outputs/roborazzi/
          retention-days: 7               # хранить 7 дней, потом удалить
```

### Как опубликовать проект на GitHub

```bash
# 1. Инициализировать git в папке проекта
git init

# 2. Добавить все файлы
git add .

# 3. Первый коммит (PNG-эталоны должны войти в коммит!)
git commit -m "Initial commit with screenshot tests"

# 4. Создать репозиторий на github.com (через сайт или gh cli):
gh repo create my-project --public --source=. --push

# ИЛИ вручную:
git remote add origin https://github.com/<username>/<repo>.git
git branch -M main
git push -u origin main
```

После этого создай любую ветку, измени что-нибудь, открой PR — Actions запустится
автоматически.

---

## Частые ошибки и как их исправить

### Тест падает с "No image found" или NullPointerException

Скорее всего не стоит `isIncludeAndroidResources = true` в `testOptions`.
Добавь в `build.gradle.kts`:
```kotlin
testOptions {
    unitTests {
        isIncludeAndroidResources = true
    }
}
```

### Скриншоты выглядят пустыми / всё белое

Забыл `@GraphicsMode(GraphicsMode.Mode.NATIVE)`. Эта аннотация обязательна
для Compose — без неё рендеринг не работает.

### Тест всегда проходит, эталон не обновляется

Проверь что systemProperty для `roborazzi.test.record` и `roborazzi.test.verify`
прописаны в `testOptions`. Без них Roborazzi игнорирует флаги.

### На CI тест упал, хотя локально всё ок

Шрифты на Linux и macOS различаются — это нормально. Решения:
- Принять расхождение и обновить эталоны на Linux: запустить
  `./gradlew testDebugUnitTest` в GitHub Actions с режимом записи, скачать артефакт
- Использовать `RoborazziRule` с настройкой порога допустимого расхождения

### Ошибка "Cannot add extension with name 'kotlin'" (AGP 9+)

В AGP 9 Kotlin встроен внутрь. Не подключай плагин `kotlin-android` отдельно
в `plugins {}`. Только `android-application` и `kotlin-compose`.

### Roborazzi Gradle Plugin не работает с AGP 9

Плагин `io.github.takahirom.roborazzi` версии 1.40.0 несовместим с AGP 9.
Используй только библиотеки (как в этом проекте), без плагина.
Вместо задач `recordRoborazziDebug` / `verifyRoborazziDebug` — системные свойства
через `testOptions`.

---

## Совет: что покрывать скриншот-тестами

Скриншот-тесты дороги в поддержке (при каждом намеренном изменении дизайна
надо обновлять эталоны). Покрывай только то, что действительно важно:

- **Да:** ключевые компоненты (карточки, кнопки, формы)
- **Да:** состояния с разными данными (пустой/заполненный, ошибка/успех)
- **Да:** светлая и тёмная тема, если поддерживается
- **Нет:** внутренние helper-функции, которые не рендерятся напрямую
- **Нет:** анимации (скриншот делается в статике)
