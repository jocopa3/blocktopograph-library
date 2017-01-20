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
public enum GameMode {
    SURVIVIAL(0, "Survival"),
    CREATIVE(1, "Creative");
    //ADVENTURE(2, "Adventure"); // Soon, but not now
    //SPECTATOR(3, "Spectator"); // Soon, but not now
    
    public final int id;
    public final String name;
    
    GameMode(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    //private static HashMap<String, Structure> structureMap = new HashMap<>();

    //static {
    //    for (GameMode mode : values()) {
    //        structureMap.put(mode.id, mode);
    //    }
   // }
}
