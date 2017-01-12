/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph;

/**
 *
 * @author Matt
 */
public enum UsageLevel {
    VERY_STRICT(0, 1), // Used in systems with very little resources available
    STRICT(1, 2),      
    MODEST(2, 4),      // Used in systems with decent resource availability
    GREEDY(3, 7),
    VERY_GREEDY(4, 11); // Used in systems with lots of available resources; do not use unless the user explicitly asks to
    
    public final int id;
    public final int mult;
    
    UsageLevel(int id, int mult) {
        this.id = id;
        this.mult = mult;
    }
}