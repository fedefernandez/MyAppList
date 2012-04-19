package com.projectsexception.myapplist.util;

import android.util.Log;

public class CustomLog {
    
    private static final String TAG = "MyAppList";
    
    private static final int LOG_LEVEL = Log.DEBUG;
    
    private static final boolean VERBOSE = LOG_LEVEL <= Log.VERBOSE;
    private static final boolean DEBUG = LOG_LEVEL <= Log.DEBUG;
    private static final boolean INFO = LOG_LEVEL <= Log.INFO;
    private static final boolean WARN = LOG_LEVEL <= Log.WARN;
    private static final boolean ERROR = LOG_LEVEL <= Log.ERROR;

    public static void verbose(String source, String msg) {
        verbose(source, msg, null);
    }
    
    public static void verbose(String source, String msg, Throwable e) {
        log(Log.VERBOSE, source, msg, e);
    }

    public static void debug(String source, String msg) {
        debug(source, msg, null);     
    }

    public static void debug(String source, String msg, Throwable e) {
        log(Log.DEBUG, source, msg, e); 
    }

    public static void info(String source, String msg) {
        info(source, msg, null);
    }
    
    public static void info(String source, String msg, Throwable e) {
        log(Log.INFO, source, msg, e);
    }

    public static void warn(String source, String msg) {
        warn(source, msg, null);
    }
    
    public static void warn(String source, String msg, Throwable e) {
        log(Log.WARN, source, msg, e);
    }

    public static void error(String source, String msg) {
        error(source, msg, null);
    }
    
    public static void error(String source, String msg, Throwable e) {
        log(Log.ERROR, source, msg, e);
    }

    public static void log(int level, String source, String msg) {
        log(level, source, msg, null);
    }

    public static void log(int level, String source, String msg, Throwable e) {
        if (level == Log.VERBOSE) {
            if (VERBOSE) {                
                Log.v(TAG, message(source, msg, e));
            }
        } else if (level == Log.DEBUG) {
            if (DEBUG) {
                Log.d(TAG, message(source, msg, e));
            }
        } else if (level == Log.INFO) {
            if (INFO) {
                Log.i(TAG, message(source, msg, e));
            }
        } else if (level == Log.WARN) {
            if (WARN) {
                Log.w(TAG, message(source, msg, e));
            }
        } else if (level == Log.ERROR) {
            if (ERROR) {
                Log.v(TAG, message(source, msg, e));
            }
        }
    }
    
    private static String message(String source, String msg, Throwable e) {
        StringBuilder sb = new StringBuilder(source);
        sb.append(": ");
        sb.append(msg);
        if(e != null) {
            sb.append(" Caused by: ");
            sb.append(e.getClass().getName());
            if (e.getMessage() != null) {
                sb.append(" ");
                sb.append(e.getMessage());
            }
        }
        return sb.toString();
    }
}
