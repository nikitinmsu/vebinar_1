package ru.stepup.config.owner;

import java.util.List;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

/**
 * =====================================================================
 *  AuthConfig — типизированный конфиг авторизации (пример №2 на owner)
 * =====================================================================
 *  Тот же самый «пирог приоритетов», что и в {@link ServerConfig}:
 *  системные свойства &gt; переменные окружения &gt; файл контура &gt; дефолты.
 *
 *  ЗДЕСЬ ДОПОЛНИТЕЛЬНО ПОКАЗАНО:
 *  1) отдельный интерфейс на ЛОГИЧЕСКИЙ БЛОК настроек (auth.*) —
 *     каждый потребитель получает только то, что ему нужно;
 *  2) список ролей через {@code List<String>} — owner сам разобьёт строку
 *     по запятой (разделитель меняется аннотацией @Separator);
 *  3) главный урок про СЕКРЕТЫ: в git-файлах паролей быть не должно.
 *     Здесь лежат заглушки, а реальное значение приходит из переменной
 *     окружения (источник "system:env") или системного свойства:
 *        export AUTH_PASSWORD=...      (или)   -Dauth.password=...
 *     Ключ маппится на переменную окружения как AUTH_PASSWORD (точки
 *     заменяются на подчёркивания, буквы — в верхний регистр).
 *
 *  Кстати, если ключ не найден ни в одном источнике и @DefaultValue не
 *  задан — метод вернёт null. Для пароля это удобно: «нет значения —
 *  значит, секрет не задан», можно упасть с понятной ошибкой.
 */
@LoadPolicy(LoadType.MERGE)
@Sources({
        "system:properties",                                  // 1) -Dauth.password=...
        "system:env",                                         // 2) AUTH_PASSWORD=...
        "classpath:config/application-${app.env}.properties", // 3) контур (dev/test/prod)
        "classpath:config/application.properties"             // 4) общие дефолты
})
public interface AuthConfig extends Config {

    /** Логин. Ключ: {@code auth.username}. */
    @Key("auth.username")
    @DefaultValue("admin")
    String username();

    /**
     * Пароль. Ключ: {@code auth.password}.
     * ВАЖНО: в репозитории только заглушка! Настоящее значение задаётся
     * снаружи (env/системное свойство). Если оно не пришло — метод вернёт
     * значение из файла/дефолт, и это надо явно проверять в коде.
     */
    @Key("auth.password")
    @DefaultValue("")
    String password();

    /**
     * Список ролей пользователя. Ключ: {@code auth.roles}.
     * owner разбивает строку "user, admin" на List из двух элементов
     * автоматически (разделитель по умолчанию — запятая).
     */
    @Key("auth.roles")
    @DefaultValue("user, admin")
    List<String> roles();

    /** TTL токена в секундах. Ключ: {@code auth.token.ttl.seconds}. */
    @Key("auth.token.ttl.seconds")
    @DefaultValue("3600")
    int tokenTtlSeconds();
}
