package ru.stepup.config.owner;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Mutable;

/**
 * =====================================================================
 *  MutableConfig — owner в режиме «чтение + изменение свойств на лету»
 * =====================================================================
 *  Обычный конфиг owner — read-only «проекция» файла. Но если интерфейс
 *  дополнительно расширяет маркеры:
 *
 *    {@link Mutable}    — позволяет МЕНЯТЬ значения во время работы:
 *                         setProperty(key, value) / removeProperty(key) /
 *                         clear() — изменения сразу видят все методы;
 *    {@link Accessible} — позволяет «заглянуть внутрь»: getProperty(key),
 *                         propertyNames(), list(out), fill(map) и т.п.
 *
 *  Где это реально пригодится:
 *    - тесты, которым нужно подменить значение на время (без пересоздания
 *      конфига);
 *    - горячие переключения (например, feature-флаг);
 *    - отладка: распечатать, какие ключи реально «доехали» до конфига.
 *
 *  ВНИМАНИЕ (нюанс 1.0.12): setProperty/removeProperty меняют значение
 *  только у ЭТОГО экземпляра конфига и не пишут обратно в файл на диске.
 *  Для записи файла в owner 1.0.12 есть отдельные механизмы (save).
 *
 *  Здесь нет @Sources: конфиг не привязан к файлу и работает только на
 *  @DefaultValue + setProperty — этого достаточно, чтобы показать Mutable.
 */
public interface MutableConfig extends Config, Mutable, Accessible {

    /** Значение по умолчанию 18 (пока не переопределим через setProperty). */
    @DefaultValue("18")
    int minAge();

    /** Обычный строковый ключ без дефолта — вернёт null, если не задан. */
    @DefaultValue("anonymous")
    String userName();
}
