package com.protolambda.blocktopograph;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    //TODO This is kind of lazy, but repeating the Log.d(*msg*) everywhere is obnoxious
    //TODO log only if debug mode is on?
    public static final String LOG_TAG = "Blocktopograph";

    public static void i(String msg) {
        Logger.getGlobal().log(Level.INFO, msg);
    }

    public static void d(String msg) {
        // Java's logger class doesn't have a DEBUG level, so just default to INFO level
        Logger.getGlobal().log(Level.INFO, msg);
    }

    public static void w(String msg) {
        Logger.getGlobal().log(Level.WARNING, msg);
    }

    public static void e(String msg) {
        Logger.getGlobal().log(Level.SEVERE, msg);
    }

}
