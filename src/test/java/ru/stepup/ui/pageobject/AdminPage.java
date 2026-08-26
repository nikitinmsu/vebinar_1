package ru.stepup.ui.pageobject;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ru.stepup.ui.pageobject.header.AdminPageHeaders;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

/**
 * =====================================================================
 *  AdminPage — Page Object страницы администрирования /admin
 * =====================================================================
 *
 *  Админка доступна ТОЛЬКО авторизованным пользователям (иначе Spring
 *  Security редиректит на /login). На странице можно создавать, изменять
 *  и удалять товары.
 *
 *  HTML страницы:
 *  <pre>
 *      <h1>SmartShop Admin</h1>
 *      <a href="/">Вернуться на сайт</a>
 *
 *      <div class="card">
 *          <h3>Новый товар</h3>
 *          <input type="text"   id="n-name"  placeholder="Название">
 *          <input type="number" id="n-price" placeholder="Цена">
 *          <button id="add-btn" class="btn btn-add">Создать</button>
 *      </div>
 *
 *      <table>
 *          <tbody id="tbody">
 *              <tr>
 *                  <td>42</td>
 *                  <td><input id="nm-42"  value="Телефон"></td>
 *                  <td><input id="pr-42"  value="59999.99"></td>
 *                  <td>
 *                      <button class="btn btn-upd" data-action="update" data-id="42">Сохранить</button>
 *                      <button class="btn btn-del" data-action="delete" data-id="42">Удалить</button>
 *                  </td>
 *              </tr>
 *          </tbody>
 *      </table>
 *  </pre>
 *
 *  ЛОКАТОРЫ: в приоритете id. У полей в таблице id строятся по id товара:
 *  {@code #nm-{id}} (имя) и {@code #pr-{id}} (цена). Кнопки локализуем
 *  по data-action и data-id.
 *
 *  Заголовки страницы вынесены в {@link AdminPageHeaders}.
 */
public class AdminPage {

    /** Заголовки/ссылка шапки админки. */
    private final AdminPageHeaders headers = new AdminPageHeaders();

    /** Поле «Название» нового товара: #n-name (по id). */
    private final SelenideElement newProductNameInput = $("#n-name");

    /** Поле «Цена» нового товара: #n-price (по id). */
    private final SelenideElement newProductPriceInput = $("#n-price");

    /** Кнопка «Создать»: #add-btn (по id). */
    private final SelenideElement addButton = $("#add-btn");

    /** Тело таблицы товаров: #tbody (по id). */
    private final SelenideElement tableBody = $("#tbody");

    /** Все строки таблицы товаров: tr внутри tbody. */
    private final ElementsCollection tableRows = $$("#tbody tr");

    // =================================================================
    //  НАВИГАЦИЯ
    // =================================================================

    /**
     * Открывает админку напрямую. Если пользователь не авторизован —
     * будет редирект на /login (это тоже можно проверять в тестах).
     *
     * @return this
     */
    public AdminPage openPage() {
        open("/admin");
        return this;
    }

    // =================================================================
    //  ПРОВЕРКИ СТРАНИЦЫ
    // =================================================================

    /**
     * Проверяет, что админка отобразилась: шапка и таблица видны.
     *
     * @return this
     */
    public AdminPage checkPageDisplayed() {
        headers.checkHeadersVisible();
        tableBody.shouldBe(visible);
        return this;
    }

    /**
     * Возвращает количество строк в таблице товаров.
     *
     * @return число товаров в таблице
     */
    public int getTableRowCount() {
        return tableRows.size();
    }

    /**
     * Проверяет, что товар присутствует в таблице админки.
     * Поле имени #nm-{id} существует только если строка отрисована.
     *
     * @param productId id товара
     * @return this
     */
    public AdminPage checkProductInTable(long productId) {
        $("#nm-" + productId).should(exist);
        return this;
    }

    // =================================================================
    //  БИЗНЕС-ДЕЙСТВИЯ: СОЗДАНИЕ ТОВАРА
    // =================================================================

    /**
     * Создаёт товар через UI админки: заполняет поля и кликает «Создать».
     * После успешного создания сервер отвечает, и JS перерисовывает таблицу.
     *
     * @param name  название товара
     * @param price цена товара
     * @return this
     */
    public AdminPage addProduct(String name, double price) {
        newProductNameInput.shouldBe(visible).setValue(name);
        newProductPriceInput.shouldBe(visible).setValue(String.valueOf(price));
        addButton.click();
        return this;
    }

