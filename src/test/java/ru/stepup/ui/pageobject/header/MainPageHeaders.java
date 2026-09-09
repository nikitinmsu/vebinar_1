package ru.stepup.ui.pageobject.header;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;

/**
 * =====================================================================
 *  MainPageHeaders — «шапка» главной страницы SmartShop
 * =====================================================================
 *
 *  ЗАЧЕМ ОТДЕЛЬНЫЙ КЛАСС ДЛЯ ЗАГОЛОВКОВ
 *  ------------------------------------------------
 *  Заголовки (h1), ссылки навигации и счётчик корзины — это «обвязка»
 *  страницы, которая видна на КАЖДОЙ странице сайта. Логично вынести
 *  её в отдельный класс-компонент: тогда и MainPage, и другие страницы
 *  могут переиспользовать её, а тест читается как:
 *  <pre>
 *      pageObject.headers().checkTitle();   // «шапка на месте»
 *  </pre>
 *
 *  ЛОКАТОРЫ: В ПРИОРИТЕТЕ id И href
 *  ------------------------------------------------
 *  По требованиям вебинара локаторы берём в первую очередь по id, затем
 *  по href (атрибуты ссылок). id уникален на странице, а href — устойчив
 *  к изменениям вёрстки. Только если их нет — используем класс/тег.
 *
 *  ДЛЯ СПРАВКИ — как выглядит HTML шапки:
 *  <pre>
 *      <h1 id="main-title">🛍 SmartShop</h1>
 *      <a href="/admin" class="btn-outline">Администрирование</a>
 *      <button class="btn btn-cart" id="open-cart-btn">🛒 Корзина (<span id="cart-count">0</span>)</button>
 *  </pre>
 */
public class MainPageHeaders {

    // =================================================================
    //  ЛОКАТОРЫ ЭЛЕМЕНТОВ ШАПКИ
    // =================================================================

    /** Заголовок магазина. Локатор: по id (#main-title). */
    private final SelenideElement mainTitle = $("#main-title");

    /** Ссылка «Администрирование» — ведёт в админку. Локатор: по href. */
    private final SelenideElement adminLink = $("a[href='/admin']");

    /** Кнопка открытия корзины. Локатор: по id (#open-cart-btn). */
    private final SelenideElement cartButton = $("#open-cart-btn");

    /** Счётчик товаров в корзине. Локатор: по id (#cart-count). */
    private final SelenideElement cartCount = $("#cart-count");

    // =================================================================
    //  МЕТОДЫ-ПРОВЕРКИ («шапка отобразилась корректно»)
    // =================================================================

    /**
     * Проверяет, что шапка полностью отрисована: виден заголовок,
     * ссылка на админку и кнопка корзины. Вызывается после открытия
     * страницы и после каждого перехода (требование вебинара).
     *
     * @return this — для «текучих» цепочек
     */
    public MainPageHeaders checkHeadersVisible() {
        mainTitle.shouldBe(visible);
        adminLink.shouldBe(visible);
        cartButton.shouldBe(visible);
        return this;
    }

    /**
     * Проверяет точный текст заголовка магазина.
     *
     * @return this
     */
    public MainPageHeaders checkTitle(String expectedTitle) {
        mainTitle.shouldHave(exactText(expectedTitle));
        return this;
    }

    // =================================================================
    //  БИЗНЕС-ДЕЙСТВИЯ С ШАПКОЙ
    // =================================================================

    /**
     * Клик по ссылке «Администрирование».
     * Без авторизации пользователя редиректнет на /login,
     * с авторизацией — откроет /admin. Проверку результата делает тест.
     */
    public void clickAdminLink() {
        adminLink.shouldBe(visible).click();
    }

    /**
     * Клик по кнопке «Корзина» — открывает модальное окно корзины.
     */
    public void clickCartButton() {
        cartButton.shouldBe(visible).click();
    }

    /**
     * Возвращает текущее значение счётчика корзины (число товаров).
     * Счётчик обновляется JS после каждого изменения корзины.
     *
     * @return количество товаров в корзине, 0 если пусто
     */
    public int getCartCount() {
        String text = cartCount.shouldBe(visible).text();
        return Integer.parseInt(text.trim());
    }

    /**
     * Возвращает элемент-счётчик для продвинутых проверок (например,
     * проверка что счётчик обновился на конкретное значение через условие).
     *
     * @return SelenideElement счётчика
     */
    public SelenideElement cartCountElement() {
        return cartCount;
    }
}