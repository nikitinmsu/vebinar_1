import java.time.Duration

// Плагины расширяют возможности Gradle
plugins {
    id("java")          // компиляция Java, тесты, jar
    id("application")   // запуск приложения и создание дистрибутивов
}

// Информация о проекте
group = "ru.stepup"
version = "1.0-SNAPSHOT"

// Явно указываем версию Java (Gradle скачает JDK, если её нет)
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Главный класс для `gradle run`
application {
    mainClass = "ru.stepup.Main"
}

// Откуда качать зависимости
repositories {
    mavenCentral()
}

// Зависимости: testImplementation — только для тестов
dependencies {
    implementation("com.codeborne:selenide:7.17.0")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// =====================================================================
//  ТЕСТЫ: несколько «групп» через tasks.register
// =====================================================================
//
//  Ключевая идея:
//    tasks.register<T>("имя") {...} — регистрирует ОПИСАНИЕ задачи, но сама
//    задача «создаётся» только когда реально понадобится (ленивость).
//    Поэтому проект конфигурируется быстро и лишней работы не делается.
//
//    Сравните с другими способами:
//      tasks.create("имя") {...}   — создаёт задачу СРАЗУ (старый способ, не советуем)
//      tasks.named("имя") {...}    — донастраивает уже существующую задачу
//
//  Посмотреть итоговый список задач:  ./gradlew tasks
//  (наши задачи появятся в разделе "Verification tasks" — это заслуга group)
//
//  Какие задачи создадим и КАК они отбирают тесты:
//    test             — юнит-тесты (стандартная задача, уже была)
//    integrationTest  — отбор по имени класса:  include("**/Integration*.class")
//    slowTest         — отбор по JUnit-тегу @Tag("slow")  +  больше памяти (-Xmx2g)
//    smokeTest        — отбор по тегу @Tag("smoke"), failFast
//    uiTest           — UI-тесты на Selenide, отбор по @Tag("ui")
//    smokeOrSlowTest / nonSlowTest / smokeAndFastTest / smokeOrSlowButNotUiTest
//                     — демо теговых ВЫРАЖЕНИЙ:  "|" (или), "&" (и), "!" (кроме)
//    runAllTests      — запустить все группы одним заходом
// =====================================================================

// ---------- 1. Юнит-тесты (стандартная задача test) ----------
tasks.test {
    useJUnitPlatform {
        // Юнит-тесты должны быть быстрыми, поэтому остальные группы «отдаём»
        // их собственным задачам. Исключаем тяжёлые, smoke и UI-тесты
        // по их JUnit-тегам прямо на уровне JUnit Platform.
        excludeTags("slow", "smoke", "ui")
    }

    // А это — исключение по имени класса (интеграционные тесты).
    // Работает на уровне Gradle, независимо от JUnit.
    exclude("**/Integration*.class")
}

// ---------- 2. Интеграционные тесты: отбор по имени класса ----------
val integrationTest by tasks.register<Test>("integrationTest") {
    // group и description — «метаданные» задачи, видны в `./gradlew tasks`
    group = "verification"
    description = "Runs integration tests (classes matching **/Integration*.class)"

    useJUnitPlatform()

    // НОВОЙ Test-задаче обязательно указать, где брать классы и classpath.
    // (у стандартной задачи test они задаются автоматически)
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    // Фильтр по имени класса (Ant-паттерн). Запустятся только классы, чей
    // путь к .class совпадает с паттерном, например ru/stepup/IntegrationCalculatorTest.class
    include("**/Integration*.class")

    // Прогонять test-классы в нескольких JVM параллельно
    maxParallelForks = 4

    // Если в одном прогоне есть и test, и integrationTest —
    // интеграционные идут ПОСЛЕ юнит-тестов (а не параллельно с ними)
    shouldRunAfter(tasks.test)
}

// ---------- 3. Тяжёлые / медленные тесты: отбор по JUnit-тегу ----------
val slowTest by tasks.register<Test>("slowTest") {
    group = "verification"
    description = "Runs slow/heavy tests (JUnit tag 'slow') with a bigger heap"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    // Отбор по тегам из @Tag("...") на классе/методе — НЕ по имени класса!
    // Делается на уровне JUnit Platform через useJUnitPlatform { includeTags(...) }
    useJUnitPlatform {
        includeTags("slow")
        // excludeTags("flaky")  // можно и исключать отдельные теги
    }

    // Тяжёлым тестам выделяем больше памяти через аргументы JVM.
    // Более «градовый» аналог: maxHeapSize = "2g"
    jvmArgs("-Xmx2g")

    // Чтобы не нагружать машину — гоняем строго по одному классу за раз
    maxParallelForks = 1

    // Каждые N тестов перезапускаем JVM (защита от «протёкшей» памяти)
    forkEvery = 50

    // Жёсткий таймаут на каждый тест-метод
    timeout = Duration.ofMinutes(10)

    shouldRunAfter(tasks.test)
}

// ---------- 4. Smoke-тесты: быстрые «проверки на жизнь» ----------
val smokeTest by tasks.register<Test>("smokeTest") {
    group = "verification"
    description = "Runs quick smoke tests (JUnit tag 'smoke')"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("smoke")
    }

    // Упал первый же тест — дальше гонять бессмысленно, останавливаемся
    failFast = true

    // Smoke-тесты обязаны быть молниеносными
    timeout = Duration.ofSeconds(30)
}

