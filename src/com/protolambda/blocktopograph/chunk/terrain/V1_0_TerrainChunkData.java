package com.protolambda.blocktopograph.chunk.terrain;

import com.protolambda.blocktopograph.world.WorldData;
import com.protolambda.blocktopograph.chunk.Chunk;
import com.protolambda.blocktopograph.chunk.ChunkTag;
import com.protolambda.blocktopograph.map.Biome;
import com.protolambda.blocktopograph.util.Noise;

import java.nio.ByteBuffer;

public class V1_0_TerrainChunkData extends TerrainChunkData {


    public volatile ByteBuffer terrainData, data2D;

    public static final int chunkW = 16, chunkL = 16, chunkH = 16;

    public static final int area = chunkW * chunkL;
    public static final int vol = area * chunkH;

    public static final int POS_VERSION = 0;
    public static final int POS_BLOCK_IDS = POS_VERSION + 1;
    public static final int POS_META_DATA = POS_BLOCK_IDS + vol;
    public static final int POS_SKY_LIGHT = POS_META_DATA + (vol >> 1);
    public static final int POS_BLOCK_LIGHT = POS_SKY_LIGHT + (vol >> 1);
    public static final int TERRAIN_LENGTH = POS_BLOCK_LIGHT + (vol >> 1);

    public static final int POS_HEIGHTMAP = 0;
    // it looks like each biome takes 2 bytes, and the first 1 byte of every 2 bytes is always 0!?
    public static final int POS_BIOME_DATA = POS_HEIGHTMAP + area + area;
    public static final int DATA2D_LENGTH = POS_BIOME_DATA + area;

    public V1_0_TerrainChunkData(Chunk chunk, byte subChunk) {
        super(chunk, subChunk);
    }

    @Override
    public void write() throws WorldData.WorldDBException {
        this.chunk.worldData.writeChunkData(chunk.x, chunk.z, ChunkTag.TERRAIN, chunk.dimension, subChunk, true, terrainData.array());
        this.chunk.worldData.writeChunkData(chunk.x, chunk.z, ChunkTag.DATA_2D, chunk.dimension, subChunk, true, data2D.array());
    }

    @Override
    public boolean loadTerrain() {
        if(terrainData == null){
            try {
                byte[] rawData = this.chunk.worldData.getChunkData(chunk.x, chunk.z, ChunkTag.TERRAIN, chunk.dimension, subChunk, true);
                if(rawData == null) return false;
                this.terrainData = ByteBuffer.wrap(rawData);
                return true;
            } catch (Exception e){
                //data is not present
                return false;
            }
        }
        else return true;
    }

    @Override
    public boolean load2DData() {
        if(data2D == null){
            try {
                byte[] rawData = this.chunk.worldData.getChunkData(chunk.x, chunk.z, ChunkTag.DATA_2D, chunk.dimension, subChunk, false);
                if(rawData == null) return false;
                this.data2D = ByteBuffer.wrap(rawData);
                return true;
            } catch (Exception e){
                //data is not present
                return false;
            }
        }
        else return true;
    }


    @Override
    public void createEmpty() {

        byte[] terrain = new byte[TERRAIN_LENGTH];

        //version byte
        terrain[0] = terrainData.get(0);

        int x, y, z, i = 1, realY;
        byte bedrock = (byte) 7;
        byte sandstone = (byte) 24;

        //generate super basic terrain (one layer of bedrock, 31 layers of sandstone)
        for(x = 0; x < chunkW; x++){
            for(z = 0; z < chunkL; z++){
                for(y = 0, realY = chunkH * this.subChunk; y < chunkH; y++, i++, realY++){
                    terrain[i] = (realY == 0 ? bedrock : (realY < 32 ? sandstone : 0));
                }
            }
        }


        //fill meta-data with 0
        for(; i < POS_META_DATA; i++){
            terrain[i] = (byte) 0;
        }

        //fill blocklight with 0xff
        for(; i < POS_BLOCK_LIGHT; i++){
            terrain[i] = (byte) 0xff;
        }

        //fill block-light with 0xff
        for(; i < TERRAIN_LENGTH; i++){
            terrain[i] = (byte) 0xff;
        }

        this.terrainData = ByteBuffer.wrap(terrain);
        i = 0;


        if(this.subChunk == (byte) 0){

            byte[] data2d = new byte[DATA2D_LENGTH];

            //fill heightmap
            for(; i < POS_BIOME_DATA;){
                data2d[i++] = 0;
                data2d[i++] = 32;
            }

            //fill biome data
            for(; i < DATA2D_LENGTH;){
                data2d[i++] = 1;//biome: plains
                data2d[i++] = (byte) 42;//r
                data2d[i++] = (byte) 42;//g
                data2d[i++] = (byte) 42;//b
            }

            this.data2D = ByteBuffer.wrap(data2d);
        }


    }


    @Override
    public byte getBlockTypeId(int x, int y, int z) {
        if (x >= chunkW || y >= chunkH || z >= chunkL || x < 0 || y < 0 || z < 0) {
            return 0;
        }
        return terrainData.get(POS_BLOCK_IDS + getOffset(x, y, z));
    }

