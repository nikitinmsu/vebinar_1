package ru.stepup.ui.pageobject;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Condition.hidden;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * =====================================================================
 *  CartModal — Page Object модального окна корзины
 * =====================================================================
 *
 *  Корзина в SmartShop — модальное окно поверх страницы (не отдельный
 *  URL), поэтому это «компонент», а не страница. Открывается кликом по
 *  кнопке «Корзина» в шапке.
 *
 *  HTML окна:
 *  <pre>
 *      <div id="cartModal" class="modal">            <-- скрыто по умолчанию (display:none)
 *          <span id="close-modal">×</span>
 *          <h2>Корзина SmartShop</h2>
 *          <div id="cart-items">
 *              <div class="cart-item" id="cart-item-42">
 *                  <div><b>Телефон</b></div>
 *                  <div class="qty-controls">
 *                      <button data-action="cart-qty" data-index="0" data-step="-1">-</button>
 *                      <span>2</span>
 *                      <button data-action="cart-qty" data-index="0" data-step="1">+</button>
 *                  </div>
 *                  <div>119999.98 ₽</div>
 *                  <button data-action="remove" data-index="0">✕</button>
 *              </div>
 *          </div>
 *          <div>Сумма: <span id="total-price">0</span> ₽</div>
 *          <button id="makeOrder" class="btn">Оформить заказ</button>
 *      </div>
 *  </pre>
 *
 *  ВАЖНАЯ ОСОБЕННОСТЬ: у элементов внутри корзины НЕТ id по товару —
 *  JS использует data-index (позицию товара в корзине). Поэтому методы
 *  изменения/удаления сначала вычисляют индекс товара по id его строки
 *  (#cart-item-{id}), а потом уже кликают по нужной кнопке.
 */
public class CartModal {

    /** Модальное окно целиком: #cartModal (локатор по id). */
    private final SelenideElement modal = $("#cartModal");

    /** Контейнер списка позиций: #cart-items (локатор по id). */
    private final SelenideElement cartItemsContainer = $("#cart-items");

    /** Коллекция строк-позиций: .cart-item. */
    private final ElementsCollection cartItems = $$("#cart-items .cart-item");

    /** Строка «Пусто», когда корзина пуста: #empty-cart.
     *  ВАЖНО: этот элемент появляется в DOM только ПОСЛЕ первого рендера
     *  корзины (например, когда добавили товар, а потом удалили). В самом
     *  начале, пока корзину ни разу не открывали с товарами, контейнер
     *  #cart-items просто пуст — элемента «Пусто» ещё нет. Поэтому для
     *  проверки пустоты полагаемся на отсутствие позиций .cart-item. */
    private final SelenideElement emptyCart = $("#empty-cart");

    /** Итоговая сумма: #total-price (локатор по id). */
    private final SelenideElement totalPrice = $("#total-price");

    /** Кнопка «Оформить заказ»: #makeOrder (локатор по id). */
    private final SelenideElement makeOrderButton = $("#makeOrder");

    // =================================================================
    //  ПРОВЕРКИ ОТОБРАЖЕНИЯ
    // =================================================================

    /**
     * Проверяет, что модальное окно корзины открыто (видно).
     *
     * @return this
     */
    public CartModal checkVisible() {
        modal.shouldBe(visible);
        return this;
    }

    /**
     * Проверяет, что модальное окно закрыто (скрыто). Полезно для
     * проверки стартового состояния DOM.
     *
     * @return this
     */
    public CartModal checkHidden() {
        modal.shouldBe(hidden);
        return this;
    }

    // =================================================================
    //  ПРОВЕРКИ СОСТАВА И КОЛИЧЕСТВА
    // =================================================================

    /**
     * Количество позиций в корзине (строк .cart-item).
     *
     * @return число позиций
     */
    public int getItemsCount() {
        return cartItems.size();
    }

    /**
     * Проверяет количество позиций в корзине.
     *
     * @param expected ожидаемое число позиций
     * @return this
     */
    public CartModal checkItemsCount(int expected) {
        assertThat(getItemsCount())
                .as("количество позиций в корзине")
                .isEqualTo(expected);
        return this;
    }

    /**
     * Возвращает список названий товаров в корзине (в порядке добавления).
     * Используется для проверки «состава корзины».
     *
     * @return список названий
     */
    public List<String> getItemNames() {
        List<String> names = new ArrayList<>();
        for (SelenideElement item : cartItems) {
            names.add(item.$("b").text());
        }
        return names;
    }

    /**
     * Проверяет, что корзина пуста.
     * Ориентируемся на отсутствие позиций .cart-item (см. комментарий
     * у поля emptyCart — элемента «Пусто» в самом начале ещё нет).
     *
     * @return this
     */
    public CartModal checkEmpty() {
        assertThat(cartItems.size()).as("количество позиций в пустой корзине").isZero();
        return this;
    }

    // =================================================================
    //  РАБОТА С КОНКРЕТНОЙ ПОЗИЦИЕЙ
    // =================================================================

    /**
     * Возвращает строку корзины для товара: #cart-item-{id}.
     *
     * @param productId id товара
     * @return элемент строки корзины
     */
    public SelenideElement cartItem(long productId) {
        return $("#cart-item-" + productId);
    }

    /**
     * Проверяет, что товар присутствует в корзине.
     *
     * @param productId id товара
     * @return this
     */
    public CartModal checkItemVisible(long productId) {
        cartItem(productId).shouldBe(visible);
        return this;
    }

    /**
     * Проверяет, что товар ОТСУТСТВУЕТ в корзине.
     *
     * @param productId id товара
     * @return this
     */
    public CartModal checkItemAbsent(long productId) {
        cartItem(productId).shouldBe(hidden);
        return this;
    }

    /**
     * Возвращает количество товара в позиции (число в блоке счётчика).
     *
     * @param productId id товара
     * @return количество
     */
    public int getItemQuantity(long productId) {
        return Integer.parseInt(cartItem(productId).$(".qty-controls span").text().trim());
    }

    /**
     * Проверяет количество конкретного товара в корзине.
     *
     * @param productId id товара
     * @param expected  ожидаемое количество
     * @return this
     */
    public CartModal checkItemQuantity(long productId, int expected) {
        assertThat(getItemQuantity(productId))
                .as("количество товара " + productId + " в корзине")
                .isEqualTo(expected);
        return this;
    }

    /**
     * Возвращает сумму по позиции (цена * количество) — текст блока
     * с правой стороны строки корзины.
     *
     * @param productId id товара
     * @return сумма по позиции
     */
    public double getItemSubtotal(long productId) {
        String raw = cartItem(productId)
                .$("div[style*='text-align:right']")
                .text()
                .replace("₽", "")
                .trim();
        return Double.parseDouble(raw);
    }

    /**
     * Проверяет сумму по позиции (цена * количество).
     *
     * @param productId id товара
     * @param expected  ожидаемая сумма по позиции
     * @return this
     */
    public CartModal checkItemSubtotal(long productId, double expected) {
        assertThat(getItemSubtotal(productId))
                .as("сумма по позиции товара " + productId)
                .isEqualTo(expected);
        return this;
    }

    /**
     * Вычисляет ИНДЕКС позиции в корзине по id товара.
     * JS оперирует data-index, поэтому для кликов по кнопкам
     * «+/−/удалить» нужен именно индекс строки.
     *
     * @param productId id товара
     * @return индекс позиции в коллекции .cart-item
     */
    private int indexOfItem(long productId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getAttribute("id").equals("cart-item-" + productId)) {
                return i;
            }
        }
        throw new AssertionError("Товар с id=" + productId + " не найден в корзине");
    }

    // =================================================================
    //  БИЗНЕС-ДЕЙСТВИЯ С ПОЗИЦИЕЙ
    // =================================================================

    /**
     * Увеличивает количество товара в корзине на 1 (кнопка «+»).
     *
     * @param productId id товара
     * @return this
     */
    public CartModal increaseQuantity(long productId) {
        int index = indexOfItem(productId);
        $("button[data-action='cart-qty'][data-index='" + index + "'][data-step='1']")
                .click();
        return this;
    }

    /**
     * Уменьшает количество товара в корзине на 1 (минимум 1).
     *
     * @param productId id товара
     * @return this
     */
    public CartModal decreaseQuantity(long productId) {
        int index = indexOfItem(productId);
        $("button[data-action='cart-qty'][data-index='" + index + "'][data-step='-1']")
                .click();
        return this;
    }

    /**
     * Удаляет позицию из корзины (кнопка «✕»).
     *
     * @param productId id товара
     * @return this
     */
    public CartModal removeItem(long productId) {
        int index = indexOfItem(productId);
        $("button[data-action='remove'][data-index='" + index + "']")
                .click();
        return this;
    }

    // =================================================================
    //  СУММЫ И ОФОРМЛЕНИЕ
    // =================================================================

    /**
     * Возвращает итоговую сумму корзины (#total-price).
     *
     * @return сумма в рублях
     */
    public double getTotalPrice() {
        return Double.parseDouble(totalPrice.text().replace("₽", "").trim());
    }

    /**
     * Проверяет итоговую сумму корзины.
     *
     * @param expected ожидаемая сумма
     * @return this
     */
    public CartModal checkTotalPrice(double expected) {
        assertThat(getTotalPrice())
                .as("итоговая сумма корзины")
                .isEqualTo(expected);
        return this;
    }

    /**
     * Проверяет, что итоговая сумма соответствует сумме по позициям
     * (пересчитываем сами и сравниваем — «проверить суммы»).
     *
     * @return this
     */
    public CartModal checkTotalEqualsSumOfItems() {
        double sum = 0;
        for (SelenideElement item : cartItems) {
            String raw = item.$("div[style*='text-align:right']").text().replace("₽", "").trim();
            sum += Double.parseDouble(raw);
        }
        assertThat(getTotalPrice()).as("сумма позиций и итог").isEqualTo(sum);
        return this;
    }

    /**
     * Клик по кнопке «Оформить заказ».
     *
     * @return this
     */
    public CartModal clickCheckout() {
        makeOrderButton.shouldBe(visible).click();
        return this;
    }

    // =================================================================
    //  ДОСТУП К ЭЛЕМЕНТАМ (для DOM-проверок)
    // =================================================================

    public SelenideElement modalElement() {
        return modal;
    }

    public SelenideElement totalPriceElement() {
        return totalPrice;
    }

    /**
     * Проверяет, что итоговая сумма отображает ожидаемый текст.
     * Удобно для точных DOM-проверок вида «сумма = 300 ₽».
     *
     * @param expectedText ожидаемый текст, например "300"
     * @return this
     */
    public CartModal checkTotalText(String expectedText) {
        totalPrice.shouldHave(text(expectedText));
        return this;
    }
}