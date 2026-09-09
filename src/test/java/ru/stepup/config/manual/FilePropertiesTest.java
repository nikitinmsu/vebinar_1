package ru.stepup.config.manual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * =====================================================================
 *  FilePropertiesTest — проверка чтения свойств через FileInputStream
 * =====================================================================
 *  Материал вебинара «Конфигурирование проекта», способ №2.
 *  В отличие от classpath-ресурса, файл лежит на диске вне jar и может
 *  правиться без пересборки. В тесте создаём такой файл во временной папке
 *  (@TempDir) и читаем его нашим классом.
 */
class FilePropertiesTest {

    /** JUnit создаст уникальную временную папку для каждого теста и удалит её после. */
    @TempDir
    Path tempDir;

    @Test
    void loadsPropertiesFromExternalFile() throws Exception {
        // given: «внешний» файл конфигурации рядом с приложением
        Path configFile = tempDir.resolve("app.properties");
        Files.writeString(configFile,
                "# внешний конфиг, лежит на диске\n"
                        + "host = external-host\n"
                        + "port = 9999\n"
                        + "password = super-secret\n");

        // when: читаем через FileInputStream
        Properties props = FileProperties.load(configFile);

        // then: значения прочитаны
        assertThat(props.getProperty("host")).isEqualTo("external-host");
        assertThat(props.getProperty("port")).isEqualTo("9999");
        assertThat(props.getProperty("password")).isEqualTo("super-secret");
    }

    @Test
    void loadAcceptsStringPathToo() throws Exception {
        Path configFile = tempDir.resolve("app2.properties");
        Files.writeString(configFile, "timeout.ms = 1000\n");

        // Обе перегрузки load(String) и load(Path) работают одинаково.
        Properties props = FileProperties.load(configFile.toString());
        assertThat(props.getProperty("timeout.ms")).isEqualTo("1000");
    }

    @Test
    void throwsWhenFileDoesNotExist() {
        // FileInputStream в отличие от getResourceAsStream бросает ошибку,
        // если файла нет: наш класс заворачивает её в понятное исключение.
        assertThatThrownBy(() -> FileProperties.load(tempDir.resolve("nope.properties")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не удалось прочитать файл");
    }

    @Test
    void existsHelpsToMakeOverrideOptional() {
        // Комбинированный приём: дефолт из classpath + внешний файл, ЕСЛИ он есть.
        // Метод exists() помогает проверить наличие, не ловя исключения.
        assertThat(FileProperties.exists(tempDir.resolve("missing.properties"))).isFalse();
    }
}
