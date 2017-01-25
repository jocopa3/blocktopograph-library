package com.protolambda.blocktopograph.chunk;

import com.protolambda.blocktopograph.world.WorldData;
import com.protolambda.blocktopograph.map.Dimension;
import com.protolambda.blocktopograph.util.ConcurrentLRUCacheOld;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.protolambda.blocktopograph.SystemProfile;
import java.util.concurrent.ConcurrentMap;

public class ChunkManager {

    private ConcurrentMap<Long, Chunk> chunks;
    private ConcurrentMap<Long, Byte> nullChunks;

    private WorldData worldData;

    public final Dimension dimension;

    public ChunkManager(WorldData worldData, Dimension dimension) {
        this.worldData = worldData;
        this.dimension = dimension;

        createCache();
    }

    private void createCache() {
        if (chunks != null) {
            chunks.clear();
            chunks = null;
        }

        long cacheSize = SystemProfile.calculateMaxObjects(10265, 0.6);

        System.out.println("Chunk Cache Size: " + cacheSize);

        chunks = new ConcurrentLinkedHashMap.Builder<Long, Chunk>()
                .maximumWeightedCapacity(cacheSize)
                .build();

        if (nullChunks != null) {
            nullChunks.clear();
            nullChunks = null;
        }

        cacheSize = SystemProfile.calculateMaxObjects(40, 0.1);

        System.out.println("Null Chunk Cache Size: " + cacheSize);

        nullChunks = new ConcurrentLinkedHashMap.Builder<Long, Byte>()
                .maximumWeightedCapacity(cacheSize)
                .build();
    }

    private int cacheClears = 0;

    public void clearCache() {
        clearCache(false);
    }
    
    public void clearCache(boolean forceClear) {
        cacheClears++;

        if (cacheClears >= 3 && !forceClear) {
            createCache();
            cacheClears = 0;
        } else {
            chunks.clear();
            nullChunks.clear();
        }

        System.gc();
    }

    public static long xzToKey(int x, int z) {
        return (((long) x) << 32) | (((long) z) & 0xFFFFFFFFL);
    }

    public Chunk getChunk(int cX, int cZ) {
        long key = xzToKey(cX, cZ);
        Chunk chunk;

        if (nullChunks.containsKey(key)) {
            return null;
        } else if (!chunks.containsKey(key)) {
            chunk = new Chunk(worldData, cX, cZ, dimension);
            if (chunk.isEmptyChunk()) {
                nullChunks.put(key, (byte) 0);
                return null;
            } else {
                this.chunks.put(key, chunk);
            }
            //System.out.println(this.dimension.name + ": " + this.chunks.size() + " [" + cX + ", " + cZ + "]");
        } else {
            chunk = chunks.get(key);
        }

        return chunk;
    }

    public void disposeAll() {
        chunks.clear();
        nullChunks.clear();
    }
}
