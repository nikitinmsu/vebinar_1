package ru.stepup.ui.test;

import com.codeborne.selenide.Selenide;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.stepup.ui.base.BaseUiTest;
import ru.stepup.ui.pageobject.CartModal;
import ru.stepup.ui.pageobject.LoginPage;
import ru.stepup.ui.pageobject.MainPage;
import ru.stepup.api.model.ProductRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static com.codeborne.selenide.Selenide.$;

/**
 * =====================================================================
 *  NegativeUiTest — негативные тест-кейсы
 * =====================================================================
 *
 *  Негативный тест проверяет, что система корректно реагирует на
 *  неверные/некорректные действия пользователя: отклоняет, показывает
 *  ошибку, не ломается. Это проверка «система ведёт себя так, как
 *  задумано, когда что-то идёт не так».
 *
 *  Здесь собраны негативные кейсы и через UI, и через API
 *  (гибридный подход — см. требования вебинара).
 */
@Tag("ui")
class NegativeUiTest extends BaseUiTest {

    // =================================================================
    //  НЕГАТИВ ЧЕРЕЗ UI
    // =================================================================

    @Test
    @DisplayName("Неверный пароль: показывается сообщение об ошибке, вход не выполняется")
    void wrongPasswordShowsErrorAndStaysOnLogin() {
        LoginPage loginPage = new LoginPage().openPage();
        loginPage.checkPageDisplayed();
        checkUrl("/login");

        // Вводим неверные учётные данные.
        loginPage.loginExpectingFailure("admin", "incorrect-password");

        // Ожидаем, что мы ОСТАЛИСЬ на /login (а не ушли в админку).
        checkUrlWithQuery("/login", "error");

        // И видим блок ошибки.
        loginPage.checkError("Неверные учетные данные пользователя");

        // Админка НЕ открылась.
        assertThat(Selenide.webdriver().driver().url()).doesNotContain("/admin");
    }

    @Test
    @DisplayName("Доступ к /admin без авторизации перекидывает на /login")
    void adminPageWithoutAuthRedirectsToLogin() {
        // Пытаемся открыть админку напрямую, не авторизуясь.
        Selenide.open("/admin");

        // Spring Security редиректит на страницу входа.
        checkUrlContains("/login");

        // Форма входа видна, поля на месте.
        LoginPage loginPage = new LoginPage();
        loginPage.checkPageDisplayed();
    }

    @Test
    @DisplayName("Количество товара не может стать меньше 1 (кнопка «-»)")
    void quantityCannotGoBelowOne() {
        // Создаём товар через API и перезагружаем страницу.
        long productId = createProductViaApi(uniqueName("Минимум-Один"), 50.0);
        Selenide.open("/");
        checkUrl("/");

        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();

        // Уменьшаем количество кнопкой «-» при стартовом значении 1.
        mainPage.productCard(productId).decreaseQuantity();

        // Негативная проверка: значение не ушло в 0 или минус — осталось 1.
        assertThat($("#q-" + productId).val()).isEqualTo("1");
    }

    @Test
    @DisplayName("«Оформить заказ» при пустой корзине ничего не делает")
    void checkoutWithEmptyCartDoesNothing() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();

        // Открываем пустую корзину и жмём «Оформить заказ».
        CartModal cart = mainPage.openCart();
        cart.checkVisible();
        cart.checkEmpty();
        cart.clickCheckout();

        // Негативная проверка: JS игнорирует клик при сумме 0 —
        // корзина осталась пустой, сумма 0, страница не изменилась.
        cart.checkEmpty();
        assertThat(cart.getTotalPrice()).isZero();
        assertThat(mainPage.getCartCount()).isZero();
        checkUrl("/");
    }

    // =================================================================
    //  НЕГАТИВ ЧЕРЕЗ API (часть проверок — на уровне REST)
    // =================================================================

    @Test
    @DisplayName("API: создание товара с пустым названием отклоняется (400)")
    void addProductWithEmptyNameRejected() {
        Response response = goodsApi.addProduct(new ProductRequest("", 10.0));
        assertThat(response.getStatusCode()).as("код ответа").isEqualTo(400);
    }

    @Test
    @DisplayName("API: отрицательная цена отклоняется (400) с сообщением")
    void addProductWithNegativePriceRejected() {
        Response response = goodsApi.addProduct(new ProductRequest("Негатив-Цена", -5.0));

        assertThat(response.getStatusCode()).isEqualTo(400);
        // В теле ответа сервер присылает человекочитаемое сообщение.
        assertThat(response.asString()).contains("Price can not be less, than 0");
    }

    @Test
    @DisplayName("API: удаление несуществующего товара возвращает 404")
    void deleteNonExistentProductReturns404() {
        // Гарантированно несуществующий id.
        long ghostId = 999_999L;
        Response response = goodsApi.deleteProduct(ghostId);
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("API: добавление товара без авторизации отклоняется (401)")
    void addProductWithoutAuthRejected() {
        // Специально НЕ используем goodsApi (у него Basic Auth) —
        // делаем запрос без авторизации через RestAssured напрямую.
        Response response = io.restassured.RestAssured.given()
                .contentType("application/json")
                .body(new ProductRequest("Без-Авторизации", 1.0))
                .post(ru.stepup.api.config.Endpoints.GOODS_ADD);
        assertThat(response.getStatusCode()).isEqualTo(401);
    }
}