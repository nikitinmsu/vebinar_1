package ru.stepup.config.core;

import org.aeonbits.owner.ConfigFactory;
import ru.stepup.config.owner.AuthConfig;
import ru.stepup.config.owner.ServerConfig;

/**
 * =====================================================================
 *  ProjectConfig — ЕДИНАЯ ТОЧКА ВХОДА во все настройки проекта
 * =====================================================================
 *  Проблема, которую решает этот класс: если в каждом классе писать
 *  собственное чтение свойств (System.getProperty, Properties.load...),
 *  то источники, приоритеты и имена ключей «разъедутся» по всему коду.
 *  Вместо этого ВСЕ обращения к конфигурации идут через ОДИН фасад:
 *
 *      ProjectConfig.server().port();   // конфиг сервера
 *      ProjectConfig.auth().password(); // учётные данные
 *
 *  ЧТО ФАСАД ДАЁТ:
 *  1) выбор КОНТУРА (dev/test/prod) в одном месте и гарантия, что значение
 *     app.env дойдёт до owner (мы кладём его в системные свойства);
 *  2) ленивое создание + кэширование owner-конфигов (создаём один раз);
 *  3) удобные типизированные геттеры вместо ручного чтения Properties;
 *  4) reset() для тестов и «горячего» переключения контура.
 *
 *  ПРИОРИТЕТ ОПРЕДЕЛЕНИЯ КОНТУРА (см. {@link #resolveProfile()}):
 *     1) системное свойство -Dapp.env=prod   (из команды/CI);
 *     2) переменная окружения APP_ENV=prod;
 *     3) контур по умолчанию — dev.
 *
 *  САМ МЕХАНИЗМ ПОДСТАНОВКИ ЗНАЧЕНИЙ живёт НЕ здесь, а в owner-интерфейсах
 *  (ServerConfig/AuthConfig): аннотации @Sources + @LoadPolicy(MERGE) строят
 *  «пирог приоритетов» из системных свойств, окружения, файла контура и
 *  общих дефолтов. Фасад лишь выбирает контур и отдаёт готовые объекты.
 */
public final class ProjectConfig {

    /** Имя системного свойства, которое выбирает контур (dev|test|prod). */
    public static final String ENV_SYSTEM_PROPERTY = "app.env";

    /** Имя переменной окружения-«дублёра» для выбора контура. */
    private static final String ENV_VARIABLE = "APP_ENV";

    // Кэши созданных owner-конфигов. volatile + double-checked locking,
    // чтобы фасад безопасно работал из нескольких потоков.
    private static volatile ServerConfig serverConfig;
    private static volatile AuthConfig authConfig;

    /** Приватный конструктор: фасад утилитный, экземпляры не создаём. */
    private ProjectConfig() {
        throw new AssertionError("Утилитный класс не инстанцируется");
    }

    // =================================================================
    //  ПУБЛИЧНЫЙ API ФАСАДА
    // =================================================================

    /**
     * Типизированный конфиг сервера для ТЕКУЩЕГО контура.
     *
     * @return owner-прокси интерфейса {@link ServerConfig}
     */
    public static ServerConfig server() {
        ServerConfig local = serverConfig;
        if (local == null) {
            synchronized (ProjectConfig.class) {
                local = serverConfig;
                if (local == null) {
                    // Проверяем/выставляем app.env ДО создания конфига:
                    // от него зависит, какой application-*.properties прочитает owner.
                    ensureEnvSystemProperty();
                    local = ConfigFactory.create(ServerConfig.class);
                    serverConfig = local;
                }
            }
        }
        return local;
    }

    /**
     * Типизированный конфиг авторизации для ТЕКУЩЕГО контура.
     *
     * @return owner-прокси интерфейса {@link AuthConfig}
     */
    public static AuthConfig auth() {
        AuthConfig local = authConfig;
        if (local == null) {
            synchronized (ProjectConfig.class) {
                local = authConfig;
                if (local == null) {
                    ensureEnvSystemProperty();
                    local = ConfigFactory.create(AuthConfig.class);
                    authConfig = local;
                }
            }
        }
        return local;
    }

    /**
     * Текущий активный контур. Значение берётся с тем же приоритетом,
     * что и в {@link #resolveProfile()}.
     *
     * @return объект {@link Profile}
     */
    public static Profile activeProfile() {
        return resolveProfile();
    }

    /**
     * Сбросить кэш owner-конфигов. Нужно, если контур меняется «на лету»
     * (в тестах) — после сброса следующий вызов server()/auth() пересоздаст
     * конфиг уже под новый app.env.
     */
    public static synchronized void reset() {
        serverConfig = null;
        authConfig = null;
    }

    /**
     * Полный базовый URL сервиса, собранный из типизированных геттеров.
     * Показывает, как удобно «склеивать» отдельные настройки в одно значение.
     *
     * @return например {@code http://127.0.0.1:8080/api/v1}
     */
    public static String serverBaseUrl() {
        ServerConfig cfg = server();
        boolean defaultPort = ("http".equals(cfg.scheme()) && cfg.port() == 80)
                || ("https".equals(cfg.scheme()) && cfg.port() == 443);
        String hostAndPort = defaultPort ? cfg.host() : cfg.host() + ":" + cfg.port();
        return cfg.scheme() + "://" + hostAndPort + cfg.contextPath();
    }

    // =================================================================
    //  ВСПОМОГАТЕЛЬНАЯ ЛОГИКА
    // =================================================================

    /**
     * Определяет активный контур по правилам «снаружи — важнее»:
     * системное свойство &gt; переменная окружения &gt; dev по умолчанию.
     *
     * @return выбранный {@link Profile}
     */
    private static Profile resolveProfile() {
        // 1) Системное свойство: ./gradlew run -Dapp.env=test
        String fromProperty = System.getProperty(ENV_SYSTEM_PROPERTY);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return Profile.fromString(fromProperty);
        }
        // 2) Переменная окружения: export APP_ENV=test
        String fromEnv = System.getenv(ENV_VARIABLE);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return Profile.fromString(fromEnv);
        }
        // 3) Контур по умолчанию — локальная разработка.
        return Profile.defaultProfile();
    }

    /**
     * Гарантирует, что системное свойство app.env задано. owner подставляет
     * ${app.env} в пути @Sources (ServerConfig/AuthConfig), поэтому значение
     * ОБЯЗАНО быть в System.getProperties() к моменту ConfigFactory.create().
     *
     * Если пользователь задал контур переменной окружения APP_ENV, а не
     * свойством -D, мы «поднимаем» его в системное свойство, чтобы owner
     * гарантированно его увидел.
     */
    private static void ensureEnvSystemProperty() {
        if (System.getProperty(ENV_SYSTEM_PROPERTY) == null) {
            System.setProperty(ENV_SYSTEM_PROPERTY, resolveProfile().label());
        }
    }
}
