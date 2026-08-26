package ru.stepup.ui.pageobject;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

/**
 * =====================================================================
 *  ProductCard — «компонент» карточки товара на главной странице
 * =====================================================================
 *
 *  Главная страница рендерит товары из API в виде карточек:
 *  <pre>
 *      <div class="product-card" id="card-42"
 *           data-id="42" data-name="Телефон" data-price="59999.99">
 *          <h4>Телефон</h4>
 *          <div>59999.99 ₽</div>
 *          <div class="qty-controls">
 *              <button class="qty-btn" data-action="qty-change" data-id="42" data-step="-1">-</button>
 *              <input type="number" id="q-42" class="qty-input" value="1" min="1">
 *              <button class="qty-btn" data-action="qty-change" data-id="42" data-step="1">+</button>
 *          </div>
 *          <button class="btn" data-action="add-to-cart" data-name="Телефон"
 *                  data-price="59999.99" data-id="42">В корзину</button>
 *      </div>
 *  </pre>
 *
 *  Это НЕ отдельная страница, а повторяющийся блок внутри MainPage,
 *  поэтому реализуем его как КОМПОНЕНТ (вспомогательный Page Object).
 *  Локаторы строятся динамически по id товара — id карточки и id поля
 *  количества совпадают с id товара.
 */
public class ProductCard {

    /** id товара, с карточкой которого работаем. */
    private final long productId;

    /** Корневой элемент карточки: #card-{id} (локатор по id). */
    private final SelenideElement card;

    /** Поле ввода количества: #q-{id} (локатор по id). */
    private final SelenideElement qtyInput;

    /** Кнопка «В корзину»: по атрибуту data-action + data-id. */
    private final SelenideElement addToCartButton;

    /** Кнопка «-» (уменьшить количество). */
    private final SelenideElement minusButton;

    /** Кнопка «+» (увеличить количество). */
    private final SelenideElement plusButton;

    /**
     * Создаёт компонент для карточки конкретного товара.
     *
     * @param productId id товара (он же — часть id элементов карточки)
     */
    public ProductCard(long productId) {
        this.productId = productId;
        this.card = $("#card-" + productId);
        this.qtyInput = $("#q-" + productId);
        this.addToCartButton = $("button[data-action='add-to-cart'][data-id='" + productId + "']");
        this.minusButton = $("button[data-action='qty-change'][data-id='" + productId + "'][data-step='-1']");
        this.plusButton = $("button[data-action='qty-change'][data-id='" + productId + "'][data-step='1']");
    }

    // =================================================================
    //  ПРОВЕРКИ
    // =================================================================

    /**
     * Проверяет, что карточка товара видна на странице.
     *
     * @return this — для цепочек
     */
    public ProductCard checkVisible() {
        card.shouldBe(visible);
        return this;
    }

    /**
     * Возвращает название товара из атрибута data-name карточки.
     * Именно этот атрибут JS отправляет в корзину, поэтому проверяем его.
     *
     * @return название товара
     */
    public String getDataName() {
        return card.getAttribute("data-name");
    }

    /**
     * Возвращает цену товара из атрибута data-price.
     *
     * @return цена товара
     */
    public double getDataPrice() {
        return Double.parseDouble(card.getAttribute("data-price"));
    }

    // =================================================================
    //  БИЗНЕС-ДЕЙСТВИЯ
    // =================================================================

    /**
     * Устанавливает количество товара в поле ввода.
     * Selenide.setValue очищает поле и вводит новое значение.
     *
     * @param quantity новое количество (>= 1)
     * @return this
     */
    public ProductCard setQuantity(int quantity) {
        qtyInput.setValue(String.valueOf(quantity));
        return this;
    }

    /**
     * Клик по кнопке «+» — увеличивает количество на 1.
     *
     * @return this
     */
    public ProductCard increaseQuantity() {
        plusButton.click();
        return this;
    }

    /**
     * Клик по кнопке «-» — уменьшает количество на 1 (минимум 1).
     *
     * @return this
     */
    public ProductCard decreaseQuantity() {
        minusButton.click();
        return this;
    }

    /**
     * Клик по кнопке «В корзину» — добавляет товар (в текущем количестве)
     * в корзину.
     *
     * @return this
     */
    public ProductCard clickAddToCart() {
        addToCartButton.shouldBe(visible).click();
        return this;
    }

    /**
     * Короткий сценарий: задать количество и добавить в корзину.
     *
     * @param quantity количество товара
     * @return this
     */
    public ProductCard addToCart(int quantity) {
        return setQuantity(quantity).clickAddToCart();
    }
}