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

---

# Вебинар: Allure — отчёты о тестах

Учебный блок о том, как превратить «сухие» результаты автотестов в
наглядный HTML-отчёт Allure: подключение к проекту, основные аннотации,
шаги (`@Step`), слушатели для UI и API и готовые примеры.

## Что такое Allure и зачем он нужен

Allure — это инструмент построения отчётов о тестах. Он собирает
результаты прогона (какие тесты прошли/упали, сколько заняли, что именно
проверяли, какие шаги выполняли, что отправляли в API, как выглядела
страница при падении) и складывает их в один понятный HTML-отчёт.

Чем полезен на практике:
- **Читаемость.** Вместо `AssertionError` в консоли — дерево тестов с
  шагами, вложениями и понятными именами.
- **Диагностика.** При падении UI-теста сразу виден **скриншот** и
  **HTML-дамп** страницы; для API — **запрос и ответ целиком**.
- **Аналитика.** Видно, сколько тестов, какие упали, по каким фичам,
  какой важности (`@Severity`), кто владелец (`@Owner`).
- **Интеграция.** Отчёт легко приложить к CI-сборке или отдать команде.

## План вебинара (что рассказывать и в каком порядке)

### Блок 1. Проблема: «зелёный прогон» ещё не отчёт
- Что показывает обычный Gradle-отчёт (`build/reports/tests/...`) и чего
  ему не хватает: нет шагов, вложений, иерархии, скриншотов.
- Идея Allure: тесты сами «рассказывают» о себе через аннотации и шаги,
  а инструмент собирает это в красивый отчёт.
- Показать итоговый отчёт «как будет в конце» — чтобы был понятен смысл
  дальнейших блоков.

### Блок 2. Подключение Allure к проекту (5 минут)
- Плагин `io.qameta.allure` в `build.gradle.kts` — что он делает:
  подключает JUnit5-слушатель, настраивает все `Test`-задачи и добавляет
  задачи `allureReport` / `allureServe`.
- Три зависимости-расширения:
  `allure-rest-assured`, `allure-selenide`, `allure-java-commons`.
- Блок `allure { version ... adapter { allureJavaVersion ... frameworks { jupiter } } }`:
  версия рантайма отчёта, версия Java-адаптеров, авто-слушатель JUnit.
- **Ключевая мысль:** версия зависимостей и `allureJavaVersion` должны
  совпадать (у нас `2.35.5`), иначе возможны конфликты.
- Проверка: `./gradlew test --tests "ru.stepup.CalculatorTest"` — после
  прогона появляется `build/allure-results/`.

### Блок 3. Основные аннотации Allure
- **Иерархия** `@Epic` → `@Feature` → `@Story`: как Allure строит дерево
  отчёта и даёт по нему фильтры.
- **Важность** `@Severity` (`BLOCKER`, `CRITICAL`, `NORMAL`, `MINOR`,
  `TRIVIAL`) — на методе теста.
- **Владелец** `@Owner` — кто отвечает за тесты.
- **Ссылки** `@Link` / `@TmsLink` / `@Issue` — на документацию, задачу,
  Swagger.
- **Стабильный id** `@AllureId` — для интеграции с системой управления
  тестами (ТМС).
- `@DisplayName` (JUnit) — человекочитаемое имя теста в отчёте.
- Примеры: `AuthorizationUiTest`, `CartUiTest`, `AdminUiTest`,
  `NegativeUiTest`, `DomUiTest`, `SelectorsDemoTest`, `GoodsApiTest`.

### Блок 4. Шаги `@Step`: без параметров и с параметрами
- Зачем: разбить тест на понятные подшаги прямо в отчёте.
- **Без параметров:** `@Step("Открыть корзину")`.
- **С параметрами:** `@Step("Добавить товар id={productId} в корзину
  (количество: {quantity})")` — значения подставляются из аргументов
  метода (можно обращаться и к полям объекта: `{product.name}`).
