/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protolambda.blocktopograph;

/**
 * TODO: Split this up into PC and Android
 * @author Matt
 */
public class SystemProfile {
    private static final SystemProfile SYSTEM_PROFILE = new SystemProfile();
    
    private final int CPU_CORES; // Number of usable cores
    private final long SYSTEM_RAM; // in MBs
    
    private UsageLevel CPU_USE;
    private UsageLevel RAM_USE;
    
    private SystemProfile() {
        CPU_CORES = Runtime.getRuntime().availableProcessors();
        SYSTEM_RAM = Runtime.getRuntime().maxMemory();
        
        if(SYSTEM_RAM < 1250000000L) {
            RAM_USE = UsageLevel.VERY_STRICT;
        } else if(SYSTEM_RAM < 2500000000L) {
            RAM_USE = UsageLevel.STRICT;
        } else if(SYSTEM_RAM < 4500000000L) {
            RAM_USE = UsageLevel.MODEST;
        } else if(SYSTEM_RAM < 8500000000L) {
            RAM_USE = UsageLevel.GREEDY;
        } else {
            RAM_USE = UsageLevel.VERY_GREEDY;
        }
        
        switch(CPU_CORES) {
            case 0: // No way a CPU has 0 cores but include it anyway...
            case 1:
                CPU_USE = UsageLevel.VERY_STRICT;
                break;
            case 2:
                CPU_USE = UsageLevel.STRICT;
                break;
            case 3:
            case 4:
                CPU_USE = UsageLevel.MODEST;
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                CPU_USE = UsageLevel.GREEDY;
                break;
            default: // Anything higher than 8 cores
                CPU_USE = UsageLevel.VERY_GREEDY;
        }
    }
    
    public static int getCPUCores() {
        return SYSTEM_PROFILE.CPU_CORES;
    }
    
    public static long getAvailableRAM() {
        return SYSTEM_PROFILE.SYSTEM_RAM;
    }
    
    public static UsageLevel getRAMUsagePolicy() {
        return SYSTEM_PROFILE.RAM_USE;
    }
}