    // =================================================================
    //  БИЗНЕС-ДЕЙСТВИЯ: РЕДАКТИРОВАНИЕ ТОВАРА
    // =================================================================

    /**
     * Изменяет цену товара в таблице и сохраняет (кнопка «Сохранить»).
     *
     * @param productId id товара
     * @param newPrice  новая цена
     * @return this
     */
    public AdminPage updateProductPrice(long productId, double newPrice) {
        $("#pr-" + productId).setValue(String.valueOf(newPrice));
        $("button[data-action='update'][data-id='" + productId + "']").click();
        return this;
    }

    /**
     * Изменяет название товара в таблице и сохраняет.
     *
     * @param productId id товара
     * @param newName   новое название
     * @return this
     */
    public AdminPage updateProductName(long productId, String newName) {
        $("#nm-" + productId).setValue(newName);
        $("button[data-action='update'][data-id='" + productId + "']").click();
        return this;
    }

    /**
     * Возвращает цену товара из поля #pr-{id} (текущее значение в таблице).
     *
     * @param productId id товара
     * @return цена
     */
    public double getProductPrice(long productId) {
        return Double.parseDouble($("#pr-" + productId).val());
    }

    /**
     * Возвращает название товара из поля #nm-{id}.
     *
     * @param productId id товара
     * @return название
     */
    public String getProductName(long productId) {
        return $("#nm-" + productId).val();
    }

    // =================================================================
    //  БИЗНЕС-ДЕЙСТВИЯ: УДАЛЕНИЕ ТОВАРА
    // =================================================================

    /**
     * Удаляет товар через UI админки (кнопка «Удалить» + подтверждение).
     * ВАЖНО: в браузере откроется диалог confirm() — его нужно явно
     * подтвердить через {@code Selenide.confirm()}, иначе следующий же
     * WebDriver-командой Selenium бросит UnhandledAlertException.
     * После удаления JS перерисовывает таблицу.
     *
     * @param productId id товара
     * @return this
     */
    public AdminPage deleteProduct(long productId) {
        $("button[data-action='delete'][data-id='" + productId + "']").click();
        // Подтверждаем JS-диалог «Удалить товар из SmartShop?».
        // Selenide сам дождётся его появления и нажмёт «ОК».
        com.codeborne.selenide.Selenide.confirm();
        return this;
    }

    /**
     * Проверяет, что товара больше НЕТ в таблице админки.
     *
     * @param productId id товара
     * @return this
     */
    public AdminPage checkProductAbsent(long productId) {
        // Поле #nm-{id} исчезает вместе со строкой таблицы.
        $("#nm-" + productId).shouldNot(exist);
        return this;
    }

    // =================================================================
    //  ДОСТУП К КОМПОНЕНТАМ
    // =================================================================

    /**
     * Возвращает «шапку» админки.
     *
     * @return объект шапки
     */
    public AdminPageHeaders headers() {
        return headers;
    }

    /**
     * Возвращает коллекцию строк таблицы — для проверки количества.
     *
     * @return ElementsCollection строк
     */
    public ElementsCollection tableRows() {
        return tableRows;
    }

    /**
     * Проверяет, что количество строк в таблице стало равно ожидаемому.
     * Используем Selenide-условие size(...) — оно ждёт появления/исчезновения
     * строк (JS перерисовывает таблицу после ответа сервера асинхронно).
     *
     * @param expected ожидаемое количество строк
     * @return this
     */
    public AdminPage checkTableRowCount(int expected) {
        tableRows.shouldHave(com.codeborne.selenide.CollectionCondition.size(expected));
        return this;
    }

    /**
     * Ищет id товара по его названию в таблице админки.
     * Нужно, когда товар создан через UI и сервер не вернул его id
     * (идем по названию: у поля имени в строке id = "nm-{id}").
     *
     * @param name название товара (значение поля #nm-*)
     * @return id товара
     * @throws AssertionError если товар с таким названием не найден
     */
    public long findProductIdByName(String name) {
        for (SelenideElement row : tableRows) {
            SelenideElement nameInput = row.$("input[id^='nm-']");
            if (nameInput.exists() && name.equals(nameInput.val())) {
                // Достаём id из атрибута id элемента: "nm-42" -> "42".
                String id = nameInput.getAttribute("id").substring(3);
                return Long.parseLong(id);
            }
        }
        throw new AssertionError("Товар с названием '" + name + "' не найден в таблице админки");
    }
}