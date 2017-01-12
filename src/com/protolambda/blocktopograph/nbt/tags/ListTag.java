package com.protolambda.blocktopograph.nbt.tags;

import com.protolambda.blocktopograph.nbt.convert.NBTConstants;

import java.util.ArrayList;

public class ListTag extends Tag<ArrayList<Tag>> {
    private NBTConstants.NBTType listType;

    private static final long serialVersionUID = -4765717626522070446L;

    public ListTag(String name, ArrayList<Tag> value, NBTConstants.NBTType listType) {
        super(name, value);
        
        this.listType = listType;
    }

    @Override
    public NBTConstants.NBTType getType() {
        return NBTConstants.NBTType.LIST;
    }
    
    public NBTConstants.NBTType getListType() {
        return listType;
    }
    
    public boolean setListType(NBTConstants.NBTType type) {
        if(getValue() == null || getValue().isEmpty()) {
            listType = type;
            return true;
        }
        
        return false;
    }

    @Override
    public String toString(){
        String name = getName();
        String type = getType().name();
        ArrayList<Tag> value = getValue();
        StringBuilder bldr = new StringBuilder();
        bldr.append(type == null ? "?" : ("TAG_" + type))
                .append(name == null ? "(?)" : ("(" + name + ")"));

        if(value != null) {
            bldr.append(": ")
                .append(value.size())
                .append(" entries\n[\n");
            for (Tag entry : value) {
                //pad every line of the value
                bldr.append("   ")
                    .append(entry.toString().replaceAll("\n", "\n   "))
                    .append("\n");
            }
            bldr.append("]");
        } else bldr.append(":?");

        return bldr.toString();
    }


    @Override
    public ListTag getDeepCopy() {
        if(value != null){
            ArrayList<Tag> copy = new ArrayList<>();
            for(Tag tag : value){
                copy.add(tag.getDeepCopy());
            }
            return new ListTag(name, copy, listType);
        } else return new ListTag(name, null, listType);
    }

}
