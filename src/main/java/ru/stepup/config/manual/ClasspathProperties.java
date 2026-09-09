package ru.stepup.config.manual;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * =====================================================================
 *  СПОСОБ 1. Чтение properties из CLASSPATH через getResourceAsStream
 * =====================================================================
 *  Самый простой и распространённый способ положить настройки в проект:
 *  файл кладётся в ресурсы (src/main/resources) и попадает в classpath
 *  (в папку build/classes или внутрь jar). Читаем его потоком, который
 *  открывает загрузчик классов.
 *
 *  Ключевые слова для вебинара:
 *    - getResourceAsStream(path) — открывает ресурс как InputStream;
 *    - путь задаётся ОТ КОРНЯ classpath и БЕЗ ведущего слэша;
 *    - классы в jar/classpath — это РЕСУРСЫ (read-only): файл нельзя
 *      редактировать «на лету», чтобы поменять настройку — пересборка;
 *    - если ресурс не найден, метод вернёт null (а не бросит исключение),
 *      поэтому нужна явная проверка.
 *
 *  Зачем это нужно в проекте:
 *    - «дефолтные» настройки всегда лежат вместе с кодом и версионируются
 *      в git (не потеряются, не разъедутся между машинами);
 *    - загрузка не зависит от текущей папки запуска (работает и из IDE,
 *      и из jar, и из CI) — в отличие от чтения файла с диска.
 *
 *  Минусы:
 *    - значение нельзя поменять без пересборки/переупаковки jar;
 *    - секреты в classpath-ресурсе попадут внутрь артефакта — для паролей
 *      это плохая идея (см. способ 2 и переменные окружения).
 */
public final class ClasspathProperties {

    /** Приватный конструктор: класс утилитный, экземпляры не создаём. */
    private ClasspathProperties() {
        throw new AssertionError("Утилитный класс не инстанцируется");
    }

    /**
     * Читает .properties-файл из classpath.
     *
     * @param resourcePath путь от корня classpath, например {@code "config/manual-example.properties"}
     *                     (ведущего слэша НЕ должно быть: {@code "config/..."} — верно,
     *                     {@code "/config/..."} — вернёт null для getClassLoader())
     * @return загруженные {@link Properties}
     * @throws IllegalStateException если ресурс с таким путём не найден в classpath
     */
    public static Properties load(String resourcePath) {
        // Получаем загрузчик классов того класса, который «владеет» ресурсами.
        // У ClassLoader.getResourceAsStream путь всегда без ведущего '/'.
        ClassLoader classLoader = ClasspathProperties.class.getClassLoader();

        try (InputStream in = classLoader.getResourceAsStream(resourcePath)) {
            // getResourceAsStream НЕ бросает исключение, если ресурса нет:
            // он возвращает null. Проверяем явно, чтобы дать понятную ошибку.
            if (in == null) {
                throw new IllegalStateException("Ресурс не найден в classpath: " + resourcePath);
            }
            Properties props = new Properties();
            // java.util.Properties умеет разбирать формат key=value сам,
            // включая комментарии (# / !) и переносы строк.
            props.load(in);
            return props;
        } catch (IOException e) {
            // InputStream завёрнут в try-with-resources, поэтому поток закроется сам.
            // IOException оборачиваем в unchecked, чтобы не тащить throws в вызывающий код.
            throw new UncheckedIOException("Ошибка чтения ресурса: " + resourcePath, e);
        }
    }

    /**
     * Удобная обёртка: прочитать значение как строку с запасным дефолтом.
     *
     * @param props        уже загруженные свойства
     * @param key          имя ключа
     * @param defaultValue значение, если ключа нет
     * @return значение ключа или дефолт
     */
    public static String get(Properties props, String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
