package ru.stepup.ui.pageobject;

import ru.stepup.ui.pageobject.header.LoginPageHeaders;

import static com.codeborne.selenide.Selenide.open;

/**
 * =====================================================================
 *  LoginPage — Page Object страницы входа /login
 * =====================================================================
 *
 *  На сайт попадаешь на страницу входа двумя путями:
 *   1. кликнул «Администрирование» без авторизации -> редирект на /login;
 *   2. открыл /login напрямую.
 *
 *  Форма — стандартная Spring Security:
 *  <pre>
 *      <form class="login-form" method="post" action="/login">
 *          <h2>Please sign in</h2>
 *          <input id="username" type="text"     name="username" ...>
 *          <input id="password" type="password" name="password" ...>
 *          <button type="submit" class="primary">Sign in</button>
 *      </form>
 *  </pre>
 *
 *  ПОВЕДЕНИЕ ПОСЛЕ SUBMIT:
 *  - успешный вход: сервер редиректит на /admin (defaultSuccessUrl);
 *  - неудачный вход: сервер редиректит на /login?error и показывает
 *    блок <div class="alert alert-danger">Неверные учетные данные...
 *
 *  Заголовки/поля вынесены в {@link LoginPageHeaders}.
 */
public class LoginPage {

    /** Заголовки и поля формы входа. */
    private final LoginPageHeaders headers = new LoginPageHeaders();

    /**
     * Открывает страницу входа напрямую.
     *
     * @return this
     */
    public LoginPage openPage() {
        open("/login");
        return this;
    }

    // =================================================================
    //  ВВОД ДАННЫХ
    // =================================================================

    /**
     * Вводит логин в поле #username.
     *
     * @param username логин
     * @return this
     */
    public LoginPage enterUsername(String username) {
        headers.usernameInput().setValue(username);
        return this;
    }

    /**
     * Вводит пароль в поле #password.
     *
     * @param password пароль
     * @return this
     */
    public LoginPage enterPassword(String password) {
        headers.passwordInput().setValue(password);
        return this;
    }

    /**
     * Кликает «Sign in» — отправляет форму.
     *
     * @return this
     */
    public LoginPage clickSignIn() {
        headers.signInButton().click();
        return this;
    }

    // =================================================================
    //  ГОТОВЫЕ СЦЕНАРИИ ВХОДА
    // =================================================================

    /**
     * Полный сценарий успешного входа: заполнить форму и нажать «Sign in».
     * После успешного входа сервер редиректит на /admin.
     *
     * ВАЖНО: после сабмита сервер выполняет цепочку редиректов
     * (/login -> 302 -> /admin). Чтобы проверка URL в тесте не «выстрелила»
     * раньше времени, явно ждём появления админки (Selenide опрашивает
     * страницу до таймаута Configuration.timeout).
     *
     * @param username логин
     * @param password пароль
     * @return Page Object админки {@link AdminPage}
     */
    public AdminPage loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickSignIn();
        // Ждём загрузки админки — теперь тест может смело проверять URL.
        AdminPage adminPage = new AdminPage();
        adminPage.checkPageDisplayed();
        return adminPage;
    }

    /**
     * Полный сценарий НЕуспешного входа: заполнить форму, нажать «Sign in»
     * и остаться на странице входа с блоком ошибки.
     *
     * @param username логин
     * @param password пароль
     * @return this (мы всё ещё на странице входа)
     */
    public LoginPage loginExpectingFailure(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickSignIn();
        return this;
    }

    // =================================================================
    //  ПРОВЕРКИ
    // =================================================================

    /**
     * Проверяет, что страница входа отобразилась (форма и поля видны).
     *
     * @return this
     */
    public LoginPage checkPageDisplayed() {
        headers.checkHeadersVisible();
        return this;
    }

    /**
     * Проверяет, что показан блок ошибки авторизации.
     *
     * @param expectedMessage ожидаемый текст ошибки
     * @return this
     */
    public LoginPage checkError(String expectedMessage) {
        headers.checkErrorVisible(expectedMessage);
        return this;
    }
}