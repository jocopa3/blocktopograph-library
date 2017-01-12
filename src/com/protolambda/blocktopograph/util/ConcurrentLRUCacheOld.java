/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.util;

import com.protolambda.blocktopograph.chunk.Chunk;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A basic concurrent LRU Cache implementation If the cache is full, it will
 * evict the first element added
 *
 * @author Matt
 */
public class ConcurrentLRUCacheOld<K, V> implements Map<K, V> {

    private final ConcurrentHashMap<K, V> data;
    private final ConcurrentLinkedQueue<K> dataQueue;

    private final long capacity;
    //private long size = 0;

    public ConcurrentLRUCacheOld(long capacity) {
        data = new ConcurrentHashMap<>();
        dataQueue = new ConcurrentLinkedQueue<>();

        this.capacity = capacity;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return data.get(key);
    }

    @Override
    public V put(K key, V value) {
        //size++;
        dataQueue.add(key);
        data.put(key, value);
        
        if (data.size() > capacity) {
            K headKey = dataQueue.poll();
            V removedData = data.remove(headKey);

            return removedData;
        }
        
        return null;
    }

    @Override
    public V remove(Object key) {
        if (data.isEmpty()) {
            return null;
        }

        if (data.contains(key)) {
            //size--;
            
            //Hopefully this works...
            dataQueue.remove((K)key);
            
            return data.remove(key);
        }

        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        data.putAll(m);
        dataQueue.addAll(m.keySet());
    }

    @Override
    public void clear() {
        data.clear();
        dataQueue.clear();
    }

    @Override
    public Set<K> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<V> values() {
        return data.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return data.entrySet();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (data.containsKey(key)) {
            return data.get(key);
        }

        return put(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        Object current = get(key);
        if (!Objects.equals(current, value)
                || (current == null && !containsKey(key))) {
            return false;
        }

        remove(key);
        return true;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return Map.super.replace(key, oldValue, newValue); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public V replace(K key, V value) {
        return Map.super.replace(key, value); //To change body of generated methods, choose Tools | Templates.
    }
}
