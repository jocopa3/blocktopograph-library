/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.map;

import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.chunk.Version;
import com.protolambda.blocktopograph.map.renderer.*;
import com.protolambda.blocktopograph.util.io.ImageUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.jxmapviewer.viewer.AbstractTileFactory;
import org.jxmapviewer.viewer.Tile;
import org.jxmapviewer.viewer.TileCache;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.util.GeoUtil;

/**
 *
 * @author Matt
 */
public class MCTileProvider extends TileFactory {

    private final TileCache<String> tileCache = new TileCache<>();
    private Map<String, MCTile> tileMap = new HashMap<String, MCTile>();
    MCTileProviderInfo info;

    private int threadPoolSize = Runtime.getRuntime().availableProcessors();
    ;
    private ExecutorService service;

    public MCTileProvider(MCTileProviderInfo info) {
        super(info);
        this.info = info;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        /*
        String key = "Tile [" + tpx + ", " + tpy + ", " + zoom + "]";
        
        if (!tileMap.containsKey(key)) {
            Tile tile = new Tile(tpx, tpy, zoom);
            startLoading(tile);

            tileMap.put(tile.toString(), tile);

            return tile;
        } else {
            return tileMap.get(key);
        }
         */
        return getTile(x, y, zoom, true);
    }

    public Tile getTile(int tpx, int tpy, int zoom, boolean eagerLoad) {
        //System.out.println("getTile:" + x + ", " + y + ", " + zoom);
        String key = "Tile [" + tpx + ", " + tpy + ", " + zoom + "]";
        /*
        if (!tileMap.containsKey(key)) {
            Tile tile = new Tile(tpx, tpy, zoom);
            startLoading(tile);

            tileMap.put(tile.toString(), tile);

            //return tile;
        } else {
            //return tileMap.get(key);
        }
         */

        // wrap the tiles horizontally --> mod the X with the max width
        // and use that
        int tileX = tpx;// tilePoint.getX();
        //int numTilesWide = (int) getMapSize(zoom).getWidth();
        //if (tileX < 0)
        //{
        //    tileX = numTilesWide - (Math.abs(tileX) % numTilesWide);
        //}

        //tileX = tileX % numTilesWide;
        int tileY = tpy;
        // TilePoint tilePoint = new TilePoint(tileX, tpy);
        // System.out.println("loading: " + url);

        MCTile tile;
        // System.out.println("testing for validity: " + tilePoint + " zoom = " + zoom);
        if (!tileMap.containsKey(key)) {
            MapType map = this.info.worldProvider.getMapType();

            tile = new MCTile(tileX, tileY, zoom, this.info.worldProvider.getChunkManager(map.dimension), map);
            startLoading(tile);
            tileMap.put(key, tile);
        } else {
            tile = tileMap.get(key);
            // if its in the map but is low and isn't loaded yet
            // but we are in high mode
            if (tile.getPriority() == Tile.Priority.Low && eagerLoad && !tile.isLoaded()) {
                // System.out.println("in high mode and want a low");
                // tile.promote();
                promote((MCTile) tile);
            }
        }

        return tile;
    }

    @Override
    public void dispose() {
        if (service != null) {
            service.shutdown();
            service = null;
        }
    }

    /**
     * @return the tile cache
     */
    public TileCache getTileCache() {
        return tileCache;
    }

    public static final int TILESIZE = 256, HALF_WORLDSIZE = 1 << 20;

    public static int worldSizeInBlocks = 2 * HALF_WORLDSIZE,
            viewSizeW = worldSizeInBlocks * TILESIZE / Dimension.OVERWORLD.chunkW,
            viewSizeL = worldSizeInBlocks * TILESIZE / Dimension.OVERWORLD.chunkL;

