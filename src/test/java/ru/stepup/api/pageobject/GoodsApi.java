package ru.stepup.api.pageobject;

import io.restassured.response.Response;
import ru.stepup.api.builder.RestApiBuilder;
import ru.stepup.api.config.Endpoints;
import ru.stepup.api.model.Product;
import ru.stepup.api.model.ProductRequest;
import ru.stepup.api.model.ResultData;

/**
 * =====================================================================
 *  GoodsApi — Page Object для REST-ручек сервиса «Товары»
 * =====================================================================
 *
 *  ПАТТЕРН PAGE OBJECT (применительно к API)
 *  ------------------------------------------------
 *  В UI-автотестах Page Object скрывает селекторы и клики за методами:
 *  <pre>
 *      loginPage.enterLogin("admin").enterPassword("secret").clickSubmit();
 *  </pre>
 *  Тест не знает ни id кнопки, ни пути к ней — он вызывает понятные
 *  «бизнес-действия».
 *
 *  Здесь та же идея, только вместо DOM — HTTP. Каждый метод этого класса
 *  = ОДНА ручка API, описанная человеческим языком:
 *  <pre>
 *      goodsApi.addProduct(new ProductRequest("Телефон", 59999.99)); // POST /goods/add
 *      goodsApi.getProduct(42);                                       // GET  /goods/{id}
 *  </pre>
 *
 *  ЧТО ТАКОЕ «БИЗНЕС-МЕТОД РУЧКИ»
 *  ------------------------------------------------
 *  Это метод, который инкапсулирует ВЕСЬ HTTP-сценарий вызова ручки:
 *  метод запроса, путь, тело, авторизацию и обработку ответа. Внутри
 *  он использует {@link RestApiBuilder} (строит запрос) и {@link Endpoints}
 *  (знает адреса). А ВНЕШНИЙ ТЕСТ ничего этого не видит — он просто
 *  говорит: «добавь товар», «получи товар», «удали товар».
 *
 *  Преимущества:
 *  1. Тесты читаются как спецификация, а не как простыня HTTP-кода.
 *  2. Изменился путь/тело/заголовок — правим ОДИН метод, все тесты живы.
 *  3. Переиспользование: один и тот же сценарий вызова ручки не
 *     дублируется в каждом тесте.
 *
 *  Какой тип возвращают методы?
 *  Методы возвращают {@link Response} RestAssured — сырой ответ с кодом
 *  статуса, заголовками и телом. Это даёт тесту максимальную гибкость:
 *  проверить статус-код, затем распарсить тело в нужный DTO
 *  (Product, ResultData, ProductPage) или просто достать поле через
 *  jsonPath(). Парсинг оставляем в тестах/ассертах, чтобы каждый тест
 *  решал, какой уровень проверок ему нужен.
 */
public class GoodsApi {

    /** Билдер запросов — строит HTTP-запрос с авторизацией и заголовками. */
    private final RestApiBuilder request = new RestApiBuilder();

    // =================================================================
    //  БИЗНЕС-МЕТОДЫ РУЧЕК
    // =================================================================

    /**
     * Добавить новый товар.  <b>POST /goods/add</b>
     *
     * Сервер вернёт {@link ResultData}: сообщение и id созданного товара
     * в {@code data.id}.
     *
     * @param product данные товара (name обязателен, price >= 0)
     * @return ответ сервера
     */
    public Response addProduct(ProductRequest product) {
        return request.doPost(Endpoints.GOODS_ADD, product);
    }

    /**
     * Получить товар по идентификатору.  <b>GET /goods/{id}</b>
     *
     * @param id идентификатор товара
     * @return ответ сервера (200 + {@link Product}, либо 404)
     */
    public Response getProduct(long id) {
        return request.doGet(Endpoints.goodsById(id));
    }

    /**
     * Частично обновить товар.  <b>PATCH /goods/{id}</b>
     *
     * Передаются только те поля, которые нужно изменить. Сервер вернёт
     * обновлённый {@link Product}.
     *
     * @param id      идентификатор товара
     * @param product новые значения полей
     * @return ответ сервера (200 + {@link Product}, либо 400/404)
     */
    public Response updateProduct(long id, ProductRequest product) {
        return request.doPatch(Endpoints.goodsById(id), product);
    }

    /**
     * Удалить товар.  <b>DELETE /goods/{id}</b>
     *
     * @param id идентификатор товара
     * @return ответ сервера (200 либо 404)
     */
    public Response deleteProduct(long id) {
        return request.doDelete(Endpoints.goodsById(id));
    }


    public Response getAllProducts(int page, int size) {
        return request.doGetWithQueryParams(Endpoints.GOODS_LIST, "page", page, "size", size);
    }

    // =================================================================
    //  ВЫСОКОУРОВНЕВЫЕ СЦЕНАРИИ (композиция ручек)
    // =================================================================
    // Это «составные» бизнес-действия из нескольких ручек. Их тоже можно
    // класть в Page Object: тесты становятся совсем лаконичными, а логика
    // «подготовь данные -> сделай действие -> верни результат» не
    // дублируется.

    /**
     * Сценарий: создать товар и сразу вернуть его из ответа сервера
     * (идём в обход доп. запроса GET — читаем {@link ResultData}).
     *
     * @param name  название товара
     * @param price цена товара
     * @return id созданного товара
     * @throws IllegalStateException если сервер не вернул id
     */
    public Long createProductAndReturnId(String name, double price) {
        Response response = addProduct(new ProductRequest(name, price));
        ResultData result = response.as(ResultData.class);
        Long id = result.getDataId();
        if (id == null) {
            throw new IllegalStateException(
                    "Сервер не вернул id созданного товара. Ответ: " + response.body().asString());
        }
        return id;
    }
}
