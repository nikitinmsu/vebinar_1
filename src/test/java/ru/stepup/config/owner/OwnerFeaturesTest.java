package ru.stepup.config.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.Test;

/**
 * =====================================================================
 *  OwnerFeaturesTest — проверка «витрины» возможностей aeonbits.owner
 * =====================================================================
 *  Каждый тест проверяет одну-две фичи owner на интерфейсе OwnerFeatures,
 *  читающем файл config/owner-features.properties. Это живая демонстрация
 *  для вебинара: вместо ручного парсинга типов — методы нужных типов.
 */
class OwnerFeaturesTest {

    /** Создаём owner-конфиг один раз на все тесты класса. */
    private final OwnerFeatures features = ConfigFactory.create(OwnerFeatures.class);

    @Test
    void mapsMethodNameToKeyAutomatically() {
        // Ключ в файле называется ровно как метод: appName.
        assertThat(features.appName()).isEqualTo("StepUp Config Webinar");
    }

    @Test
    void mapsCustomKeyWithAnnotation() {
        // В файле ключи owner.features.host / owner.features.port,
        // а методы называются иначе — связывает @Key.
        assertThat(features.serverHost()).isEqualTo("localhost");
        assertThat(features.serverPort()).isEqualTo(3306);
    }

    @Test
    void convertsPrimitivesFromDefaultValue() {
        // owner сам делает Integer.parseInt / Double.parseDouble и т.п.
        assertThat(features.retryCount()).isEqualTo(3);
        assertThat(features.pi()).isEqualTo(3.1415);
        assertThat(features.enabled()).isTrue();
    }

    @Test
    void convertsToEnumCaseSensitively() {
        // Значение "NANOSECONDS" из @DefaultValue конвертируется в TimeUnit.
        // Регистр важен: "nanoseconds" не подошёл бы.
        assertThat(features.timeUnit()).isEqualTo(TimeUnit.NANOSECONDS);
    }

    @Test
    void splitsStringArrayByComma() {
        // "apple, pear, orange" -> массив из трёх элементов; пробелы вычищены.
        assertThat(features.fruits()).containsExactly("apple", "pear", "orange");
    }

    @Test
    void usesCustomSeparatorForNumbers() {
        // @Separator(";") — значения разделены точкой с запятой.
        assertThat(features.fibonacci())
                .containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55);
    }

    @Test
    void supportsCollections() {
        // List<String> — то же, что массив, но коллекция.
        assertThat(features.letters()).containsExactly("a", "b", "c");
    }

    @Test
    void convertsUrlAndFileOutOfTheBox() {
        // URL создаётся автоматически из строки "https://example.com".
        assertThat(features.homepage().getProtocol()).isEqualTo("https");
        assertThat(features.homepage().getHost()).isEqualTo("example.com");
        // File поддерживает "~" -> домашний каталог пользователя.
        assertThat(features.notesFile().getName()).isEqualTo("notes.txt");
    }

    @Test
    void usesCustomConverterForBusinessObject() {
        // Строка "db1.internal:5432" превратилась в Endpoint(host, port).
        assertThat(features.primaryDb().host()).isEqualTo("db1.internal");
        assertThat(features.primaryDb().port()).isEqualTo(5432);
    }

    @Test
    void customConverterAppliesToEachCollectionElement() {
        // Тот же конвертер вызывается для каждого элемента списка.
        assertThat(features.replicaDbs()).extracting(Endpoint::host)
                .containsExactly("db2.internal", "db3.internal");
        assertThat(features.replicaDbs()).extracting(Endpoint::port)
                .containsExactly(5432, 5432);
    }

    @Test
    void expandsVariablesInsidePropertyValue() {
        // В файле db.endpoint = ${owner.features.host}:${owner.features.port},
        // owner подставил значения других ключей: localhost:3306.
        assertThat(features.dbEndpoint()).isEqualTo("localhost:3306");
    }

    @Test
    void formatsParameterizedValueFromArguments() {
        // Шаблон из файла "Welcome, %s!" + аргумент -> готовая строка.
        assertThat(features.greeting("Anna")).isEqualTo("Welcome, Anna!");
    }
}
