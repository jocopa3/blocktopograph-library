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
public enum Difficulty {
    PEACEFUL(0, "Peaceful"),
    EASY(1, "Easy"),
    NORMAL(2, "Normal"),
    HARD(3, "Hard");
    
    public final int id;
    public final String name;
    
    Difficulty(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
