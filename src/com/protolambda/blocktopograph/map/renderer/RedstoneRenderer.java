package com.protolambda.blocktopograph.map.renderer;

import com.protolambda.blocktopograph.chunk.Chunk;
import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.chunk.Version;
import com.protolambda.blocktopograph.chunk.terrain.TerrainChunkData;
import com.protolambda.blocktopograph.map.Block;
import com.protolambda.blocktopograph.map.Dimension;
import java.awt.image.BufferedImage;


public class RedstoneRenderer implements MapRenderer {

    /*
    TODO make the X-ray viewable blocks configurable, without affecting performance too much...
     */

    /**
     * Render a single chunk to provided bitmap (bm)
     * @param cm ChunkManager, provides chunks, which provide chunk-data
     * @param bm Bitmap to render to
     * @param dimension Mapped dimension
     * @param chunkX X chunk coordinate (x-block coord / Chunk.WIDTH)
     * @param chunkZ Z chunk coordinate (z-block coord / Chunk.LENGTH)
     * @param bX begin block X coordinate, relative to chunk edge
     * @param bZ begin block Z coordinate, relative to chunk edge
     * @param eX end block X coordinate, relative to chunk edge
     * @param eZ end block Z coordinate, relative to chunk edge
     * @param pX texture X pixel coord to start rendering to
     * @param pY texture Y pixel coord to start rendering to
     * @param pW width (X) of one block in pixels
     * @param pL length (Z) of one block in pixels
     * @return bm is returned back
     *
     * @throws Version.VersionException when the version of the chunk is unsupported.
     */
    public BufferedImage renderToBitmap(ChunkManager cm, BufferedImage bm, Dimension dimension, int chunkX, int chunkZ, int bX, int bZ, int eX, int eZ, int pX, int pY, int pW, int pL) throws Version.VersionException {

        Chunk chunk = cm.getChunk(chunkX, chunkZ);
        Version cVersion = chunk.getVersion();

        if(cVersion == Version.ERROR) return MapType.ERROR.renderer.renderToBitmap(cm, bm, dimension, chunkX, chunkZ, bX, bZ, eX, eZ, pX, pY, pW, pL);

        //the bottom sub-chunk is sufficient to get heightmap data.
        TerrainChunkData data;


        int x, y, z, color, i, j, tX, tY;

        //render width in blocks
        int rW = eX - bX;
        int size2D = rW * (eZ - bZ);
        int index2D;
        Block[] bestBlock = new Block[size2D];

        int[] minValue = new int[size2D];
        int bValue;
        Block block;

        int average;
        int r, g, b;

        int subChunk;
        for(subChunk = 0; subChunk < cVersion.subChunks; subChunk++) {
            data = chunk.getTerrain((byte) subChunk);
            if (data == null || !data.loadTerrain()) break;

            for (z = bZ; z < eZ; z++) {
                for (x = bX; x < eX; x++) {

                    for (y = 0; y < cVersion.subChunkHeight; y++) {
                        block = Block.getBlock(data.getBlockTypeId(x, y, z) & 0xff, 0);

                        index2D = (z * rW) + x;
                        
                        if (block == Block.B_55_0_REDSTONE_WIRE) {
                            bValue = data.getBlockData(x, y, z);
                        } else {
                            continue;
                        }
                        
                        if (bValue > minValue[(z * rW) + x]) {
                            minValue[index2D] = bValue;
                            bestBlock[index2D] = block;
                        }
                    }
                }
            }
        }

        if(subChunk == 0) return MapType.CHESS.renderer.renderToBitmap(cm, bm, dimension, chunkX, chunkZ, bX, bZ, eX, eZ, pX, pY, pW, pL);

        for (z = bZ, tY = pY; z < eZ; z++, tY += pL) {
            for (x = bX, tX = pX; x < eX; x++, tX += pW) {
                block = bestBlock[(z * rW) + x];
                if (block == null) {
                    color = 0xff000000;
                } else {

                    r = block.color.red;
                    g = block.color.green;
                    b = block.color.blue;
                    
                    r = (r/16) + (r/16) * minValue[(z * rW) + x]; 
                    
                    color = (r << 16) | (g << 8) | (b) | 0xff000000;
                }


                for (i = 0; i < pL; i++) {
                    for (j = 0; j < pW; j++) {
                        bm.setRGB(tX + j, tY + i, color);
                    }
                }


            }
        }

        return bm;
    }

}
