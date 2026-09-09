package ru.stepup.ui.test;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.stepup.ui.base.BaseUiTest;
import ru.stepup.ui.pageobject.AdminPage;
import ru.stepup.ui.pageobject.LoginPage;
import ru.stepup.ui.pageobject.MainPage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * =====================================================================
 *  AdminUiTest — тесты администрирования SmartShop
 * =====================================================================
 *
 *  Проверяем:
 *  1. вход в Админку (ссылка «Администрирование» + форма логина);
 *  2. добавление товара через UI админки и его появление в таблице;
 *  3. наличие других товаров в таблице (коллекция строк);
 *  4. изменение цены товара в админке и отражение на главной странице.
 *
 *  ГИБРИД API + UI
 *  ------------------------------------------------
 *  Там, где удобнее и надёжнее подготовить данные через REST API —
 *  используем API (см. createProductViaApi в BaseUiTest). Сами
 *  пользовательские действия выполняются через UI (Page Object).
 *  Созданные товары удаляются в @AfterEach (BaseUiTest).
 *
 *  ПРАВИЛО: ПОСЛЕ КАЖДОГО ДЕЙСТВИЯ ПРОВЕРЯЕМ URL И СОСТОЯНИЕ СТРАНИЦЫ.
 */
@Tag("ui")
class AdminUiTest extends BaseUiTest {

    @Test
    @DisplayName("Вход в Админку: главная -> «Администрирование» -> логин -> /admin")
    void loginToAdminThroughUi() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();
        checkUrl("/");

        // Шаг 1: клик по «Администрирование».
        mainPage.clickAdminLink();

        // Шаг 2: мы на странице входа — входим под админом.
        LoginPage loginPage = new LoginPage();
        loginPage.checkPageDisplayed();
        checkUrl("/login2");

        AdminPage adminPage = loginPage.loginAs(
                ru.stepup.api.config.ApiConfig.getUsername(),
                ru.stepup.api.config.ApiConfig.getPassword());

