package ru.stepup.config.owner;

import static org.assertj.core.api.Assertions.assertThat;

import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.Test;

/**
 * =====================================================================
 *  MutableConfigTest — owner в режиме чтения и изменения на лету
 * =====================================================================
 *  Демонстрирует два маркер-интерфейса owner:
 *    - Mutable    — setProperty/removeProperty/clear;
 *    - Accessible — getProperty/propertyNames и др.
 *  Это удобно, когда значение нужно подменить в тесте или переключить
 *  feature-флаг без пересоздания конфига.
 */
class MutableConfigTest {

    @Test
    void defaultValueIsUsedUntilChanged() {
        MutableConfig cfg = ConfigFactory.create(MutableConfig.class);

        // Значения берутся из @DefaultValue, т.к. файла у конфига нет.
        assertThat(cfg.minAge()).isEqualTo(18);
        assertThat(cfg.userName()).isEqualTo("anonymous");
    }

    @Test
    void setPropertyChangesValueOnTheFly() {
        MutableConfig cfg = ConfigFactory.create(MutableConfig.class);

        // setProperty возвращает СТАРОЕ значение.
        String old = cfg.setProperty("minAge", "21");
        assertThat(old).isEqualTo("18");
        // Новое значение сразу видно всем методам конфига.
        assertThat(cfg.minAge()).isEqualTo(21);
    }

    @Test
    void accessibleLetsInspectUnderlyingKeys() {
        MutableConfig cfg = ConfigFactory.create(MutableConfig.class);
        cfg.setProperty("custom", "value");

        // Accessible.propertyNames() — какие ключи знает конфиг.
        assertThat(cfg.propertyNames()).contains("minAge", "userName", "custom");
        // Accessible.getProperty — прочитать значение как сырую строку.
        assertThat(cfg.getProperty("custom")).isEqualTo("value");
    }

    @Test
    void removePropertyDeletesValueAndReturnsPrevious() {
        MutableConfig cfg = ConfigFactory.create(MutableConfig.class);

        // Сначала добавим значение, потом удалим его.
        cfg.setProperty("custom", "temp-value");
        assertThat(cfg.removeProperty("custom")).isEqualTo("temp-value");

        // После удаления свойства больше нет (getProperty вернёт null).
        // НЮАНС owner 1.0.12: @DefaultValue «оживает» только для НОВОГО
        // экземпляра конфига — removeProperty не откатывает к дефолту.
        assertThat(cfg.getProperty("custom")).isNull();
    }
}
