package ru.stepup.ui.selector;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.stepup.ui.base.BaseUiTest;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * =====================================================================
 *  SelectorsDemoTest — наглядные примеры CSS и XPath селекторов
 * =====================================================================
 *
 *  ЧТО ТАКОЕ СЕЛЕКТОР?
 *  ------------------------------------------------
 *  Селектор — это «адрес» элемента в DOM-дереве страницы. DOM (Document
 *  Object Model) — то, во что браузер превращает HTML. С помощью селекторов
 *  мы говорим Selenide: «найди мне вот этот элемент».
 *
 *  ДВА ОСНОВНЫХ СЕМЕЙСТВА:
 *  ------------------------------------------------
 *  1. CSS-селекторы — родной язык браузеров, тот же, что в CSS-файлах.
 *     Компактные, читаются слева направо. Selenide: $("css-селектор").
 *  2. XPath-селекторы — язык запросов к XML/DOM. Мощнее CSS: умеет искать
 *     по ТЕКСТУ, по ПОЗИЦИИ, по РОДИТЕЛЯМ. Selenide: $x("//xpath").
 *
 *  ТАБЛИЦА СООТВЕТСТВИЙ (самое важное):
 *  <pre>
 *      Ищем                                  CSS                  XPath
 *      ------------------------------------  -------------------  --------------------------
 *      элемент с id="main-title"             #main-title          //*[@id='main-title']
 *      элемент с классом "product-card"      .product-card        //*[contains(@class,'product-card')]
 *      все элементы с тегом "h1"             h1                   //h1
 *      по атрибуту href="/admin"             a[href='/admin']     //a[@href='/admin']
 *      по атрибуту data-id="42"              [data-id='42']       //*[@data-id='42']
 *      по тексту "Администрирование"         (нет прямого способа) //a[text()='Администрирование']
 *      дочерний h4 внутри #card-42           #card-42 h4          //*[@id='card-42']//h4
 *  </pre>
 *
 *  ПРАВИЛА ВЕБИНАРА ДЛЯ ЛОКАТОРОВ (в порядке приоритета):
 *    1) id — уникален и стабилен (#main-title, #cart-count);
 *    2) href — у ссылок (a[href='/admin']);
 *    3) прочие атрибуты (data-*), классы, теги;
 *    4) XPath — только когда CSS не справляется (поиск по тексту и т.п.).
 *
 *  Все тесты ниже выполняются на ЖИВОМ сайте SmartShop и показывают,
 *  что CSS и XPath находят одни и те же элементы.
 */
@Tag("ui")
class SelectorsDemoTest extends BaseUiTest {

    /** id товара, созданного через API специально для демонстрации. */
    private long demoProductId;

    /**
     * Перед каждым тестом создаём свой товар через API (см. BaseUiTest) —
     * чтобы карточка на главной странице гарантированно существовала
     * и демонстрация не зависела от состояния базы.
     *
     * ВАЖНО: BaseUiTest.@BeforeEach открывает "/" ДО этого метода,
     * а товар создаётся ПОСЛЕ — поэтому перезагружаем страницу, чтобы
     * карточка созданного товара появилась в DOM.
     */
    @BeforeEach
    void createDemoProduct() {
        demoProductId = createProductViaApi(uniqueName("Демо-Товар"), 1234.5);
        Selenide.open("/");
        checkUrl("/");
    }

    // =================================================================
    //  1. БАЗОВЫЕ CSS-СЕЛЕКТОРЫ
    // =================================================================

    @Test
    @DisplayName("CSS: поиск по id (#main-title)")
    void cssById() {
        // Локатор "#id" — символ # + значение атрибута id.
        SelenideElement title = $("#main-title");
        title.shouldBe(visible);
        // Тот же элемент можно найти по тегу h1 — это «другой адрес».
        assertThat($("h1").text()).isEqualTo(title.text());
    }

    @Test
    @DisplayName("CSS: поиск по классу (.product-card)")
    void cssByClass() {
        // Локатор ".class" — точка + имя класса. Возвращает КОЛЛЕКЦИЮ ($$).
        ElementsCollection cards = $$(".product-card");
        cards.shouldHave(sizeGreaterThan(0)); // коллекция не пустая
        System.out.println("На странице карточек товаров: " + cards.size());
    }

    @Test
    @DisplayName("CSS: поиск по атрибуту и его значению")
    void cssByAttribute() {
        // Локатор [attr='value'] — квадратные скобки + значение атрибута.
        // Кнопка «В корзину» идентифицируется по data-action + data-id.
        SelenideElement addBtn = $("button[data-action='add-to-cart'][data-id='" + demoProductId + "']");
        addBtn.shouldBe(visible);
        assertThat(addBtn.text()).isEqualTo("В корзину");
    }

    @Test
    @DisplayName("CSS: поиск ссылки по href")
    void cssByHref() {
        // У ссылки нет id — берём по атрибуту href (приоритет №2 вебинара).
        SelenideElement adminLink = $("a[href='/admin']");
        adminLink.shouldBe(visible);
        assertThat(adminLink.text()).isEqualTo("Администрирование");
    }

    @Test
    @DisplayName("CSS: составной селектор и потомки (descendant)")
    void cssCombinator() {
        // Пробел в CSS-селекторе = «потомок». "h4 внутри карточки товара".
        SelenideElement cardTitle = $("#card-" + demoProductId + " h4");
        cardTitle.shouldBe(visible);
        // Название в карточке совпадает с именем созданного товара.
        assertThat(cardTitle.text()).startsWith("Демо-Товар");
    }

