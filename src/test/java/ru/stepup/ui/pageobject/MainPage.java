package ru.stepup.ui.pageobject;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import ru.stepup.ui.pageobject.header.MainPageHeaders;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * =====================================================================
 *  MainPage — Page Object главной страницы SmartShop
 * =====================================================================
 *
 *  ЧТО ТАКОЕ PAGE OBJECT
 *  ------------------------------------------------
 *  Паттерн, при котором каждая страница сайта описывается классом:
 *  в классе живут ЛОКАТОРЫ элементов и БИЗНЕС-МЕТОДЫ (что пользователь
 *  может СДЕЛАТЬ на странице). Тест больше не знает про селекторы и
 *  клики — он вызывает понятные методы:
 *  <pre>
 *      mainPage.openCart()                 // «открой корзину»
 *      mainPage.productCard(42).addToCart(2)  // «добавь товар 42 в количестве 2»
 *  </pre>
 *
 *  Преимущества:
 *   1. Тесты читаются как сценарий, а не как простыня HTML-селекторов.
 *   2. Сменилась вёрстка — правим ОДИН класс, все тесты живы.
 *   3. Локаторы не дублируются в каждом тесте.
 *
 *  Главная страница /:
 *  <pre>
 *      <h1 id="main-title">🛍 SmartShop</h1>
 *      <a href="/admin" class="btn-outline">Администрирование</a>
 *      <button class="btn btn-cart" id="open-cart-btn">🛒 Корзина (<span id="cart-count">0</span>)</button>
 *      <div id="products-list" class="products-grid"> ... карточки товаров ... </div>
 *  </pre>
 *
 *  Элементы шапки вынесены в отдельный класс-компонент
 *  {@link MainPageHeaders} (требование вебинара: заголовки — отдельно).
 */
public class MainPage {

    /** «Шапка» страницы — заголовок, ссылка на админку, счётчик корзины. */
    private final MainPageHeaders headers = new MainPageHeaders();

    /** Контейнер списка товаров: #products-list (локатор по id). */
    private final SelenideElement productsList = $("#products-list");

    /** Все карточки товаров на странице: .product-card (id у них нет). */
    private final ElementsCollection productCards = $$(".product-card");

    // =================================================================
    //  НАВИГАЦИЯ
    // =================================================================

    /**
     * Открывает главную страницу по относительному адресу "/"
     * (baseUrl подставится из UiConfig).
     *
     * @return this — PageObject возвращает себя для цепочек
     */
    public MainPage openPage() {
        open("/");
        return this;
    }

    // =================================================================
    //  ПРОВЕРКИ СТРАНИЦЫ
    // =================================================================

    /**
     * Проверяет, что главная страница отобразилась корректно:
     * шапка видна, список товаров не пуст.
     *
     * @return this
     */
    public MainPage checkPageDisplayed() {
        headers.checkHeadersVisible();
        productsList.shouldBe(visible);
        return this;
    }

    /**
     * Проверяет, что мы действительно на главной странице (URL == "/").
     * Это стандартная проверка «после каждого перехода».
     *
     * @return this
     */
    public MainPage checkUrlIsMain() {
        assertThat(WebDriverRunner.url().replaceFirst("\\?.*$", ""))
                .as("текущий URL")
                .isEqualTo(ru.stepup.ui.config.UiConfig.baseUrl() + "/");
        return this;
    }

    /**
     * Возвращает количество карточек товаров на странице.
     * Используется для проверки «количество элементов коллекции».
     *
     * @return число карточек в #products-list
     */
    public int getProductsCount() {
        return productCards.size();
    }

    /**
     * Проверяет, что на странице есть ровно ожидаемое количество товаров.
     *
     * @param expected ожидаемое число карточек
     * @return this
     */
    public MainPage checkProductsCount(int expected) {
        assertThat(getProductsCount())
                .as("количество карточек товаров на странице")
                .isEqualTo(expected);
        return this;
    }

    /**
     * Проверяет, что карточка товара с указанным id есть на странице.
     *
     * @param productId id товара
     * @return this
     */
    public MainPage checkProductVisible(long productId) {
        productCard(productId).checkVisible();
        return this;
    }

    // =================================================================
    //  РАБОТА С КАРТОЧКАМИ ТОВАРОВ
    // =================================================================

    /**
     * Возвращает компонент-карточку конкретного товара.
     *
     * @param productId id товара
     * @return {@link ProductCard} для работы с карточкой
     */
    public ProductCard productCard(long productId) {
        return new ProductCard(productId);
    }

    // =================================================================
    //  РАБОТА С КОРЗИНОЙ (со стороны главной страницы)
    // =================================================================

    /**
     * Добавляет товар в корзину «из UI»: задаёт количество и кликает
     * «В корзину». После действия счётчик в шапке должен измениться —
     * проверку делает тест.
     *
     * @param productId id товара
     * @param quantity  количество
     * @return this
     */
    public MainPage addProductToCart(long productId, int quantity) {
        productCard(productId).addToCart(quantity);
        return this;
    }

    /**
     * Открывает модальное окно корзины кликом по кнопке «Корзина».
     *
     * @return Page Object корзины {@link CartModal}
     */
    public CartModal openCart() {
        headers.clickCartButton();
        return new CartModal();
    }

    /**
     * Возвращает текущее значение счётчика корзины в шапке страницы.
     *
     * @return число товаров в корзине
     */
    public int getCartCount() {
        return headers.getCartCount();
    }

    // =================================================================
    //  ПЕРЕХОДЫ НА ДРУГИЕ СТРАНИЦЫ
    // =================================================================

    /**
     * Кликает по ссылке «Администрирование».
     * ВАЖНО: результат зависит от авторизации:
     *   - не авторизован -> редирект на /login;
     *   - авторизован     -> страница /admin.
     * Поэтому метод просто кликает, а куда попали — проверяет тест
     * через checkUrl(...).
     *
     * @return this
     */
    public MainPage clickAdminLink() {
        headers.clickAdminLink();
        return this;
    }

    // =================================================================
    //  ДОСТУП К КОМПОНЕНТАМ
    // =================================================================

    /**
     * Возвращает «шапку» страницы (заголовок, ссылки, счётчик).
     *
     * @return объект шапки
     */
    public MainPageHeaders headers() {
        return headers;
    }

    /**
     * Возвращает коллекцию всех карточек товаров — для продвинутых
     * проверок количества и состава коллекции.
     *
     * @return ElementsCollection Selenide
     */
    public ElementsCollection productCards() {
        return productCards;
    }
}