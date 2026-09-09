package ru.stepup.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import org.aeonbits.owner.ConfigFactory;
import ru.stepup.config.core.Profile;
import ru.stepup.config.core.ProjectConfig;
import ru.stepup.config.manual.ClasspathProperties;
import ru.stepup.config.manual.FileProperties;
import ru.stepup.config.owner.MutableConfig;
import ru.stepup.config.owner.OwnerFeatures;

/**
 * =====================================================================
 *  ConfigDemoMain — сквозная демонстрация всех способов конфигурирования
 * =====================================================================
 *  Запуск:  ./gradlew runConfigDemo        (задача в build.gradle.kts)
 *  или:     ./gradlew run -Dapp.env=test   (сменить контур на test)
 *
 *  Программа последовательно показывает материалы вебинара:
 *    БЛОК 1. Ручное чтение через getResourceAsStream (classpath);
 *    БЛОК 2. Ручное чтение через FileInputStream (внешний файл);
 *    БЛОК 3. Типизированный конфиг aeonbits.owner (OwnerFeatures);
 *    БЛОК 4. owner: Mutable/Accessible — смена значений на лету;
 *    БЛОК 5. Единая точка входа ProjectConfig + контура dev/test/prod.
 *
 *  Код намеренно «плоский» и с подробными комментариями — это конспект
 *  вебинара, а не боевой сервис.
 */
public final class ConfigDemoMain {

    /** Приватный конструктор: класс с main() не инстанцируется. */
    private ConfigDemoMain() {
        throw new AssertionError("Не инстанцируется");
    }

    /**
     * Точка входа демонстрации.
     *
     * @param args не используются
     */
    public static void main(String[] args) throws IOException {
        System.out.println("========== Вебинар: конфигурирование проекта ==========");

        block1_classpath();
        block2_externalFile();
        block3_ownerFeatures();
        block4_ownerMutable();
        block5_profiles();

        System.out.println("========== Демонстрация завершена ==========");
    }

    // -----------------------------------------------------------------
    //  БЛОК 1. getResourceAsStream — читаем файл из classpath
    // -----------------------------------------------------------------
    private static void block1_classpath() {


        System.out.println("\n--- БЛОК 1: ClasspathProperties (getResourceAsStream) ---");

        // Ресурс лежит в src/main/resources/config/manual-example.properties
        // и после сборки попадает в classpath (build/classes, jar).
        Properties props = ClasspathProperties.load("config/manual-example.properties");

        // Всё руками: достаём строку и сами конвертируем в int.
        String host = ClasspathProperties.get(props, "host", "localhost");
        int port = Integer.parseInt(ClasspathProperties.get(props, "port", "8080"));
        String user = ClasspathProperties.get(props, "username", "user");

        System.out.printf("  host=%s, port=%d, username=%s%n", host, port, user);
    }

    // -----------------------------------------------------------------
    //  БЛОК 2. FileInputStream — читаем ВНЕШНИЙ файл с диска
    // -----------------------------------------------------------------
    private static void block2_externalFile() throws IOException {
        System.out.println("\n--- БЛОК 2: FileProperties (FileInputStream, внешний файл) ---");

        // Для наглядности создаём настоящий файл во временной папке и пишем в него
        // пару свойств — как будто это конфиг, положенный рядом с приложением.
        Path tempFile = Files.createTempFile("demo-external", ".properties");
        Files.writeString(tempFile,
                "# Внешний файл конфигурации\n"
                        + "host = external-host\n"
                        + "port = 9999\n"
                        + "password = super-secret-not-in-git\n",
                StandardCharsets.UTF_8);

        try {
            Properties props = FileProperties.load(tempFile); // <-- FileInputStream внутри
            System.out.printf("  %s%n", tempFile.toAbsolutePath());
            System.out.printf("  host=%s, port=%s, password=%s%n",
                    props.getProperty("host"),
                    props.getProperty("port"),
                    props.getProperty("password"));
        } finally {
            // Подчищаем временный файл после демонстрации.
            Files.deleteIfExists(tempFile);
        }
    }

