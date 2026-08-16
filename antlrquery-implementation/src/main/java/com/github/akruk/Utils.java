package com.github.akruk;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Utils {
    private Utils(){}

    @SafeVarargs
    public static <K, V> LinkedHashMap<K, V> linkedHashMap(
            Map.Entry<? extends K, ? extends V>... entries) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>(entries.length);
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