// ---------- 5. UI-тесты на Selenide (в стандартную сборку НЕ входят) ----------
val uiTest by tasks.register<Test>("uiTest") {
    group = "verification"
    description = "Runs UI tests with Selenide (JUnit tag 'ui'). Needs a browser!"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("ui")
    }

    // Системные свойства прокидываются в тестовую JVM через systemProperty
    systemProperty("selenide.browser", "chrome")  // какой браузер использовать
    systemProperty("selenide.headless", "true")   // работать без окна браузера

    // Альтернатива — JVM-аргумент для указания драйвера вручную:
    // jvmArgs("-Dwebdriver.chrome.driver=/usr/local/bin/chromedriver")
}

// ---------- 6. JUnit-теговые ВЫРАЖЕНИЯ: операторы ! & | ----------
//
//  JUnit Platform умеет отбирать тесты не только по одному тегу,
//  а по целому ВЫРАЖЕНИЮ:
//    "smoke"                  — только тег smoke
//    "smoke | slow"           — ЛИБО smoke, ЛИБО slow   (объединение)
//    "smoke & fast"           — И smoke, И fast одновременно (пересечение)
//    "!slow"                  — всё, КРОМЕ slow (отрицание)
//    "(smoke | slow) & !ui"   — скобки: (smoke или slow), но не ui
//
//  ВАЖНО: операторы — это функция JUnit Platform, поэтому они работают
//  ТОЛЬКО внутри useJUnitPlatform { includeTags("...") }. В Gradle-блоке
//  filter { } операторов НЕТ: там лишь includeTestsMatching("по имени").
//
//  Задачи ниже — чисто демонстрационные, в check/build они не входят.

// Пример 1: объединение — тег smoke ИЛИ slow
val smokeOrSlowTest by tasks.register<Test>("smokeOrSlowTest") {
    group = "verification"
    description = "Runs tests tagged 'smoke' OR 'slow' (expression: 'smoke | slow')"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("smoke | slow")
    }
}

// Пример 2: отрицание — ВСЕ тесты, кроме slow и ui
val nonSlowTest by tasks.register<Test>("nonSlowTest") {
    group = "verification"
    description = "Runs everything EXCEPT 'slow' and 'ui' (expression: '!slow & !ui')"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("!slow & !ui")
    }
}

// Пример 3: пересечение — И smoke, И fast одновременно.
// Наглядно работает потому, что класс SmokeTest помечен обоими тегами:
//   @Tag("smoke") @Tag("fast") class SmokeTest
val smokeAndFastTest by tasks.register<Test>("smokeAndFastTest") {
    group = "verification"
    description = "Runs tests tagged BOTH 'smoke' AND 'fast' (expression: 'smoke & fast')"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("smoke & fast")
    }
}

// Пример 4: комбинация со скобками — (smoke ИЛИ slow), но НЕ ui
val smokeOrSlowButNotUiTest by tasks.register<Test>("smokeOrSlowButNotUiTest") {
    group = "verification"
    description = "Runs (smoke OR slow) but NOT ui (expression: '(smoke | slow) & !ui')"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("(smoke | slow) & !ui")
    }
}

