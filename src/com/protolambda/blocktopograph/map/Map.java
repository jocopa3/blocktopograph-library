/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.map;

import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.chunk.terrain.TerrainChunkData;
import com.protolambda.blocktopograph.nbt.convert.DataConverter;
import com.protolambda.blocktopograph.nbt.tags.CompoundTag;
import com.protolambda.blocktopograph.nbt.tags.IntTag;
import com.protolambda.blocktopograph.nbt.tags.ListTag;
import com.protolambda.blocktopograph.util.math.DimensionVector3;
import com.protolambda.blocktopograph.world.World;
import com.protolambda.blocktopograph.world.WorldData;
import com.protolambda.blocktopograph.world.WorldProvider;

/**
 *
 * @author Matt
 */
public class Map {

    public static DimensionVector3<Integer> getSpawnPos(WorldProvider worldProvider) throws Exception {
        try {
            CompoundTag level = worldProvider.getWorld().level;
            int spawnX = ((IntTag) level.getChildTagByKey("SpawnX")).getValue();
            int spawnY = ((IntTag) level.getChildTagByKey("SpawnY")).getValue();
            int spawnZ = ((IntTag) level.getChildTagByKey("SpawnZ")).getValue();
            if (spawnY == 256) {
                TerrainChunkData data = worldProvider.getChunkManager(Dimension.OVERWORLD)
                        .getChunk(spawnX >> 4, spawnZ >> 4)
                        .getTerrain((byte) 0);
                if (data.load2DData()) {
                    spawnY = data.getHeightMapValue(spawnX % 16, spawnZ % 16) + 1;
                }
            }
            return new DimensionVector3<>(spawnX, spawnY, spawnZ, Dimension.OVERWORLD);
        } catch (Exception e) {
            throw new Exception("Could not find spawn");
        }
    }

    public static DimensionVector3<Float> getPlayerPos(WorldProvider worldProvider) throws Exception {
        try {
            WorldData wData = worldProvider.getWorld().getWorldData();
            //wData.openDB();
            byte[] data = wData.db.get(World.SpecialDBEntryType.LOCAL_PLAYER.keyBytes, wData.globalReadOptions);

            final CompoundTag player = data != null
                    ? (CompoundTag) DataConverter.read(data).get(0)
                    : (CompoundTag) worldProvider.getWorld().level.getChildTagByKey("Player");

            ListTag posVec = (ListTag) player.getChildTagByKey("Pos");
            if (posVec == null || posVec.getValue() == null) {
                throw new Exception("No \"Pos\" specified");
            }
            if (posVec.getValue().size() != 3) {
                throw new Exception("\"Pos\" value is invalid. value: " + posVec.getValue().toString());
            }

            IntTag dimensionId = (IntTag) player.getChildTagByKey("DimensionId");
            if (dimensionId == null || dimensionId.getValue() == null) {
                throw new Exception("No \"DimensionId\" specified");
            }
            Dimension dimension = Dimension.getDimension(dimensionId.getValue());
            if (dimension == null) {
                dimension = Dimension.OVERWORLD;
            }

            return new DimensionVector3<>(
                    (float) posVec.getValue().get(0).getValue(),
                    (float) posVec.getValue().get(1).getValue(),
                    (float) posVec.getValue().get(2).getValue(),
                    dimension);

        } catch (Exception e) {
            //Log.e(e.toString());
            e.printStackTrace();

            Exception e2 = new Exception("Could not find player.");
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }
}
