package com.protolambda.blocktopograph.map;

import com.protolambda.blocktopograph.map.renderer.MapType;

import java.util.HashMap;

public enum Dimension {

    NULL(3, "null", "Null", 16, 16, 16, 1),
    OVERWORLD(0, "overworld", "Overworld", 16, 16, 256, 1),
    NETHER(1, "nether", "Nether", 16, 8, 128, 8),
    END(2, "end", "End", 16, 16, 256, 1);//mcpe: SOON^TM /jk

    public final int id;
    public final int chunkW, chunkL, chunkH;
    public final int dimensionScale;
    public final String dataName, name;

    Dimension(int id, String dataName, String name, int chunkW, int chunkL, int chunkH, int dimensionScale) {
        this.id = id;
        this.dataName = dataName;
        this.name = name;
        this.chunkW = chunkW;
        this.chunkL = chunkL;
        this.chunkH = chunkH;
        this.dimensionScale = dimensionScale;
    }

    private static HashMap<String, Dimension> dimensionMap = new HashMap<>();

    static {
        for (Dimension dimension : Dimension.values()) {
            dimensionMap.put(dimension.dataName, dimension);
        }
    }

    public static Dimension getDimension(String dataName) {
        if (dataName == null) {
            return null;
        }
        return dimensionMap.get(dataName.toLowerCase());
    }

    public static Dimension getDimension(int id) {
        for (Dimension dimension : values()) {
            if (dimension.id == id) {
                return dimension;
            }
        }
        return null;
    }

    public MapType getDefaultMapType() {
        switch (this) {
            case OVERWORLD:
                return MapType.OVERWORLD_SATELLITE;
            case NETHER:
                return MapType.NETHER;
            case END:
                return MapType.END_SATELLITE;
            default:
                return MapType.CHESS;
        }
    }
}
