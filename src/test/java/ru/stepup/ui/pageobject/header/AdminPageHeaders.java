package ru.stepup.ui.pageobject.header;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

/**
 * =====================================================================
 *  AdminPageHeaders — «шапка» страницы администрирования /admin
 * =====================================================================
 *
 *  Шапка админки состоит из заголовка и ссылки возврата на сайт.
 *  Локатор ссылки берём по href ("/") — по требованиям вебинара.
 *
 *  HTML (фрагмент):
 *  <pre>
 *      <h1>SmartShop Admin</h1>
 *      <a href="/" style="color:#64748b">Вернуться на сайт</a>
 *  </pre>
 */
public class AdminPageHeaders {

    /** Заголовок админки. id у него нет — берём по тегу h1. */
    private final SelenideElement adminTitle = $("h1");

    /** Ссылка «Вернуться на сайт». Локатор: по href. */
    private final SelenideElement backToSiteLink = $("a[href='/']");

    /**
     * Проверяет, что шапка админки отобразилась.
     *
     * @return this
     */
    public AdminPageHeaders checkHeadersVisible() {
        adminTitle.shouldBe(visible);
        backToSiteLink.shouldBe(visible);
        return this;
    }

    /**
     * Клик по ссылке «Вернуться на сайт» — переход на главную страницу.
     */
    public void clickBackToSite() {
        backToSiteLink.shouldBe(visible).click();
    }

    /**
     * Возвращает элемент заголовка для проверки точного текста.
     *
     * @return элемент h1
     */
    public SelenideElement adminTitleElement() {
        return adminTitle;
    }
}