- Где ставить: в **Page Object** (бизнес-шаги) и в **хелперах тестов**.
- Как работает: AspectJ-агент (его подключает плагин Allure), поэтому
  `@Step` работает в обычных методах без наследования от Allure-классов.
- Примеры: `LoginPage`, `MainPage`, `AdminPage`, `CartModal`,
  `ProductCard`, `GoodsApi`, `BaseUiTest`, `GoodsApiTest`.

### Блок 5. Слушатели: UI и API
- **Selenide** (`AllureSelenide`): в `UiConfig.init()` —
  `SelenideLogger.addListener("AllureSelenide", new AllureSelenide());`
  После этого все шаги Selenide попадают в отчёт, а при падении
  автоматически прикладываются скриншот и HTML-страницы.
- **RestAssured** (`AllureRestAssured`): в `ApiConfig.baseRequestSpec()` —
  `.addFilter(new AllureRestAssured())` в `RequestSpecBuilder`. После этого
  каждый запрос/ответ из `GoodsApi` виден в отчёте (метод, URL, заголовки,
  тело, статус).
- **Важно:** слушатели надо подключать ДО первых действий (у нас —
  в `@BeforeAll` через `UiConfig.init()` и в фабрике спецификаций API).
- Показать «до/после»: без фильтра в отчёте только проверки, с фильтром —
  вся «переписка» с сервисом.

### Блок 6. Вложения `@Attachment`
- Зачем: приложить к отчёту произвольный контент (HTML, текст, скриншот,
  JSON).
- Примеры в `BaseUiTest`: `attachPageSource()` (HTML страницы),
  `attachCurrentUrl()` (URL).
- Как вызвать: `attachPageSource();` внутри теста — вложение появится в
  шаге теста.

### Блок 7. Сборка и просмотр отчёта
- `./gradlew allureReport` — собрать статичный HTML-отчёт в
  `build/reports/allure-report/allureReport`.
- `./gradlew allureServe` — собрать и сразу открыть отчёт в браузере.
- `./gradlew allureReport --depends-on-tests` — прогнать тесты и собрать
  отчёт одним заходом.
- Разобрать, что видно в отчёте: дерево Epic/Feature/Story, шаги, вложения,
  графики по важности/статусу.

### Блок 8. «Под капотом» и в CI
- `build/allure-results/` — сырые JSON-результаты (их и читает генератор).
- В CI: прогнать тесты → заархивировать `build/allure-results` → собрать
  отчёт отдельным шагом.
- Обсудить: Allure 2 (`allure-commandline`, zip) vs Allure 3 (нужен Node);
  у нас зафиксирован стабильный Allure 2.

## Как запускать материалы

```bash
# 1) Юнит-тесты (сервер не нужен) + отчёт
./gradlew test --tests "ru.stepup.CalculatorTest"   # быстрый чистый прогон
./gradlew allureServe                               # собрать и открыть отчёт

# 2) API-тесты (нужен запущенный сервис на http://127.0.0.1:8080)
./gradlew apiTest
./gradlew allureServe

# 3) UI-тесты (нужен сервис + браузер Chrome)
./gradlew uiTest
./gradlew allureServe

# Отчёт одним заходом (тесты + сборка):
./gradlew allureReport --depends-on-tests
```

Где что лежит:
- сырые результаты — `build/allure-results/`;
- готовый отчёт — `build/reports/allure-report/allureReport/index.html`;
- скриншоты падений UI-тестов — `build/reports/tests/ui-screenshots`.

> ВНИМАНИЕ: среди юнит-тестов есть **намеренно падающий** пример
> (`AssertJ_StringAssertionsTest.customDescription`) — он показывает, как
> выглядит сообщение об ошибке с `as(...)`. Для чистого прогона запускайте
> конкретный класс: `./gradlew test --tests "ru.stepup.CalculatorTest"`.

## Куда «встроен» Allure в проекте