    @Override
    public byte getBlockData(int x, int y, int z) {
        if (x >= chunkW || y >= chunkH || z >= chunkL || x < 0 || y < 0 || z < 0) {
            return 0;
        }
        int offset = getOffset(x, y, z);
        byte dualData = terrainData.get(POS_META_DATA + (offset >>> 1));
        return (byte) ((offset & 1) == 1 ? ((dualData >>> 4) & 0xf) : (dualData & 0xf));
    }

    @Override
    public byte getSkyLightValue(int x, int y, int z) {
        if (x >= chunkW || y >= chunkH || z >= chunkL || x < 0 || y < 0 || z < 0) {
            return 0;
        }
        int offset = getOffset(x, y, z);
        byte dualData = terrainData.get(POS_SKY_LIGHT + (offset >>> 1));
        return (byte) ((offset & 1) == 1 ? (dualData >>> 4) & 0xf : dualData & 0xf);
    }

    @Override
    public byte getBlockLightValue(int x, int y, int z) {
        if (x >= chunkW || y >= chunkH || z >= chunkL || x < 0 || y < 0 || z < 0) {
            return 0;
        }
        int offset = getOffset(x, y, z);
        byte dualData = terrainData.get(POS_BLOCK_LIGHT + (offset >>> 1));
        return (byte) ((offset & 1) == 1 ? (dualData >>> 4) & 0xf : dualData & 0xf);
    }

    /**
     * Sets a block type, and also set the corresponding dirty table entry and set the saving flag.
     */
    @Override
    public void setBlockTypeId(int x, int y, int z, int type) {
        if (x >= chunkW || y >= chunkH || z >= chunkL || x < 0 || y < 0 || z < 0) {
            return;
        }
        terrainData.put(POS_BLOCK_IDS + getOffset(x, y, z), (byte) type);
    }

    @Override
    public void setBlockData(int x, int y, int z, int newData) {
        if (x >= chunkW || y >= chunkH || z >= chunkL || x < 0 || y < 0 || z < 0) {
            return;
        }
        int offset = getOffset(x, y, z);
        int pos = POS_META_DATA + (offset >> 1);
        byte oldData = terrainData.get(POS_META_DATA + (offset >> 1));
        if ((offset & 1) == 1) {
            terrainData.put(pos, (byte) ((newData << 4) | (oldData & 0xf)));
        } else {
            terrainData.put(pos, (byte) ((oldData & 0xf0) | (newData & 0xf)));
        }
    }

    private int getOffset(int x, int y, int z) {
        return (x * chunkW + z) * chunkH + y;
    }

    @Override
    public byte getBiome(int x, int z) {
        return data2D.get(POS_BIOME_DATA + get2Di(x, z));
    }

    private int getNoise(int base, int x, int z){
        // noise values are between -1 and 1
        // 0.0001 is added to the coordinates because integer values result in 0
        double oct1 = Noise.noise(
                ((double) (this.chunk.x * chunkW + x) / 100.0) + 0.0001,
                ((double) (this.chunk.z * chunkL + z) / 100.0) + 0.0001);
        double oct2 = Noise.noise(
                ((double) (this.chunk.x * chunkW + x) / 20.0) + 0.0001,
                ((double) (this.chunk.z * chunkL + z) / 20.0) + 0.0001);
        double oct3 = Noise.noise(
                ((double) (this.chunk.x * chunkW + x) / 3.0) + 0.0001,
                ((double) (this.chunk.z * chunkL + z) / 3.0) + 0.0001);
        return (int) (base + 60 + (40 * oct1) + (14 * oct2) + (6 * oct3));
    }

    /*
        MCPE 1.0 stopped embedding foliage color data in the chunk data,
         so now we fake the colors by combining biome colors with Perlin noise
     */

    @Override
    public byte getGrassR(int x, int z) {
        Biome biome = Biome.getBiome(getBiome(x, z) & 0xff);
        int res = getNoise(30 + (biome.color.red / 5), x, z);
        return (byte) (res > 0xff ? 0xff : (res < 0 ? 0 : res));
    }

    @Override
    public byte getGrassG(int x, int z) {
        Biome biome = Biome.getBiome(getBiome(x, z) & 0xff);
        int res = getNoise(120 + (biome.color.green / 5), x, z);
        return (byte) (res > 0xff ? 0xff : (res < 0 ? 0 : res));
    }

    @Override
    public byte getGrassB(int x, int z) {
        Biome biome = Biome.getBiome(getBiome(x, z) & 0xff);
        int res = getNoise(30 + (biome.color.blue / 5), x, z);
        return (byte) (res > 0xff ? 0xff : (res < 0 ? 0 : res));
    }

    private int get2Di(int x, int z) {
        return z * chunkL + x;
    }

    @Override
    public int getHeightMapValue(int x, int z) {
        short h = data2D.getShort(POS_HEIGHTMAP + (get2Di(x, z) << 1));
        return ((h & 0xff) << 8) | ((h >> 8) & 0xff);//little endian to big endian
    }
}
