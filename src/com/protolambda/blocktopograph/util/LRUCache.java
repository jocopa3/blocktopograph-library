/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.util;

import com.protolambda.blocktopograph.chunk.Chunk;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Matt
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private long maxCacheSize = 16; // Default cache size

    public LRUCache(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > maxCacheSize;
    }
    
    public void setMaxCacheSize(long newSize) {
        maxCacheSize = newSize;
    }
    
    public long getMaxCacheSize() {
        return maxCacheSize;
    }
}