package com.protolambda.blocktopograph.world;

import com.protolambda.blocktopograph.Log;
import com.protolambda.blocktopograph.chunk.ChunkTag;
import com.protolambda.blocktopograph.map.Dimension;
import com.protonail.leveldb.jna.LevelDB;
import com.protonail.leveldb.jna.LevelDBKeyIterator;
import com.protonail.leveldb.jna.LevelDBOptions;
import com.protonail.leveldb.jna.LevelDBReadOptions;
import com.protonail.leveldb.jna.LevelDBWriteOptions;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper around level.dat world spec en levelDB database.
 */
public class WorldData {

    public static class WorldDataLoadException extends Exception {

        private static final long serialVersionUID = 659185044124115547L;

        public WorldDataLoadException(String msg) {
            super(msg);
        }
    }

    public static class WorldDBException extends Exception {

        private static final long serialVersionUID = -3299282170140961220L;

        public WorldDBException(String msg) {
            super(msg);
        }
    }

    public static class WorldDBLoadException extends Exception {

        private static final long serialVersionUID = 4412238820886423076L;

        public WorldDBLoadException(String msg) {
            super(msg);
        }
    }

    private World world;

    public LevelDB db = null;
    public ConcurrentHashMap<String, LevelDBReadOptions> ros = new ConcurrentHashMap<>();

    public WorldData(World world) {
        this.world = world;
    }

    protected static final int DEFAULT_BLOCK_SIZE = 0x28000;
    protected static final boolean SHOULD_CREATE_IF_MISSING = false;

    // Will later include settings for stuff...
    private void openDB(File DBFile) throws IOException {
        LevelDBOptions options = new LevelDBOptions();
        options.setCreateIfMissing(SHOULD_CREATE_IF_MISSING);
        options.setBlockSize(DEFAULT_BLOCK_SIZE);
        
        db = new LevelDB(DBFile.getAbsolutePath(), options);

        System.out.println("Opened Database for " + world.getWorldDisplayName());
    }

    //load db when needed (does not load it!)
    public void load() throws WorldDataLoadException, IOException {

        if (db != null) {
            return;
        }

        File dbFile = new File(this.world.worldFolder, "db");
        if (!dbFile.canRead()) {
            if (!dbFile.setReadable(true, false)) {
                throw new WorldDataLoadException("World-db folder is not readable! World-db folder: " + dbFile.getAbsolutePath());
            }
        }
        if (!dbFile.canWrite()) {
            if (!dbFile.setWritable(true, false)) {
                throw new WorldDataLoadException("World-db folder is not writable! World-db folder: " + dbFile.getAbsolutePath());
            }
        }

        Log.d("WorldFolder: " + this.world.worldFolder.getAbsolutePath());
        Log.d("WorldFolder permissions: read: " + dbFile.canRead() + " write: " + dbFile.canWrite());

        if (dbFile.listFiles() == null) {
            throw new WorldDataLoadException("Failed loading world-db: cannot list files in worldfolder");
        }

        for (File dbEntry : dbFile.listFiles()) {
            Log.d("File in db: " + dbEntry.getAbsolutePath());
        }

        openDB(dbFile);
    }

    //another method for debugging, makes it easy to print a readable byte array
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes, int start, int end) {
        char[] hexChars = new char[(end - start) * 2];
        for (int j = start; j < end; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[(j - start) * 2] = hexArray[v >>> 4];
            hexChars[(j - start) * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //close db to make it available for other apps (Minecraft itself!)
    public void closeDB() throws WorldDBException {
        if (this.db == null) {
            throw new WorldDBException("DB is null!");
        }

        try {
            this.db.close();
        } catch (Exception e) {
            //db was already closed (hopefully)
            e.printStackTrace();
        }

        this.db = null;

        System.out.println("Closed Database for " + world.getWorldDisplayName());
    }

    /**
     * WARNING: DELETES WORLD !!!
     */
    public void destroy() throws WorldDBException, IOException {
        if (this.db == null) {
            throw new WorldDBException("DB is null!");
        }

        this.db.close();
        //this.db.destroy();
        this.db = null;
    }

    public static String asString(byte[] value) {
        if (value == null) {
            return null;
        }
        try {
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public final LevelDBReadOptions globalReadOptions = new LevelDBReadOptions();
    public final LevelDBWriteOptions globalWriteOptions = new LevelDBWriteOptions();

    public byte[] getChunkData(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk) throws WorldDBException, WorldDBLoadException {

        //ensure that the db is opened
        //this.openDB();
        // Get read options for the current thread
        //ReadOptions ro = ros.get(Thread.currentThread().getName());
        // If read options for the thread don't exist, create a new snapshot
        //if(ro == null)
        //{
        //ro = new ReadOptions();
        //ro.snapshot(db.getSnapshot());
        //ro.fillCache(true);
        //ros.put(Thread.currentThread().getName(), ro);
        //}

        if(db == null) 
            return null;
        
        byte[] chunkKey = getChunkDataKey(x, z, type, dimension, subChunk, asSubChunk);
        //Log.d("Getting cX: "+x+" cZ: "+z+ " with key: "+bytesToHex(chunkKey, 0, chunkKey.length));

        return db.get(chunkKey, globalReadOptions);
    }

    public void writeChunkData(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk, byte[] chunkData) throws WorldDBException {
        //ensure that the db is opened
        //this.openDB();

        db.put(getChunkDataKey(x, z, type, dimension, subChunk, asSubChunk), chunkData, globalWriteOptions);
    }

    public void removeChunkData(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk) throws WorldDBException {
        //ensure that the db is opened
        //this.openDB();

        db.delete(getChunkDataKey(x, z, type, dimension, subChunk, asSubChunk), globalWriteOptions);
    }

    public String[] getPlayers() {
        List<String> players = getDBKeysStartingWith("player_");
        return players.toArray(new String[players.size()]);
    }

    public List<String> getDBKeysStartingWith(String startWith) {
        LevelDBKeyIterator it = new LevelDBKeyIterator(db, globalReadOptions);

        ArrayList<String> items = new ArrayList<>();
        
        try {
            byte[] keyData = new byte[0];
            for (it.seekToFirst(); it.hasNext(); keyData = it.next()) {
                String keyStr = asString(keyData);
                if (keyStr.startsWith(startWith)) {
                    items.add(keyStr);
                }
            }
        } finally {
            it.close();
        }

        return items;
    }

    public static byte[] getChunkDataKey(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk) {
        if (dimension == Dimension.OVERWORLD) {
            byte[] key = new byte[asSubChunk ? 10 : 9];
            System.arraycopy(getReversedBytes(x), 0, key, 0, 4);
            System.arraycopy(getReversedBytes(z), 0, key, 4, 4);
            key[8] = type.dataID;
            if (asSubChunk) {
                key[9] = subChunk;
            }
            return key;
        } else {
            byte[] key = new byte[asSubChunk ? 14 : 13];
            System.arraycopy(getReversedBytes(x), 0, key, 0, 4);
            System.arraycopy(getReversedBytes(z), 0, key, 4, 4);
            System.arraycopy(getReversedBytes(dimension.id), 0, key, 8, 4);
            key[12] = type.dataID;
            if (asSubChunk) {
                key[13] = subChunk;
            }
            return key;
        }
    }

    public static byte[] getReversedBytes(int i) {
        return new byte[]{
            (byte) i,
            (byte) (i >> 8),
            (byte) (i >> 16),
            (byte) (i >> 24)
        };
    }
}
