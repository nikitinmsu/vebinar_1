package ru.stepup.ui.test;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.stepup.ui.base.BaseUiTest;
import ru.stepup.ui.pageobject.AdminPage;
import ru.stepup.ui.pageobject.CartModal;
import ru.stepup.ui.pageobject.LoginPage;
import ru.stepup.ui.pageobject.MainPage;

import static org.assertj.core.api.Assertions.assertThat;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * =====================================================================
 *  DomUiTest — проверки DOM-дерева страниц
 * =====================================================================
 *
 *  UI-автотесты можно писать и «снизу вверх»: вместо кликов проверять
 *  САМУ СТРУКТУРУ DOM — теги, атрибуты, вложенность, количество узлов.
 *  Это полезно, когда мы тестируем вёрстку или хотим убедиться, что
 *  фронтенд отдаёт правильную разметку (не только «работает», но и
 *  «выглядит как надо» для фронтендеров/SEO).
 *
 *  Что умеем проверять в DOM:
 *    - теги и их атрибуты (id, class, href, data-*);
 *    - тексты узлов (title, h1, кнопки);
 *    - вложенность (потомки, родители);
 *    - количество однотипных узлов (коллекции);
 *    - состояние (скрыт/виден, атрибут value).
 *
 *  Selenide-инструменты для этого:
 *    $("...").getAttribute("id"),  .val(),  .text(),  .innerHtml();
 *    $("...").exists() / isDisplayed();
 *    $$("...").size()  — количество элементов коллекции.
 */
@Tag("ui")
class DomUiTest extends BaseUiTest {

    // =================================================================
    //  1. ГЛАВНАЯ СТРАНИЦА: ОБЩАЯ СТРУКТУРА
    // =================================================================

    @Test
    @DisplayName("DOM главной страницы: язык, title, шапка, контейнер товаров")
    void mainPageDocumentStructure() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();

        // Атрибут lang у корневого тега html.
        assertThat($("html").getAttribute("lang")).as("язык страницы").isEqualTo("ru");

        // Заголовок вкладки браузера (тег <title>).
        // ВНИМАНИЕ: $("title").text() в Chrome возвращает пустую строку
        // (особенность Selenium для невизуальных элементов). Поэтому
        // используем специальный метод Selenide.title().
        assertThat(Selenide.title()).isEqualTo("SmartShop — Лучшие товары");

        // Заголовок h1 с id="main-title".
        assertThat($("h1#main-title").exists()).as("тег h1 с id").isTrue();

        // Ссылка «Администрирование»: тег a, href, текст.
        SelenideElement adminLink = $("a[href='/admin']");
        assertThat(adminLink.exists()).isTrue();
        // ВНИМАНИЕ: браузер возвращает атрибут href в АБСОЛЮТНОМ виде
        // (http://127.0.0.1:8080/admin), хотя в HTML он относительный.
        // Проверяем и полный вид, и окончание "/admin".
        assertThat(adminLink.getAttribute("href"))
                .endsWith("/admin")
                .isEqualTo(ru.stepup.ui.config.UiConfig.baseUrl() + "/admin");
        assertThat(adminLink.text()).isEqualTo("Администрирование");

        // Кнопка корзины: id, внутри span-счётчик с начальным значением 0.
        SelenideElement cartBtn = $("#open-cart-btn");
        assertThat(cartBtn.exists()).isTrue();
        assertThat(cartBtn.find("#cart-count").text()).isEqualTo("0");

