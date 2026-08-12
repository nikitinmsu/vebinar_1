package ru.stepup.api.assertion;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import ru.stepup.api.model.Product;

/**
 * =====================================================================
 *  ProductAssert — собственный AssertJ-ассерт для товара
 * =====================================================================
 *
 *  ЧТО ТАКОЕ AbstractAssert&lt;SELF, ACTUAL&gt;
 *  ------------------------------------------------
 *  Встроенные проверки AssertJ (assertThat(строка), assertThat(число)...)
 *  умеют проверять только «стандартные» типы. Для своих классов можно
 *  написать собственный ассерт, унаследовавшись от
 *  {@code AbstractAssert<SELF, ACTUAL>}, где:
 *
 *    - SELF   — тип самого ассерта (для «текучих» цепочек, fluent API);
 *    - ACTUAL — тип проверяемого объекта (наш Product).
 *
 *  Зачем такой «самотип» SELF? Чтобы работали цепочки:
 *  <pre>
 *      assertThat(product).hasName("Телефон").hasPrice(59999.99);
 *  </pre>
 *  Если методы возвращали бы тип родителя (AbstractAssert), то после
 *  первого вызова мы «потеряли» бы методы hasName/hasPrice. Возвращая
 *  тип SELF (здесь — ProductAssert), мы сохраняем весь доступный набор
 *  методов на протяжении всей цепочки. Именно поэтому в каждом методе
 *  в конце стоит {@code return this;} — а тип возврата {@code ProductAssert}.
 *
 *  КАК ИСПОЛЬЗОВАТЬ
 *  ------------------------------------------------
 *  Статический фабричный метод {@link #assertThat(Product)} позволяет
 *  писать те же читаемые строки, что и для встроенных типов:
 *  <pre>
 *      import static ru.stepup.api.assertion.ProductAssert.assertThat;
 *
 *      assertThat(product).hasName("Телефон").hasPrice(59999.99);
 *  </pre>
 */
public class ProductAssert extends AbstractAssert<ProductAssert, Product> {

    /**
     * Конструктор, вызываемый из фабричного метода.
     *
     * @param actual проверяемый товар (может быть null — это валидно,
     *               просто упадут проверки isNotNull и т.п.)
     */
    public ProductAssert(Product actual) {
        super(actual, ProductAssert.class);
    }

    // =================================================================
    //  ФАБРИЧНЫЙ МЕТОД — «входная точка» для цепочек
    // =================================================================

    /**
     * Возвращает ассерт для переданного товара.
     * Статический импорт этого метода делает проверки читаемыми:
     * {@code assertThat(product).hasName("X")}.
     *
     * @param actual проверяемый товар
     * @return ассерт для товара
     */
    public static ProductAssert assertThat(Product actual) {
        return new ProductAssert(actual);
    }

    // =================================================================
    //  ПРОВЕРКИ (каждая возвращает SELF для fluent-цепочек)
    // =================================================================
    // Соглашение по неймингу AssertJ:
    //   hasXxx(...)   — проверка «есть поле со значением Xxx»;
    //   isNotNull()   — наследуется из AbstractAssert, проверяет наличие объекта.
    //
    // ВАЖНО: перед работой с полями ВСЕГДА делаем isNotNull() — иначе
    // при null-товаре будем получать NPE вместо понятного сообщения об ошибке.

    /**
     * Проверяет, что товар существует (не null).
     * Переопределяем, чтобы сообщение об ошибке было человекочитаемым.
     *
     * @return этот же ассерт (для цепочек)
     */
    @Override
    public ProductAssert isNotNull() {
        super.isNotNull();
        return this;
    }

    /**
     * Проверяет идентификатор товара.
     *
     * @param expected ожидаемый id
     * @return этот же ассерт
     */
    public ProductAssert hasId(Long expected) {
        isNotNull();
        // info.overridingErrorMessage(...) — подменяем стандартное сообщение
        // AssertJ своим, более понятным. ВАЖНО: в этой версии AssertJ метод
        // принимает ТОЛЬКО готовую строку (без формат-аргументов), поэтому
        // строку форматируем сами через String.format.
        info.overridingErrorMessage(
                String.format("Ожидали id товара = [%s], но он = [%s]", expected, actual.getId()));
        objects.assertEqual(info, actual.getId(), expected);
        return this;
    }

    /**
     * Проверяет название товара.
     *
     * @param expected ожидаемое название
     * @return этот же ассерт
     */
    public ProductAssert hasName(String expected) {
        isNotNull();
        info.overridingErrorMessage(
                String.format("Ожидали название товара = [%s], но получили [%s]",
                        expected, actual.getName()));
        objects.assertEqual(info, actual.getName(), expected);
        return this;
    }

    /**
     * Проверяет цену товара с учётом погрешности сравнения double.
     * Для денег сравнение через {@code ==} ненадёжно, поэтому используем
     * {@code isCloseTo} — разница меньше порога считается ок.
     *
     * @param expected ожидаемая цена
     * @param offset   допустимая погрешность (например, 0.01)
     * @return этот же ассерт
     */
    public ProductAssert hasPrice(double expected, double offset) {
        isNotNull();
        info.overridingErrorMessage(
                String.format("Ожидали цену товара = [%s] (погрешность [%s]), но получили [%s]",
                        expected, offset, actual.getPrice()));
        Assertions.assertThat(actual.getPrice())
                .isCloseTo(expected, Offset.offset(offset));
        return this;
    }

    /**
     * Проверяет цену товара БЕЗ погрешности — для целых значений цены.
     *
     * @param expected ожидаемая цена
     * @return этот же ассерт
     */
    public ProductAssert hasPrice(double expected) {
        return hasPrice(expected, 0.0);
    }
}