```
build.gradle.kts                                   плагин, зависимости, блок allure {}
src/test/java/ru/stepup/ui/config/UiConfig.java    AllureSelenide в SelenideLogger.addListener
src/test/java/ru/stepup/api/config/ApiConfig.java  AllureRestAssured фильтр в RequestSpecBuilder
src/test/java/ru/stepup/ui/base/BaseUiTest.java    @Step на хелперах + @Attachment (HTML, URL)
src/test/java/ru/stepup/ui/pageobject/*.java       @Step на бизнес-методах Page Object
src/test/java/ru/stepup/api/pageobject/GoodsApi.java  @Step на бизнес-методах API
src/test/java/ru/stepup/ui/test/*.java             @Epic/@Feature/@Story/@Severity/@Owner/@Link/@AllureId
src/test/java/ru/stepup/api/test/GoodsApiTest.java  аннотации Allure для API-тестов
```

## Шпаргалка: аннотации Allure

| Аннотация            | Где ставить | Что делает |
|----------------------|-------------|------------|
| `@Epic("...")`       | класс       | верхний уровень дерева отчёта |
| `@Feature("...")`    | класс       | функциональность внутри Epic |
| `@Story("...")`      | метод/класс | пользовательский сценарий |
| `@Severity(...)`     | метод/класс | важность: BLOCKER/CRITICAL/NORMAL/MINOR/TRIVIAL |
| `@Owner("...")`      | класс/метод | владелец тестов |
| `@Link(...)`         | класс/метод | ссылка на документацию/ресурс |
| `@TmsLink("...")`    | метод       | ссылка на кейс в ТМС |
| `@Issue("...")`      | метод       | ссылка на задачу/баг |
| `@AllureId("...")`   | метод       | стабильный id теста |
| `@Step("...")`       | метод       | шаг в отчёте (значения из аргументов) |
| `@Attachment(...)`   | метод       | вложение в отчёт |
| `@DisplayName(...)`  | класс/метод | (JUnit) читаемое имя теста |

## Ключевые принципы (для рассказа на вебинаре)

1. **Тест — это история.** `@Epic/@Feature/@Story` и `@Step` превращают
   код в читаемый сценарий, понятный не только разработчику.
2. **Одна строка — максимум пользы.** `AllureSelenide` и `AllureRestAssured`
   подключаются одной строкой каждый, но дают скриншоты, HTML и полные
   HTTP-логи.
3. **Слушатели — до первых действий.** Иначе первые шаги не попадут в отчёт.
4. **Версии должны совпадать.** Зависимости Allure и `allureJavaVersion`
   держим одной версией (`2.35.5`).
5. **Отчёт — часть пайплайна.** Результаты (`build/allure-results`) —
   артефакт сборки, отчёт собирается из них отдельным шагом.

---

# Вебинар: конфигурирование проекта (варианты «подтягивания» свойств)

Учебный блок о том, как проект получает свои настройки: от ручного чтения
`java.util.Properties` до типизированных конфигов на `aeonbits.owner`,
с единой точкой входа и контурами (dev/test/prod). Весь код снабжён
подробными комментариями и лежит в пакетах `ru.stepup.config.*`.

## План вебинара

### Блок 1. `getResourceAsStream` — читаем properties из classpath
- Зачем класть настройки в ресурсы (`src/main/resources`): версионирование
  в git, работа из IDE/jar/CI независимо от текущей папки.
- Как открыть поток: `ClassLoader.getResourceAsStream("config/....properties")`.
- Подводные камни: путь **без** ведущего `/`, при отсутствии ресурса метод
  возвращает `null` (нужна явная проверка).
- Пример: `ru.stepup.config.manual.ClasspathProperties`.

### Блок 2. `FileInputStream` — читаем внешний файл с диска
- Когда нужен внешний файл: правки без пересборки, секреты вне git/jar.
- `new FileInputStream(file)` и `try-with-resources`.
- Отличие от classpath: файла может не быть → `FileNotFoundException`,
  путь зависит от рабочей папки запуска.
- Комбинированный приём: дефолты из classpath + внешний файл, если существует.
- Пример: `ru.stepup.config.manual.FileProperties`.

