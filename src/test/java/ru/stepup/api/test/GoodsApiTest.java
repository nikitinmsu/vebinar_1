package ru.stepup.api.test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.stepup.api.model.Product;
import ru.stepup.api.model.ProductList;
import ru.stepup.api.model.ProductRequest;
import ru.stepup.api.model.ResultData;
import ru.stepup.api.pageobject.GoodsApi;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.stepup.api.assertion.ProductAssert.assertThat;
import static ru.stepup.api.config.Endpoints.getBaseUri;
import static ru.stepup.api.config.Endpoints.goodsById;

/**
 * =====================================================================
 *  GoodsApiTest — автотесты REST-ручек сервиса «Товары»
 * =====================================================================
 *
 *  ВАЖНЕЙШЕЕ ПРАВИЛО ЭТОГО КЛАССА:
 *  в тестах вызываются ТОЛЬКО бизнес-методы из {@link GoodsApi}
 *  (Page Object) — addProduct, getProduct, updateProduct, deleteProduct,
 *  getAllProducts. Никаких «сырых» HTTP-вызовов, сборки URL или
 *  авторизации внутри теста НЕТ: это всё спрятано под капотом.
 *
 *  Откуда тест знает, что делать? Смотрите цепочку:
 *  <pre>
 *      Тест                     -> GoodsApi (Page Object)
 *      goodsApi.addProduct(x)   -> RestApiBuilder (строит запрос)
 *                                -> ApiConfig (auth, base url)
 *                                -> Endpoints (адреса ручек)
 *      Далее — проверки ответа:
 *      assertThat(response).statusCode(200)   — встроенный AssertJ
 *      assertThat(product).hasName("X")       — НАШ ProductAssert
 *  </pre>
 *
 *  ЗАПУСК:
 *  Нужен запущенный сервер (по умолчанию http://127.0.0.1:8080).
 *  <pre>
 *      ./gradlew apiTest
 *      // или с переопределением адреса/кредов:
 *      ./gradlew apiTest -Dapi.base.uri=http://localhost -Dapi.port=9090 \
 *                        -Dapi.username=admin -Dapi.password=secret
 *  </pre>
 *
 *  Класс помечен тегом @Tag("api") — именно по нему задача apiTest
 *  отбирает тесты (см. build.gradle.kts).
 *
 *  НЮАНС РЕАЛЬНОГО СЕРВИСА (проверено на живом API):
 *  - список товаров отдаётся в виде {"goods": [...]} без мета-данных
 *    пагинации (см. {@link ProductList});
 *  - GET несуществующего товара может вернуть 500 (особенность сервера),
 *    поэтому негативную проверку 404 делаем через удалённый товар;
 *  - сервис хранит товары в памяти и при разрастании БД ручка /goods/list
 *    начинает отдавать 500. Поэтому каждый тест УДАЛЯЕТ созданные им
 *    товары в {@code @AfterEach} — прогон идемпотентен и не «мусорит».
 */
@Tag("api")
class GoodsApiTest {

    // Page Object — единственная «точка входа» в API из тестов.
    private final GoodsApi goodsApi = new GoodsApi();

    // Созданные в тестах id товаров — удаляем их после каждого теста,
    // чтобы не засорять сервис и не ловить его баг с большим списком.
    private final List<Long> createdIds = new ArrayList<>();

    // =================================================================
    //  ОЧИСТКА ДАННЫХ ПОСЛЕ КАЖДОГО ТЕСТА
    // =================================================================

    /**
     * Удаляет все товары, созданные в ходе теста.
     * Вызывается JUnit после каждого теста (в т.ч. после падения),
     * поэтому тесты независимы друг от друга и перезапускаемы.
     */
    @AfterEach
    void cleanupCreatedProducts() {
        for (Long id : createdIds) {
            goodsApi.deleteProduct(id);
        }
        createdIds.clear();
    }

    /**
     * Создаёт товар и запоминает его id для последующей очистки.
     *
     * @param name  название товара
     * @param price цена товара
     * @return id созданного товара
     */
    private long createProduct(String name, double price) {
        long id = goodsApi.createProductAndReturnId(name, price);
        createdIds.add(id);
        return id;
    }

    // =================================================================
    //  ХЕЛПЕРЫ
    // =================================================================

