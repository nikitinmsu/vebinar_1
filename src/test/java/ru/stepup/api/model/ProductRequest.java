package ru.stepup.api.model;

/**
 * =====================================================================
 *  ProductRequest — тело запроса на создание/обновление товара
 * =====================================================================
 *
 *  Этот объект отправляется в ручках POST /goods/add и PATCH /goods/{id}.
 *  Схема (см. Swagger, components/schemas/ProductRequest):
 *  <pre>
 *      {
 *          "name":  "Телефон",     // обязательное поле, String
 *          "price": 59999.99        // необязательное, double >= 0
 *      }
 *  </pre>
 *
 *  DTO (Data Transfer Object) — «бедный» класс без логики: только поля,
 *  конструкторы и аксессоры. Он нужен, чтобы Jackson (и RestAssured)
 *  превращали JSON <-> Java-объект, а тесты работали с типизированными
 *  данными, а не со «строками на глазок».
 *
 *  Для Jackson важно, чтобы у класса был пустой конструктор и сеттеры
 *  (или поля с аннотацией @JsonProperty) — поэтому конструктор без
 *  аргументов и сеттеры обязательны. Дополнительно добавлен удобный
 *  конструктор со всеми полями, чтобы в тестах объект создавался одной
 *  строкой.
 */
public class ProductRequest {

    /** Название товара. Обязательное поле для API. */
    private String name;

    /** Цена товара. Не может быть отрицательной (по спецификации >= 0). */
    private double price;

    /**
     * Пустой конструктор — обязателен для Jackson при десериализации.
     */
    public ProductRequest() {
    }

    /**
     * Конструктор для удобного создания объекта в тестах.
     *
     * @param name  название товара
     * @param price цена товара
     */
    public ProductRequest(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // =================================================================
    //  Аксессоры (getters / setters)
    // =================================================================
    // Сеттеры использует Jackson при чтении JSON, геттеры — при
    // сериализации Java-объекта обратно в JSON.

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // =================================================================
    //  Служебные методы для читаемых сообщений об ошибках в AssertJ
    // =================================================================

    @Override
    public String toString() {
        return "ProductRequest{name='" + name + "', price=" + price + '}';
    }
}