        // Контейнер товаров: id и класс.
        assertThat($("#products-list").getAttribute("class")).contains("products-grid");
    }

    @Test
    @DisplayName("DOM главной страницы: у карточки товара правильная структура и атрибуты")
    void productCardDomStructure() {
        // Создаём товар с уникальным именем и фиксированной ценой.
        String name = uniqueName("DOM-Карточка");
        double price = 333.33;
        long productId = createProductViaApi(name, price);
        Selenide.open("/");
        checkUrl("/");

        // Корневой узел карточки: #card-{id}, класс product-card, data-атрибуты.
        SelenideElement card = $("#card-" + productId);
        assertThat(card.exists()).as("карточка товара в DOM").isTrue();
        assertThat(card.getAttribute("class")).contains("product-card");
        assertThat(card.getAttribute("data-id")).isEqualTo(String.valueOf(productId));
        assertThat(card.getAttribute("data-name")).isEqualTo(name);
        assertThat(card.getAttribute("data-price")).isEqualTo(String.valueOf(price));

        // Внутри карточки есть заголовок h4 с названием товара.
        assertThat(card.find("h4").text()).isEqualTo(name);

        // Поле количества: id=q-{id}, класс qty-input, type number, min=1.
        SelenideElement qtyInput = card.find("#q-" + productId);
        assertThat(qtyInput.getAttribute("class")).contains("qty-input");
        assertThat(qtyInput.getAttribute("type")).isEqualTo("number");
        assertThat(qtyInput.getAttribute("min")).isEqualTo("1");
        assertThat(qtyInput.val()).isEqualTo("1");

        // Кнопки «-» и «+» (data-action=qty-change) и «В корзину».
        assertThat(card.find("button[data-action='qty-change'][data-step='-1']").exists()).isTrue();
        assertThat(card.find("button[data-action='qty-change'][data-step='1']").exists()).isTrue();
        assertThat(card.find("button[data-action='add-to-cart']").text()).isEqualTo("В корзину");
    }

    // =================================================================
    //  2. МОДАЛЬНОЕ ОКНО КОРЗИНЫ
    // =================================================================

    @Test
    @DisplayName("DOM корзины: модалка скрыта, элементы на месте, структура позиции")
    void cartModalDomStructure() {
        long productId = createProductViaApi(uniqueName("DOM-Корзина"), 10.0);
        Selenide.open("/");
        checkUrl("/");

        // До открытия модалка скрыта (style display:none).
        CartModal modal = new CartModal();
        modal.checkHidden();

        // Добавляем товар и открываем корзину.
        MainPage mainPage = new MainPage();
        mainPage.addProductToCart(productId, 2);
        CartModal cart = mainPage.openCart();
        cart.checkVisible();

        // Структура позиции #cart-item-{id}.
        SelenideElement item = cart.cartItem(productId);
        assertThat(item.getAttribute("class")).contains("cart-item");
        // Название в <b>.
        assertThat(item.find("b").text()).isEqualTo(mainPage.productCard(productId).getDataName());
        // Количество в <span> внутри блока .qty-controls.
        assertThat(item.find(".qty-controls span").text()).isEqualTo("2");
        // Сумма по позиции в div с выравниванием вправо.
        assertThat(item.find("div[style*='text-align:right']").text()).contains("20");
        // Кнопка удаления с data-action=remove.
        assertThat(item.find("button[data-action='remove']").exists()).isTrue();

        // Итоговая сумма в #total-price.
        assertThat(cart.totalPriceElement().text()).isEqualTo("20");

        // Кол-во позиций в DOM.
        int domItems = $$("#cart-items .cart-item").size();
        // Мы добавляли ОДИН товар (в количестве 2) — позиция в DOM одна.
        assertThat(domItems).as("позиций в DOM корзины").isEqualTo(1);
        // А счётчик в шапке показывает СУММУ КОЛИЧЕСТВ всех позиций — у нас 2.
        assertThat(mainPage.getCartCount()).as("счётчик корзины (сумма количеств)").isEqualTo(2);
    }

    // =================================================================
    //  3. СТРАНИЦА ВХОДА
    // =================================================================

    @Test
    @DisplayName("DOM /login: форма, поля и кнопка имеют корректные атрибуты")
    void loginPageDomStructure() {
        new LoginPage().openPage();
        checkUrl("/login");

        // Форма: класс login-form, метод POST, action /login.
        SelenideElement form = $(".login-form");
        assertThat(form.getAttribute("method")).isEqualTo("post");
        // ВНИМАНИЕ: браузер возвращает атрибут action в АБСОЛЮТНОМ виде
        // (http://127.0.0.1:8080/login), хотя в HTML написано относительное
        // значение "/login". Поэтому проверяем окончание строки.
        assertThat(form.getAttribute("action")).endsWith("/login");

        // Заголовок формы.
        assertThat(form.find("h2").text()).isEqualTo("Please sign in");

        // Поле логина: id, name, type, required.
        SelenideElement username = $("#username");
        assertThat(username.getAttribute("name")).isEqualTo("username");
        assertThat(username.getAttribute("type")).isEqualTo("text");
        assertThat(username.getAttribute("required")).isNotNull();

        // Поле пароля: id, name, type, required.
        SelenideElement password = $("#password");
        assertThat(password.getAttribute("name")).isEqualTo("password");
        assertThat(password.getAttribute("type")).isEqualTo("password");
        assertThat(password.getAttribute("required")).isNotNull();

        // Кнопка отправки формы.
        assertThat($("button[type='submit']").text()).isEqualTo("Sign in");
    }

    // =================================================================
    //  4. СТРАНИЦА АДМИНИСТРИРОВАНИЯ
    // =================================================================

    @Test
    @DisplayName("DOM /admin: форма добавления, таблица с заголовками и структура строки")
    void adminPageDomStructure() {
        // Готовим товар через API — чтобы в таблице была строка для проверки.
        long productId = createProductViaApi(uniqueName("DOM-Админ"), 42.0);

        // Входим в админку.
        AdminPage adminPage = new AdminPage().openPage();
        if (Selenide.webdriver().driver().url().contains("/login")) {
            adminPage = new LoginPage()
                    .loginAs(ru.stepup.api.config.ApiConfig.getUsername(),
                            ru.stepup.api.config.ApiConfig.getPassword());
        }
        adminPage.checkPageDisplayed();
        checkUrl("/admin");

        // Заголовок страницы.
        assertThat($("h1").text()).isEqualTo("SmartShop Admin");

        // Форма добавления нового товара: поля и кнопка по id.
        assertThat($("#n-name").getAttribute("placeholder")).isEqualTo("Название");
        assertThat($("#n-price").getAttribute("placeholder")).isEqualTo("Цена");
        assertThat($("#add-btn").text()).isEqualTo("Создать");

        // Заголовки таблицы (thead).
        ElementsCollection headers = $$("table thead th");
        assertThat(headers.texts()).containsExactly("ID", "Название", "Цена (₽)", "Действия");

        // Структура строки созданного товара.
        assertThat($("#nm-" + productId).exists()).as("поле имени в таблице").isTrue();
        assertThat($("#pr-" + productId).exists()).as("поле цены в таблице").isTrue();
        assertThat($("button[data-action='update'][data-id='" + productId + "']").text()).isEqualTo("Сохранить");
        assertThat($("button[data-action='delete'][data-id='" + productId + "']").text()).isEqualTo("Удалить");
    }

    // =================================================================
    //  5. КОНСИСТЕНТНОСТЬ DOM И ДАННЫХ
    // =================================================================

    @Test
    @DisplayName("Количество карточек в DOM совпадает с количеством товаров в API")
    void domCardsCountMatchesApiGoodsCount() {
        // Создаём пару товаров через API.
        createProductViaApi(uniqueName("DOM-Число-1"), 1.0);
        createProductViaApi(uniqueName("DOM-Число-2"), 2.0);
        Selenide.open("/");
        checkUrl("/");

        // Сколько карточек в DOM.
        int domCount = $$(".product-card").size();
        // Сколько товаров в API (главная страница показывает первые 20).
        int apiCount = goodsApi.getAllProducts(0, 20).jsonPath().getList("goods").size();

        // Должно совпадать: иначе часть товаров не отрисовалась.
        assertThat(domCount).as("карточки в DOM == товары в API").isEqualTo(apiCount);
        assertThat(domCount).isGreaterThan(0);
    }
}