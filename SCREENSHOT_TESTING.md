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

## Стек и зачем каждый инструмент

| Библиотека | Версия | Роль |
|---|---|---|
| Robolectric | 4.14.1 | Эмулирует Android API на обычной JVM — без эмулятора |
| Roborazzi | 1.40.0 | Делает скриншоты через Robolectric и сравнивает с эталоном |
| AGP | 9.0.1 | Android Gradle Plugin |
| Kotlin | 2.0.21 | Встроен в AGP 9, не подключается отдельно |

**Ключевой момент:** тесты запускаются как обычные unit-тесты (`./gradlew testDebugUnitTest`),
не требуют эмулятор или устройство. Это делает их быстрыми (~10 сек на 10 тестов)
и надёжными в CI.

### Как работает связка изнутри

```
JUnit запускает тест
  └─> RobolectricTestRunner инициализирует Android-окружение на JVM
        └─> @GraphicsMode(NATIVE) включает нативный Skia-рендер
              └─> composeTestRule.setContent {} рендерит Compose в off-screen bitmap
                    └─> captureRoboImage() сохраняет PNG или сравнивает с эталоном
```

Ключевая деталь: `@GraphicsMode(Mode.NATIVE)` переключает Robolectric с заглушки-рендерера
на настоящий Skia (тот же движок, что в реальном Android). Без этой аннотации
Compose рисует пустой белый прямоугольник.

---

## Как подключить в свой проект

### Шаг 1 — Добавить зависимости

В `gradle/libs.versions.toml`:

```toml
[versions]
roborazzi   = "1.40.0"
robolectric = "4.14.1"

[libraries]
roborazzi            = { group = "io.github.takahirom.roborazzi", name = "roborazzi",            version.ref = "roborazzi" }
roborazzi-compose    = { group = "io.github.takahirom.roborazzi", name = "roborazzi-compose",    version.ref = "roborazzi" }
roborazzi-junit-rule = { group = "io.github.takahirom.roborazzi", name = "roborazzi-junit-rule", version.ref = "roborazzi" }
robolectric          = { group = "org.robolectric",               name = "robolectric",          version.ref = "robolectric" }
```

> Без версионного каталога — указать зависимости напрямую в `dependencies {}` с явными версиями.

### Шаг 2 — Подключить в app/build.gradle.kts

