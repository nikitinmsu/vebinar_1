package ru.stepup.ui.base;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import ru.stepup.api.pageobject.GoodsApi;
import ru.stepup.ui.config.UiConfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.codeborne.selenide.Selenide.open;

/**
 * =====================================================================
 *  BaseUiTest — базовый класс для ВСЕХ UI-тестов SmartShop
 * =====================================================================
 *
 *  ЗАЧЕМ НУЖЕН БАЗОВЫЙ КЛАСС
 *  ------------------------------------------------
 *  Любой UI-тест повторяет одни и те же шаги:
 *    1. настроить Selenide (один раз);
 *    2. открыть главную страницу (перед каждым тестом);
 *    3. подчистить данные и закрыть браузер (после каждого теста);
 *    4. уметь проверять URL после каждого перехода.
 *  Если писать это в каждом тестовом классе — код задублируется.
 *  Выносим общую логику сюда, а тесты наследуют её через `extends`.
 *
 *  ЖИЗНЕННЫЙ ЦИКЛ ТЕСТА (JUnit 5)
 *  ------------------------------------------------
 *  <pre>
 *      @BeforeAll   configureSelenide()  — один раз на весь класс
 *      @BeforeEach  openShop()           — перед КАЖДЫМ тестом
 *      @Test        testSomething()      — сам тест
 *      @AfterEach   cleanup()            — после КАЖДОГО теста
 *  </pre>
 *
 *  ПРО ТЕГ @Tag("ui")
 *  ------------------------------------------------
 *  Класс помечен {@code @Tag("ui")} — по этому тегу Gradle-задача uiTest
 *  отбирает UI-тесты (см. build.gradle.kts). Теги наследуются
 *  подклассами, поэтому все наши тестовые классы попадут в задачу uiTest.
 */
@Tag("ui")
public abstract class BaseUiTest {

    /**
     * Page Object для REST-ручек сервиса (пакет ru.stepup.api).
     * Нужен, чтобы в части тестов готовить данные через API (быстрее
     * и надёжнее, чем «накликивать» всё в UI), а после теста удалять их.
     * Подключение идемпотентно: RestAssured настраивается один раз.
     */
    protected final GoodsApi goodsApi = new GoodsApi();

    /**
     * id товаров, созданных тестом через API. После каждого теста
     * они будут удалены — тесты не «мусорят» в базе сервиса и
     * не влияют друг на друга (требование «удалять данные после теста»).
     */
    protected final List<Long> createdProductIds = new ArrayList<>();

    // =================================================================
    //  ЖИЗНЕННЫЙ ЦИКЛ
    // =================================================================

    /**
     * Настройка Selenide. Выполняется ОДИН раз перед всеми тестами класса.
     * Идемпотентно: сколько бы раз ни вызывалось — настройка применится
     * лишь один раз (см. UiConfig.init).
     */
    @BeforeAll
    static void configureSelenide() {
        UiConfig.init();
    }

    /**
     * «ХАППИ-ПАТ» ПЕРЕД КАЖДЫМ ТЕСТОМ:
     * открываем главную страницу магазина и убеждаемся, что URL корректный.
     *
     * Почему открываем именно "/"? Корзина в SmartShop живёт в переменной
     * JavaScript и сбрасывается при каждой загрузке страницы. Открыв "/",
     * мы гарантируем одинаковое стартовое состояние для каждого теста.
     */
    @BeforeEach
    void openShop() {
        open("/");
        // Сразу после открытия проверяем корректность URL (см. checkUrl).
        checkUrl("/");
    }

    /**
     * ОЧИСТКА ПОСЛЕ КАЖДОГО ТЕСТА (выполняется даже при падении теста).
     *
     * Что делаем:
     * 1) удаляем через API все товары, созданные в ходе теста
     *    (соблюдение принципа «удали за собой данные»);
     * 2) закрываем браузер — даёт ПОЛНУЮ изоляцию тестов:
     *    - свежая корзина (переменная JS пуста),
     *    - свежий сеанс авторизации (не «протекает» в следующий тест),
     *    - чистый DOM без «хвостов» предыдущего теста.
     */
    @AfterEach
    void cleanup() {
        // 1. Удаляем созданные товары. Не критично, если удаление вернёт
        //    ошибку (товара могло уже не быть) — главное, попытаться.
        for (Long id : createdProductIds) {
            try {
                goodsApi.deleteProduct(id);
            } catch (RuntimeException e) {
                System.err.println("Не удалось удалить товар id=" + id + ": " + e.getMessage());
            }
        }
        createdProductIds.clear();

        // 2. Закрываем браузер (заодно Selenide сохранит скриншот и HTML
        //    на случай, если тест упал — см. UiConfig).
        Selenide.closeWebDriver();
    }

