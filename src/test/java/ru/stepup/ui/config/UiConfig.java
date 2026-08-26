package ru.stepup.ui.config;

import com.codeborne.selenide.Configuration;

/**
 * =====================================================================
 *  UiConfig — «витрина» конфигурации Selenide
 * =====================================================================
 *
 *  ВСЕ НАСТРОЙКИ SELENIDE ВЫНЕСЕНЫ В ОТДЕЛЬНЫЙ ФАЙЛ
 *  ------------------------------------------------
 *  Все параметры браузера, ожиданий и диагностики живут в файле
 *  {@code src/test/resources/selenide.properties}. Selenide читает его
 *  АВТОМАТИЧЕСКИ из classpath при старте (класс SelenideProperties),
 *  поэтому в коде настраивать ничего не нужно.
 *
 *  ПРИОРИТЕТ ЗНАЧЕНИЙ:
 *  <pre>
 *      1) системные свойства  -Dselenide.xxx=...   (самый высокий);
 *      2) файл selenide.properties;
 *      3) встроенные дефолты Selenide.
 *  </pre>
 *
 *  Что делает этот класс тогда?
 *  ------------------------------------------------
 *  1. {@link #init()} — «точка входа» для вебинара: однократно трогает
 *     Configuration (запуская чтение файла) и печатает итоговые значения,
 *     чтобы было наглядно видно, что файл реально применился.
 *  2. {@link #baseUrl()} — удобный геттер адреса приложения для
 *     проверок URL в тестах и Page Object'ах.
 */
public final class UiConfig {

    /** Флаг: инициализация уже выполнена. */
    private static boolean initialized = false;

    /** Приватный конструктор — класс утилитный, экземпляры не создаём. */
    private UiConfig() {
        throw new AssertionError("Утилитный класс не инстанцируется");
    }

    /**
     * Однократно «включает» конфигурацию и показывает применённые значения.
     * Вызывается из {@code BaseUiTest} в @BeforeAll. Идемпотентен.
     *
     * Файл selenide.properties Selenide прочитает сам при первой же работе
     * с {@link Configuration} (мы специально обращаемся к нему ниже).
     */
    public static synchronized void init() {
        if (initialized) {
            return; // уже инициализировали
        }

        // Обращение к полям Configuration инициирует загрузку
        // selenide.properties (статический блок Configuration).
        System.out.println("=== Selenide configuration (from src/test/resources/selenide.properties) ===");
        System.out.println("  baseUrl            = " + Configuration.baseUrl);
        System.out.println("  browser            = " + Configuration.browser);
        System.out.println("  headless           = " + Configuration.headless);
        System.out.println("  browserSize        = " + Configuration.browserSize);
        System.out.println("  timeout            = " + Configuration.timeout);
        System.out.println("  pageLoadTimeout    = " + Configuration.pageLoadTimeout);
        System.out.println("  pageLoadStrategy   = " + Configuration.pageLoadStrategy);
        System.out.println("  screenshots        = " + Configuration.screenshots);
        System.out.println("  savePageSource     = " + Configuration.savePageSource);
        System.out.println("  reportsFolder      = " + Configuration.reportsFolder);
        System.out.println("===========================================================================");

        initialized = true;
    }

    /**
     * Базовый адрес приложения (из Selenide.Configuration.baseUrl,
     * прочитанного из selenide.properties).
     *
     * @return строка вида {@code http://127.0.0.1:8080}
     */
    public static String baseUrl() {
        return Configuration.baseUrl;
    }
}