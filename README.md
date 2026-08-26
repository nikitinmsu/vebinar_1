# Вебинар: UI-автотестирование на Selenide (SmartShop)

Учебный проект-демонстрация для вебинара по UI-автотестированию.
Целевой сайт — интернет-магазин **SmartShop**, запущенный локально
на `http://127.0.0.1:8080` (REST API + Swagger на `/swagger-ui/index.html`).

Проект содержит:
- **UI-тесты на Selenide** (пакет `ru.stepup.ui`) — основная часть вебинара;
- REST API-тесты на RestAssured (пакет `ru.stepup.api`) — используются и как
  самостоятельные тесты, и как «подготовка/подчистка данных» в UI-тестах;
- юнит-тесты и примеры AssertJ (пакеты `ru.stepup`, `ru.stepup.api.assertion`).

---

## Требования

- Java 21 (Gradle скачает toolchain сам)
- Запущенный сервис SmartShop на `http://127.0.0.1:8080`
- Chrome (Selenide сам скачает подходящий chromedriver через WebDriverManager)
- Доступ к сети для скачивания зависимостей и драйверов

Учётные данные для авторизации (и в UI, и в API): **admin / secret123**.

---

## Как запустить

```bash
./gradlew uiTest                 # UI-тесты (Selenide, headless Chrome)
./gradlew apiTest                # REST API-тесты (RestAssured, нужен сервер)
./gradlew test                   # юнит-тесты
./gradlew runAllTests            # test + integrationTest + slowTest + smokeTest
```

Полезные флаги:

```bash
# переопределить адрес сервиса / креды для API-части
./gradlew uiTest -Dapi.base.uri=http://localhost -Dapi.port=9090 \
                 -Dapi.username=admin -Dapi.password=secret123
```

Вся конфигурация Selenide (браузер, headless, baseUrl, таймауты, скриншоты)
вынесена в отдельный файл **`src/test/resources/selenide.properties`** —
Selenide читает его из classpath автоматически. Системное свойство
`-Dselenide.*` имеет приоритет над файлом (например, чтобы открыть браузер
с окном, достаточно передать в тестовую JVM `-Dselenide.headless=false`).

Отчёты о тестах: `build/reports/tests/uiTest/index.html`.
Скриншоты и HTML-дампы падений: `build/reports/tests/ui-screenshots`.

---

## Структура UI-тестов (`src/test/java/ru/stepup/ui/`)

```
src/test/resources/selenide.properties       ВСЕ настройки Selenide (браузер, baseUrl, таймауты, скриншоты)
config/UiConfig.java                         «Витрина» конфигурации: печатает применённые значения, геттер baseUrl()
base/BaseUiTest.java                         Базовый класс: @BeforeEach/@AfterEach, проверки URL, API-хелперы

pageobject/header/                   ЗАГОЛОВКИ страниц — вынесены в отдельные классы
   MainPageHeaders.java              Шапка главной: #main-title, ссылка «Администрирование», счётчик корзины
   LoginPageHeaders.java             Форма входа: #username, #password, кнопка Sign in
   AdminPageHeaders.java             Шапка админки: h1, ссылка «Вернуться на сайт»

pageobject/                          Page Object-ы страниц и компонентов
   MainPage.java                     Главная: карточки товаров, кнопка корзины
   ProductCard.java                  Компонент карточки товара (#card-{id}, #q-{id})
   CartModal.java                    Модальное окно корзины (#cartModal, #cart-items, #total-price)
   LoginPage.java                    Страница входа /login
   AdminPage.java                    Админка /admin (создание/изменение/удаление товаров)

selector/SelectorsDemoTest.java      Примеры CSS и XPath селекторов на живом сайте

test/                                Тест-сценарии
   AuthorizationUiTest.java          Авторизация: вход в админку, ошибка при неверном пароле
   CartUiTest.java                   Корзина: добавление, счётчик, состав, удаление, суммы
   AdminUiTest.java                  Админка: вход, добавление, смена цены, отражение на главной
   NegativeUiTest.java               Негативные кейсы (UI + API: 400/401/404 и др.)
   DomUiTest.java                    Проверки DOM-дерева (теги, атрибуты, коллекции)
```

---

## Ключевые принципы, показанные в вебинаре

1. **Selenide** — «умные» ожидания вместо `sleep()`, автоматическое
   скачивание драйвера, скриншоты при падении.
2. **Page Object** — локаторы и бизнес-методы спрятаны в классах страниц;
   заголовки вынесены в отдельные классы-компоненты.
3. **Локаторы**: в приоритете `id`, затем `href`, затем атрибуты/классы;
   XPath — для поиска по тексту и позиции.
4. **Проверки после каждого действия** — корректность URL (`checkUrl`),
   видимость элементов, количество элементов коллекций.
5. **Гибрид API + UI** — данные готовятся через REST (`GoodsApi`,
   admin/secret123), пользовательские сценарии выполняются в UI.
6. **Очистка данных после теста** — созданные товары удаляются через API
   в `@AfterEach`, браузер закрывается (полная изоляция тестов).
7. **Негативные тесты** — ошибки авторизации, неверные данные, доступ
   без прав, лимиты количества.
8. **DOM-проверки** — структура дерева: теги, атрибуты, вложенность,
   количество узлов, состояние видимости.

---

## Нюансы сервиса, учтённые в тестах

- GET удалённого товара может вернуть **500** (особенность сервера) —
  надёжный признак удаления — повторный DELETE с кодом **404**.
- В Chrome Selenium возвращает `href`/`action` в **абсолютном** виде,
  а текст тега `<title>` — пустым (используем `Selenide.title()`).
- Поле `#empty-cart` появляется в DOM только после первого рендера корзины,
  поэтому «пустоту» проверяем по отсутствию позиций `.cart-item`.
- Кнопка «Удалить» в админке вызывает JS-диалог `confirm()` — его
  подтверждаем через `Selenide.confirm()`.