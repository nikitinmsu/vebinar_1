package ru.stepup.api.model;

import java.util.List;

/**
 * =====================================================================
 *  ProductList — ответ ручки GET /goods/list
 * =====================================================================
 *
 *  Реальный формат ответа (проверено на работающем сервисе):
 *  <pre>
 *      {
 *          "goods": [ { "id":1, "name":"Телефон", "price":100.5 }, ... ]
 *      }
 *  </pre>
 *
 *  Обратите внимание: сервис НЕ отдаёт страницу Spring Data (нет полей
 *  content/totalElements/totalPages). Параметры page и size сервер
 *  использует для среза списка, но мета-данных пагинации в ответе нет.
 *  Поэтому в модели только одно поле — список товаров {@code goods}.
 *
 *  «Лишние» поля, которые сервер может добавить в будущем, Jackson
 *  просто проигнорирует: мы объявляем только то, что реально проверяем.
 */
public class ProductList {

    /** Товары на запрошенной странице. */
    private List<Product> goods;

    /** Пустой конструктор — обязателен для Jackson. */
    public ProductList() {
    }

    // =================================================================
    //  Аксессоры (getters / setters)
    // =================================================================

    public List<Product> getGoods() {
        return goods;
    }

    public void setGoods(List<Product> goods) {
        this.goods = goods;
    }

    @Override
    public String toString() {
        return "ProductList{goods.size=" + (goods == null ? 0 : goods.size()) + '}';
    }
}
