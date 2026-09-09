package ru.stepup.config.owner;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.ConverterClass;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.Separator;
import org.aeonbits.owner.Config.Sources;

/**
 * =====================================================================
 *  OwnerFeatures — «витрина» возможностей aeonbits.owner
 * =====================================================================
 *  Читает свой собственный файл config/owner-features.properties и
 *  демонстрирует ВОЗМОЖНОСТИ библиотеки на одном интерфейсе. Это не
 *  «боевой» конфиг (для него см. ServerConfig / AuthConfig), а наглядный
 *  полигон для вебинара. Идём по методам сверху вниз — каждый метод
 *  показывает одну-две фичи owner.
 *
 *  ТАБЛИЦА ФИЧ, ПОКАЗАННЫХ ЗДЕСЬ:
 *    №   фича                                   метод
 *    --- -------------------------------------- -------------------------
 *    1   авто-маппинг «метод = ключ»            appName()
 *    2   @Key: ключ не совпадает с методом      serverHost()/serverPort()
 *    3   @DefaultValue + примитивы              retryCount()/pi()/enabled()
 *    4   enum-тип (регистр ЗНАЧИМ)              timeUnit()
 *    5   массивы String[] (разделитель — ,)     fruits()
 *    6   @Separator: свой разделитель           fibonacci()
 *    7   коллекции List<String>                 letters()
 *    8   URL и File из коробки                  homepage()/notesFile()
    9   @ConverterClass: свой конвертер         primaryDb()/replicaDbs()
    10  переменные ${...} между ключами         dbEndpoint()
    11  параметризованное значение (%s)         greeting(String)
 *
 *  Чего в 1.0.12 НЕТ «из коробки» (упомянуть на вебинаре):
 *    - Map-методы не поддерживаются (для сложного — отдельный конвертер);
 *    - java.time.Duration/ByteSize живут в отдельном артефакте
 *      owner-java8-extras и требуют @ConverterClass явно.
 */
@Sources("classpath:config/owner-features.properties")
public interface OwnerFeatures extends Config {

    // -----------------------------------------------------------------
    // 1. АВТО-МАППИНГ: имя метода совпадает с ключом файла
    //    appName()  ->  ищет ключ "appName" в properties.
    // -----------------------------------------------------------------
    String appName();

    // -----------------------------------------------------------------
    // 2. @Key: когда ключ в файле называется иначе, чем метод.
    //    В файле лежит "owner.features.host", метод называется serverHost().
    // -----------------------------------------------------------------
    @Key("owner.features.host")
    String serverHost();

    /** Ключ "owner.features.port" в файле. */
    @Key("owner.features.port")
    int serverPort();

    // -----------------------------------------------------------------
    // 3. @DefaultValue: значение, если ключа нет нигде (файл/sys/env).
    //    owner сам конвертирует строку в int / double / boolean.
    // -----------------------------------------------------------------
    @DefaultValue("3")
    int retryCount();

    @DefaultValue("3.1415")
    double pi();

    @DefaultValue("true")
    boolean enabled();

    // -----------------------------------------------------------------
    // 4. ENUM: значение "NANOSECONDS" из файла/дефолта превращается в
    //    TimeUnit.NANOSECONDS. Регистр ЗНАЧИМ: "nanoseconds" не подойдёт!
    // -----------------------------------------------------------------
    @DefaultValue("NANOSECONDS")
    TimeUnit timeUnit();

    // -----------------------------------------------------------------
    // 5. МАССИВ String[]: owner делит строку по запятой (по умолчанию).
    //    "apple, pear, orange" -> {"apple", " pear", " orange"} и пробелы
    //    owner вычищает сам.
    // -----------------------------------------------------------------
    @DefaultValue("apple, pear, orange")
    String[] fruits();

    // -----------------------------------------------------------------
    // 6. @Separator: свой разделитель для массивов/коллекций.
    //    Здесь значения разделены точкой с запятой.
    // -----------------------------------------------------------------
    @Separator(";")
    @DefaultValue("0; 1; 1; 2; 3; 5; 8; 13; 21; 34; 55")
    int[] fibonacci();

    // -----------------------------------------------------------------
    // 7. КОЛЛЕКЦИИ: List/Set/Collection поддерживаются так же, как массивы.
    // -----------------------------------------------------------------
    @DefaultValue("a, b, c")
    List<String> letters();

    // -----------------------------------------------------------------
    // 8. URL и File «из коробки»: owner сам вызовет new URL(...).
    //    Для File символ "~" разворачивается в домашний каталог
    //    (системное свойство user.home).
    // -----------------------------------------------------------------
    @DefaultValue("https://example.com")
    URL homepage();

    @DefaultValue("~/notes.txt")
    File notesFile();

    // -----------------------------------------------------------------
    // 9. @ConverterClass: свой конвертер строки в бизнес-объект.
    //    primaryDb()   -> одно значение "db1.internal:5432"
    //    replicaDbs()  -> тот же конвертер применится к каждому элементу
    //                     списка, разделённого запятой.
    // -----------------------------------------------------------------
    @ConverterClass(EndpointConverter.class)
    @DefaultValue("db1.internal:5432")
    Endpoint primaryDb();

    @ConverterClass(EndpointConverter.class)
    @DefaultValue("db2.internal:5432, db3.internal:5432")
    List<Endpoint> replicaDbs();

    // -----------------------------------------------------------------
    // 10. ПЕРЕМЕННЫЕ ${...}: значение dbEndpoint в файле равно
    //     "${owner.features.host}:${owner.features.port}", owner подставит
    //     значения других ключей -> "localhost:3306". Удобно, чтобы не
    //     дублировать host/port в каждом составном ключе.
    // -----------------------------------------------------------------
    @Key("db.endpoint")
    String dbEndpoint();

    // -----------------------------------------------------------------
    // 11. ПАРАМЕТРИЗОВАННЫЕ значения: %s заменяется аргументом метода
    //     по правилам java.util.Formatter. Из файла берётся шаблон
    //     "Welcome, %s!", вызываем greeting("Anna").
    // -----------------------------------------------------------------
    @Key("greeting.template")
    String greeting(String name);
}
