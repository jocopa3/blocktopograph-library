package com.protolambda.blocktopograph.chunk;

import com.protolambda.blocktopograph.world.WorldData;
import com.protolambda.blocktopograph.chunk.terrain.V0_9_TerrainChunkData;
import com.protolambda.blocktopograph.map.Dimension;

import java.util.HashMap;
import java.util.Map;


public class ChunkManager {

    private Map<Long, Chunk> chunks = new HashMap<>();

    private WorldData worldData;

    public final Dimension dimension;

    public ChunkManager(WorldData worldData, Dimension dimension){
        this.worldData = worldData;
        this.dimension = dimension;
    }


    public static long xzToKey(int x, int z){
        return (((long) x) << 32) | (((long) z) & 0xFFFFFFFFL);
    }

    public Chunk getChunk(int cX, int cZ) {
        long key = xzToKey(cX, cZ);
        Chunk chunk = chunks.get(key);
        if(chunk == null) {
            chunk = new Chunk(worldData, cX, cZ, dimension);
            this.chunks.put(key, chunk);
        }
        return chunk;
    }

    public void disposeAll(){
        this.chunks.clear();
    }
}
