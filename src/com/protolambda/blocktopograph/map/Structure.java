/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.map;

import java.util.HashMap;

/**
 *
 * @author Matt
 */
public enum Structure {
    END_CITY(0, 0, "endcity", "End City"),
    NETHER_FORTRESS(1, 0, "fortress", "Nether Fortress"),
    MINESHAFT(2, 0, "mineshaft", "Mineshaft"),
    OCEAN_MONUMENT(3, 0, "monument", "Ocean Monument"),
    STRONGHOLD(4, 0, "stronghold", "Stronghold"),
    PYRAMID(5, 0, "temple", "Pyramid"),
    JUNGLE_TEMPLE(5, 1, "temple", "Jungle Temple"),
    WITCH_HUT(5, 2, "temple", "Witch Hut"),
    VILLAGE(6, 0, "village", "Village");

    public final int id, type;
    public final String dataName, name;

    Structure(int id, int type, String dataName, String name) {
        this.id = id;
        this.type = type;
        this.dataName = dataName;
        this.name = name;
    }

    private static HashMap<String, Structure> structureMap = new HashMap<>();

    static {
        for (Structure structure : values()) {
            structureMap.put(structure.dataName, structure);
        }
    }

    public static Structure getStructure(String dataName) {
        if (dataName == null) {
            return null;
        }
        return structureMap.get(dataName.toLowerCase());
    }

    public static Structure getStructure(int id, int type) {
        for (Structure structure : values()) {
            if (structure.id == id) {
                if (structure.type == type) {
                    return structure;
                }
            }
        }
        return null;
    }
    
    public static Structure getStructure(int id) {
        return getStructure(id, 0);
    }
}
