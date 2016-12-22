/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.map;

import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.map.renderer.MapType;
import org.jxmapviewer.viewer.Tile;

/**
 *
 * @author Matt
 */
public class MCTile extends Tile {
    
    private ChunkManager cm;
    private MapType map;
    
    public MCTile(int x, int y, int zoom, ChunkManager cm, MapType map) {
        super(x, y, zoom);
        
        this.cm = cm;
        this.map = map;
    }
    
    public ChunkManager getChunkManager() {
        return cm;
    }
    
    public MapType getMapType() {
        return map;
    }
}