### Блок 3. `aeonbits.owner` — типизированные конфиги
- Идея: интерфейс `extends Config` + файл → owner сам находит значения и
  конвертирует типы (`int`, `double`, `enum`, `URL`, `File`, списки…).
- «Витрина» возможностей на одном интерфейсе `OwnerFeatures`:
  авто-маппинг «метод = ключ», `@Key`, `@DefaultValue`, `@Separator`,
  массивы и `List`, enum (регистр значим!), свой конвертер через
  `@ConverterClass` + `Converter<T>`, переменные `${...}` в значениях,
  параметризация значений `%s` аргументами метода.
- Изменение на лету: маркеры `Mutable` (setProperty/removeProperty) и
  `Accessible` (getProperty/propertyNames) — `MutableConfig`.
- Что в 1.0.12 НЕ «из коробки»: `Map`-методы, `java.time.Duration`
  (в отдельном артефакте owner-java8-extras) — обсудить на вебинаре.

### Блок 4. Единая точка входа в конфиги
- Проблема «сырого» подхода: источники и приоритеты разъезжаются по коду.
- Решение: фасад `ProjectConfig` — все обращения через
  `ProjectConfig.server()`, `ProjectConfig.auth()`; ленивое создание
  и кэширование owner-конфигов, `reset()` для тестов.
- Приоритет выбора контура: `-Dapp.env` → `APP_ENV` → dev по умолчанию.

### Блок 5. Конфиги под разные стенды/контуры (dev/test/prod)
- Файлы `application.properties` (общие дефолты) +
  `application-dev|test|prod.properties` (переопределения контура).
- «Пирог приоритетов» на `@LoadPolicy(LoadType.MERGE)` + `@Sources`:
  `system:properties` → `system:env` → файл контура → общие дефолты →
  `@DefaultValue`.
- Один и тот же код отдаёт разные значения: dev `:8080`, test `:8081`,
  prod `https://smartshop.example.com:8443`.
- Безопасность: в git — только заглушки; настоящие секреты приходят из
  переменных окружения / секретниц CI (`ServerConfig`, `AuthConfig`).

## Как запускать материалы

```bash
./gradlew runConfigDemo                 # сквозная демонстрация всех блоков
./gradlew runConfigDemo -Dapp.env=prod  # показать конфиг прод-контура

# Тесты (быстрые юнит-тесты, сервер не нужен):
./gradlew test --tests "ru.stepup.config.manual.*"     # блоки 1–2
./gradlew test --tests "ru.stepup.config.owner.*"      # блок 3
./gradlew test --tests "ru.stepup.config.core.*"       # блоки 4–5
./gradlew test --tests "ru.stepup.config.*"            # все сразу
```

## Структура учебного кода

```
src/main/resources/config/
   application.properties                  общие дефолты (самый низкий приоритет)
   application-dev.properties              контур DEV
   application-test.properties             контур TEST
   application-prod.properties             контур PROD
   owner-features.properties               файл для «витрины» OwnerFeatures
   manual-example.properties               файл для ручных примеров (блоки 1–2)

src/main/java/ru/stepup/config/
   ConfigDemoMain.java                     сквозная демонстрация (main)
   manual/ClasspathProperties.java         БЛОК 1: getResourceAsStream
   manual/FileProperties.java              БЛОК 2: FileInputStream
   owner/ServerConfig.java                 БЛОК 5: конфиг сервера + приоритеты
   owner/AuthConfig.java                   БЛОК 5: учётные данные + секреты
   owner/OwnerFeatures.java                БЛОК 3: «витрина» возможностей owner
   owner/Endpoint.java, EndpointConverter.java  пример своего конвертера
   owner/MutableConfig.java                БЛОК 3: Mutable + Accessible
   core/Profile.java                       перечисление контуров
   core/ProjectConfig.java                 БЛОК 4: ЕДИНАЯ точка входа

src/test/java/ru/stepup/config/
   manual/ClasspathPropertiesTest.java     проверка блока 1
   manual/FilePropertiesTest.java          проверка блока 2
   owner/OwnerFeaturesTest.java            проверка возможностей owner
   owner/MutableConfigTest.java            проверка Mutable/Accessible
   owner/ServerAuthConfigTest.java         проверка приоритетов источников
   core/ProjectConfigProfilesTest.java     проверка контуров dev/test/prod
```

