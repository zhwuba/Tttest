package com.freeme.themeclub.wallpaper.cache;

import java.util.LinkedHashMap;
import java.util.Map;


public class DataCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    
    private int maximumCapacity = 0;

    public DataCache(int maximumCapacity) {
        this(maximumCapacity, 0);
    }

    public DataCache(int maximumCapacity, int initialCapacity) {
        this(maximumCapacity, initialCapacity, 0.75f);
    }

    public DataCache(int maximumCapacity, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
        this.maximumCapacity = maximumCapacity;
    }

    public DataCache(Map<K, V> map, int maximumCapacity) {
        this(maximumCapacity);
        putAll(map);
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maximumCapacity;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }
}