package phannguyen.sample.gpsgeofencingtrackingexperiment.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHandler {

    private static final String PREF_NAME = "AppSharePrefs";

    public static void setLocationRequestUpdateStatus(Context context, boolean status){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("location_request_update", status);
        editor.apply();
    }

    public static boolean getLocationRequestUpdateStatus(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("location_request_update",false);
    }

    public static void setLastMomentGPSNotChange(Context context, long time){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("gpsmomentnotchange",time);
        editor.apply();
    }

    public static long getLastMomentGPSNotChange(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong("gpsmomentnotchange",0);
    }

    public static float getLastLngLocation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat("last_lng",0);
    }

    public static float getLastLatLocation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat("last_lat",0);
    }

    public static void setLastLngLocation(Context context, float lng){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("last_lng", lng);
        editor.apply();
    }

    public static void setLastLatLocation(Context context, float lat){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("last_lat", lat);
        editor.apply();
    }
}
