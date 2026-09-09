package ru.stepup.config.manual;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * =====================================================================
 *  СПОСОБ 2. Чтение properties из ВНЕШНЕГО ФАЙЛА через FileInputStream
 * =====================================================================
 *  Когда настройки нужно менять БЕЗ пересборки проекта (или в них лежат
 *  секреты, которым не место в git), файл выносят за пределы проекта —
 *  на диск рядом с приложением или в каталог конфигурации (/etc, config/).
 *
 *  Ключевые слова для вебинара:
 *    - FileInputStream(File) — поток из файла файловой системы;
 *    - путь может быть абсолютным ("/etc/myapp/app.properties") или
 *      относительным ("config/app.properties" — от текущей папки запуска);
 *    - если файла нет — FileInputStream бросит FileNotFoundException
 *      (в отличие от getResourceAsStream, который молча вернёт null);
 *    - файл доступен на запись: правки применяются без пересборки jar.
 *
 *  Зачем это нужно в проекте:
 *    - ops/разработчик может поменять пароль или адрес стенда прямо на
 *      сервере/в CI-агente, не трогая код;
 *    - «боевые» секреты не зашиваются в артефакт и не попадают в git;
 *    - удобно накладывать «локальные» настройки поверх дефолтов.
 *
 *  Минусы (почему «ручной» способ — не серебряная пуля):
 *    - надо знать, ГДЕ лежит файл (абсолютный путь/рабочая папка);
 *    - если файла нет на другой машине — приложение упадёт;
 *    - каждый читающий класс сам открывает поток и сам парсит типы.
 *    Дальше в вебинаре покажем, как owner убирает эту рутину.
 *
 *  На практике оба способа часто КОМБИНИРУЮТ: дефолты из classpath,
 *  а поверх — внешний файл, если он существует (см. примеры owner:
 *  @LoadPolicy(LoadType.MERGE) в ServerConfig).
 */
public final class FileProperties {

    /** Приватный конструктор: класс утилитный, экземпляры не создаём. */
    private FileProperties() {
        throw new AssertionError("Утилитный класс не инстанцируется");
    }

    /**
     * Читает .properties-файл из ФАЙЛОВОЙ СИСТЕМЫ.
     * Поток открывается через {@link FileInputStream} — ровно тот класс,
     * который вынесен в заголовок вебинара.
     *
     * @param filePath путь к файлу (абсолютный или относительный)
     * @return загруженные {@link Properties}
     * @throws IllegalStateException если файл не существует или не читается
     */
    public static Properties load(String filePath) {
        return load(Path.of(filePath));
    }

    /**
     * Перегрузка для {@link Path} — удобно в тестах с {@code @TempDir}.
     *
     * @param path путь к файлу
     * @return загруженные {@link Properties}
     */
    public static Properties load(Path path) {
        // try-with-resources сам закроет поток — даже если load() упадёт.
        try (InputStream in = new FileInputStream(path.toFile())) {
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException e) {
            // FileNotFoundException — подкласс IOException: файла нет, пути нет,
            // нет прав на чтение — всё это окажется здесь с понятным текстом.
            throw new UncheckedIOException("Не удалось прочитать файл: " + path.toAbsolutePath(), e);
        }
    }

    /**
     * Проверяет существование внешнего файла, чтобы аккуратно сделать
     * «опциональное» переопределение: дефолт из classpath + внешний файл,
     * если он есть.
     *
     * @param path путь к файлу
     * @return true, если файл существует и это обычный файл
     */
    public static boolean exists(Path path) {
        return Files.isRegularFile(path);
    }
}
