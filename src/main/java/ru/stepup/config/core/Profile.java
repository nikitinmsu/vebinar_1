package ru.stepup.config.core;

/**
 * =====================================================================
 *  Profile — перечисление «контуров» (стендов), под которые собирается конфиг
 * =====================================================================
 *  Терминология из жизни: один и тот же проект ездит по нескольким
 *  окружениям — локально у разработчика (dev), на QA-стенде (test),
 *  в проде (prod). У каждого свои адреса, учётки, таймауты.
 *
 *  Здесь заведено СООТВЕТСТВИЕ между короткой меткой контура и именем
 *  файла настроек:
 *
 *      DEV  ->  config/application-dev.properties
 *      TEST ->  config/application-test.properties
 *      PROD ->  config/application-prod.properties
 *
 *  Строковая метка хранится в системном свойстве {@code app.env}
 *  (см. {@link ProjectConfig}), а owner по ней находит нужный файл
 *  через переменную ${app.env} в @Sources.
 */
public enum Profile {

    DEV("dev"),
    TEST("test"),
    PROD("prod");

    /** Короткая метка контура: совпадает с суффиксом файла application-<метка>.properties. */
    private final String label;

    Profile(String label) {
        this.label = label;
    }

    /** Метка контура ("dev", "test", "prod"). */
    public String label() {
        return label;
    }

    /**
     * Разбирает строку в Profile (регистр не важен: "DEV", "Dev", "dev").
     *
     * @param value значение из системного свойства / переменной окружения
     * @return соответствующий Profile
     * @throws IllegalArgumentException если контур неизвестен
     */
    public static Profile fromString(String value) {
        for (Profile profile : values()) {
            if (profile.label.equalsIgnoreCase(value)) {
                return profile;
            }
        }
        throw new IllegalArgumentException(
                "Неизвестный контур: '" + value + "'. Допустимые: dev, test, prod");
    }

    /**
     * Контур по умолчанию. Именно на нём «взлетит» проект, если разработчик
     * ничего не передал (удобно для локальной разработки).
     *
     * @return {@link #DEV}
     */
    public static Profile defaultProfile() {
        return DEV;
    }

    /**
     * Имя системного свойства, которым выбирается контур.
     * Вынесено в константу, чтобы не разъезжались строки.
     *
     * @return {@value ProjectConfig#ENV_SYSTEM_PROPERTY}
     */
    public static String systemPropertyName() {
        return ProjectConfig.ENV_SYSTEM_PROPERTY;
    }

    @Override
    public String toString() {
        // toString() переопределён для красивого вывода в логах: "DEV".
        return name() + " (" + label + ")";
    }
}
