package com.protolambda.blocktopograph.chunk;

import com.protolambda.blocktopograph.world.WorldData;
import com.protolambda.blocktopograph.map.Dimension;
import com.protolambda.blocktopograph.util.ConcurrentLRUCacheOld;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.protolambda.blocktopograph.SystemProfile;
import java.util.concurrent.ConcurrentMap;

public class ChunkManager {

    private ConcurrentMap<Long, Chunk> chunks;

    private WorldData worldData;

    public final Dimension dimension;

    public ChunkManager(WorldData worldData, Dimension dimension) {
        this.worldData = worldData;
        this.dimension = dimension;
        
        // MAGIC!
        int cacheSize = 7000 * SystemProfile.getRAMUsagePolicy().mult;
        
        System.out.println("Chunk Cache Size: " + cacheSize);
        
        chunks = new ConcurrentLinkedHashMap.Builder<Long, Chunk>()
                .maximumWeightedCapacity(cacheSize)
                .build();
    }

    public static long xzToKey(int x, int z) {
        return (((long) x) << 32) | (((long) z) & 0xFFFFFFFFL);
    }

    public Chunk getChunk(int cX, int cZ) {
        long key = xzToKey(cX, cZ);
        Chunk chunk = chunks.get(key);
        if (chunk == null) {
            chunk = new Chunk(worldData, cX, cZ, dimension);
            this.chunks.put(key, chunk);
            //System.out.println(this.dimension.name + ": " + this.chunks.size() + " [" + cX + ", " + cZ + "]");
        }
        return chunk;
    }

    public void disposeAll() {
        this.chunks.clear();
    }
}
