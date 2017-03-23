/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.nbt;

import com.protolambda.blocktopograph.nbt.convert.DataConverter;
import com.protolambda.blocktopograph.nbt.convert.LevelDataConverter;
import com.protolambda.blocktopograph.nbt.tags.CompoundTag;
import com.protolambda.blocktopograph.nbt.tags.Tag;
import com.protolambda.blocktopograph.world.World;
import com.protolambda.blocktopograph.world.WorldData;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class CompoundTagNBT extends EditableNBT {

    private CompoundTag rootTag;
    private File nbtFile;

    public CompoundTagNBT(File NBTFile) {
        nbtFile = NBTFile;
        try {
            rootTag = new CompoundTag("Root", null);
            rootTag = LevelDataConverter.read(NBTFile, rootTag);
        } catch (IOException e) {
            e.printStackTrace();
            rootTag = null;
        }
    }

    @Override
    public Iterable<Tag> getTags() {
        if (rootTag == null) {
            return null;
        }

        return rootTag.getValue();
    }

    @Override
    public boolean save() {
        if (rootTag == null) {
            return false;
        }

        try {
            LevelDataConverter.write(rootTag, nbtFile);
        } catch (IOException ex) {
            Logger.getLogger(CompoundTagNBT.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    @Override
    public String getRootTitle() {
        if (rootTag == null) {
            return null;
        }

        return rootTag.getName();
    }

    @Override
    public void addRootTag(Tag tag) {
        if (rootTag == null) {
            return;
        }
        
        rootTag.getValue().add(tag);
    }

    @Override
    public void removeRootTag(Tag tag) {
        if (rootTag == null) {
            return;
        }
        
        rootTag.getValue().remove(tag);
    }

}