## Ключевые принципы (для рассказа на вебинаре)

1. **Секреты — не в git.** В файлах лежат заглушки, реальные пароли задаются
   снаружи (переменные окружения, секретницы CI) — так они не попадут в
   артефакт и историю коммитов.
2. **«Пирог приоритетов».** Одна строка кода, но значение можно переопределить
   на любом уровне: дефолты < контур < переменная окружения < `-D...`.
3. **Контур выбирается снаружи** (`-Dapp.env=prod`), код не меняется —
   это и есть готовность к нескольким стендам.
4. **Типизация вместо ручного парсинга** — главная ценность owner: меньше
   кода и ошибок конвертации (`int port()` вместо `Integer.parseInt(...)`).
5. **Единая точка входа** (`ProjectConfig`) — проще найти, отладить и
   переопределить любую настройку; нет «самодеятельности» в каждом классе.

---

# SOLID: принципы на конкретных примерах проекта

Разбор, где и как в этом проекте применяются принципы SOLID.
Каждый пример — реальный класс из кода, на который можно сослаться на вебинаре.

## S — Single Responsibility (единственная ответственность)

> У класса должна быть одна причина для изменения.

1. **Page Object = одна страница/компонент.** `MainPage.java` отвечает только
   за главную страницу, `LoginPage.java` — за вход, `AdminPage.java` — за админку,
   `CartModal.java` — за модальное окно корзины, `ProductCard.java` — за карточку
   товара. Ни один класс не знает про чужую страницу.
2. **Заголовки вынесены в отдельные классы-компоненты**
   (`pageobject/header/MainPageHeaders.java`, `LoginPageHeaders.java`,
   `AdminPageHeaders.java`). У страницы появляется только одна причина меняться:
   если меняется вёрстка шапки — правится header-класс, а не вся страница.
3. **API-слой разбит по ответственностям** (`ru.stepup.api`):
   - `ApiConfig.java` — только конфигурация RestAssured (адрес, auth, таймауты);
   - `Endpoints.java` — только URL и пути ручек;
   - `RestApiBuilder.java` — только «техническая» отправка HTTP-запросов;
   - `GoodsApi.java` — только бизнес-операции над товарами.
   Каждый класс меняется по своей причине: адрес сменился — правим `Endpoints`,
   способ отправки — `RestApiBuilder`, бизнес-сценарий — `GoodsApi`.
4. **Разные DTO под разные задачи.** `Product.java` — модель ответа сервера
   (с полем `id`), `ProductRequest.java` — тело запроса (без `id`). Один класс
   не совмещает «что шлём» и «что получаем».

## O — Open/Closed (открытость/закрытость)

> Класс открыт для расширения, но закрыт для изменения.

1. **`RestApiBuilder` — расширяется без изменения.** Чтобы поддержать новый
   HTTP-метод или ручку, достаточно ДОБАВИТЬ метод (`doGet`, `doPost`, `doPatch`,
   `doDelete` — все самостоятельные). Существующие методы и потребители
   (`GoodsApi`) не трогаются.
2. **`ProductAssert extends AbstractAssert`** — расширяем возможности AssertJ
   своими проверками (`hasId`, `hasName`, `hasPrice`) без изменения библиотеки.
   Класс `ProductAssert.java` «доращивает» чужой код, а не правит его.
3. **`EndpointConverter implements Converter<Endpoint>`** — подключаемся к
   owner-библиотеке через её интерфейс `Converter<T>` и аннотацию
   `@ConverterClass` (см. `OwnerFeatures.java`). Библиотека не меняется — мы
   лишь добавляем новую реализацию.
