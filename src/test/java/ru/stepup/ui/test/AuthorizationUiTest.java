package ru.stepup.ui.test;

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
 *  AuthorizationUiTest — тесты авторизации в SmartShop
 * =====================================================================
 *
 *  Проверяем вход в Админку через «Администрирование» (ссылка в шапке
 *  главной страницы). Сценарии:
 *  1. Неавторизованный пользователь при клике на «Администрирование»
 *     попадает на страницу входа /login.
 *  2. После успешного входа (admin/secret123) попадаем на /admin.
 *  3. Неверный пароль -> снова /login с блоком ошибки.
 *
 *  ПРАВИЛО ВЕБИНАРА: ПОСЛЕ КАЖДОГО ДЕЙСТВИЯ ПРОВЕРЯЕМ URL.
 *  За это отвечают методы BaseUiTest.checkUrl(...) / checkUrlWithQuery(...).
 *
 *  Изоляция тестов: BaseUiTest в @AfterEach закрывает браузер, поэтому
 *  «сессия» авторизации не протекает из одного теста в другой.
 */
@Tag("ui")
class AuthorizationUiTest extends BaseUiTest {

    @Test
    @DisplayName("Клик на «Администрирование» без авторизации ведёт на /login")
    void adminLinkRedirectsUnauthorizedUserToLogin() throws InterruptedException {
        // Дано: открыта главная страница (делает BaseUiTest.@BeforeEach).
        // Проверяем стартовое состояние: URL == "/", страница отобразилась.
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();
        checkUrl("/");

        // Действие 1: кликаем «Администрирование» в шапке.
        mainPage.clickAdminLink();

        // Проверка: попали на страницу входа (Spring Security редиректит).
        // Используем Page Object, который сам ждёт появления формы.
        LoginPage loginPage = new LoginPage();
        loginPage.checkPageDisplayed();
        checkUrl("/login");

        // Проверка: формы входа на странице ровно одна, поля видны.
        assertThat(loginPageCheckFormVisible()).isTrue();
    }

    @Test
    @DisplayName("Успешный вход (admin/secret123) открывает админку /admin")
    void successfulLoginOpensAdminPage() {
        // Переходим на страницу входа напрямую.
        LoginPage loginPage = new LoginPage().openPage();
        loginPage.checkPageDisplayed();
        checkUrl("/login");

        // Действие: заполняем форму и нажимаем «Sign in».
        // Логин/пароль берём из ApiConfig (те же, что и для REST-тестов).
        AdminPage adminPage = loginPage.loginAs(
                ru.stepup.api.config.ApiConfig.getUsername(),
                ru.stepup.api.config.ApiConfig.getPassword());

        // Проверка URL: сервер после входа редиректит на /admin.
        checkUrl("/admin");

        // Проверка: админка отобразилась (шапка и таблица видимы).
        adminPage.checkPageDisplayed();

        // Проверка заголовка страницы админки.
        assertThat(adminPage.headers().adminTitleElement().text()).isEqualTo("SmartShop Admin");
    }

    @Test
    @DisplayName("Полный путь: главная -> Администрирование -> логин -> админка")
    void fullPathToAdminThroughMainPage() {
        // 1. На главной странице кликаем «Администрирование».
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();
        checkUrl("/");
        mainPage.clickAdminLink();

        // 2. Оказались на /login — входим.
        LoginPage loginPage = new LoginPage();
        loginPage.checkPageDisplayed();
        checkUrl("/login");

        AdminPage adminPage = loginPage.loginAs(
                ru.stepup.api.config.ApiConfig.getUsername(),
                ru.stepup.api.config.ApiConfig.getPassword());

        // 3. Проверяем, что мы в админке.
        checkUrl("/admin");
        adminPage.checkPageDisplayed();
    }

    @Test
    @DisplayName("Неверный пароль -> блок ошибки на /login")
    void wrongPasswordShowsError() {
        // Открываем страницу входа и вводим НЕВЕРНЫЙ пароль.
        LoginPage loginPage = new LoginPage().openPage();
        loginPage.checkPageDisplayed();
        checkUrl("/login");

        loginPage.loginExpectingFailure("admin", "wrong-password");

        // Сервер редиректит на /login?error.
        checkUrlWithQuery("/login", "error");

        // На странице виден блок ошибки с понятным сообщением.
        loginPage.checkError("Неверные учетные данные пользователя");

        // Убеждаемся, что мы НЕ в админке (URL всё ещё /login).
        checkUrlWithQuery("/login", "error");
    }

    /**
     * Вспомогательная проверка: на странице входа видна ровно одна
     * форма с двумя полями. Сделана отдельным методом, чтобы показать,
     * как можно проверять состав DOM без привязки к конкретному тесту.
     *
     * @return true, если форма входа найдена
     */
    private boolean loginPageCheckFormVisible() {
        return com.codeborne.selenide.Selenide.$(".login-form").isDisplayed()
                && com.codeborne.selenide.Selenide.$("#username").isDisplayed()
                && com.codeborne.selenide.Selenide.$("#password").isDisplayed();
    }
}