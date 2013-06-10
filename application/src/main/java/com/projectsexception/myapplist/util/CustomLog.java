package com.projectsexception.myapplist.util;

import android.util.Log;

public class CustomLog {
    
    private static final String TAG = "MyAppList";
    
    private static final int LOG_LEVEL = Log.INFO;
    
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
    
    public static void verbose(String source, Throwable e) {
        log(Log.VERBOSE, source, null, e);
    }

    public static void debug(String source, String msg) {
        debug(source, msg, null);     
    }

    public static void debug(String source, String msg, Throwable e) {
        log(Log.DEBUG, source, msg, e); 
    }

    public static void debug(String source, Throwable e) {
        log(Log.DEBUG, source, null, e); 
    }

    public static void info(String source, String msg) {
        info(source, msg, null);
    }
    
    public static void info(String source, String msg, Throwable e) {
        log(Log.INFO, source, msg, e);
    }
    
    public static void info(String source, Throwable e) {
        log(Log.INFO, source, null, e);
    }

    public static void warn(String source, String msg) {
        warn(source, msg, null);
    }
    
    public static void warn(String source, String msg, Throwable e) {
        log(Log.WARN, source, msg, e);
    }
    
    public static void warn(String source, Throwable e) {
        log(Log.WARN, source, null, e);
    }

    public static void error(String source, String msg) {
        error(source, msg, null);
    }
    
    public static void error(String source, String msg, Throwable e) {
        log(Log.ERROR, source, msg, e);
    }
    
    public static void error(String source, Throwable e) {
        log(Log.ERROR, source, null, e);
    }

    public static void log(int level, String source, String msg) {
        log(level, source, msg, null);
    }

    public static void log(int level, String source, String msg, Throwable e) {
        if (level == Log.VERBOSE) {
            if (VERBOSE) {
                if (e == null) {
                    Log.v(TAG, message(source, msg));                    
                } else {
                    Log.v(TAG, message(source, msg), e);
                }
            }
        } else if (level == Log.DEBUG) {
            if (DEBUG) {
                if (e == null) {
                    Log.d(TAG, message(source, msg));                    
                } else {
                    Log.d(TAG, message(source, msg), e);
                }
            }
        } else if (level == Log.INFO) {
            if (INFO) {
                if (e == null) {
                    Log.i(TAG, message(source, msg));                    
                } else {
                    Log.i(TAG, message(source, msg), e);
                }
            }
        } else if (level == Log.WARN) {
            if (WARN) {
                if (e == null) {
                    Log.w(TAG, message(source, msg));                    
                } else {
                    Log.w(TAG, message(source, msg), e);
                }
            }
        } else if (level == Log.ERROR) {
            if (ERROR) {
                if (e == null) {
                    Log.e(TAG, message(source, msg));                    
                } else {
                    Log.e(TAG, message(source, msg), e);
                }
            }
        }
    }
    
    private static String message(String source, String msg) {
        StringBuilder sb = new StringBuilder(source);
        if (msg != null) {
            sb.append(": ");
            sb.append(msg);
            sb.append(" ");
        }
        return sb.toString();
    }
}
