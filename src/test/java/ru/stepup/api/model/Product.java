package ru.stepup.api.model;

/**
 * =====================================================================
 *  Product — сущность «товар», возвращаемая сервером
 * =====================================================================
 *
 *  Сервер возвращает этот объект в ответах ручек:
 *    GET  /goods/{id}  — конкретный товар
 *    PATCH /goods/{id} — обновлённый товар
 *    GET  /goods/list  — список таких объектов внутри страницы (content)
 *
 *  Предполагаемая JSON-схема (поле id выдаёт сервер, его нельзя задать
 *  в запросе — поэтому здесь три поля, а не два как в ProductRequest):
 *  <pre>
 *      {
 *          "id":    42,
 *          "name":  "Телефон",
 *          "price": 59999.99
 *      }
 *  </pre>
 *
 *  Именно для этого класса будет написан свой AssertJ-ассерт
 *  {@code ProductAssert} (см. пакет assertion) — чтобы проверки
 *  в тестах читались как предложение:
 *  <pre>
 *      assertThat(product).hasName("Телефон").hasPrice(59999.99);
 *  </pre>
 */
public class Product {

    /** Уникальный идентификатор товара, присваивается сервером. */
    private Long id;

    /** Название товара. */
    private String name;

    /** Цена товара. */
    private double price;

    /** Пустой конструктор — обязателен для Jackson. */
    public Product() {
    }

    /**
     * Конструктор для удобного создания объекта в тестах.
     *
     * @param id    идентификатор товара
     * @param name  название товара
     * @param price цена товара
     */
    public Product(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // =================================================================
    //  Аксессоры (getters / setters)
    // =================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + '}';
    }
}
