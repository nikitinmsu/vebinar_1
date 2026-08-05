package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * =====================================================================
 *  AssertJ — проверки для ЧИСЕЛ (IntegerAssert, DoubleAssert, ...)
 * =====================================================================
 *
 *  Для каждого типа в AssertJ есть свой «ассертер»:
 *    int/long  ->  AbstractIntegerAssert / AbstractLongAssert
 *    double    ->  DoubleAssert  (float -> FloatAssert)
 *    BigDecimal->  AbstractBigDecimalAssert
 *  Благодаря этому многие проверки («чётное», «NaN», «по модулю») уже
 *  типизированы и не дадут смешать типы.
 *
 *  Запустить только этот класс:
 *     ./gradlew test --tests "ru.stepup.AssertJ_NumericAssertionsTest"
 */
class AssertJ_NumericAssertionsTest {

    @Test
    @DisplayName("Сравнение чисел")
    void comparison() {
        assertThat(5).isEqualTo(5);
        assertThat(5).isNotEqualTo(4);

        assertThat(7).isGreaterThan(5);
        assertThat(7).isGreaterThanOrEqualTo(7);   // >=
        assertThat(3).isLessThan(10);
        assertThat(3).isLessThanOrEqualTo(3);      // <=

        // Диапазон ВКЛЮЧИТЕЛЬНО: [1; 20] — и 1, и 20 подойдут.
        assertThat(10).isBetween(1, 20);

        // Строго между: концы 1 и 20 НЕ входят в интервал.
        assertThat(10).isStrictlyBetween(1, 20);

        // Принадлежность множеству значений.
        assertThat(1).isIn(1, 2, 3);
        assertThat(1).isNotIn(10, 20, 30);
    }

    @Test
    @DisplayName("Знак и чётность")
    void signAndParity() {
        assertThat(5).isPositive();
        assertThat(-5).isNegative();
        assertThat(0).isZero();

        assertThat(5).isOdd();      // 5 % 2 != 0
        assertThat(-5).isOdd();     // -5 тоже нечётное
        assertThat(4).isEven();     // 4 % 2 == 0

        assertThat(4).isNotZero();
        assertThat(-1).isNotPositive();  // -1 не является положительным
    }

    @Test
    @DisplayName("Дробные числа и точность (важная тема!)")
    void doubles() {
        // Классическая ловушка плавающей арифметики:
        // 0.1 + 0.2 == 0.30000000000000004, а НЕ ровно 0.3.
        double sum = 0.1 + 0.2;

        // Так проверять НЕЛЬЗЯ — тест упадёт:
        // assertThat(sum).isEqualTo(0.3);

        // Правильно: сравнивать с ДОПУСКОМ.
        // within(0.000_001) означает «разница не больше 0.000001».
        assertThat(sum).isEqualTo(0.3, within(0.000_001));

        // isCloseTo — то же самое, но читается нагляднее.
        assertThat(sum).isCloseTo(0.3, within(0.000_001));

        // Для дробных тоже работают диапазон и знак.
        assertThat(3.14).isBetween(3.0, 3.2);
        assertThat(sum).isPositive();

        // Отдельная проверка для NaN («не число»).
        assertThat(Double.NaN).isNaN();
    }

    @Test
    @DisplayName("BigDecimal: сравнение по значению")
    void bigDecimal() {
        // BigDecimal сравнивают по ЧИСЛОВОМУ значению, а не через equals():
        // из-за разного масштаба equals() вернёт false для 0.10 и 0.1.
        assertThat(new java.math.BigDecimal("0.10"))
                .isEqualByComparingTo(new java.math.BigDecimal("0.1"));
    }
}