    // =================================================================
    //  ПРОВЕРКИ URL (требование: «после каждого действия проверять URL»)
    // =================================================================

    /**
     * Проверяет, что текущий URL в браузере имеет ровно указанный путь.
     *
     * Примеры:
     * <pre>
     *      checkUrl("/")            — мы на главной странице
     *      checkUrl("/login")       — мы на странице входа
     *      checkUrl("/admin")       — мы в админке
     *  </pre>
     *
     * @param expectedPath ожидаемый путь, например {@code "/admin"}
     */
    protected void checkUrl(String expectedPath) {
        String currentUrl = WebDriverRunner.url();
        String actualPath = pathOf(currentUrl);
        assertThat(actualPath)
                .as("путь текущего URL: " + currentUrl)
                .isEqualTo(expectedPath);
    }

    /**
     * Проверяет, что путь текущего URL содержит подстроку.
     * Полезно, когда в URL есть query-параметры, например после неудачного
     * входа сервер редиректит на {@code /login?error}.
     *
     * @param part ожидаемая подстрока пути, например {@code "/login"}
     */
    protected void checkUrlContains(String part) {
        String currentUrl = WebDriverRunner.url();
        String actualPath = pathOf(currentUrl);
        assertThat(actualPath)
                .as("путь текущего URL: " + currentUrl)
                .contains(part);
    }

    /**
     * Проверяет и путь, и query-строку URL (например {@code /login?error}).
     *
     * @param expectedPath  ожидаемый путь
     * @param expectedQuery ожидаемая query-строка БЕЗ знака «?», может быть null
     */
    protected void checkUrlWithQuery(String expectedPath, String expectedQuery) {
        String currentUrl = WebDriverRunner.url();
        URI uri = URI.create(currentUrl);
        assertThat(uri.getPath()).as("путь URL: " + currentUrl).isEqualTo(expectedPath);
        if (expectedQuery != null) {
            assertThat(uri.getQuery()).as("query-строка URL: " + currentUrl).isEqualTo(expectedQuery);
        }
    }

    // =================================================================
    //  ХЕЛПЕРЫ ДЛЯ РАБОТЫ С ДАННЫМИ ЧЕРЕЗ API
    // =================================================================

    /**
     * Создаёт товар через API (быстрее и надёжнее, чем через UI) и
     * регистрирует его id для автоматической очистки в @AfterEach.
     *
     * Зачем: часть тестов проверяет главную страницу/админку — товар
     * должен там БЫТЬ. Создать его «вручную» через UI в каждом тесте —
     * долго и хрупко. API-подготовка данных (setup через API) —
     * общепринятая практика в UI-автотестах.
     *
     * @param name  название товара (лучше уникальное — см. uniqueName)
     * @param price цена товара
     * @return id созданного товара
     */
    protected long createProductViaApi(String name, double price) {
        long id = goodsApi.createProductAndReturnId(name, price);
        createdProductIds.add(id);
        return id;
    }

    /**
     * Генерирует уникальное имя товара, чтобы тесты не «дрались» за
     * одинаковые имена (и чтобы по имени можно было точно найти товар
     * на странице).
     *
     * @param prefix читаемый префикс (например, "Телефон")
     * @return уникальное имя вида {@code Телефон-1786000000000-123}
     */
    protected String uniqueName(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    /**
     * Достаёт путь из полного URL (отбрасывает хост, порт и query).
     *
     * @param url полный URL
     * @return путь, например {@code /admin}
     */
    private String pathOf(String url) {
        return URI.create(url).getPath();
    }
}