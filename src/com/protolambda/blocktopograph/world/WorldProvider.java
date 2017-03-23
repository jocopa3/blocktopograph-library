package com.protolambda.blocktopograph.world;

import com.protolambda.blocktopograph.world.WorldData;
import com.protolambda.blocktopograph.world.World;
import com.protolambda.blocktopograph.Log;
import com.protolambda.blocktopograph.chunk.ChunkManager;
import com.protolambda.blocktopograph.map.Dimension;
import com.protolambda.blocktopograph.map.marker.AbstractMarker;
import com.protolambda.blocktopograph.nbt.convert.NBTConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.protolambda.blocktopograph.chunk.NBTChunkData;
import com.protolambda.blocktopograph.chunk.Version;
import com.protolambda.blocktopograph.map.renderer.MapType;
import com.protolambda.blocktopograph.nbt.EditableNBT;
import com.protolambda.blocktopograph.nbt.convert.DataConverter;
import com.protolambda.blocktopograph.nbt.tags.CompoundTag;
import com.protolambda.blocktopograph.nbt.tags.Tag;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldProvider implements WorldProviderInterface {

    private World world;
    ChunkManager cmOverworld;
    ChunkManager cmNether;
    ChunkManager cmEnd;

    public WorldProvider(World world) {
        this.world = world;
        cmOverworld = new ChunkManager(world.getWorldData(), Dimension.OVERWORLD);
        cmNether = new ChunkManager(world.getWorldData(), Dimension.NETHER);
        cmEnd = new ChunkManager(world.getWorldData(), Dimension.END);
    }

    /**
     * Short-hand for opening special entries with
     * openEditableNbtDbEntry(keyName)
     */
    public EditableNBT openSpecialEditableNbtDbEntry(final World.SpecialDBEntryType entryType)
            throws IOException {
        return openEditableNbtDbEntry(entryType.keyName);
    }

    /**
     * Load NBT data of this key from the database, converting it into
     * structured Java Objects. These objects are wrapped in a nice EditableNBT,
     * ready for viewing and editing.
     *
     * @param keyName Key corresponding with NBT data in the database.
     * @return EditableNBT, NBT wrapper of NBT objects to view or to edit.
     * @throws IOException when database fails.
     */
    public EditableNBT openEditableNbtDbEntry(final String keyName) throws IOException {
        final byte[] keyBytes = keyName.getBytes(NBTConstants.CHARSET);
        WorldData worldData = world.getWorldData();
        try {
            worldData.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] entryData = worldData.db.get(keyBytes, worldData.globalReadOptions);
        if (entryData == null) {
            return null;
        }

        final ArrayList<Tag> workCopy = DataConverter.read(entryData);

        return new EditableNBT() {

            @Override
            public Iterable<Tag> getTags() {
                return workCopy;
            }

            @Override
            public boolean save() {
                try {
                    WorldData wData = world.getWorldData();
                    wData.load();
                    wData.db.put(keyBytes, DataConverter.write(workCopy), wData.globalWriteOptions);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public String getRootTitle() {
                return keyName;
            }

            @Override
            public void addRootTag(Tag tag) {
                workCopy.add(tag);
            }

            @Override
            public void removeRootTag(Tag tag) {
                workCopy.remove(tag);
            }
        };

    }

    //returns an editableNBT, where getTags() provides a compound tag as item with player-data
    /**
     * Loads local player data "~local-player" or level.dat>"Player" into an
     * EditableNBT.
     *
     * @return EditableNBT, local player NBT data wrapped in a handle to use for
     * saving + metadata
     * @throws Exception
     */
    public EditableNBT getEditablePlayer() throws Exception {

        /*
                Logic path:
                1. try to find the player-data in the db:
                        if found -> return that
                        else -> go to 2
                2. try to find the player-data in the level.dat:
                        if found -> return that
                        else -> go to 3
                3. no player-data available: warn the user
         */
        EditableNBT editableNBT;
        try {
            editableNBT = openSpecialEditableNbtDbEntry(World.SpecialDBEntryType.LOCAL_PLAYER);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Failed to read \"~local_player\" from the database.");
        }

        //check if it is not found in the DB
        if (editableNBT == null) {
            editableNBT = openEditableNbtLevel("Player");
        }

        //check if it is not found in level.dat as well
        if (editableNBT == null) {
            throw new Exception("Failed to find \"~local_player\" in DB and \"Player\" in level.dat!");
        }

        return editableNBT;

    }

    /**
     * Opens an editableNBT for just the subTag if it is not null. Opens the
     * whole level.dat if subTag is null. *
     */
    public EditableNBT openEditableNbtLevel(String subTagName) {

        //make a copy first, the user might not want to save changed tags.
        final CompoundTag workCopy = world.level.getDeepCopy();
        final ArrayList<Tag> workCopyContents;
        final String contentTitle;
        if (subTagName == null) {
            workCopyContents = workCopy.getValue();
            contentTitle = "level.dat";
        } else {
            workCopyContents = new ArrayList<>();
            Tag subTag = workCopy.getChildTagByKey(subTagName);
            if (subTag == null) {
                return null;
            }
            workCopyContents.add(subTag);
            contentTitle = "level.dat>" + subTagName;
        }

        EditableNBT editableNBT = new EditableNBT() {

            @Override
            public Iterable<Tag> getTags() {
                return workCopyContents;
            }

            @Override
            public boolean save() {
                try {
                    //write a copy of the workCopy, the workCopy may be edited after saving
                    world.writeLevel(workCopy.getDeepCopy());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public String getRootTitle() {
                return contentTitle;
            }

            @Override
            public void addRootTag(Tag tag) {
                workCopy.getValue().add(tag);
                workCopyContents.add(tag);
            }

            @Override
            public void removeRootTag(Tag tag) {
                workCopy.getValue().remove(tag);
                workCopyContents.remove(tag);
            }
        };

        //if this editable nbt is only a view of a sub-tag, not the actual root
        editableNBT.enableRootModifications = (subTagName != null);

        return editableNBT;
    }

    //TODO the dimension should be derived from mapTypes.
    // E.g. split xray into xray-overworld and xray-nether, but still use the same [MapRenderer],
    //  splitting allows to pass more sophisticated use of [MapRenderer]s
    private MapType mapType = MapType.OVERWORLD_SATELLITE;
    private Dimension dimension = mapType.dimension;

    public Dimension getDimension() {
        return this.dimension;
    }

    public MapType getMapType() {
        return this.mapType;
    }

    // TODO grid should be rendered independently of tiles, it could be faster and more responsive.
    // However, it does need to adjust itself to the scale and position of the map,
    //  which is not an easy task.
    public boolean showGrid = true;

    @Override
    public boolean getShowGrid() {
        return showGrid;
    }

    private boolean fatal = false;

    @Override
    public void onFatalDBError(WorldData.WorldDBException worldDBException) {

        Log.d(worldDBException.getMessage());
        worldDBException.printStackTrace();

        //already dead? (happens on multiple onFatalDBError(e) calls)
        if (fatal) {
            return;
        }

        fatal = true;
    }

    @Override
    public void changeMapType(MapType mapType) {
        this.mapType = mapType;
        this.dimension = mapType.dimension;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    public NBTChunkData getChunkEntityNBT(int x, int z, Dimension d) {
        try {
            return getChunkManager(d).getChunk(x, z).getEntity();
        } catch (Version.VersionException ex) {
            return null;
        }
    }

    public NBTChunkData getChunkBlockEntityNBT(int x, int z, Dimension d) {
        try {
            return getChunkManager(d).getChunk(x, z).getBlockEntity();
        } catch (Version.VersionException ex) {
            return null;
        }
    }

    //@Override
    public ChunkManager getChunkManager(Dimension d) {
        switch (d) {
            case OVERWORLD:
                return cmOverworld;
            case NETHER:
                return cmNether;
            case END:
                return cmEnd;
            default:
                return cmOverworld;
        }
    }

    @Override
    public void addMarker(AbstractMarker marker) {
        //mapFragment.addMarker(marker);
    }

    public void clean() {
        if (cmOverworld != null) {
            cmOverworld.disposeAll();
        }
        if (cmNether != null) {
            cmNether.disposeAll();
        }
        if (cmEnd != null) {
            cmEnd.disposeAll();
        }
    }
}