```kotlin
dependencies {
    // уже есть в Compose-проекте:
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

В блоке `android {}` файла `app/build.gradle.kts`:

```kotlin
testOptions {
    unitTests {
        // Robolectric не найдёт ресурсы (strings, drawables) без этого
        isIncludeAndroidResources = true

        all { test ->
            // Переключение режима через Gradle-свойства (-P флаги)
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

По умолчанию (без `-P` флагов) тесты **записывают** эталоны. Безопасно:
при первом запуске PNG создаются автоматически, тесты всегда проходят.

### Шаг 4 — Написать тест

```kotlin
@RunWith(RobolectricTestRunner::class)   // JUnit использует Robolectric вместо JVM
@GraphicsMode(GraphicsMode.Mode.NATIVE)  // обязательно для Compose — включает Skia
@Config(sdk = [35])                      // явно фиксируем версию Android для детерминизма
class MyButtonScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun button_defaultState() {
        composeTestRule.setContent {
            MyAppTheme {
                MyButton(text = "Click me")
            }
        }
        // путь относительно папки app/
        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/button_default.png"
        )
    }
}
```

### Шаг 5 — Создать папку для эталонов и записать первый раз

```bash
mkdir -p app/src/test/screenshots

./gradlew testDebugUnitTest   # записывает PNG
```

Открой созданные PNG, убедись что выглядят правильно, и сделай `git add app/src/test/screenshots/`.
**Эти PNG — часть кодовой базы, их нужно коммитить.**

---

## Режимы работы

### Запись (по умолчанию)

```bash
./gradlew testDebugUnitTest
```

Перезаписывает PNG в `app/src/test/screenshots/`. Тест всегда проходит.
Используй после намеренного изменения дизайна.

### Верификация

```bash
./gradlew testDebugUnitTest -Proborazzi.test.record=false -Proborazzi.test.verify=true
```

Сравнивает рендер с PNG-эталоном попиксельно. Если есть расхождение — тест падает.
Используй в CI и перед коммитом.

### Где смотреть diff при падении

```
app/build/outputs/roborazzi/
├── profile_card_online_light_compare.png   ← эталон | diff | новый рендер
├── profile_card_online_light_actual.png    ← что рендерит код сейчас
└── ...
```

`_compare.png` — самый полезный файл: три картинки рядом. Сразу видно что изменилось.

---

## CI/CD: два workflow

### Workflow 1 — верификация при каждом PR

Файл: `.github/workflows/screenshot-tests.yml`

Запускается автоматически когда открываешь или обновляешь PR в `main`.
Прогоняет все тесты в режиме верификации.

```
Открыл PR → GitHub Actions запустил ubuntu-latest → 
  ./gradlew testDebugUnitTest -Proborazzi.test.verify=true →
    Тест прошёл → PR можно мёрджить ✓
    Тест упал  → Artifacts → screenshot-diffs → смотришь diff
```

На вкладке **Checks** в PR появляется список тестов с результатами (через `dorny/test-reporter`).
При падении — артефакт `screenshot-diffs` хранится 7 дней.

### Workflow 2 — перезапись эталонов на Linux

Файл: `.github/workflows/update-screenshots.yml`

Запускается **вручную**: GitHub → Actions → Update Screenshot Baselines → Run workflow.

Зачем нужен: macOS и Linux по-разному рендерят некоторые шрифты и антиалиасинг.
Если разрабатываешь на Mac а CI на Linux — после первого запуска тесты упадут из-за
мелких пиксельных расхождений, хотя UI не изменился.

Решение: один раз запусти этот workflow после добавления новых тестов. Он запишет
эталоны прямо на Linux и сделает коммит в ту же ветку.

```
Actions → Update Screenshot Baselines → Run workflow (выбери ветку) →
  Записывает PNG на ubuntu-latest →
  git commit "chore: update screenshot baselines [skip ci]" →
  Теперь верификационный workflow проходит ✓
```

---

## Интеграция в большой multi-module проект

### Структура модулей

В реальном проекте скриншот-тесты живут рядом с тестируемым кодом:

```
:core:design-system/
  src/test/java/.../screenshot/
    ButtonScreenshotTest.kt
    ChipScreenshotTest.kt
  src/test/screenshots/
    button_primary_enabled.png
    button_primary_disabled.png
    chip_selected.png

:feature:profile/
  src/test/java/.../screenshot/
    ProfileCardScreenshotTest.kt
  src/test/screenshots/
    profile_card_online.png
    profile_card_offline.png

:feature:feed/
  src/test/java/.../screenshot/
    FeedItemScreenshotTest.kt
  src/test/screenshots/
    feed_item_text.png
    feed_item_image.png
```

### Gradle-конфигурация через convention plugin

Чтобы не дублировать `testOptions {}` в каждом `build.gradle.kts`, создай
convention plugin в `build-logic/`:

```kotlin
// build-logic/src/main/kotlin/screenshot-test-convention.gradle.kts
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
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
}
```

Применяешь в каждом модуле одной строкой:
```kotlin
// feature/profile/build.gradle.kts
plugins {
    id("screenshot-test-convention")
}
```

### Запуск тестов по всем модулям

```bash
# Верификация всех модулей сразу
./gradlew testDebugUnitTest -Proborazzi.test.record=false -Proborazzi.test.verify=true

# Только один модуль
./gradlew :feature:profile:testDebugUnitTest -Proborazzi.test.verify=true

# Параллельно (ускоряет в ~2x на 4-ядерной машине)
./gradlew testDebugUnitTest --parallel -Proborazzi.test.verify=true
```

### Workflow для multi-module

Верификационный workflow одинаковый — `./gradlew testDebugUnitTest` сам найдёт
все модули. Единственное что меняется — пути к артефактам:

```yaml
- name: Upload diff images on failure
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: screenshot-diffs
    # ** захватывает вложенные модули
    path: '**/build/outputs/roborazzi/'
    retention-days: 7
```

И публикация результатов тестов:
```yaml
- name: Publish test results
  uses: dorny/test-reporter@v1
  if: always()
  with:
    name: Screenshot Test Results
    path: '**/build/test-results/testDebugUnitTest/*.xml'
    reporter: java-junit
```

---

## Продвинутая настройка: RoborazziRule

`RoborazziRule` даёт больше контроля чем прямой вызов `captureRoboImage`.

### Порог допустимого расхождения

Если шрифты на macOS и Linux слегка отличаются — можно задать допустимый процент
отличающихся пикселей вместо перезаписи эталонов:

```kotlin
@get:Rule
val roborazziRule = RoborazziRule(
    options = RoborazziRule.Options(
        roborazziOptions = RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(
                changeThreshold = 0.01f  // допускаем 1% отличающихся пикселей
            )
        )
    )
)
```

Используй с осторожностью: высокий порог маскирует реальные регрессии.

### Автоматический захват для каждого теста

```kotlin
@get:Rule
val roborazziRule = RoborazziRule(
    captureRoot = composeTestRule.onRoot(),
    options = RoborazziRule.Options(
        captureType = RoborazziRule.CaptureType.LastImage(),
        outputDirectoryPath = "src/test/screenshots"
    )
)

@Test
fun button_enabled() {
    composeTestRule.setContent { MyButton(enabled = true) }
    // скриншот сделается автоматически — имя файла = имя теста
}
```

---

## Naming convention для эталонов

Рекомендуемый паттерн: `{компонент}_{состояние}_{вариант}.png`

```
button_primary_enabled_light.png
button_primary_enabled_dark.png
button_primary_disabled_light.png
profile_card_online.png
profile_card_offline_long_name.png
feed_item_text_only.png
feed_item_with_image.png
```

Правила:
- всё в snake_case
- состояние (enabled/disabled/loading/error) — обязательно
- тема (light/dark) — если компонент по-разному выглядит в темах
- нет смысла добавлять имя класса в имя файла — папка `screenshots/` уже это определяет

---

## Частые ошибки

### Тест падает с NullPointerException или "No image found"

Не стоит `isIncludeAndroidResources = true` в `testOptions`. Без него Robolectric
не находит ресурсы (strings, drawables, темы).

### Скриншоты белые или пустые

Забыл `@GraphicsMode(GraphicsMode.Mode.NATIVE)`. Без неё Robolectric не включает
нативный Skia — Compose рендерится в пустой холст.

### На CI тест упал, локально всё ок

Эталоны записаны на macOS, CI работает на Linux. Запусти `Update Screenshot Baselines`
workflow (см. выше) — он перезапишет PNG прямо на ubuntu-latest.

### Тест всегда проходит, не сравнивает

Не прописаны `systemProperty` для `roborazzi.test.verify` в `testOptions`.
Без явной передачи этого свойства Roborazzi видит `null` и по умолчанию
работает в режиме записи.

### Ошибка "Cannot add extension with name 'kotlin'" (AGP 9+)

В AGP 9 Kotlin встроен внутрь. Не подключай `kotlin-android` плагин отдельно
в `plugins {}` — только `android-application`/`android-library` и `kotlin-compose`.

### Roborazzi Gradle Plugin не работает с AGP 9

Плагин `io.github.takahirom.roborazzi` несовместим с AGP 9 (использует удалённый
`TestedExtension`). Используй только библиотеки без плагина. Вместо задач
`recordRoborazziDebug` / `verifyRoborazziDebug` — системные свойства через `testOptions`.

---

## Сценарий демо (показать на собеседовании/ревью)

1. Открыть `app/src/test/screenshots/profile_card_online_light.png`
2. Найти `StatusOnline` в `Color.kt`:
   ```kotlin
   val StatusOnline = Color(0xFF4CAF50)  // зелёный
   ```
3. Изменить на красный:
   ```kotlin
   val StatusOnline = Color(0xFFE53935)
   ```
4. Запустить верификацию:
   ```bash
   ./gradlew testDebugUnitTest -Proborazzi.test.record=false -Proborazzi.test.verify=true
   ```
5. Тесты `profileCard_online_*` упадут. Открыть
   `app/build/outputs/roborazzi/profile_card_online_light_compare.png` — видно
   что зелёный кружок стал красным
6. Откатить `Color.kt`, запустить верификацию снова — всё проходит

Это и есть главная ценность: тест поймал **случайное** изменение цвета,
которое легко пропустить на code review.

---

## Что покрывать скриншот-тестами

Скриншот-тесты дороги в поддержке: при каждом намеренном изменении дизайна
нужно перезаписывать эталоны. Покрывай только важное:

| Покрывать | Не покрывать |
|---|---|
| Ключевые UI-компоненты (карточки, кнопки, формы) | Внутренние helper-функции |
| Все видимые состояния (loading, error, empty, filled) | Анимации (снимок статичен) |
| Светлая и тёмная тема | Компоненты без визуального вывода |
| Граничные данные (очень длинный текст, 0 элементов) | Дублирующиеся состояния |
