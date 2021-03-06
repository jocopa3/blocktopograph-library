package com.protolambda.blocktopograph.map.renderer;

import com.protolambda.blocktopograph.chunk.Chunk;
import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.chunk.Version;
import com.protolambda.blocktopograph.chunk.terrain.TerrainChunkData;
import com.protolambda.blocktopograph.map.Biome;
import com.protolambda.blocktopograph.map.Dimension;
import java.awt.image.BufferedImage;



public class BiomeRenderer implements MapRenderer {

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
        if(chunk == null) return MapType.CHESS.renderer.renderToBitmap(cm, bm, dimension, chunkX, chunkZ, bX, bZ, eX, eZ, pX, pY, pW, pL);
        Version cVersion = chunk.getVersion();

        if(cVersion == Version.ERROR) return MapType.ERROR.renderer.renderToBitmap(cm, bm, dimension, chunkX, chunkZ, bX, bZ, eX, eZ, pX, pY, pW, pL);

        //the bottom sub-chunk is sufficient to get biome data.
        TerrainChunkData data = chunk.getTerrain((byte) 0);
        if(data == null || !data.load2DData()) return MapType.CHESS.renderer.renderToBitmap(cm, bm, dimension, chunkX, chunkZ, bX, bZ, eX, eZ, pX, pY, pW, pL);

        boolean west = true, north = true;
        TerrainChunkData dataW = null, dataN = null;
        
        Chunk dataWC = cm.getChunk(chunkX - 1, chunkZ);
        if(dataWC != null)
            dataW = dataWC.getTerrain((byte) 0);
        else
            west = false;
        
        Chunk dataNC = cm.getChunk(chunkX, chunkZ-1);
        if(dataNC != null)
            dataN = dataNC.getTerrain((byte) 0);
        else
            north = false;
        

        west &= dataW != null && dataW.load2DData();
        north &= dataN != null && dataN.load2DData();
        
        int x, y, z, biomeID, color, i, j, tX, tY;
        int yW, yN;
        int r, g, b;
        float yNorm, yNorm2, heightShading;
        Biome biome;

        for (z = bZ, tY = pY ; z < eZ; z++, tY += pL) {
            for (x = bX, tX = pX; x < eX; x++, tX += pW) {

                y = data.getHeightMapValue(x, z);

                yNorm = (float) y / (float) dimension.chunkH;
                yNorm2 = yNorm*yNorm;
                yNorm = ((6f*yNorm2) - (15f*yNorm) + 10f)*yNorm2*yNorm;

                yW = (x == 0) ? (west ? dataW.getHeightMapValue(dimension.chunkW - 1, z) : y)//chunk edge
                        : data.getHeightMapValue(x - 1, z);//within chunk
                yN = (z == 0) ? (north ? dataN.getHeightMapValue(x, dimension.chunkL - 1) : y)//chunk edge
                        : data.getHeightMapValue(x, z - 1);//within chunk

                heightShading = SatelliteRenderer.getHeightShading(y, yW, yN) * 0.5f;
                heightShading += 0.3f;
                
                biomeID = data.getBiome(x, z) & 0xff;
                biome = Biome.getBiome(biomeID);

                color = biome == null ? 0xff000000 : ((int)(biome.color.red*heightShading) << 16) | ((int)(biome.color.green*heightShading) << 8) | ((int)(biome.color.blue*heightShading)) | 0xff000000;

                for(i = 0; i < pL; i++){
                    for(j = 0; j < pW; j++){
                        bm.setRGB(tX + j, tY + i, color);
                    }
                }


            }
        }

        return bm;
    }

}