    /*
    @Override
    protected void startLoading(Tile tile) {
        System.out.println("startLoading");
        if (tile.getImage() != null) {
            return;
        }
        System.out.println("null1");
        BufferedImage img = null;

        try {
            img = tileCache.get(tile.toString());
        } catch (IOException ex) {
            Logger.getLogger(MCTileProvider.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (img == null) {
            System.out.println("null2");
            img = new BufferedImage(TILESIZE, TILESIZE, BufferedImage.TYPE_USHORT_565_RGB);
            MapRenderer renderer = info.worldProvider.getMapType().renderer;
            Dimension dimension = info.worldProvider.getMapType().dimension;
            ChunkManager cm = info.worldProvider.getChunkManager(dimension);
            // 1 chunk per tile on scale 1.0
            int pixelsPerBlockW_unscaled = TILESIZE / dimension.chunkW;
            int pixelsPerBlockL_unscaled = TILESIZE / dimension.chunkL;
            int scale = tile.getZoom();
            // this will be the amount of chunks in the width of one tile
            int invScale = Math.round(1 / scale);

            //scale the amount of pixels, less pixels per block if zoomed out
            int pixelsPerBlockW = Math.round(pixelsPerBlockW_unscaled / scale);
            int pixelsPerBlockL = Math.round(pixelsPerBlockL_unscaled / scale);

            int minChunkX = (tile.getX()) * scale;
            int minChunkZ = (tile.getY()) * scale;
            int maxChunkX = minChunkX + scale;
            int maxChunkZ = minChunkZ + scale;

            int pixelsPerChunkW = pixelsPerBlockW * dimension.chunkW;
            int pixelsPerChunkL = pixelsPerBlockL * dimension.chunkL;

            int x, z, pX, pY;
            String tileTxt;
            tileTxt = "(" + (minChunkX * dimension.chunkW) + "; " + (minChunkZ * dimension.chunkL) + ")";

            for (z = minChunkZ, pY = 0; z < maxChunkZ; z++, pY += pixelsPerChunkL) {
                for (x = minChunkX, pX = 0; x < maxChunkX; x++, pX += pixelsPerChunkW) {
                    try {
                        renderer.renderToBitmap(cm, img, dimension,
                                x, z,
                                0, 0,
                                dimension.chunkW, dimension.chunkL,
                                pX, pY,
                                pixelsPerBlockW, pixelsPerBlockL);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
     */
 /*
            // for translating to origin
            // HALF_WORLDSIZE and TILESIZE must be a power of two
            int tilesInHalfWorldW = (HALF_WORLDSIZE * pixelsPerBlockW) / TILESIZE;
            int tilesInHalfWorldL = (HALF_WORLDSIZE * pixelsPerBlockL) / TILESIZE;
            
            // translate tile coord to origin, multiply origin-relative-tile-coordinate with the chunks per tile
            //int minChunkX = (tile.getX() - tilesInHalfWorldW) * invScale;
            //int minChunkZ = (tile.getY() - tilesInHalfWorldL) * invScale;
            int minChunkX = (tile.getX()) * invScale;
            int minChunkZ = (tile.getY()) * invScale;
            int maxChunkX = minChunkX + invScale;
            int maxChunkZ = minChunkZ + invScale;
            
            //scale pixels to dimension scale (Nether 1 : 8 Overworld)
            pixelsPerBlockW *= dimension.dimensionScale;
            pixelsPerBlockL *= dimension.dimensionScale;
            
            int x, z, pX, pY;
            String tileTxt;
            
            //check if the tile is not aligned with its inner chunks
            //hacky: it must be a single chunk that is to big for the tile, render just the visible part, easy.
            int alignment = invScale % dimension.dimensionScale;
            if (alignment > 0) {
            
            int chunkX = minChunkX / dimension.dimensionScale;
            if (minChunkX % dimension.dimensionScale < 0) {
            chunkX -= 1;
            }
            int chunkZ = minChunkZ / dimension.dimensionScale;
            if (minChunkZ % dimension.dimensionScale < 0) {
            chunkZ -= 1;
            }
            
            int stepX = dimension.chunkW / dimension.dimensionScale;
            int stepZ = dimension.chunkL / dimension.dimensionScale;
            int minX = (minChunkX % dimension.dimensionScale) * stepX;
            if (minX < 0) {
            minX += dimension.chunkW;
            }
            int minZ = (minChunkZ % dimension.dimensionScale) * stepZ;
            if (minZ < 0) {
            minZ += dimension.chunkL;
            }
            int maxX = (maxChunkX % dimension.dimensionScale) * stepX;
            if (maxX <= 0) {
            maxX += dimension.chunkW;
            }
            int maxZ = (maxChunkZ % dimension.dimensionScale) * stepZ;
            if (maxZ <= 0) {
            maxZ += dimension.chunkL;
            }
            
            tileTxt = chunkX + ";" + chunkZ + " (" + ((chunkX * dimension.chunkW) + minX) + "; " + ((chunkZ * dimension.chunkL) + minZ) + ")";
            
            renderer.renderToBitmap(cm, img, dimension,
            chunkX, chunkZ,
            minX, minZ,
            maxX, maxZ,
            0, 0,
            pixelsPerBlockW, pixelsPerBlockL);
            
            } else {
            
            minChunkX /= dimension.dimensionScale;
            minChunkZ /= dimension.dimensionScale;
            maxChunkX /= dimension.dimensionScale;
            maxChunkZ /= dimension.dimensionScale;
            
            tileTxt = "(" + (minChunkX * dimension.chunkW) + "; " + (minChunkZ * dimension.chunkL) + ")";
            
            int pixelsPerChunkW = pixelsPerBlockW * dimension.chunkW;
            int pixelsPerChunkL = pixelsPerBlockL * dimension.chunkL;
            
            System.out.println(pixelsPerBlockW+", "+pixelsPerBlockL);
            
            for (z = minChunkZ, pY = 0; z < maxChunkZ; z++, pY += pixelsPerChunkL) {
            
            for (x = minChunkX, pX = 0; x < maxChunkX; x++, pX += pixelsPerChunkW) {
            
            try {
            renderer.renderToBitmap(cm, img, dimension,
            x, z,
            0, 0,
            dimension.chunkW, dimension.chunkL,
            pX, pY,
            pixelsPerBlockW, pixelsPerBlockL);
            } catch (Exception e) {
            
            e.printStackTrace();
            }
            
            }
            }
            }
     */
 /*
            drawText(tileTxt, img, Color.WHITE.getRGB(), 0);
            //draw tile-edges white
            for (int i = 0; i < TILESIZE; i++) {

                //horizontal edges
                img.setRGB(i, 0, Color.WHITE.getRGB());
                img.setRGB(i, TILESIZE - 1, Color.WHITE.getRGB());

                //vertical edges
                img.setRGB(0, i, Color.WHITE.getRGB());
                img.setRGB(TILESIZE - 1, i, Color.WHITE.getRGB());
            }
        }

        tile.image = new SoftReference<>(img);

        byte[] bimg = ImageUtil.asBytes(img);
        tileCache.put(tile.toString(), bimg, img);
        tile.setLoaded(true);

        fireTileLoadedEvent(tile);
    }
     */
    @Override
    protected synchronized void startLoading(Tile tile) {
        if (tile.isLoading()) {
            // System.out.println("already loading. bailing");
            return;
        }
        tile.setLoading(true);
        try {
            tileQueue.put((MCTile) tile);
            getService().submit(createTileRunner((MCTile) tile));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Subclasses can override this if they need custom TileRunners for some
     * reason
     *
     * @param tile the tile (unused!)
     * @return the tile runner
     */
    protected Runnable createTileRunner(MCTile tile) {
        return new MCTileProvider.TileRunner();
    }

    /**
     * Increase the priority of this tile so it will be loaded sooner.
     *
     * @param tile the tile
     */
    public synchronized void promote(MCTile tile) {
        if (tileQueue.contains(tile)) {
            try {
                tileQueue.remove(tile);
                tile.setPriority(MCTile.Priority.High);
                tileQueue.put(tile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Not yet implemented
    public static BufferedImage drawText(String text, BufferedImage b, int textColor, int bgColor) {
        /* Get text dimensions
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setTextSize(b.getHeight() / 16f);
        StaticLayout mTextLayout = new StaticLayout(text, textPaint, b.getWidth() / 2, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        // Create bitmap and canvas to draw to
        Canvas c = new Canvas(b);

        if(bgColor != 0){
            // Draw background
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(bgColor);
            c.drawPaint(paint);
        }

        // Draw text
        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();
         */
        Graphics graphics = b.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Segoe", Font.PLAIN, b.getHeight() / 16));
        graphics.drawString(text, 2, b.getHeight() - 5);
        return b;
    }

    public void clearCache() {
        tileMap.clear();
        tileCache.clearCache();
    }

    public void clearQueue() {
        tileQueue.clear();
    }

    /**
     * ==== threaded tile loading stuff ===
     */
    /**
     * Thread pool for loading the tiles
     */
    private BlockingQueue<MCTile> tileQueue = new PriorityBlockingQueue<MCTile>(5, new Comparator<MCTile>() {
        @Override
        public int compare(MCTile o1, MCTile o2) {
            if (o1.getPriority() == MCTile.Priority.Low && o2.getPriority() == MCTile.Priority.High) {
                return 1;
            }
            if (o1.getPriority() == MCTile.Priority.High && o2.getPriority() == MCTile.Priority.Low) {
                return -1;
            }
            return 0;

        }
    });

    /**
     * Subclasses may override this method to provide their own executor
     * services. This method will be called each time a tile needs to be loaded.
     * Implementations should cache the ExecutorService when possible.
     *
     * @return ExecutorService to load tiles with
     */
    protected synchronized ExecutorService getService() {
        if (service == null) {
            // System.out.println("creating an executor service with a threadpool of size " + threadPoolSize);
            service = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
                private int count = 0;

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "tile-pool-" + count++);
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.setDaemon(true);
                    return t;
                }
            });
        }
        return service;
    }

    /**
     * Set the number of threads to use for loading the tiles. This controls the
     * number of threads used by the ExecutorService returned from getService().
     * Note, this method should be called before loading the first tile. Calls
     * after the first tile are loaded will have no effect by default.
     *
     * @param size the thread pool size
     */
    public void setThreadPoolSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size invalid: " + size
                    + ". The size of the threadpool must be greater than 0.");
        }
        threadPoolSize = size;
    }

    /**
     * An inner class which actually loads the tiles. Used by the thread queue.
     * Subclasses can override this if necessary.
     */
    private class TileRunner implements Runnable {

        /**
         * implementation of the Runnable interface.
         */
        @Override
        public void run() {
            /*
             * 3 strikes and you're out. Attempt to load the url. If it fails, decrement the number of tries left and
             * try again. Log failures. If I run out of try s just get out. This way, if there is some kind of serious
             * failure, I can get out and let other tiles try to load.
             */
            final MCTile tile = tileQueue.remove();

            int trys = 3;
            while (!tile.isLoaded() && trys > 0) {
                System.out.println(trys);
                try {
                    if (shouldDiscardTile(tile, true)) {
                        return;
                    }
                    BufferedImage img;
                    String key = tile.toString();
                    img = tileCache.get(key);

                    if (img == null) {
                        img = getRenderedImage(tile);

                        byte[] bimg = ImageUtil.asBytes(img);
                        // img = PaintUtils.loadCompatibleImage(new ByteArrayInputStream(bimg));

                        tileCache.put(key, bimg, img);
                        img = tileCache.get(key);
                    }
                    if (img == null) {
                        // System.out.println("error loading: " + uri);
                        trys--;
                    } else {
                        final BufferedImage i = img;
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                tile.image = new SoftReference<BufferedImage>(i);
                                tile.setLoaded(true);

                                if (!shouldDiscardTile(tile, true)) {
                                    fireTileLoadedEvent(tile);
                                }
                            }
                        });
                    }
                } catch (OutOfMemoryError memErr) {
                    tileCache.needMoreMemory();
                } catch (Throwable e) {
                    if (trys == 0) {
                    } else {
                        trys--;
                    }
                }
            }
            tile.setLoading(false);
        }

        private boolean shouldDiscardTile(MCTile tile, boolean remove) {
            if (tile.getMapType().equals(info.worldProvider.getMapType())) {
                return false;
            }

            if (remove) {
                tile.setLoading(false);
                tileQueue.remove(tile);
                tileMap.remove(tile.toString());
            }

            return true;
        }

        private BufferedImage getRenderedImage(MCTile tile) {
            BufferedImage img = null;

            img = new BufferedImage(TILESIZE, TILESIZE, BufferedImage.TYPE_INT_ARGB);
            MapRenderer renderer = tile.getMapType().renderer;
            Dimension dimension = tile.getMapType().dimension;
            ChunkManager cm = tile.getChunkManager();

            // 1 chunk per tile on scale 1.0
            int pixelsPerBlockW_unscaled = TILESIZE / dimension.chunkW;
            int pixelsPerBlockL_unscaled = TILESIZE / dimension.chunkL;

            int scale = 1 << tile.getZoom();

            // this will be the amount of chunks in the width of one tile
            int invScale = Math.round(1 / scale);
            //scale the amount of pixels, less pixels per block if zoomed out
            int pixelsPerBlockW = Math.round(pixelsPerBlockW_unscaled / scale);
            int pixelsPerBlockL = Math.round(pixelsPerBlockL_unscaled / scale);
            

            float minChunkX = (tile.getX()) * scale;
            float minChunkZ = (tile.getY()) * scale;
            float maxChunkX = minChunkX + scale;
            float maxChunkZ = minChunkZ + scale;

            float pixelsPerChunkW = pixelsPerBlockW * dimension.chunkW;
            float pixelsPerChunkL = pixelsPerBlockL * dimension.chunkL;

            float x, z, pX, pY;
            String tileTxt;
            tileTxt = "(" + (minChunkX * dimension.chunkW) + "; " + (minChunkZ * dimension.chunkL) + ")";

            for (z = minChunkZ, pY = 0; z < maxChunkZ; z++, pY += pixelsPerChunkL) {
                for (x = minChunkX, pX = 0; x < maxChunkX; x++, pX += pixelsPerChunkW) {
                    try {
                        renderer.renderToBitmap(cm, img, dimension,
                                Math.round(x), Math.round(z),
                                0, 0,
                                dimension.chunkW, dimension.chunkL,
                                Math.round(pX), Math.round(pY),
                                Math.round(pixelsPerBlockW), Math.round(pixelsPerBlockL));
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
             /*
            // for translating to origin
            // HALF_WORLDSIZE and TILESIZE must be a power of two
            int tilesInHalfWorldW = (HALF_WORLDSIZE * pixelsPerBlockW) / TILESIZE;
            int tilesInHalfWorldL = (HALF_WORLDSIZE * pixelsPerBlockL) / TILESIZE;

            // translate tile coord to origin, multiply origin-relative-tile-coordinate with the chunks per tile
            //int minChunkX = (tile.getX() - tilesInHalfWorldW) * invScale;
            //int minChunkZ = (tile.getY() - tilesInHalfWorldL) * invScale;
            int minChunkX = (tile.getX()) * invScale;
            int minChunkZ = (tile.getY()) * invScale;
            int maxChunkX = minChunkX + invScale;
            int maxChunkZ = minChunkZ + invScale;

            //scale pixels to dimension scale (Nether 1 : 8 Overworld)
            pixelsPerBlockW *= dimension.dimensionScale;
            pixelsPerBlockL *= dimension.dimensionScale;

            int x, z, pX, pY;
            String tileTxt;

            //check if the tile is not aligned with its inner chunks
            //hacky: it must be a single chunk that is to big for the tile, render just the visible part, easy.
            int alignment = invScale % dimension.dimensionScale;
            if (alignment > 0) {

                int chunkX = minChunkX / dimension.dimensionScale;
                if (minChunkX % dimension.dimensionScale < 0) {
                    chunkX -= 1;
                }
                int chunkZ = minChunkZ / dimension.dimensionScale;
                if (minChunkZ % dimension.dimensionScale < 0) {
                    chunkZ -= 1;
                }

                int stepX = dimension.chunkW / dimension.dimensionScale;
                int stepZ = dimension.chunkL / dimension.dimensionScale;
                int minX = (minChunkX % dimension.dimensionScale) * stepX;
                if (minX < 0) {
                    minX += dimension.chunkW;
                }
                int minZ = (minChunkZ % dimension.dimensionScale) * stepZ;
                if (minZ < 0) {
                    minZ += dimension.chunkL;
                }
                int maxX = (maxChunkX % dimension.dimensionScale) * stepX;
                if (maxX <= 0) {
                    maxX += dimension.chunkW;
                }
                int maxZ = (maxChunkZ % dimension.dimensionScale) * stepZ;
                if (maxZ <= 0) {
                    maxZ += dimension.chunkL;
                }

                tileTxt = chunkX + ";" + chunkZ + " (" + ((chunkX * dimension.chunkW) + minX) + "; " + ((chunkZ * dimension.chunkL) + minZ) + ")";
                try {
                    renderer.renderToBitmap(cm, img, dimension,
                            chunkX, chunkZ,
                            minX, minZ,
                            maxX, maxZ,
                            0, 0,
                            pixelsPerBlockW, pixelsPerBlockL);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            } else {

                minChunkX /= dimension.dimensionScale;
                minChunkZ /= dimension.dimensionScale;
                maxChunkX /= dimension.dimensionScale;
                maxChunkZ /= dimension.dimensionScale;

                tileTxt = "(" + (minChunkX * dimension.chunkW) + "; " + (minChunkZ * dimension.chunkL) + ")";

                int pixelsPerChunkW = pixelsPerBlockW * dimension.chunkW;
                int pixelsPerChunkL = pixelsPerBlockL * dimension.chunkL;

                System.out.println(pixelsPerBlockW + ", " + pixelsPerBlockL);

                for (z = minChunkZ, pY = 0; z < maxChunkZ; z++, pY += pixelsPerChunkL) {

                    for (x = minChunkX, pX = 0; x < maxChunkX; x++, pX += pixelsPerChunkW) {

                        try {
                            renderer.renderToBitmap(cm, img, dimension,
                                    x, z,
                                    0, 0,
                                    dimension.chunkW, dimension.chunkL,
                                    pX, pY,
                                    pixelsPerBlockW, pixelsPerBlockL);
                        } catch (Exception e) {

                            e.printStackTrace();
                        }

                    }
                }
            }
*/
            drawText(tileTxt, img, Color.WHITE.getRGB(), 0);
            //draw tile-edges white
            for (int i = 0; i < TILESIZE; i++) {

                //horizontal edges
                img.setRGB(i, 0, Color.WHITE.getRGB());
                img.setRGB(i, TILESIZE - 1, Color.WHITE.getRGB());

                //vertical edges
                img.setRGB(0, i, Color.WHITE.getRGB());
                img.setRGB(TILESIZE - 1, i, Color.WHITE.getRGB());
            }

            return img;
        }
    }
}
