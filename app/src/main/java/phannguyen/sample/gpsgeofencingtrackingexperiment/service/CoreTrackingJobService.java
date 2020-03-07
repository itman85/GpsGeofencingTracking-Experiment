package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.work.ExistingWorkPolicy;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.storage.SharedPreferencesHandler;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_RESULT;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_SOURCE;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.DETECT_LOCATION_ACCURACY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ERROR_TAG;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_CHECK_STILL_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_VERY_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.LOCATION_RESULT_TAG;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.LONG_STAY_AROUND_TIME;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.SHORT_STAY_AROUND_TIME;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.STAY_DISTANCE_IN_MET;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.STILL_CONFIDENCE;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.TIMEOUT_STAY_LOCATION;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.UPDATE_STILL_TIME;

/**
 * UNUSED
 */
public class CoreTrackingJobService extends JobIntentService {

    private static final int JOB_ID = 1009;

    private static final String TAG = "CoreTrackingJobSv";


    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        // if it already enqueued, so next one will be call onHandleWork as its turn and not call onCreate again.
        // so no matter how many enqueued, it only call onCreate and onDestroy one time and call onHandleWork multiple times
        // Destroy called only there no more task in queue or running
        FileLogs.writeLog(context,TAG,"I","Enqueue Core Tracking Job");
        enqueueWork(context, CoreTrackingJobService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SbLog.i(TAG,"Core tracking Created");
        FileLogs.writeLog(this,TAG,"I","Create Core Tracking");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        SbLog.i(TAG,"CoreTrackingLocation job service on handle");
        FileLogs.writeLog(this,TAG,"I","CoreTrackingLocation job service on handle");
        FileLogs.writeLogByDate(this,TAG,"I","CoreTrackingLocation job service on handle");
        boolean res = handleLocationIntent(intent);
        if (!res) {
            FileLogs.writeLog(this,TAG,"I","CoreTrackingLocation get fused location now");
            FileLogs.writeLogByDate(this,TAG,"I","CoreTrackingLocation get fused location now");
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
                    if(location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
                        FileLogs.writeLog(this,LOCATION_RESULT_TAG,"I","From Get Fused Last Location Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                        processLocationData(location,true);
                    } else {
                        FileLogs.writeLog(this,LOCATION_RESULT_TAG, "I", "Location From Fused last location accuracy larger than " + DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                        startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS,ExistingWorkPolicy.KEEP.ordinal());
                    }
                } else {
                    startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS,ExistingWorkPolicy.KEEP.ordinal());
                }
            }).addOnFailureListener(e -> {
                if (e != null) {
                    SbLog.e(TAG, e.getMessage());
                    FileLogs.writeLog(this,ERROR_TAG, "I", "service Error " + e.getMessage());
                }
                startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS,ExistingWorkPolicy.KEEP.ordinal());
            });

            //
            /*try {
                if (LocationRequestUpdateService.locationManager != null) {
                    String locationProvider = LocationRequestUpdateService.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER :
                            LocationManager.NETWORK_PROVIDER;
                    FileLogs.writeLog(this,TAG, "I", "CoreTrackingLocation get provider "+locationProvider+" location now");
                    FileLogs.writeLogByDate(this,TAG, "I", "CoreTrackingLocation get provider "+locationProvider+" location now");
                    @SuppressLint("MissingPermission")
                    Location location = LocationRequestUpdateService.locationManager.getLastKnownLocation(locationProvider);
                    if (location != null) {
                        //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
                        if (location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
                            FileLogs.writeLog(this,LOCATION_RESULT_TAG, "I", "From " + locationProvider + " Provider Get Last Location Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                            processLocationData(location);
                        } else {
                            FileLogs.writeLog(this,LOCATION_RESULT_TAG, "I", "From " + locationProvider + " Provider Get Last Location accuracy larger than " + DETECT_LOCATION_ACCURACY + " Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                            startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
                        }
                    } else {
                        startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
                    }
                } else {
                    FileLogs.writeLog(this,TAG, "I", "CoreTrackingLocation get provider location Fail");
                }
            }catch (Exception ex){
                FileLogs.writeLog(this,ERROR_TAG, "I", "CoreTrackingLocation get provider location Error " + Log.getStackTraceString(ex));
            }*/

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SbLog.i(TAG,"Core tracking Destroyed");
        FileLogs.writeLog(this,TAG,"I","Destroy Core Tracking");
    }

    private void startAlarmLocationTrigger(int delayInMs,int workPolicy){
        WorkManagerHelper.startLocationTriggerWorkerOnetimeRequest(this,delayInMs/1000, workPolicy);
    }

    private static void cancelLocationTriggerAlarm(Context context) {
        Log.i(TAG, "Cancel Location Trigger Interval Worker");
        FileLogs.writeLog(context,TAG,"I","Cancel Location Trigger worker");
        WorkManagerHelper.cancelLocationTriggerWorkerOnetimeRequest(context);
    }

    /**
     *
     * @param intent
     * @return true force get location tracking, false: ignore
     */
    private boolean handleLocationIntent(Intent intent){
        if(intent.hasExtra(BUNDLE_EXTRA_LOCATION_RESULT)) {
            Location location = intent.getParcelableExtra(BUNDLE_EXTRA_LOCATION_RESULT);
            if (location != null) {
                FileLogs.writeLog(this,TAG,"I","CoreTrackingLocation receive location intent and process now");
                FileLogs.writeLog(this,LOCATION_RESULT_TAG,"I","*CoreTrackingLocation receive location intent From "+ intent.getStringExtra(BUNDLE_EXTRA_LOCATION_SOURCE)+" Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                processLocationData(location,false);
                return true;
            }
        }
        return false;
    }

    private void processLocationData(Location location,boolean isSave){
        boolean isMove = checkUserLocationData(location);
        if(isMove){
            SbLog.i(TAG,"***User moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLog(this,TAG,"I","***User moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            //store location info local db
            FileLogs.writeLog(this,LOCATION_RESULT_TAG,"I","Save Location At Moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLogByDate(this,LOCATION_RESULT_TAG,"I","Save Location At Moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            //store location
            if(isSave)
                updateLastLocation(this,(float) location.getLatitude(),(float) location.getLongitude());
            //UtilsFn.saveLocation(location,this);
            //check if location request update still alive, then start alarm
            startAlarmLocationTrigger(INTERVAL_MOVE_IN_MS,ExistingWorkPolicy.KEEP.ordinal());
        }else{
            long lastStayMoment = SharedPreferencesHandler.getLastMomentGPSChange(this);
            //check if user don't move for long time => user STILL
            if(System.currentTimeMillis() - lastStayMoment >= TIMEOUT_STAY_LOCATION){
                SbLog.i(TAG,"***User Stay for long time Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                FileLogs.writeLog(this,TAG,"I","***User stay for certain time Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                FileLogs.writeLogByDate(this,TAG,"I","***User stay for certain time Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                getSnapshotCurrentActivity(this,location);
            }else{
                Log.i(TAG,"***User Stay a bit Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                FileLogs.writeLog(this,TAG,"I","***User stay a bit Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                FileLogs.writeLogByDate(this,TAG,"I","***User stay a bit Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                //check if location request update still alive, then start alarm
                startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS,ExistingWorkPolicy.KEEP.ordinal());
            }
        }
    }
    /**
     *
     * @param location
     * @return true if user move, false if not move or move too slow
     */
    private boolean checkUserLocationData(Location location){
        float lastLng = SharedPreferencesHandler.getLastLngLocation(this);
        float lastLat = SharedPreferencesHandler.getLastLatLocation(this);
        if(lastLat==0||lastLng==0){
            return true;
        }else{
            float distance = getMetersFromLatLong(lastLat,lastLng, (float) location.getLatitude(), (float) location.getLongitude());
            //seem not move
            if(distance <= STAY_DISTANCE_IN_MET){
                return false;
            }else{
                //user move
                return true;
            }
        }
    }

    private static float getMetersFromLatLong(float lat1, float lng1, float lat2, float lng2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }

    /**
     *
     * @param context
     * @param lat
     * @param lng
     * @return 0: stay around for long time, 1: stay around for awhile, 2: move
     */
    private static Constant.LOCATION_CHANGE updateLastLocation(Context context, float lat, float lng){
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
                SharedPreferencesHandler.setLastLatLocation(context, lat);
                SharedPreferencesHandler.setLastLngLocation(context, lng);
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

    private void getSnapshotCurrentActivity(Context context, Location location){
        Awareness.getSnapshotClient(context).getDetectedActivity()
                .addOnSuccessListener(dar -> {
                    ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                    // getMostProbableActivity() is good enough for basic Activity detection.
                    // To work within a threshold of confidence,
                    // use ActivityRecognitionResult.getProbableActivities() to get a list of
                    // potential current activities, and check the confidence of each one.
                    DetectedActivity probableActivity = arr.getMostProbableActivity();
                    int confidence = probableActivity.getConfidence();
                    String activityStr = probableActivity.toString();
                    Log.i(TAG,"Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    FileLogs.writeLog(context,TAG,"I","Get Snapshot Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    //check if STILL now, so cancel tracking
                    if(probableActivity.getType() == DetectedActivity.STILL && confidence >= STILL_CONFIDENCE){
                        //user still, so cancel tracking location alarm
                        FileLogs.writeLog(context,TAG,"I","User STILL now, Cancel location trigger interval worker, Stop Request Update Location");
                        FileLogs.writeLogByDate(context,TAG,"I","User STILL now, Cancel location trigger interval worker, Stop Request Update Location");
                        //stop interval check location work
                        cancelLocationTriggerAlarm(context);
                        //stop request update location service
                        Map<String,Object> bundle = new HashMap<>();
                        bundle.put("action", "STOP");
                        ServiceHelper.startLocationRequestUpdateService(context,bundle);
                        //stop request update geo fencing service
                        //ModulePresenter.startGeofencingRequestUpdateService(context,bundle);
                        //
                        updateLastLocation(context,(float) location.getLatitude(),(float) location.getLongitude());
                        //store location info local db
                        FileLogs.writeLog(context,LOCATION_RESULT_TAG,"I","Save Location At Still Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                        FileLogs.writeLogByDate(context,LOCATION_RESULT_TAG,"I","Save Location At Still Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                        //save location
                        //UtilsFn.saveLocation(location,mContext);
                    }else{
                        FileLogs.writeLog(context,TAG,"I","User NOT STILL now, keep tracking location by interval worker trigger");
                        FileLogs.writeLogByDate(context,TAG,"I","User NOT STILL now, keep tracking location by interval worker trigger");
                        //seem user not move, so track again after quite long time
                        startAlarmLocationTrigger(INTERVAL_VERY_SLOW_MOVE_IN_MS,ExistingWorkPolicy.REPLACE.ordinal());
                    }
                })

                .addOnFailureListener(e -> {
                    SbLog.e(TAG, "Could not detect activity: " + e);
                    FileLogs.writeLog(context,ERROR_TAG,"E","Get Snapshot could not detect activity: " + e.getMessage());
                });
    }
}
