package com.protolambda.blocktopograph.chunk;


import com.protolambda.blocktopograph.world.WorldData;

import java.io.IOException;

public abstract class ChunkData {

    public final Chunk chunk;



    public ChunkData(Chunk chunk){
        this.chunk = chunk;
    }

    public abstract void createEmpty();

    public abstract void write() throws IOException, WorldData.WorldDBException;

}
