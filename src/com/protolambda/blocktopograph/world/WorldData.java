package com.protolambda.blocktopograph.world;

import com.protolambda.blocktopograph.Log;
import com.protolambda.blocktopograph.chunk.ChunkTag;
import com.protolambda.blocktopograph.map.Dimension;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
/**
 * Wrapper around level.dat world spec en levelDB database.
 */
public class WorldData {

    public static class WorldDataLoadException extends Exception {
        private static final long serialVersionUID = 659185044124115547L;

        public WorldDataLoadException(String msg){ super(msg); }
    }

    public static class WorldDBException extends Exception {
        private static final long serialVersionUID = -3299282170140961220L;

        public WorldDBException(String msg){ super(msg); }
    }

    public static class WorldDBLoadException extends Exception {
        private static final long serialVersionUID = 4412238820886423076L;

        public WorldDBLoadException(String msg){ super(msg); }
    }

    private World world;

    public DB db = null;

    public WorldData(World world){
        this.world = world;
    }
    
    protected static final int DEFAULT_CACHE_SIZE = 16384 * 64; 
    protected static final boolean SHOULD_CREATE_IF_MISSING = false;
    
    // Will later include settings for stuff...
    private void openDB(File DBFile) throws IOException {
        Options options = new Options();
        options.createIfMissing(SHOULD_CREATE_IF_MISSING);
        options.cacheSize(DEFAULT_CACHE_SIZE); 
        options.compressionType(CompressionType.ZLIB);
        db = factory.open(DBFile, options); //Open the db folder
    }

    //load db when needed (does not load it!)
    public void load() throws WorldDataLoadException, IOException {

        if(db != null) return;

        File dbFile = new File(this.world.worldFolder, "db");
        if(!dbFile.canRead()){
            if(!dbFile.setReadable(true, false)) throw new WorldDataLoadException("World-db folder is not readable! World-db folder: "+dbFile.getAbsolutePath());
        }
        if(!dbFile.canWrite()){
            if(!dbFile.setWritable(true, false)) throw new WorldDataLoadException("World-db folder is not writable! World-db folder: "+dbFile.getAbsolutePath());
        }

        Log.d("WorldFolder: "+this.world.worldFolder.getAbsolutePath());
        Log.d("WorldFolder permissions: read: " + dbFile.canRead() + " write: "+dbFile.canWrite());

        if(dbFile.listFiles() == null) throw new WorldDataLoadException("Failed loading world-db: cannot list files in worldfolder");

        for(File dbEntry : dbFile.listFiles()){
            Log.d("File in db: "+dbEntry.getAbsolutePath());
        }
        
        openDB(dbFile);
    }

    //another method for debugging, makes it easy to print a readable byte array
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes, int start, int end) {
        char[] hexChars = new char[(end-start) * 2];
        for ( int j = start; j < end; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[(j-start) * 2] = hexArray[v >>> 4];
            hexChars[(j-start) * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //close db to make it available for other apps (Minecraft itself!)
    public void closeDB() throws WorldDBException {
        if(this.db == null) throw new WorldDBException("DB is null!");

        try {
            this.db.close();
        } catch (Exception e){
            //db was already closed (probably)
            e.printStackTrace();
        }
        
        this.db = null;
    }

    /** WARNING: DELETES WORLD !!! */
    public void destroy() throws WorldDBException, IOException {
        if(this.db == null) throw new WorldDBException("DB is null!");

        this.db.close();
        //this.db.destroy();
        this.db = null;
    }

    public byte[] getChunkData(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk) throws WorldDBException, WorldDBLoadException {

        //ensure that the db is opened
        //this.openDB();

        byte[] chunkKey = getChunkDataKey(x, z, type, dimension, subChunk, asSubChunk);
        //Log.d("Getting cX: "+x+" cZ: "+z+ " with key: "+bytesToHex(chunkKey, 0, chunkKey.length));
        return db.get(chunkKey);
    }

    public void writeChunkData(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk, byte[] chunkData) throws WorldDBException {
        //ensure that the db is opened
        //this.openDB();

        db.put(getChunkDataKey(x, z, type, dimension, subChunk, asSubChunk), chunkData);
    }

    public void removeChunkData(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk) throws WorldDBException {
        //ensure that the db is opened
        //this.openDB();

        db.delete(getChunkDataKey(x, z, type, dimension, subChunk, asSubChunk));
    }

    public String[] getPlayers(){
        List<String> players = getDBKeysStartingWith("player_");
        return players.toArray(new String[players.size()]);
    }

    public List<String> getDBKeysStartingWith(String startWith){
        DBIterator it = db.iterator();

        ArrayList<String> items = new ArrayList<>();
        try {
        for(it.seekToFirst(); it.hasNext(); it.next()){
           String keyStr = asString(it.peekNext().getKey());
            if(keyStr.startsWith(startWith)) items.add(keyStr);
        }
        } finally {
            try {
                // Make sure you close the iterator to avoid resource leaks.
                it.close();
            } catch (IOException ex) {
                Log.e(startWith);
            }
        }

        return items;
    }

    public static byte[] getChunkDataKey(int x, int z, ChunkTag type, Dimension dimension, byte subChunk, boolean asSubChunk){
        if(dimension == Dimension.OVERWORLD) {
            byte[] key = new byte[asSubChunk ? 10 : 9];
            System.arraycopy(getReversedBytes(x), 0, key, 0, 4);
            System.arraycopy(getReversedBytes(z), 0, key, 4, 4);
            key[8] = type.dataID;
            if(asSubChunk) key[9] = subChunk;
            return key;
        } else {
            byte[] key = new byte[asSubChunk ? 14 : 13];
            System.arraycopy(getReversedBytes(x), 0, key, 0, 4);
            System.arraycopy(getReversedBytes(z), 0, key, 4, 4);
            System.arraycopy(getReversedBytes(dimension.id), 0, key, 8, 4);
            key[12] = type.dataID;
            if(asSubChunk) key[13] = subChunk;
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