    // -----------------------------------------------------------------
    //  БЛОК 3. aeonbits.owner — OwnerFeatures (витрина возможностей)
    // -----------------------------------------------------------------
    private static void block3_ownerFeatures() {
        System.out.println("\n--- БЛОК 3: OwnerFeatures (aeonbits.owner) ---");

        // Один вызов фабрики — и у нас типизированный объект конфига.
        OwnerFeatures features = ConfigFactory.create(OwnerFeatures.class);

        // Никаких parseInt/parseBoolean: owner сам сконвертировал типы.
        System.out.println("  appName        = " + features.appName());
        System.out.println("  retryCount     = " + features.retryCount() + " (int)");
        System.out.println("  pi             = " + features.pi() + " (double)");
        System.out.println("  enabled        = " + features.enabled() + " (boolean)");
        System.out.println("  timeUnit       = " + features.timeUnit() + " (enum)");
        System.out.println("  fruits         = " + String.join(", ", features.fruits()) + " (String[])");
        System.out.println("  fibonacci[10]  = " + features.fibonacci()[10] + " (@Separator \";\")");
        System.out.println("  homepage       = " + features.homepage() + " (URL)");
        System.out.println("  notesFile      = " + features.notesFile() + " (File, ~ развёрнут)");
        System.out.println("  primaryDb      = " + features.primaryDb() + " (Endpoint, @ConverterClass)");
        System.out.println("  replicaDbs     = " + features.replicaDbs() + " (List<Endpoint>)");
        System.out.println("  dbEndpoint     = " + features.dbEndpoint() + " (переменные ${...})");
        System.out.println("  greeting(Anna) = " + features.greeting("Anna") + " (параметризация %s)");
    }

    // -----------------------------------------------------------------
    //  БЛОК 4. owner: Mutable + Accessible
    // -----------------------------------------------------------------
    private static void block4_ownerMutable() {
        System.out.println("\n--- БЛОК 4: MutableConfig (Mutable + Accessible) ---");

        MutableConfig cfg = ConfigFactory.create(MutableConfig.class);
        System.out.println("  minAge до изменения   = " + cfg.minAge());

        // Меняем значение на лету — остальные методы сразу видят новое значение.
        cfg.setProperty("minAge", "21");
        System.out.println("  minAge после setProperty(21) = " + cfg.minAge());

        // Accessible: посмотреть, какие ключи реально знает конфиг.
        System.out.println("  известно ключей: " + cfg.propertyNames());
    }

    // -----------------------------------------------------------------
    //  БЛОК 5. Единая точка входа ProjectConfig + контура dev/test/prod
    // -----------------------------------------------------------------
    private static void block5_profiles() {
        System.out.println("\n--- БЛОК 5: ProjectConfig — контура dev / test / prod ---");

        // Прогоняем один и тот же код под РАЗНЫМИ контурами и показываем,
        // как меняются значения. В реальной жизни контур задаётся ОДИН раз
        // (./gradlew run -Dapp.env=prod), здесь — для наглядности все три.
        for (Profile profile : Profile.values()) {
            // 1) Выставляем контур в системное свойство (это делает и сам
            //    ProjectConfig, но нам нужно ДО создания конфига).
            System.setProperty(ProjectConfig.ENV_SYSTEM_PROPERTY, profile.label());
            // 2) Сбрасываем кэш owner-конфигов, чтобы они пересоздались.
            ProjectConfig.reset();

            // 3) Читаем значения через ЕДИНУЮ точку входа.
            System.out.printf("  [%-4s] profile=%s | server=%s | auth=%s/%s%n",
                    profile.label(),
                    ProjectConfig.server().environment(),
                    ProjectConfig.serverBaseUrl(),
                    ProjectConfig.auth().username(),
                    ProjectConfig.auth().password());
        }

        // Возвращаем «заводской» контур по умолчанию и сбрасываем кэш,
        // чтобы демонстрация была повторяемой (например, в тестах).
        System.clearProperty(ProjectConfig.ENV_SYSTEM_PROPERTY);
        ProjectConfig.reset();
    }
}
