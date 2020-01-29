package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import android.util.Log;

public class SbLog {
    private static final boolean IS_SHOW_LOG_FOR_DEBUG = true;

    public static void d(String tag, String message){
        log(Log.DEBUG,tag,message);
    }

    public static void e(String tag, String message){
        log(Log.ERROR,tag,message);
    }

    public static void e(String tag,Throwable throwable){
        log(Log.ERROR,tag,Log.getStackTraceString(throwable));
    }

    public static void i(String tag, String message){
        log(Log.INFO,tag,message);
    }

    public static void v(String tag, String message){
        log(Log.VERBOSE,tag,message);
    }

    public static void w(String tag, String message){
        log(Log.WARN,tag,message);
    }

    public static void wtf(String tag, String message){
        log(Log.ASSERT,tag,message);
    }

    private static void log(int status, String TAG, String message) {
        if(IS_SHOW_LOG_FOR_DEBUG) {
            switch (status) {
                case Log.ASSERT:
                    Log.wtf(TAG, message);
                    break;
                case Log.DEBUG:
                    Log.d(TAG, message);
                    break;
                case Log.ERROR:
                    Log.e(TAG, message);
                    break;
                case Log.INFO:
                    Log.i(TAG, message);
                    break;
                case Log.VERBOSE:
                    Log.v(TAG, message);
                    break;
                case Log.WARN:
                    Log.w(TAG, message);
                    break;

                default:
                    break;
            }
        }

    }
}
