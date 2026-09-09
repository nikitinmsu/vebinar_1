package ru.stepup.config.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import ru.stepup.config.owner.ServerConfig;
import ru.stepup.config.owner.ServerConfig.Environment;

/**
 * =====================================================================
 *  ProjectConfigProfilesTest — единая точка входа ProjectConfig и контура
 * =====================================================================
 *  Ключевой сценарий вебинара: ОДИН И ТОТ ЖЕ код (ProjectConfig.server(),
 *  ProjectConfig.auth()) отдаёт РАЗНЫЕ значения в зависимости от контура.
 *  Контур выбирается системным свойством app.env (dev|test|prod).
 *
 *  ВАЖНО: значение app.env — глобальное для JVM, поэтому тесты обязаны
 *  восстанавливать предыдущее значение и сбрасывать кэш ProjectConfig
 *  (см. @AfterEach), чтобы не «протекать» в соседние тесты.
 */
class ProjectConfigProfilesTest {

    /** Предыдущее значение app.env, чтобы вернуть его после теста. */
    private String previousEnv;

    @AfterEach
    void tearDown() {
        // Возвращаем исходное значение app.env (или убираем свойство).
        if (previousEnv == null) {
            System.clearProperty(ProjectConfig.ENV_SYSTEM_PROPERTY);
        } else {
            System.setProperty(ProjectConfig.ENV_SYSTEM_PROPERTY, previousEnv);
        }
        // Сбрасываем кэш owner-конфигов — следующий тест начнёт с чистого листа.
        ProjectConfig.reset();
    }

    @Test
    void defaultProfileIsDevWhenNothingIsSet() {
        // Этот тест имеет смысл, только если переменная окружения APP_ENV
        // не задана (иначе «дефолт» переопределится ею — так и задумано).
        Assumptions.assumeTrue(System.getenv("APP_ENV") == null, "APP_ENV не задан в окружении");

        previousEnv = System.clearProperty(ProjectConfig.ENV_SYSTEM_PROPERTY);
        ProjectConfig.reset();

        // Ничего не передали -> контур разработчика.
        assertThat(ProjectConfig.activeProfile()).isEqualTo(Profile.DEV);
    }

    @Test
    void devProfileProvidesLocalServerAndCredentials() {
        switchTo("dev");

        assertThat(ProjectConfig.activeProfile()).isEqualTo(Profile.DEV);
        assertThat(ProjectConfig.server().environment()).isEqualTo(Environment.DEV);
        assertThat(ProjectConfig.serverBaseUrl()).isEqualTo("http://127.0.0.1:8080/api/v1");
        assertThat(ProjectConfig.auth().username()).isEqualTo("admin");
        assertThat(ProjectConfig.auth().password()).isEqualTo("dev_secret_123");
    }

    @Test
    void testProfileProvidesQaServerAndCredentials() {
        switchTo("test");

        assertThat(ProjectConfig.activeProfile()).isEqualTo(Profile.TEST);
        assertThat(ProjectConfig.server().environment()).isEqualTo(Environment.TEST);
        assertThat(ProjectConfig.serverBaseUrl()).isEqualTo("http://127.0.0.1:8081/api/v1");
        assertThat(ProjectConfig.auth().username()).isEqualTo("qa_user");
        assertThat(ProjectConfig.auth().password()).isEqualTo("qa_secret_456");
    }

    @Test
    void prodProfileSwitchesToHttpsAndRealHost() {
        switchTo("prod");

        assertThat(ProjectConfig.activeProfile()).isEqualTo(Profile.PROD);
        assertThat(ProjectConfig.server().environment()).isEqualTo(Environment.PROD);
        assertThat(ProjectConfig.serverBaseUrl()).isEqualTo("https://smartshop.example.com:8443/api/v1");
        assertThat(ProjectConfig.auth().username()).isEqualTo("service_account");
        // В проде пароль — заглушка: настоящий должен прийти из переменной
        // окружения / секретницы CI, а не из git-файла.
        assertThat(ProjectConfig.auth().password()).isEqualTo("REPLACE_ME_IN_CI");
    }

    @Test
    void switchingProfileChangesValuesWithoutChangingCode() {
        // Демонстрация: меняется только контур, а код обращения — тот же самый.
        ServerConfig server = ProjectConfig.server();

        switchTo("test");
        assertThat(server().environment()).isEqualTo(Environment.TEST);
        assertThat(server().port()).isEqualTo(8081);

        switchTo("prod");
        assertThat(server().environment()).isEqualTo(Environment.PROD);
        assertThat(server().port()).isEqualTo(8443);
    }

    @Test
    void facadeCachesConfigUntilReset() {
        switchTo("dev");

        // Повторный вызов возвращает тот же экземпляр (кэш).
        assertThat(ProjectConfig.server()).isSameAs(ProjectConfig.server());
        assertThat(ProjectConfig.auth()).isSameAs(ProjectConfig.auth());
    }

    // -----------------------------------------------------------------
    //  Хелперы
    // -----------------------------------------------------------------

    private ServerConfig server() {
        return ProjectConfig.server();
    }

    private void switchTo(String label) {
        // Фиксируем предыдущее значение только при первом переключении.
        if (previousEnv == null) {
            previousEnv = System.getProperty(ProjectConfig.ENV_SYSTEM_PROPERTY);
        }
        System.setProperty(ProjectConfig.ENV_SYSTEM_PROPERTY, label);
        // После смены контура обязательно сбрасываем кэш owner-конфигов.
        ProjectConfig.reset();
    }
}
