package ru.stepup.ui.test;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.stepup.ui.base.BaseUiTest;
import ru.stepup.ui.pageobject.CartModal;
import ru.stepup.ui.pageobject.MainPage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * =====================================================================
 *  CartUiTest — тесты корзины SmartShop (добавление/удаление/суммы)
 * =====================================================================
 *
 *  Что проверяем:
 *  1. добавление товаров и изменение СЧЁТЧИКА корзины в шапке (#cart-count);
 *  2. состав корзины (какие товары и в каком количестве в корзине);
 *  3. изменение количества -> пересчёт суммы по позиции и итога;
 *  4. удаление позиций из корзины;
 *  5. корректность сумм (итог = сумма по позициям);
 *  6. пустая корзина.
 *
 *  ПОДГОТОВКА ДАННЫХ ЧЕРЕЗ API
 *  ------------------------------------------------
 *  Товары для теста создаём через REST API (быстрее и надёжнее, чем
 *  «накликивать» через админку), затем работаем с ними ЧЕРЕЗ UI.
 *  Это пример гибридного подхода: «подготовка данных — API,
 *  проверка пользовательского сценария — UI».
 *  В @AfterEach (BaseUiTest) созданные товары удаляются.
 *
 *  ПРАВИЛО ВЕБИНАРА: ПОСЛЕ КАЖДОГО ДЕЙСТВИЯ ПРОВЕРЯЕМ URL, наличие
 *  элементов и количество элементов коллекций.
 */
@Tag("ui")
class CartUiTest extends BaseUiTest {

    /** Товар №1 (цена с копейками, чтобы проверить честные суммы). */
    private long product1Id;
    /** Товар №2. */
    private long product2Id;

    /**
     * Перед каждым тестом: создаём два товара через API и ОБНОВЛЯЕМ
     * главную страницу, чтобы новые карточки появились в DOM.
     * (BaseUiTest открывает "/" ДО этого метода, поэтому переоткрываем.)
     */
    @BeforeEach
    void createProductsViaApi() {
        product1Id = createProductViaApi(uniqueName("Корзина-Товар1"), 100.5);
        product2Id = createProductViaApi(uniqueName("Корзина-Товар2"), 250.0);
        // Перезагружаем страницу, чтобы карточки товаров появились.
        Selenide.open("/");
        checkUrl("/");
    }

    // =================================================================
    //  1. СЧЁТЧИК КОРЗИНЫ
    // =================================================================

    @Test
    @DisplayName("Счётчик корзины увеличивается после добавления товаров")
    void addProductsUpdatesCartCounter() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();

        // На старте счётчик = 0.
        assertThat(mainPage.getCartCount()).as("счётчик корзины до добавления").isEqualTo(0);

        // Действие 1: добавляем первый товар в количестве 1.
        mainPage.productCard(product1Id).addToCart(1);
        // Проверка: счётчик стал 1 (ждём через Selenide-условие).
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("1"));

        // Действие 2: добавляем второй товар в количестве 2.
        mainPage.productCard(product2Id).addToCart(2);
        // Проверка: счётчик стал 1 + 2 = 3.
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("3"));

        // Финальная проверка через метод Page Object.
        assertThat(mainPage.getCartCount()).as("итоговый счётчик корзины").isEqualTo(3);

        // URL не изменился — мы всё ещё на главной странице.
        checkUrl("/");
    }

    @Test
    @DisplayName("Счётчик учитывает количество товара, а не только позиции")
    void cartCounterCountsQuantitiesNotJustLines() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();

        // Добавляем ОДИН товар в количестве 5.
        mainPage.productCard(product1Id).addToCart(5);

        // Счётчик должен стать 5 (сумма всех количеств), а не 1.
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("5"));
    }

    // =================================================================
    //  2. СОСТАВ КОРЗИНЫ
    // =================================================================

    @Test
    @DisplayName("Корзина содержит добавленные товары")
    void cartContainsAddedProducts() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();

        // Добавляем оба товара в корзину.
        mainPage.addProductToCart(product1Id, 1);
        mainPage.addProductToCart(product2Id, 1);
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("2"));

        // Открываем корзину.
        CartModal cart = mainPage.openCart();
        cart.checkVisible();

        // Проверка состава: две позиции, обе карточки видны.
        cart.checkItemsCount(2);
        cart.checkItemVisible(product1Id);
        cart.checkItemVisible(product2Id);

        // Проверка имён в корзине (состав корзины).
        assertThat(cart.getItemNames()).contains(
                mainPage.productCard(product1Id).getDataName(),
                mainPage.productCard(product2Id).getDataName());

        // Проверка: количество каждого товара в корзине = 1.
        cart.checkItemQuantity(product1Id, 1);
        cart.checkItemQuantity(product2Id, 1);
    }

    // =================================================================
    //  3. ИЗМЕНЕНИЕ КОЛИЧЕСТВА И СУММЫ
    // =================================================================

    @Test
    @DisplayName("Увеличение количества пересчитывает сумму по позиции и итог")
    void increaseQuantityUpdatesSubtotalAndTotal() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();
        double price1 = mainPage.productCard(product1Id).getDataPrice();

        mainPage.addProductToCart(product1Id, 1);
        CartModal cart = mainPage.openCart();
        cart.checkVisible();

        // Изначально: 1 шт по цене price1.
        cart.checkItemQuantity(product1Id, 1);
        cart.checkItemSubtotal(product1Id, price1);
        cart.checkTotalPrice(price1);

        // Действие: увеличиваем количество на 1 (кнопка «+» в корзине).
        cart.increaseQuantity(product1Id);

        // Проверка: количество стало 2, сумма по позиции удвоилась.
        cart.checkItemQuantity(product1Id, 2);
        cart.checkItemSubtotal(product1Id, price1 * 2);
        cart.checkTotalPrice(price1 * 2);
        // Итог согласуется с суммой по позициям.
        cart.checkTotalEqualsSumOfItems();

        // Счётчик в шапке тоже обновился (1 -> 2).
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("2"));
    }

    // =================================================================
    //  4. УДАЛЕНИЕ ПОЗИЦИЙ
    // =================================================================

    @Test
    @DisplayName("Удаление позиции убирает её из корзины и меняет итог")
    void removeItemUpdatesCart() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();
        double price1 = mainPage.productCard(product1Id).getDataPrice();
        double price2 = mainPage.productCard(product2Id).getDataPrice();

        mainPage.addProductToCart(product1Id, 1);
        mainPage.addProductToCart(product2Id, 2);
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("3"));

        CartModal cart = mainPage.openCart();
        cart.checkVisible();
        cart.checkItemsCount(2);

        // Действие: удаляем первый товар (кнопка «✕»).
        cart.removeItem(product1Id);

        // Проверка: осталась одна позиция, удалённого товара нет.
        cart.checkItemsCount(1);
        cart.checkItemAbsent(product1Id);
        cart.checkItemVisible(product2Id);

        // Проверка итога: сумма = price2 * 2 (товар2 в количестве 2).
        cart.checkTotalPrice(price2 * 2);
        cart.checkTotalEqualsSumOfItems();

        // Счётчик в шапке уменьшился: 3 -> 2.
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("2"));
    }

    // =================================================================
    //  5. СУММЫ
    // =================================================================

    @Test
    @DisplayName("Итог корзины равен сумме всех позиций")
    void totalEqualsSumOfItems() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();
        double price1 = mainPage.productCard(product1Id).getDataPrice();
        double price2 = mainPage.productCard(product2Id).getDataPrice();

        // Товар1 в количестве 2, товар2 в количестве 3.
        mainPage.addProductToCart(product1Id, 2);
        mainPage.addProductToCart(product2Id, 3);
        mainPage.headers().cartCountElement().shouldHave(Condition.exactText("5"));

        CartModal cart = mainPage.openCart();
        cart.checkVisible();

        // Ожидаемая сумма: 2*price1 + 3*price2.
        double expected = 2 * price1 + 3 * price2;
        cart.checkTotalPrice(expected);
        cart.checkTotalEqualsSumOfItems();

        // Сверка с расчётом «в лоб» по строкам корзины.
        assertThat(cart.getItemSubtotal(product1Id)).isEqualTo(2 * price1);
        assertThat(cart.getItemSubtotal(product2Id)).isEqualTo(3 * price2);
    }

    // =================================================================
    //  6. ПУСТАЯ КОРЗИНА
    // =================================================================

    @Test
    @DisplayName("Пустая корзина показывает «Пусто» и сумму 0")
    void emptyCartShowsEmptyMessage() {
        MainPage mainPage = new MainPage();
        mainPage.checkPageDisplayed();

        // Ничего не добавляли — счётчик 0.
        assertThat(mainPage.getCartCount()).isEqualTo(0);

        CartModal cart = mainPage.openCart();
        cart.checkVisible();

        // Проверка: пусто, позиций нет, сумма 0.
        cart.checkEmpty();
        cart.checkItemsCount(0);
        cart.checkTotalText("0");
        assertThat(cart.getTotalPrice()).isZero();
    }
}