        // Шаг 3: проверяем, что мы в админке.
        checkUrl("/admin");
        adminPage.checkPageDisplayed();
        assertThat(adminPage.headers().adminTitleElement().text()).isEqualTo("SmartShop Admin");
    }

    @Test
    @DisplayName("Добавление товара через админку: товар появляется в таблице")
    void addProductViaAdminUiShowsInTable() throws InterruptedException {
        // Входим в админку напрямую (страница сама перекинет на /login,
        // если не авторизованы — но мы авторизуемся через UI).
        AdminPage adminPage = new AdminPage().openPage();
        if (isOnLoginPage()) {
            adminPage = new LoginPage()
                    .loginAs(ru.stepup.api.config.ApiConfig.getUsername(),
                            ru.stepup.api.config.ApiConfig.getPassword());
        }
        adminPage.checkPageDisplayed();
        checkUrl("/admin");

        // Запоминаем число строк ДО добавления.
        int rowsBefore = adminPage.getTableRowCount();

        // Действие: создаём товар через UI админки (поля + «Создать»).
        String newProductName = uniqueName("Из-Админки");
        adminPage.addProduct(newProductName, 777.77);

        // Проверка 1: количество строк в таблице увеличилось на 1.
        // (JS перерисовывает таблицу после ответа сервера.)
        adminPage.checkTableRowCount(rowsBefore + 1);

        // Проверка 2: находим созданный товар в таблице по названию
        // и проверяем, что его id действительно появился.
        long newProductId = adminPage.findProductIdByName(newProductName);
        adminPage.checkProductInTable(newProductId);
        assertThat(adminPage.getProductPrice(newProductId)).isEqualTo(777.77);

        // Регистрируем id для очистки в @AfterEach (API-подчистка).
        createdProductIds.add(newProductId);
        Thread.sleep(20000);
    }

    @Test
    @DisplayName("В таблице админки отображаются другие (ранее созданные) товары")
    void adminTableShowsExistingProducts() {
        // Готовим товар через API — он должен появиться и в админке.
        long apiProductId = createProductViaApi(uniqueName("Существующий"), 500.0);

        AdminPage adminPage = new AdminPage().openPage();
        if (isOnLoginPage()) {
            adminPage = new LoginPage()
                    .loginAs(ru.stepup.api.config.ApiConfig.getUsername(),
                            ru.stepup.api.config.ApiConfig.getPassword());
        }
        adminPage.checkPageDisplayed();
        checkUrl("/admin");

        // Проверка: товар, созданный через API, виден в таблице админки
        // и имеет ожидаемую цену.
        adminPage.checkProductInTable(apiProductId);
        assertThat(adminPage.getProductPrice(apiProductId)).isEqualTo(500.0);

        // Проверка: коллекция строк не пустая и содержит наш товар.
        assertThat(adminPage.getTableRowCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Смена цены в админке отражается на главной странице")
    void changePriceInAdminReflectsOnMainPage() {
        // Готовим товар через API (быстрее) и смотрим на него в UI.
        String productName = uniqueName("Для-Смены-Цены");
        long productId = createProductViaApi(productName, 100.0);

        // Входим в админку.
        AdminPage adminPage = new AdminPage().openPage();
        if (isOnLoginPage()) {
            adminPage = new LoginPage()
                    .loginAs(ru.stepup.api.config.ApiConfig.getUsername(),
                            ru.stepup.api.config.ApiConfig.getPassword());
        }
        adminPage.checkPageDisplayed();
        checkUrl("/admin");

        // Товар есть в таблице с ценой 100.0.
        adminPage.checkProductInTable(productId);
        assertThat(adminPage.getProductPrice(productId)).isEqualTo(100.0);

        // Действие: меняем цену в поле #pr-{id} и жмём «Сохранить».
        double newPrice = 1234.5;
        adminPage.updateProductPrice(productId, newPrice);

        // Проверка 1: цена в таблице админки обновилась (поле перерисовано).
        assertThat(adminPage.getProductPrice(productId)).isEqualTo(newPrice);

        // Переходим на главную страницу через ссылку «Вернуться на сайт».
        adminPage.headers().clickBackToSite();
        checkUrl("/");

        // Проверка 2: карточка товара на главной странице показывает
        // НОВУЮ цену (в атрибуте data-price карточки).
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();
        mainPage.checkProductVisible(productId);
        assertThat(mainPage.productCard(productId).getDataPrice()).isEqualTo(newPrice);

        // Проверка 3: видимый текст цены в карточке тоже обновился.
        // В карточке цена выводится в div: "{price} ₽".
        com.codeborne.selenide.SelenideElement priceDiv =
                com.codeborne.selenide.Selenide.$("#card-" + productId + " div[style*='color:var(--primary)']");
        assertThat(priceDiv.text()).startsWith("1234.5");
    }

    @Test
    @DisplayName("Удаление товара через админку убирает его из таблицы")
    void deleteProductViaAdminUi() {
        // Создаём товар через API и входим в админку.
        long productId = createProductViaApi(uniqueName("Удалим-Через-UI"), 900.0);

        AdminPage adminPage = new AdminPage().openPage();
        if (isOnLoginPage()) {
            adminPage = new LoginPage()
                    .loginAs(ru.stepup.api.config.ApiConfig.getUsername(),
                            ru.stepup.api.config.ApiConfig.getPassword());
        }
        adminPage.checkPageDisplayed();
        checkUrl("/admin");

        adminPage.checkProductInTable(productId);

        // Действие: удаляем товар (кнопка «Удалить», confirm() подтвердится
        // автоматически — Selenide по умолчанию принимает диалоги).
        adminPage.deleteProduct(productId);

        // Проверка: строка исчезла из таблицы, товара больше нет.
        adminPage.checkProductAbsent(productId);

        // Проверка через API: товар действительно удалён.
        // ВНИМАНИЕ: GET удалённого товара на этом сервере может вернуть 500
        // (известная особенность сервиса), поэтому надёжный признак удаления —
        // повторный DELETE, который вернёт 404 (товара уже нет).
        assertThat(goodsApi.deleteProduct(productId).getStatusCode()).isEqualTo(404);
    }

    /**
     * Вспомогательный метод: находимся ли мы на странице входа.
     * Используется, чтобы аккуратно авторизоваться, если админка
     * перекинула на /login.
     *
     * @return true, если текущая страница — /login
     */
    private boolean isOnLoginPage() {
        return Selenide.webdriver().driver().url().contains("/login");
    }
}