    // =================================================================
    //  2. БАЗОВЫЕ XPATH-СЕЛЕКТОРЫ
    // =================================================================

    @Test
    @DisplayName("XPath: поиск по id (//*[@id='...'])")
    void xpathById() {
        // XPath начинается с "//" (ищем где угодно в документе).
        // @id — атрибут id; * — любой тег.
        SelenideElement title = $x("//*[@id='main-title']");
        title.shouldBe(visible);
        assertThat(title.text()).contains("SmartShop");
    }

    @Test
    @DisplayName("XPath: поиск по классу через contains()")
    void xpathByClass() {
        // В CSS класс ищется как ".product-card", в XPath — через contains,
        // потому что class — это АТРИБУТ со списком классов.
        // ВНИМАНИЕ: $x() берёт ПЕРВЫЙ элемент, а $$x() — КОЛЛЕКЦИЮ.
        ElementsCollection cards = $$x("//div[contains(@class,'product-card')]");
        assertThat(cards.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("XPath: поиск по тексту (то, что CSS не умеет)")
    void xpathByText() {
        // Главное преимущество XPath: поиск по ТЕКСТУ элемента.
        // text()='...' — точное совпадение текста ссылки.
        SelenideElement adminLink = $x("//a[text()='Администрирование']");
        adminLink.shouldBe(visible);
        // contains(text(),'...') — частичное совпадение текста.
        SelenideElement cartButton = $x("//button[contains(text(),'Корзина')]");
        cartButton.shouldBe(visible);
    }

    @Test
    @DisplayName("XPath: несколько условий (and) и комбинирование с id")
    void xpathMultipleConditions() {
        // Один и тот же элемент можно найти и через id карточки,
        // и через пару атрибутов.
        SelenideElement byId = $x("//button[contains(@data-id,'" + demoProductId + "')]");
        // Ищем кнопку «+» (увеличение количества) у нашего товара.
        SelenideElement plus = $x("//button[@data-action='qty-change' and @data-id='" + demoProductId + "' and @data-step='1']");
        plus.shouldBe(visible);
        assertThat(byId.exists() || byId.isDisplayed()).isTrue();
    }

    // =================================================================
    //  3. CSS И XPATH НАХОДЯТ ОДНО И ТО ЖЕ
    // =================================================================

    @Test
    @DisplayName("CSS и XPath находят один и тот же элемент")
    void cssEqualsXpath() {
        // Локаторы разных семейств ведут к одному DOM-узлу:
        // проверяем по id атрибута DOM-элемента.
        SelenideElement cssEl = $("#main-title");
        SelenideElement xpathEl = $x("//h1[@id='main-title']");
        // Оба видны и оба отдают один и тот же текст.
        cssEl.shouldBe(visible);
        xpathEl.shouldBe(visible);
        assertThat(cssEl.getAttribute("id")).isEqualTo(xpathEl.getAttribute("id"));
        assertThat(cssEl.text()).isEqualTo(xpathEl.text());
    }

    @Test
    @DisplayName("Коллекции: CSS и XPath возвращают одинаковое количество элементов")
    void collectionsCssVsXpath() {
        int cssCount = $$(".product-card").size();
        // В XPath элементы с классом ищем через contains — коллекция та же.
        ElementsCollection byXpath = $$x("//div[contains(@class,'product-card')]");
        assertThat(byXpath.size()).as("CSS и XPath должны находить одинаковое число карточек").isEqualTo(cssCount);
    }

    // =================================================================
    //  4. ПРОДВИНУТЫЕ ПРИЁМЫ
    // =================================================================

    @Test
    @DisplayName("CSS: псевдоклассы и поиск внутри родителя")
    void cssAdvanced() {
        // :first-child — первый элемент-ребёнок. Найдём первый h4 в карточке.
        SelenideElement firstTitle = $("#card-" + demoProductId + " h4:first-child");
        firstTitle.shouldBe(visible);
        assertThat(firstTitle.text()).startsWith("Демо-Товар");

        // Кнопка «-» в карточке имеет атрибут data-step="-1".
        SelenideElement minus = $("button[data-action='qty-change'][data-id='" + demoProductId + "'][data-step='-1']");
        minus.shouldBe(visible);
        assertThat(minus.text()).isEqualTo("-");
    }

    @Test
    @DisplayName("XPath: позиция элемента (n-й элемент коллекции)")
    void xpathPosition() {
        // (//...)[n] — взять n-й элемент найденного набора (нумерация с 1).
        SelenideElement firstCard = $x("(//div[contains(@class,'product-card')])[1]");
        firstCard.shouldBe(visible);
        // У первой карточки в DOM должен быть атрибут id вида "card-...".
        assertThat(firstCard.getAttribute("id")).startsWith("card-");
    }

    @Test
    @DisplayName("CSS: текстовые проверки содержимого (Condition.text/exactText)")
    void cssTextConditions() {
        // shouldHave(text(...)) — содержит текст; exactText — точное совпадение.
        $("#main-title").shouldHave(text("SmartShop"));
        // Обратите внимание: в заголовке есть эмодзи, поэтому точное
        // совпадение пишем полностью.
        $("#main-title").shouldHave(exactText("🛍 SmartShop"));
    }
}