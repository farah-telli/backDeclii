package tn.example.backdeclitech.presence_managment.utils.updaters;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * A utility class that provides a way to update an entity based on a map of field names and values.
 * This class implements the Updater interface.
 *
 * @param <T> the type of the entity to be updated
 */
@Component
public class MapBasedUpdater<T> implements Updater<T> {

    @Override
    public T update(Map<String, Object> updates, T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        updates.forEach((key, value) -> {
            try {
                Field field = entity.getClass().getDeclaredField(key);
                field.setAccessible(true);
                field.set(entity, convertValue(field.getType(), value));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalArgumentException("Invalid field: " + key, e);
            }
        });

        return entity;
    }

    private Object convertValue(Class<?> type, Object value) {
        if (value == null) {
            return null;
        }
        if (type == Boolean.class && value instanceof String) {
            return Boolean.valueOf((String) value);
        } else if (type == LocalDateTime.class && value instanceof String) {
            String str = (String) value;
            if (str.length() == 10) {
                LocalDate date = LocalDate.parse(str);
                return date.atStartOfDay();
            } else {
                return LocalDateTime.parse(str);
            }
        } else if (type == LocalDate.class && value instanceof String) {
            return LocalDate.parse((String) value);
        } else if (type.isEnum() && value instanceof String) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Object enumValue = Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), (String) value);
            return enumValue;
        }
        return value;
    }
}
