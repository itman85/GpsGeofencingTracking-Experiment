package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import android.content.Context;
import android.location.Location;

import phannguyen.sample.gpsgeofencingtrackingexperiment.storage.SharedPreferencesHandler;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.FAR_DISTANCE_IN_MET;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.LONG_STAY_AROUND_TIME;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.SHORT_STAY_AROUND_TIME;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.STAY_DISTANCE_IN_MET;

public class LocationUtils {
    /**
     *
     * @param context
     * @param lat
     * @param lng
     * @return 0: stay around for long time, 1: stay around for awhile, 2: move
     */
    public static Constant.LOCATION_CHANGE updateLastLocation(Context context, float lat, float lng){
        float lastLng = SharedPreferencesHandler.getLastLngLocation(context);
        float lastLat = SharedPreferencesHandler.getLastLatLocation(context);
        if(lastLat == 0 || lastLng == 0){
            SharedPreferencesHandler.setLastLatLocation(context, lat);
            SharedPreferencesHandler.setLastLngLocation(context, lng);
            SharedPreferencesHandler.setLastMomentGPSChange(context, System.currentTimeMillis());
            SharedPreferencesHandler.setFirstMomentStayAround(context,false);
            FileLogs.writeLog(context,"Result","I",lat + ","+lng);
            return Constant.LOCATION_CHANGE.MOVING;
        }
        float distance = getMetersFromLatLong(lastLat,lastLng, lat, lng);
        //seem not move
        if(distance <= STAY_DISTANCE_IN_MET){
            //SharedPreferencesHandler.setLastLatLocation(context, lat);
            //SharedPreferencesHandler.setLastLngLocation(context, lng);
            long lastStayMoment = SharedPreferencesHandler.getLastMomentGPSChange(context);
            //save first time moment of stay around
            if(!SharedPreferencesHandler.isFirstMomentStayAround(context)){
                // save last stay long location
                SharedPreferencesHandler.setLastStayLatLocation(context, lat);
                SharedPreferencesHandler.setLastStayLngLocation(context, lng);
                SharedPreferencesHandler.setFirstMomentStayAround(context,true);
                SharedPreferencesHandler.setLastMomentGPSChange(context, System.currentTimeMillis());
            }

            long time = System.currentTimeMillis() - lastStayMoment;
            // update last location if user stay or move around a place too long
            if(time >= LONG_STAY_AROUND_TIME){
                //SharedPreferencesHandler.setLastMomentGPSNotChange(context, System.currentTimeMillis());
                FileLogs.writeLog(context,"Result","I"," User Stay Around Long @"+ lat + ","+lng);
                return Constant.LOCATION_CHANGE.STAYLONG;
            }else if(time >= SHORT_STAY_AROUND_TIME) {
                FileLogs.writeLog(context,"Result","I"," User Stay Around Short @"+ lat + ","+lng);
                return Constant.LOCATION_CHANGE.STAYSHORT;
            } else{
                FileLogs.writeLog(context,"Result","I"," User Going to Stay Around Short @"+ lat + ","+lng);
                return Constant.LOCATION_CHANGE.MOVINGSLOW;
            }
        }else{
            SharedPreferencesHandler.setLastLatLocation(context, lat);
            SharedPreferencesHandler.setLastLngLocation(context, lng);
            SharedPreferencesHandler.setLastMomentGPSChange(context, System.currentTimeMillis());
            SharedPreferencesHandler.setFirstMomentStayAround(context,false);
            FileLogs.writeLog(context,"Result","I"," User Moving @"+lat + ","+lng);
            return Constant.LOCATION_CHANGE.MOVING;
        }
    }

    /**
     * User on_foot but move far from last known location, so will track
     * @param location
     * @return
     */
    public static boolean checkIfUserMoveFar(Context context,Location location){
        float lastStayLng = SharedPreferencesHandler.getLastStayLngLocation(context);
        float lastStayLat = SharedPreferencesHandler.getLastStayLatLocation(context);
        if(lastStayLng==0||lastStayLat==0){
            return true;
        }else{
            float distance = LocationUtils.getMetersFromLatLong(lastStayLat,lastStayLng, (float) location.getLatitude(), (float) location.getLongitude());
            //seem not move
            if(distance <= FAR_DISTANCE_IN_MET){
                return false;
            }else{
                //user move far
                return true;
            }
        }
    }

    public static float getMetersFromLatLong(float lat1, float lng1, float lat2, float lng2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }
}
