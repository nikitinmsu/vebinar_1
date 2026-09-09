package ru.stepup.ui.pageobject.header;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

/**
 * =====================================================================
 *  LoginPageHeaders — заголовки страницы входа /login
 * =====================================================================
 *
 *  Страница входа — стандартная форма Spring Security (отдаётся самим
 *  сервером). «Заголовки» здесь: заголовок формы и поля, которые видны
 *  пользователю. Локаторы тоже предпочтительнее брать по id.
 *
 *  HTML формы (фрагмент):
 *  <pre>
 *      <form class="login-form" method="post" action="/login">
 *          <h2>Please sign in</h2>
 *          <input type="text"     id="username" name="username" ...>
 *          <input type="password" id="password" name="password" ...>
 *          <button type="submit" class="primary">Sign in</button>
 *      </form>
 *  </pre>
 *
 *  После неудачного входа сервер отдаёт ту же страницу, но с блоком
 *  ошибки:
 *  <pre>
 *      <div class="alert alert-danger" role="alert">Неверные учетные данные пользователя</div>
 *  </pre>
 */
public class LoginPageHeaders {

    /** Заголовок формы входа. Локатор: по id нет — используем тег+класс. */
    private final SelenideElement signInTitle = $(".login-form h2");

    /** Поле «Логин». Локатор: по id (#username). */
    private final SelenideElement usernameInput = $("#username");

    /** Поле «Пароль». Локатор: по id (#password). */
    private final SelenideElement passwordInput = $("#password");

    /** Кнопка «Sign in». Локатор: по атрибуту type (id у неё нет). */
    private final SelenideElement signInButton = $("button[type='submit']");

    /** Блок ошибки после неудачного входа. */
    private final SelenideElement errorAlert = $(".alert.alert-danger");

    // =================================================================
    //  МЕТОДЫ-ПРОВЕРКИ
    // =================================================================

    /**
     * Проверяет, что страница входа отобразилась корректно:
     * заголовок, оба поля и кнопка видны.
     *
     * @return this
     */
    public LoginPageHeaders checkHeadersVisible() {
        signInTitle.shouldBe(visible);
        usernameInput.shouldBe(visible);
        passwordInput.shouldBe(visible);
        signInButton.shouldBe(visible);
        return this;
    }

    /**
     * Проверяет, что на странице показан блок ошибки авторизации
     * (после неудачного входа). Если блока нет — тест упадёт с
     * понятным сообщением.
     *
     * @param expectedMessage ожидаемый текст ошибки
     * @return this
     */
    public LoginPageHeaders checkErrorVisible(String expectedMessage) {
        errorAlert.shouldBe(visible)
                .shouldHave(com.codeborne.selenide.Condition.text(expectedMessage));
        return this;
    }

    // =================================================================
    //  ДОСТУП К ЭЛЕМЕНТАМ (для PageObject LoginPage)
    // =================================================================

    public SelenideElement usernameInput() {
        return usernameInput;
    }

    public SelenideElement passwordInput() {
        return passwordInput;
    }

    public SelenideElement signInButton() {
        return signInButton;
    }
}