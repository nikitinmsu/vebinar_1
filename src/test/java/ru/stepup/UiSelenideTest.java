package ru.stepup;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI-тест на Selenide (тег @Tag("ui")).
 *
 * Запускается ТОЛЬКО задачей uiTest и требует установленный браузер
 * (Chrome) и доступ в интернет:
 *   ./gradlew uiTest
 *
 * В стандартную сборку (check / build) этот тест сознательно НЕ входит,
 * чтобы сборка не падала в окружениях без браузера. В build.gradle.kts
 * для него настроены systemProperty("selenide.browser", "chrome") и
 * systemProperty("selenide.headless", "true").
 */
@Tag("ui")
// =====================================================================
//  АННОТАЦИИ ALLURE: минимальный пример «эпик -> фича -> история».
// =====================================================================
@Epic("UI-тесты SmartShop")
@Feature("Дымовые проверки")
@Story("Доступность внешнего сайта")
@Owner("QA-команда SmartShop")
@Severity(SeverityLevel.TRIVIAL)
class UiSelenideTest {

    @Test
    @DisplayName("Example.com page is reachable")
    void testExampleDotCom() {
        open("https://example.com");

        SelenideElement h1 = $("h1");
        h1.shouldBe(visible);

        assertTrue(h1.text().contains("Example"));
    }
}
