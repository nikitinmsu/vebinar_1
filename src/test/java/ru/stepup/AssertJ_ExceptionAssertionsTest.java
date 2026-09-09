package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * =====================================================================
 *  AssertJ — проверки ИСКЛЮЧЕНИЙ
 * =====================================================================
 *
 *  В JUnit для этого используется assertThrows(...), но он умеет проверять
 *  только тип исключения. AssertJ позволяет проверить и тип, и сообщение,
 *  и причину (cause), причём цепочкой.
 *
 *  Запустить только этот класс:
 *     ./gradlew test --tests "ru.stepup.AssertJ_ExceptionAssertionsTest"
 */
class AssertJ_ExceptionAssertionsTest {

    /** Класс из проекта: divide(1, 0) бросает IllegalArgumentException("Division by zero"). */
    private final Calculator calculator = new Calculator();

    @Test
    @DisplayName("assertThatThrownBy: поймать и проверить исключение")
    void thrownBy() {
        // Внутри лямбды выполняется код, который ДОЛЖЕН бросить исключение.
        // assertThatThrownBy «ловит» его и возвращает ассертер исключения,
        // у которого есть проверки типа, сообщения, причины и т.д.
        assertThatThrownBy(() -> calculator.divide(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Division by zero")          // сообщение ровно такое
                .hasMessageContaining("by zero")         // ...содержит подстроку
                .hasMessageStartingWith("Division");     // ...начинается с подстроки
    }

    @Test
    @DisplayName("assertThatExceptionOfType: тип известен заранее")
    void exceptionOfType() {
        // Сначала указываем ОЖИДАЕМЫЙ тип исключения, затем код.
        // Удобно, когда важен именно тип, а сообщение проверять не нужно.
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> calculator.divide(10, 0))
                .withMessage("Division by zero");        // withMessage = hasMessage
    }

    @Test
    @DisplayName("Короткие фабрики для частых исключений")
    void shortcuts() {
        // Для самых популярных исключений есть готовые фабрики —
        // не приходится повторять имя класса исключения.
        assertThatIllegalArgumentException()
                .isThrownBy(() -> calculator.divide(1, 0));

        assertThatIllegalStateException()
                .isThrownBy(() -> {
                    throw new IllegalStateException("bad state");
                });

        assertThatNullPointerException()
                .isThrownBy(() -> {
                    String s = null;
                    s.length();   // обращение к null -> NPE
                });
    }

    @Test
    @DisplayName("BDD-стиль: catchThrowable(...)")
    void catchThrowableStyle() {
        // catchThrowable «ловит» исключение и возвращает его как обычное значение.
        // Дальше проверяем через assertThat — удобно, когда исключение нужно
        // проверить в нескольких независимых assertThat-блоках.
        Throwable thrown = catchThrowable(() -> calculator.divide(1, 0));

        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("zero");
    }

    @Test
    @DisplayName("Проверка, что исключение НЕ было выброшено")
    void noException() {
        // assertThatCode «пропускает» код. Если исключения нет —
        // doesNotThrowAnyException() проходит, тест зелёный.
        assertThatCode(() -> calculator.divide(4, 2))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Причина исключения (cause)")
    void cause() {
        // Иногда исключение «заворачивают» в другое. Проверяем оба уровня:
        // снаружи RuntimeException, внутри (cause) — IOException.
        assertThatThrownBy(() -> {
            try {
                throw new IOException("source failure");
            } catch (IOException e) {
                throw new RuntimeException("wrapped", e);
            }
        })
                .isInstanceOf(RuntimeException.class)
                .hasMessage("wrapped")
                .hasCauseInstanceOf(IOException.class)      // непосредственная причина
                .hasRootCauseInstanceOf(IOException.class); // самая нижняя причина
    }
}
