package ru.stepup.config.owner;

import static org.assertj.core.api.Assertions.assertThat;

import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.stepup.config.core.ProjectConfig;
import ru.stepup.config.owner.ServerConfig.Environment;

/**
 * =====================================================================
 *  ServerAuthConfigTest — «боевые» owner-конфиги: источники и приоритеты
 * =====================================================================
 *  Проверяет owner-конфиги ServerConfig и AuthConfig, которые читают
 *  «пирог источников» (@LoadPolicy(MERGE) + @Sources):
 *      system:properties > system:env > application-${app.env}.properties
 *      > application.properties > @DefaultValue
 *
 *  Здесь контур жёстко фиксируем в dev и проверяем, что значения пришли
 *  именно из файла контура / общих дефолтов / системных свойств.
 */
class ServerAuthConfigTest {

    /** Значение app.env до теста — чтобы аккуратно восстановить его после. */
    private String previousEnv;

    @BeforeEach
    void setUp() {
        // Фиксируем контур DEV: его файл config/application-dev.properties.
        previousEnv = System.getProperty(ProjectConfig.ENV_SYSTEM_PROPERTY);
        System.setProperty(ProjectConfig.ENV_SYSTEM_PROPERTY, "dev");
    }

    @AfterEach
    void tearDown() {
        // Возвращаем предыдущее значение app.env (или убираем свойство совсем).
        if (previousEnv == null) {
            System.clearProperty(ProjectConfig.ENV_SYSTEM_PROPERTY);
        } else {
            System.setProperty(ProjectConfig.ENV_SYSTEM_PROPERTY, previousEnv);
        }
        // Убираем «служебные» системные свойства, которые могли задать тесты.
        System.clearProperty("server.port");
    }

    @Test
    void serverConfigReadsValuesFromDevProfileFile() {
        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);

        // Значения из application-dev.properties (перекрывают общие дефолты).
        assertThat(cfg.environment()).isEqualTo(Environment.DEV);
        assertThat(cfg.port()).isEqualTo(8080);
        // Значения, которых нет в dev-файле, приходят из application.properties.
        assertThat(cfg.scheme()).isEqualTo("http");
        assertThat(cfg.host()).isEqualTo("127.0.0.1");
        assertThat(cfg.contextPath()).isEqualTo("/api/v1");
        assertThat(cfg.maxThreads()).isEqualTo(100);
    }

    @Test
    void authConfigReadsCredentialsFromDevProfileFile() {
        AuthConfig cfg = ConfigFactory.create(AuthConfig.class);

        assertThat(cfg.username()).isEqualTo("admin");
        // Пароль dev-контура пришёл из application-dev.properties.
        assertThat(cfg.password()).isEqualTo("dev_secret_123");
        // Список ролей разобран из строки "user, admin".
        assertThat(cfg.roles()).containsExactly("user", "admin");
        assertThat(cfg.tokenTtlSeconds()).isEqualTo(3600);
    }

    @Test
    void systemPropertyOverridesEverything() {
        // Системное свойство стоит в списке @Sources ПЕРВЫМ -> максимальный
        // приоритет. Задаём его «как будто из CI» и проверяем переопределение.
        System.setProperty("server.port", "9876");

        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);
        assertThat(cfg.port()).isEqualTo(9876);

        // А вот контур (файл) при этом не менялся.
        assertThat(cfg.environment()).isEqualTo(Environment.DEV);
    }

    @Test
    void accessibleExposesEffectiveKeys() {
        // ServerConfig расширяет Accessible: можно заглянуть в то,
        // какие ключи реально участвуют в конфиге.
        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);

        assertThat(cfg.propertyNames()).contains("server.port", "server.environment");
        assertThat(cfg.getProperty("server.port")).isEqualTo("8080");
    }
}