// ---------- 7. Подключение групп к стандартной сборке ----------
// Задача `check` запускается внутри `build`. Добавив свои тестовые задачи
// в её зависимости, мы «вплетаем» их в стандартный процесс сборки:
//   ./gradlew build  ==  compile + test + integrationTest + slowTest + smokeTest
tasks.named("check") {
    dependsOn(integrationTest, slowTest, smokeTest)
}

// UI-тесты в check сознательно НЕ добавляем: им нужен браузер и сеть.
// Запускаем их отдельной командой:  ./gradlew uiTest

// ---------- 8. Запустить все группы одним заходом ----------
tasks.register("runAllTests") {
    group = "verification"
    description = "Runs all test groups: unit, integration, slow, smoke"
    dependsOn("test", "integrationTest", "slowTest", "smokeTest")
}

// ---------- Полезные команды ----------
// ./gradlew test                  — только юнит-тесты
// ./gradlew integrationTest       — только интеграционные
// ./gradlew slowTest              — тяжёлые (с -Xmx2g)
// ./gradlew smokeTest             — быстрые проверки
// ./gradlew runAllTests           — все группы разом
// ./gradlew check                 — всё, что привязано к check (включая наши группы)
// ./gradlew build                 — check + упаковка в jar
//
// Демонстрация теговых выражений:
// ./gradlew smokeOrSlowTest       — тег smoke ИЛИ slow      ("smoke | slow")
// ./gradlew nonSlowTest           — всё, кроме slow и ui    ("!slow & !ui")
// ./gradlew smokeAndFastTest      — и smoke, и fast         ("smoke & fast")
// ./gradlew smokeOrSlowButNotUiTest — (smoke | slow) и не ui ("(smoke | slow) & !ui")
//
// Фильтры можно задавать прямо из командной строки, не трогая build-файл:
//   ./gradlew test --tests "ru.stepup.CalculatorTest"
//   ./gradlew test --tests "ru.stepup.CalculatorTest.testAdd"
//   ./gradlew integrationTest --tests "*Integration*"
//   ./gradlew test --tests "*" -i        # -i = подробный лог

// ---------- Примеры кастомных задач ----------

// Простейшая задача: выводит приветствие
tasks.register("hello") {
    doLast {
        println("Hello from Gradle! Project: ${project.name}, version: ${version}")
    }
}

// Задача, читающая системные свойства и рантайм JVM
tasks.register("showProps") {
    doLast {
        println("Java home: ${project.findProperty("javaHome") ?: System.getProperty("java.home")}")
        println("OS: ${System.getProperty("os.name")}")
        println("Available CPUs: ${Runtime.getRuntime().availableProcessors()}")
        println("Max memory: ${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB")
    }
}

// Копирование ресурсов через встроенный тип Copy
tasks.register<Copy>("copyResources") {
    from(layout.projectDirectory.file("src/main/resources"))
    into(layout.buildDirectory.dir("resources"))
}

// Упаковка jar в zip (с dependsOn для порядка выполнения)
tasks.register<Zip>("myDistZip") {
    dependsOn("jar")
    from(layout.buildDirectory.dir("libs")) {
        include("*.jar")
    }
    archiveFileName.set("${project.name}-${version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("dist"))
}

// Запуск Java-класса с аргументами через тип JavaExec
tasks.register<JavaExec>("runCustom") {
    dependsOn("classes")
    mainClass = "ru.stepup.Main"
    classpath = sourceSets.main.get().runtimeClasspath
    args("example", "arg")
}

// Запись информации о сборке в файл
tasks.register("buildInfo") {
    dependsOn("test")
    doLast {
        val infoFile = layout.buildDirectory.file("build-info.txt").get().asFile
        infoFile.writeText("""
            Project: ${project.name}
            Version: $version
            Build time: ${System.currentTimeMillis()}
            Test results: ${layout.buildDirectory.dir("reports/tests/test").get().asFile.exists()}
        """.trimIndent())
        println("Build info written to ${infoFile.path}")
    }
}

// Подключаем buildInfo к стандартному lifecycle-заданию build
tasks.named("build") {
    dependsOn("buildInfo")
}
