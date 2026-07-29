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

// Настройка тестов — используем JUnit Platform
tasks.test {
    useJUnitPlatform()
}

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
