package com.protolambda.blocktopograph.map.renderer;

import com.protolambda.blocktopograph.map.Dimension;

public enum MapType {

    //shows that a chunk was present, but couldn't be renderer
    ERROR(new ChessPatternRenderer(0xFF2B0000, 0xFF580000), Dimension.NULL),

    //simple xor pattern renderer
    DEBUG(new DebugRenderer(), Dimension.NULL),

    //simple chess pattern renderer
    CHESS(new ChessPatternRenderer(0xFF2B2B2B, 0xFF585858), Dimension.NULL),

    //just the surface of the world, with shading for height diff
    OVERWORLD_SATELLITE(new SatelliteRenderer(), Dimension.OVERWORLD),

    //cave mapping
    OVERWORLD_CAVE(new CaveRenderer(), Dimension.OVERWORLD),

    OVERWORLD_SLIME_CHUNK(new SlimeChunkRenderer(), Dimension.OVERWORLD),

    //render skylight value of highest block
    OVERWORLD_HEIGHTMAP(new HeightmapRenderer(), Dimension.OVERWORLD),

    //render biome id as biome-unique color
    OVERWORLD_BIOME(new BiomeRenderer(), Dimension.OVERWORLD),

    //render the voliage colors
    OVERWORLD_GRASS(new GrassRenderer(), Dimension.OVERWORLD),

    //render only the valuable blocks to mine (diamonds, emeralds, gold, etc.)
    OVERWORLD_XRAY(new XRayRenderer(), Dimension.OVERWORLD),
    
    OVERWORLD_REDSTONE(new RedstoneRenderer(), Dimension.OVERWORLD),

    //block-light renderer: from light-sources like torches etc.
    OVERWORLD_BLOCK_LIGHT(new BlockLightRenderer(), Dimension.OVERWORLD),

    NETHER(new NetherRenderer(), Dimension.NETHER),

    NETHER_XRAY(new XRayRenderer(), Dimension.NETHER),

    NETHER_BLOCK_LIGHT(new BlockLightRenderer(), Dimension.NETHER),

    END_SATELLITE(new SatelliteRenderer(), Dimension.END),

    END_HEIGHTMAP(new HeightmapRenderer(), Dimension.END),

    END_BLOCK_LIGHT(new BlockLightRenderer(), Dimension.END);

    //REDSTONE //TODO redstone circuit mapping
    //TRAFFIC //TODO traffic mapping (land = green, water = blue, gravel/stone/etc. = gray, rails = yellow)

    public final MapRenderer renderer;
    public final Dimension dimension;

    MapType(MapRenderer renderer, Dimension d){
        this.renderer = renderer;
        this.dimension = d;
    }

}