4. **Allure-слушатели.** `AllureSelenide` и `AllureRestAssured` подключаются
   через «точки расширения» (`SelenideLogger.addListener` в `UiConfig.java`,
   `.addFilter(new AllureRestAssured())` в `ApiConfig.java`). Добавить ещё одного
   слушателя можно, не изменяя существующих.
5. **Owner-конфиги.** Новое свойство конфига = новый метод в интерфейсе
   (`ServerConfig.java`, `AuthConfig.java`) — существующие методы не меняются.

## L — Liskov Substitution (подстановка Барбары Лисков)

> Подкласс должен заменять базовый класс без изменения поведения программы.

1. **`BaseUiTest` — абстрактный базовый класс.** Все UI-тесты
   (`CartUiTest.java`, `AdminUiTest.java`, `AuthorizationUiTest.java`,
   `NegativeUiTest.java`, `DomUiTest.java`) наследуют его и могут быть
   подставлены вместо `BaseUiTest`: жизненный цикл (`@BeforeAll`/
   `@BeforeEach`/`@AfterEach`) и хелперы (`checkUrl`, `createProductViaApi`,
   `uniqueName`) работают одинаково для всех наследников — тесты не знают,
   какой именно подкласс перед ними.
2. **`ProductAssert extends AbstractAssert<ProductAssert, Product>`.** Возврат
   типа `SELF` сохраняет «текучесть» цепочек (`assertThat(p).hasId(...).hasName(...)`),
   то есть подкласс полностью заменяет родителя в любом месте, где ожидается
   `AbstractAssert`, не ломая API.
3. **`EndpointConverter implements Converter<Endpoint>`.** Любая реализация
   `Converter<T>` взаимозаменяема: owner вызывает один и тот же контрактный
   метод `convert(Method, String)` — подставлять другие конвертеры безопасно.

## I — Interface Segregation (разделение интерфейсов)

> Не заставляй клиента зависеть от методов, которые он не использует.

1. **Отдельные конфиг-интерфейсы.** `ServerConfig.java` (настройки сервера) и
   `AuthConfig.java` (учётные данные) разведены по логическим блокам.
   Потребитель через `ProjectConfig.server()` получает только настройки сервера,
   а через `ProjectConfig.auth()` — только авторизацию. При этом `ServerConfig`
   «добирает» себе только `Accessible` (нужен для `getProperty`/`propertyNames`),
   а `AuthConfig` довольствуется одним `Config` — каждый берёт ровно то, что
   использует.
2. **Компоненты вместо «толстых» интерфейсов страниц.** `MainPage` не раскрывает
   методы работы с позициями корзины — для этого есть отдельный `CartModal`;
   `ProductCard` даёт только операции карточки, а не всей страницы. Клиент не
   видит методов, которые ему не нужны.
3. **Интерфейс `Converter<T>` — один метод.** Owner требует от конвертера ровно
   один метод `convert(...)`; мы не «платим» за неиспользуемый функционал.

## D — Dependency Inversion (инверсия зависимостей)

> Зависимость должна быть направлена на абстракцию, а не на конкретную реализацию.

1. **`RestApiBuilder` зависит от абстракции `RequestSpecification`** (интерфейс
   RestAssured), а не от конкретной реализации HTTP-клиента. `GoodsApi.java`
   работает с `RestApiBuilder` как с абстракцией «умеет выполнить запрос», а не
   знает детали авторизации и заголовков — они спрятаны в `ApiConfig`.
2. **Код зависит от owner-интерфейсов, а не от чтения Properties.** Весь код
   (в т.ч. `ProjectConfig.java`) обращается к `ServerConfig`/`AuthConfig` — это
   интерфейсы (абстракции); конкретная реализация-прокси генерируется
   `ConfigFactory.create(...)`. Поменять источник значений можно, не трогая
   потребителей.
