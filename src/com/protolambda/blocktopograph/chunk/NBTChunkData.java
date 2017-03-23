package com.protolambda.blocktopograph.chunk;

import com.protolambda.blocktopograph.nbt.EditableNBT;
import com.protolambda.blocktopograph.world.WorldData;
import com.protolambda.blocktopograph.nbt.convert.DataConverter;
import com.protolambda.blocktopograph.nbt.tags.IntTag;
import com.protolambda.blocktopograph.nbt.tags.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NBTChunkData extends ChunkData {

    public List<Tag> tags = new ArrayList<>();

    public final ChunkTag dataType;

    public NBTChunkData(Chunk chunk, ChunkTag dataType) {
        super(chunk);
        this.dataType = dataType;
        System.out.println("nbtchunk");
    }

    public void load() throws WorldData.WorldDBLoadException, WorldData.WorldDBException, IOException {
        loadFromByteArray(chunk.worldData.getChunkData(chunk.x, chunk.z, dataType, this.chunk.dimension, (byte) 0, false));
    }

    public void loadFromByteArray(byte[] data) throws IOException {
        if (data != null && data.length > 0) {
            this.tags = DataConverter.read(data);
        }
    }

    public void write() throws WorldData.WorldDBException, IOException {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        byte[] data = DataConverter.write(this.tags);
        this.chunk.worldData.writeChunkData(this.chunk.x, this.chunk.z, this.dataType, this.chunk.dimension, (byte) 0, false, data);
    }

    @Override
    public void createEmpty() {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(new IntTag("Placeholder", 42));
    }

    public EditableNBT getEditableNBTData() {
        try {
            load();
        } catch (WorldData.WorldDBLoadException ex) {
            return null;
        } catch (WorldData.WorldDBException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        
        return new EditableNBT() {
            @Override
            public Iterable<? extends Tag> getTags() {
                return tags;
            }

            @Override
            public boolean save() {
                try {
                    write();
                } catch (WorldData.WorldDBException ex) {
                    return false;
                } catch (IOException ex) {
                    return false;
                }

                return true;
            }

            @Override
            public String getRootTitle() {
                return "Chunk";
            }

            @Override
            public void addRootTag(Tag tag) {
                if (tags == null) {
                    return;
                }
                tags.add(tag);
            }

            @Override
            public void removeRootTag(Tag tag) {
                if (tags == null) {
                    return;
                }
                tags.remove(tag);
            }
        };
    }
}
