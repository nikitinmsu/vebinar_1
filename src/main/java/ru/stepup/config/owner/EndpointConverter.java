package ru.stepup.config.owner;

import java.lang.reflect.Method;
import org.aeonbits.owner.Converter;

/**
 * =====================================================================
 *  EndpointConverter — свой конвертер строки в объект {@link Endpoint}
 * =====================================================================
 *  Если значения в properties имеют НЕстандартный формат (не число, не
 *  дата, не enum), owner не сможет преобразовать их «из коробки». Тогда:
 *
 *   1) реализуем интерфейс {@link Converter}<T> — в нём ровно один метод:
 *
 *        T convert(Method method, String input)
 *
 *      - method — вызываемый метод конфига (если логика зависит от него);
 *      - input  — сырое значение из properties;
 *      - вернуть можно T или null (тогда конфиг вернёт null);
 *   2) вешаем на метод конфига аннотацию @ConverterClass(EndpointConverter.class).
 *
 *  ВАЖНО: конвертер создаётся ЗАНОВО на каждый вызов, поэтому он не должен
 *  хранить состояние между вызовами (см. javadoc интерфейса Converter).
 *
 *  Конвертер работает и для одиночного значения, и для массива/коллекции:
 *  при List&lt;Endpoint&gt; owner вызовет convert() для каждого элемента
 *  разделённой строки (см. OwnerFeatures.replicaDbs()).
 */
public class EndpointConverter implements Converter<Endpoint> {

    /**
     * Разбирает строку вида {@code "db1.internal:5432"} на Endpoint.
     *
     * @param method вызываемый метод конфига (здесь не используется)
     * @param input  значение из properties, например "db1.internal:5432"
     * @return объект {@link Endpoint}
     * @throws IllegalArgumentException если порт не число или формат неверный
     */
    @Override
    public Endpoint convert(Method method, String input) {
        // Отрезаем лишние пробелы — owner не всегда их вычищает за нас.
        String value = input.trim();
        int colon = value.lastIndexOf(':');
        if (colon <= 0 || colon == value.length() - 1) {
            throw new IllegalArgumentException("Ожидался формат host:port, а получено: '" + input + "'");
        }
        String host = value.substring(0, colon);
        int port = Integer.parseInt(value.substring(colon + 1));
        return new Endpoint(host, port);
    }
}
