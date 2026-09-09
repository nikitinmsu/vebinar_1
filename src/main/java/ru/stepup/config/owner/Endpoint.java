package ru.stepup.config.owner;

/**
 * =====================================================================
 *  Endpoint — «бизнес-объект», который owner научится создавать сам
 * =====================================================================
 *  owner умеет конвертировать строку из properties в произвольный объект.
 *  Самый простой способ — класс с публичным конструктором от String
 *  (тогда никакого конвертера не нужно), но если формат строки сложнее,
 *  пишем собственный {@link org.aeonbits.owner.Converter} (см.
 *  {@link EndpointConverter}) и вешаем его аннотацией @ConverterClass.
 *
 *  Здесь Endpoint хранит пару host:port для строки вида "db1:5432".
 */
public final class Endpoint {

    private final String host;
    private final int port;

    /**
     * @param host хост (например, "db1.internal")
     * @param port порт (например, 5432)
     */
    public Endpoint(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
