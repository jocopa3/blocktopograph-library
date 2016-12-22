package com.protolambda.blocktopograph.map.renderer;

import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.chunk.Version;
import com.protolambda.blocktopograph.map.Dimension;
import java.awt.image.BufferedImage;



public class ChessPatternRenderer implements MapRenderer {

    public final int darkShade, lightShade;// int DARK_SHADE = 0xFF2B2B2B, LIGHT_SHADE = 0xFF585858;

    ChessPatternRenderer(int darkShade, int lightShade){
        this.darkShade = darkShade;
        this.lightShade = lightShade;
    }

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
        //System.out.println("chunkX:"+chunkX+", chunkZ:"+chunkZ+", bX:"+bX+", bZ:"+bZ+", eX:"+eX+", eZ:"+eZ+", pX:"+pX+", pY:"+pY+", pW:"+pW+", pL:"+pL);
        int x, z, i, j, tX, tY;
        int color;

        for (z = bZ, tY = pY ; z < eZ; z++, tY += pL) {
            for (x = bX, tX = pX; x < eX; x++, tX += pW) {
                color = ((x + z) & 1) == 1 ? darkShade : lightShade;
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