    /**
     * Генерирует уникальное имя товара — чтобы тесты не «дрались» между
     * собой за одни и те же имена при параллельном запуске.
     *
     * @param prefix читаемый префикс для понятных сообщений об ошибках
     * @return уникальное имя товара
     */
    private String uniqueName(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    /**
     * Собирает ВСЕ товары сервиса, перебирая страницы списка.
     * Нужно, чтобы проверка «товар есть в списке» не зависела от
     * размера страницы и от порядка элементов: сервис отдаёт новый
     * товар последним, и на первой странице он может не поместиться.
     *
     * @return все товары всех страниц
     */
    private List<Product> fetchAllProducts() {
        List<Product> all = new ArrayList<>();
        for (int page = 0; ; page++) {
            Response response = goodsApi.getAllProducts(page, 100);
            if (response.getStatusCode() != 200) {
                break;
            }
            List<Product> goods = response.as(ProductList.class).getGoods();
            if (goods == null || goods.isEmpty()) {
                break;
            }
            all.addAll(goods);
            // Последняя страница: вернулось меньше запрошенного размера.
            if (goods.size() < 100) {
                break;
            }
        }
        return all;
    }

    // =================================================================
    //  1. POST /goods/add — ДОБАВИТЬ ТОВАР
    // =================================================================

    @Test
    @DisplayName("Создание товара возвращает 200 и id нового товара")
    void addProductReturnsNewProductId() {
        ProductRequest product = new ProductRequest(uniqueName("Телефон"), 59999.99);

        // Единственный «HTTP-шаг» — бизнес-метод ручки add.
        Response response = goodsApi.addProduct(product);

        assertThat(response.getStatusCode()).as("код ответа").isEqualTo(200);

        // Тело ответа — ResultData: сообщение + id созданного товара.
        ResultData result = response.as(ResultData.class);
        assertThat(result.getMessage()).as("сообщение сервера").isEqualTo("success");
        assertThat(result.getDataId()).as("id созданного товара").isNotNull();

        // Запоминаем id, чтобы очистить его в @AfterEach.
        createdIds.add(result.getDataId());
    }

    @Test
    @DisplayName("Создание товара с пустым name -> 400")
    void addProductWithoutNameIsRejected() {
        // name пустой, а это обязательное поле — сервер должен ответить 400.
        ProductRequest product = new ProductRequest("", 10.0);

        Response response = goodsApi.addProduct(product);

        // Негативная проверка: ожидаем ошибку валидации.
        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    // =================================================================
    //  2. GET /goods/{id} — ПОЛУЧИТЬ ТОВАР
    // =================================================================

    @Test
    @DisplayName("Получение созданного товара возвращает его данные")
    void getProductReturnsCreatedProduct() {
        String name = uniqueName("Ноутбук");
        long id = createProduct(name, 89999.99);

        Response response = goodsApi.getProduct(id);

        assertThat(response.getStatusCode()).isEqualTo(200);

        // Парсим тело в Product и проверяем СВОИМ ProductAssert — кастомным
        // ассертом на основе AbstractAssert. Цепочка методов читается как
        // предложение: «id такой, имя такое, цена такая».
        Product product = response.as(Product.class);
        assertThat(product)
                .hasId(id)
                .hasName(name)
                .hasPrice(89999.99, 0.01);
    }

    @Test
    @DisplayName("Получение удалённого товара -> 404")
    void getDeletedProductReturns404() {
        long id = createProduct(uniqueName("Удаляемый"), 100.0);
        goodsApi.deleteProduct(id); // удаляем, чтобы получить детерминированный 404

        Response response = goodsApi.getProduct(id);

        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    // =================================================================
    //  3. PATCH /goods/{id} — ЧАСТИЧНОЕ ОБНОВЛЕНИЕ ТОВАРА
    // =================================================================

    @Test
    @DisplayName("Обновление цены товара через PATCH")
    void updateProductPrice() {
        long id = createProduct(uniqueName("Клавиатура"), 3000.0);

        // Меняем цену — это и есть «частичное» обновление.
        // PATCH в этом сервисе требует уникальное name, поэтому генерируем новое.
        String updatedName = uniqueName("Клавиатура");
        ProductRequest update = new ProductRequest(updatedName, 2750.5);
        Response response = goodsApi.updateProduct(id, update);

        assertThat(response.getStatusCode()).isEqualTo(200);

        Product updated = response.as(Product.class);
        assertThat(updated)
                .hasId(id)
                .hasName(updatedName)
                .hasPrice(2750.5, 0.01);
    }

    // =================================================================
    //  4. DELETE /goods/{id} — УДАЛИТЬ ТОВАР
    // =================================================================

    @Test
    @DisplayName("Удаление товара, после чего GET возвращает 404")
    void deleteProductRemovesIt() {
        long id = createProduct(uniqueName("Мышь"), 1500.0);

        // Удаляем созданный товар.
        Response deleteResponse = goodsApi.deleteProduct(id);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(200);

        // Проверяем, что товара больше нет.
        Response getAfterDelete = goodsApi.getProduct(id);
        assertThat(getAfterDelete.getStatusCode()).isEqualTo(404);
    }

    // =================================================================
    //  5. GET /goods/list — СПИСОК ТОВАРОВ (ПАГИНАЦИЯ)
    // =================================================================

    @Test
    @DisplayName("Список товаров содержит только что созданный товар")
    void listContainsCreatedProduct() {
        String name = uniqueName("Монитор");
        createProduct(name, 15000.0);

        // Собираем все товары по всем страницам (см. fetchAllProducts).
        List<Product> allProducts = fetchAllProducts();
        assertThat(allProducts).as("список товаров").isNotEmpty();

        // ...и проверяем, что наш товар присутствует в списке.
        List<String> names = allProducts.stream().map(Product::getName).toList();
        assertThat(names).contains(name);
    }

    @Test
    @DisplayName("Пагинация: size ограничивает число элементов на странице")
    void paginationLimitsPageSize() {
        // Гарантируем, что в списке есть хотя бы один товар — чтобы тест
        // не зависел от порядка запуска и состояния БД.
        createProduct(uniqueName("Пагинация"), 500.0);

        // Запрашиваем первую страницу размером 1.
        Response response = goodsApi.getAllProducts(0, 1);

        assertThat(response.getStatusCode()).isEqualTo(200);

        // Сервер должен вернуть НЕ БОЛЬШЕ 1 товара (срез по размеру страницы).
        ProductList list = response.as(ProductList.class);
        assertThat(list.getGoods()).as("товаров на странице").hasSizeLessThanOrEqualTo(1);
    }
}
