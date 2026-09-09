package ru.stepup.config.owner;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

/**
 * =====================================================================
 *  ServerConfig — типизированный конфиг сервера на базе aeonbits.owner
 * =====================================================================
 *  ИДЕЯ owner: вместо «ручного» чтения java.util.Properties и ручного
 *  парсинга типов (Integer.parseInt, Boolean.parseBoolean...) мы ОБЪЯВЛЯЕМ
 *  интерфейс с методами нужных типов, а owner сам находит файл, достаёт
 *  значения и конвертирует их в int / String / enum и т.п.
 *
 *  ЧТО ЗДЕСЬ ПОКАЗАНО:
 *  1) extends Config            — «маркер»: owner генерирует реализацию;
 *  2) extends Accessible        — дополнительно даёт getProperty(...),
 *                                 propertyNames(), list(...) — «заглянуть
 *                                 внутрь» того, что реально подставилось;
 *  3) @Sources({...}) + @LoadPolicy(LoadType.MERGE)
 *       — список ИСТОЧНИКОВ и логика их объединения (см. ниже);
 *  4) @Key("server.port")       — маппинг метода на ключ файла;
 *  5) @DefaultValue("8080")     — значение, если ключ не найден нигде;
 *  6) enum-тип                  — owner конвертирует строку в enum
 *                                 (регистр ЗНАЧИМ: "DEV" != "dev").
 *
 *  ----------------------------------------------------------------
 *  ПРИОРИТЕТ ИСТОЧНИКОВ (policy MERGE, сверху — самый главный):
 *  ----------------------------------------------------------------
 *  С policy FIRST (умолчание) owner берёт ПЕРВЫЙ НАЙДЕННЫЙ файл и всё.
 *  Мы же используем MERGE: для КАЖДОГО ключа owner идёт по списку
 *  источников сверху вниз и берёт значение из ПЕРВОГО, где ключ есть.
 *  Получается знакомый всем «пирог приоритетов»:
 *
 *     1. system:properties        -Dserver.port=9090   (максимум, из CI/команды)
 *     2. system:env               переменная окружения SERVER_PORT
 *     3. classpath:application-${app.env}.properties   файл контура dev/test/prod
 *     4. classpath:application.properties              общие дефолты проекта
 *     5. @DefaultValue(...)                            «последний рубеж»
 *
 *  Переменная ${app.env} в пути источника — это «расширение переменных»:
 *  owner подставляет сюда значение системного свойства app.env
 *  (dev|test|prod). Его выставляет единая точка входа {@code ProjectConfig}.
 *
 *  ИТОГ: одна и та же строка кода cfg.port() на контуре dev вернёт 8080,
 *  а на контуре prod — 8443, без единого изменения в Java-коде.
 */
@LoadPolicy(LoadType.MERGE)
@Sources({
        "system:properties",                                 // 1) -Dserver.port=...
        "system:env",                                        // 2) SERVER_PORT=...
        "classpath:config/application-${app.env}.properties",// 3) контур (dev/test/prod)
        "classpath:config/application.properties"            // 4) общие дефолты
})
public interface ServerConfig extends Config, Accessible {

    /**
     * Контуры, на которых может крутиться приложение.
     * Хранится в ключе {@code server.environment} как строка,
     * owner сам сконвертирует её в enum (значения в файлах — ЗАГЛАВНЫЕ).
     */
    enum Environment { LOCAL, DEV, TEST, PROD, STAGE }

    /** Схема (http/https). Ключ в файлах: {@code server.scheme}. */
    @Key("server.scheme")
    @DefaultValue("http")
    String scheme();

    /** Хост сервиса. Ключ: {@code server.host}. */
    @Key("server.host")
    @DefaultValue("127.0.0.1")
    String host();

    /**
     * Порт. Обратите внимание: метод возвращает {@code int} — owner сам
     * сделает Integer.parseInt из строки файла.
     * Ключ: {@code server.port}.
     */
    @Key("server.port")
    @DefaultValue("8080")
    int port();

    /** Контекстный путь REST API. Ключ: {@code server.context.path}. */
    @Key("server.context.path")
    @DefaultValue("/api/v1")
    String contextPath();

    /** Таймаут запросов в миллисекундах. Ключ: {@code server.timeout.ms}. */
    @Key("server.timeout.ms")
    @DefaultValue("5000")
    int timeoutMs();

    /** Размер пула потоков. Ключ: {@code server.max.threads}. */
    @Key("server.max.threads")
    @DefaultValue("100")
    int maxThreads();

    /**
     * Активный контур. Демонстрация конвертации в enum и регистрозависимости.
     * Ключ: {@code server.environment}.
     */
    @Key("server.environment")
    @DefaultValue("LOCAL")
    Environment environment();
}
