package com.protolambda.blocktopograph.map.renderer;

import com.protolambda.blocktopograph.chunk.Chunk;
import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.chunk.Version;
import com.protolambda.blocktopograph.chunk.terrain.TerrainChunkData;
import com.protolambda.blocktopograph.map.Block;
import com.protolambda.blocktopograph.map.Dimension;
import java.awt.image.BufferedImage;



public class CaveRenderer implements MapRenderer {

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

        boolean solid, intoSurface;
        int id, meta, cavyness, layers, offset, streak;
        Block block;
        int x, y, z, subChunk, color, i, j, tX, tY, r, g, b;

        //the bottom sub-chunk is sufficient to get heightmap data.
        TerrainChunkData floorData = chunk.getTerrain((byte) 0);
        if(floorData == null || !floorData.load2DData()) return MapType.CHESS.renderer.renderToBitmap(cm, bm, dimension, chunkX, chunkZ, bX, bZ, eX, eZ, pX, pY, pW, pL);

        TerrainChunkData data;

        for (z = bZ, tY = pY; z < eZ; z++, tY += pL) {
            for (x = bX, tX = pX; x < eX; x++, tX += pW) {


                solid = false;
                intoSurface = false;
                cavyness = 0;
                layers = 0;
                streak = 0;
                y = floorData.getHeightMapValue(x, z);
                offset = y % cVersion.subChunkHeight;
                subChunk = y / cVersion.subChunkHeight;

                /*
                while (cavefloor > 0) {
                    caveceil = chunk.getCaveYUnderAt(x, z, cavefloor - 1);
                    cavefloor = chunk.getHighestBlockYUnderAt(x, z, caveceil - 1);

                    //do not accept first cave-layer (most likely trees) as caves
                    if (layers > 1) cavyness += caveceil - cavefloor;

                    layers++;
                }
                */


                r = g = b = 0;

                subChunkLoop: for(; subChunk >= 0; subChunk--) {

                    data = chunk.getTerrain((byte) subChunk);
                    if (data == null || !data.loadTerrain()){
                        //start at the top of the next chunk! (current offset might differ)
                        offset = cVersion.subChunkHeight - 1;
                        continue;
                    }


                    for (y = offset; y >= 0; y--) {

                        id = data.getBlockTypeId(x, y, z) & 0xff;

                        meta = data.getBlockData(x, y, z) & 0xff;
                        block = Block.getBlock(id, meta);

                        //try the default meta value: 0
                        if (block == null) block = Block.getBlock(id, 0);

                        switch (id) {
                            case 0:
                                //count the number of times it goes from solid to air
                                if(solid) layers++;

                                //count the air blocks underground,
                                // but avoid trees by skipping the first layer
                                if(intoSurface) cavyness++;
                                break;
                            case 66://rail
                                if (b < 150) {
                                    b = 150;
                                    r = g = 50;
                                }
                                break;
                            case 5://wooden plank
                                if (b < 100) {
                                    b = 100;
                                    r = g = 100;
                                }
                                break;
                            case 52://monster spawner
                                r = g = b = 255;
                                break subChunkLoop;
                            case 54://chest
                                if (b < 170) {
                                    b = 170;
                                    r = 240;
                                    g = 40;
                                }
                                break;
                            case 98://stone bricks
                                if (b < 145) {
                                    b = 145;
                                    r = g = 120;
                                }
                                break;
                            case 48://moss cobblestone
                            case 4://cobblestone
                                if (b < 140) {
                                    b = 140;
                                    r = g = 100;
                                }
                                break;
                        }
                        r += data.getBlockLightValue(x, y, z);
                        solid = block != null && block.color.alpha == 0xff;
                        intoSurface |= solid && (y < 60 || layers > 0);
                    }
                }

                if (g == 0 && layers > 0){
                    g = (r + 2) * cavyness;
                    r *= 32 * layers;
                    b = 16 * cavyness * (layers - 1);
                } else r *= r;


                r = r < 0 ? 0 : r > 255 ? 255 : r;
                g = g < 0 ? 0 : g > 255 ? 255 : g;
                //b = b < 0 ? 0 : b > 255 ? 255 : b;


                color = (r << 16) | (g << 8) | b | 0xff000000;


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