3. **Тесты зависят от Page Object, а не от «сырого» HTTP/DOM.** `GoodsApiTest.java`
   и UI-тесты вызывают бизнес-методы (`goodsApi.addProduct(...)`,
   `mainPage.addProductToCart(...)`), а не собирают URL и селекторы руками —
   верхний уровень зависит от абстракций (Page Object), детали реализации
   закрыты на нижних уровнях.
4. **`EndpointConverter` реализует интерфейс `Converter<T>` из библиотеки** — то
   есть зависит от абстракции, а библиотека «зависит» только от контракта
   интерфейса, не от нашего класса.

---

# Архитектурные решения, применённые в проекте

Сводка ключевых архитектурных паттернов и решений, которые «живут» в коде.
Каждый пункт — с указанием, где именно это реализовано.

## 1. Многослойная структура по интересам

Проект разделён на независимые слои-пакеты, каждый отвечает за свой тип задач:

- `ru.stepup.ui` — UI-слой: Page Object'ы, конфиг Selenide, базовый тест, тесты;
- `ru.stepup.api` — API-слой: конфиг RestAssured, билдер запросов, Page Object
  для REST-ручек, DTO-модели, свои ассерты;
- `ru.stepup.config` — слой конфигурации: чтение настроек, контуры dev/test/prod;
- `ru.stepup` — «корень»: юнит-примеры (`Calculator`, `User`, `StringUtils`).

Слои зависят друг от друга направленно: тесты → Page Object → билдер/конфиг.
«Деталей реализации» нижележащих слоёв в тестах нет.

## 2. Page Object (в двух видах: UI и API)

Паттерн применяется дважды:

- **UI:** `MainPage`, `LoginPage`, `AdminPage`, `CartModal`, `ProductCard` —
  локаторы и бизнес-методы спрятаны в классах страниц/компонентов. Тест
  говорит «добавь товар 42 в количестве 2», а не «найди `#q-42` и кликни».
- **API:** `GoodsApi` — тот же Page Object, но для REST: каждый метод = одна
  ручка (`addProduct`, `getProduct`, `updateProduct`, `deleteProduct`), внутри —
  билдер запросов и эндпоинты. Тест не знает про URL и авторизацию.

Плюс: изменение вёрстки/API локализуется в одном классе, все тесты остаются
живы (см. комментарии в `GoodsApi.java` и `MainPage.java`).

## 3. Компонентные объекты вместо «толстых» страниц

Повторяющиеся блоки вынесены в переиспользуемые компоненты:

- шапки страниц — `pageobject/header/*Headers.java`;
- карточка товара — `ProductCard.java`.

`MainPage` не дублирует код шапки и не знает деталей карточки — он
делегирует компонентам (композиция вместо наследования).

## 4. Базовый класс теста (Template Method)

`BaseUiTest` реализует общий «скелет» жизненного цикла UI-теста:

- `@BeforeAll` — одноразовая настройка Selenide (`UiConfig.init()`);
- `@BeforeEach` — открытие главной и проверка URL;
- `@AfterEach` — очистка данных через API + закрытие браузера (полная изоляция).

Наследники (`CartUiTest`, `AdminUiTest`, ...) доопределяют только свои шаги,
не повторяя обвязку. Заодно класс даёт общие хелперы: `checkUrl`,
`createProductViaApi`, `uniqueName`, `attachPageSource`.

## 5. Builder-паттерн для HTTP-запросов

`RestApiBuilder` собирает «каркас» запроса один раз (базовый URL, Basic Auth,
JSON-заголовки, логирование, Allure-фильтр) и даёт готовые методы под каждый
HTTP-метод: `doGet`, `doPost`, `doPatch`, `doDelete`. Это GoF-Builder применительно
к HTTP: детали конструирования скрыты, бизнес-методы в `GoodsApi` лаконичны.
Внутри используется `RequestSpecBuilder` из RestAssured.

## 6. Фасад для конфигурации

`ProjectConfig` — единая точка входа во все настройки:

- `ProjectConfig.server()` и `ProjectConfig.auth()` отдают типизированные
  конфиги, лениво создают и кэшируют их (thread-safe double-checked locking);
