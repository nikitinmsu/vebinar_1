package ru.stepup.api.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

/**
 * =====================================================================
 *  ApiConfig — конфигурация HTTP-клиента RestAssured и Basic Auth
 * =====================================================================
 *
 *  Задачи класса:
 *  1. Прочитать настройки тестовой среды (базовый адрес, порт, логин,
 *     пароль) из системных свойств или использовать значения по умолчанию.
 *  2. Один раз настроить «глобальную» конфигурацию RestAssured.
 *  3. Отдать готовый {@link RequestSpecification} с авторизацией, чтобы
 *     все запросы из Page Object были построены одинаково и без дублирования.
 *
 *  Настройки задаются системными свойствами (см. задачу apiTest в
 *  build.gradle.kts — она пробрасывает их в тестовую JVM):
 *  <pre>
 *      -Dapi.base.uri=http://127.0.0.1
 *      -Dapi.port=8080
 *      -Dapi.username=admin
 *      -Dapi.password=secret123
 *  </pre>
 *  Значения по умолчанию (admin / secret123) соответствуют локальному
 *  демо-сервису, но их легко переопределить под любое окружение.
 *
 *  Почему это важно для вебинара:
 *  - пароль не «зашит» в код, а передаётся снаружи (CI, локальная машина);
 *  - креды и адрес меняются в одном месте — в системе сборки;
 *  - тесты остаются переносимыми между окружениями.
 */
public final class ApiConfig {

    // =================================================================
    //  НАСТРОЙКИ ИЗ СИСТЕМНЫХ СВОЙСТВ (с дефолтами)
    // =================================================================
    // Метод getProperty возвращает значение свойства, а если оно не
    // задано — значение по умолчанию из второго аргумента.

    private static final String USERNAME =
            System.getProperty("api.username", "admin");

    private static final String PASSWORD =
            System.getProperty("api.password", "secret123");

    /** Флаг: была ли уже выполнена настройка RestAssured. */
    private static boolean configured;

    private ApiConfig() {
        throw new AssertionError("Утилитный класс не инстанцируется");
    }

    // =================================================================
    //  ИНИЦИАЛИЗАЦИЯ
    // =================================================================

    /**
     * Одноразовая настройка глобального состояния RestAssured.
     * Вызывается автоматически перед первым запросом (см. {@link #getRequestSpecification()}).
     *
     * Здесь задаются базовые адрес и порт — дальше в тестах можно писать
     * относительные пути из класса {@link Endpoints}.
     */
    private static synchronized void initIfNeeded() {
        if (configured) {
            return;
        }
        // Базовый адрес берём из того же класса Endpoints — ЕДИНАЯ точка правды.
        RestAssured.baseURI = Endpoints.getBaseUri();
        RestAssured.port = Endpoints.getPort();
        // Устанавливаем таймауты, чтобы тесты не «висели» бесконечно,
        // если сервер недоступен.
        RestAssured.config = RestAssured.config()
                .httpClient(io.restassured.config.HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 10_000)
                        .setParam("http.socket.timeout", 10_000));
        configured = true;
    }

    // =================================================================
    //  ГОТОВАЯ СПЕЦИФИКАЦИЯ ЗАПРОСА (используется RestApiBuilder)
    // =================================================================

    /**
     * Возвращает готовую {@link RequestSpecification}: с базовым адресом,
     * Basic-авторизацией, JSON-заголовками и логированием.
     *
     * Тонкость с Basic Auth: используем {@code preemptive().basic(...)} —
     * это значит, что заголовок Authorization отправляется СРАЗУ,
     * без «разведки» (первый запрос без кредов + ответ 401). Это
     * экономит один round-trip и не даёт серверу логировать лишний
     * запрос без авторизации.
     *
     * @return спецификация, к которой в Page Object приклеиваются путь и тело
     */
    public static RequestSpecification baseRequestSpec() {
        initIfNeeded();
        return new RequestSpecBuilder()
                .setBaseUri(Endpoints.getBaseUri())
                .setPort(Endpoints.getPort())
                .setContentType("application/json")
                .setAccept("application/json")
                .setAuth(RestAssured.preemptive().basic(USERNAME, PASSWORD))
                .build();
    }

    /**
     * Имя пользователя для Basic Auth.
     *
     * @return логин из системного свойства {@code api.username}
     */
    public static String getUsername() {
        return USERNAME;
    }

    /**
     * Пароль для Basic Auth.
     *
     * @return пароль из системного свойства {@code api.password}
     */
    public static String getPassword() {
        return PASSWORD;
    }
}
