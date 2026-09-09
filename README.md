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