- выбор контура `-Dapp.env`/`APP_ENV` происходит в одном месте;
- `reset()` позволяет переключить контур «на лету» (используется в тестах).

Это классический Façade: потребители не знают, откуда берутся значения.

## 7. Интерфейсы вместо ручного чтения Properties (aeonbits.owner)

Конфигурация объявлена как интерфейсы (`ServerConfig`, `AuthConfig`,
`OwnerFeatures`), реализацию-прокси генерирует `ConfigFactory`. Код зависит
только от интерфейсов (абстракций) — это и Dependency Inversion, и способ
получить типизацию (`int port()` вместо `Integer.parseInt`), и «пирог
приоритетов» источников через `@LoadPolicy(LoadType.MERGE)` + `@Sources`.

## 8. Fluent API (текучие интерфейсы)

- Page Object'ы возвращают `this` (`mainPage.productCard(42).addToCart(2)`,
  `loginPage.enterUsername("admin").enterPassword("secret").clickSignIn()`);
- `ProductAssert` возвращает тип `SELF`, сохраняя цепочку проверок
  (`assertThat(product).hasId(42).hasName("Телефон").hasPrice(59999.99)`).

Тесты читаются как сценарий, а не как последовательность вызовов.

## 9. Собственный слой ассертов поверх AssertJ

`ProductAssert extends AbstractAssert<ProductAssert, Product>` добавляет
предметные проверки (`hasName`, `hasPrice` с погрешностью для double).
Это расширение сторонней библиотеки без её изменения (см. Open/Closed).

## 10. «Единая точка правды» для URL и настроек

- `Endpoints` — все пути/URL ручек REST в одном классе;
- `ApiConfig` — базовый адрес, порт, креды и таймауты RestAssured;
- `UiConfig` + `selenide.properties` — все настройки браузера.

Адрес или порт меняются в одном месте, а не «размазаны» по тестам.

## 11. Гибрид «API-подготовка + UI-проверка»

Данные для UI-тестов создаются через REST (`createProductViaApi` в
`BaseUiTest`), а пользовательские сценарии выполняются в UI. Это быстрее и
надёжнее, чем «накликивать» данные через админку. Очистка — тоже через API
в `@AfterEach`. (Пример: `CartUiTest`.)

## 12. Полная изоляция тестов

- каждый тест стартует с чистой страницы (`open("/")`, корзина хранится в JS
  и сбрасывается при перезагрузке);
- браузер закрывается после каждого теста — не «протекают» сессии и DOM;
- созданные через API товары удаляются в `@AfterEach`;
- уникальные имена товаров (`uniqueName`) исключают «драку» между тестами.

## 13. «Умные» ожидания и слушатели как точки расширения

- Selenide даёт неявные ожидания вместо `sleep()`, автоматические скриншоты
  и HTML-дампы при падении;
- Allure подключается слушателями-расширениями: `AllureSelenide` через
  `SelenideLogger.addListener`, `AllureRestAssured` через `.addFilter(...)` —
  отчёт «обрастает» шагами и вложениями без изменения тестов.

## 14. Группировка тестов по тегам и Gradle-задачам

`build.gradle.kts` регистрирует отдельные `Test`-задачи, отбирающие тесты по
JUnit-тегам: `uiTest` (`@Tag("ui")`), `apiTest` (`@Tag("api")`),
`integrationTest`, `slowTest`, `smokeTest`. Запуск нужной группы — это
`./gradlew uiTest` или `./gradlew apiTest`, без правки кода; есть и объединённая
задача `runAllTests`. `-Ptags="smoke | slow"` позволяет выбрать набор на лету.

## 15. Утилитные классы-константы/хелперы

`ApiConfig`, `UiConfig`, `Endpoints`, `ClasspathProperties`, `FileProperties` —
объявлены `final` с приватным конструктором (без инстанцирования и наследования):
явный сигнал «это не объект, а набор статических функций/констант».
