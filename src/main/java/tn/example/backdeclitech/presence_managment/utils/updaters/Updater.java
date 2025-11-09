package tn.example.backdeclitech.presence_managment.utils.updaters;

import java.util.Map;

public interface Updater<T> {
    T update(Map<String, Object> updates, T entity);
}