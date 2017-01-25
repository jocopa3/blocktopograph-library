/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph.map;

/**
 *
 * @author Matt
 */
public enum Generator {
    OLD(0, "Old"),
    INFINITE(1, "Infinite"),
    FLAT(2, "Flat");
    
    public final int id;
    public final String name;
    
    Generator(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
