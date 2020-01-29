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

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.storage.SharedPreferencesHandler;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_RESULT;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_SOURCE;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.DETECT_LOCATION_ACCURACY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_CHECK_STILL_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_VERY_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.STAY_DISTANCE_IN_MET;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.STILL_CONFIDENCE;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.TIMEOUT_STAY_LOCATION;

public class CoreTrackingJobService extends JobIntentService {

    private static final int JOB_ID = 1009;

    private static final String TAG = "CoreTrackingJobSv";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, CoreTrackingJobService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        SbLog.i(TAG,"CoreTrackingLocation job service on handle");
        FileLogs.appendLog(this,TAG,"I","Remote-CoreTrackingLocation job service on handle");
        boolean res = handleLocationIntent(intent);
        if (!res) {
            FileLogs.appendLog(this,TAG,"I","Remote-CoreTrackingLocation get fused location now");
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
                    if(location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
                        FileLogs.appendLog(this,"LocationProcess","I","From Get Fused Last Location Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                        processLocationData(location);
                    } else {
                        FileLogs.appendLog(this,TAG, "I", "Remote - **** Location From Fused accuracy larger than " + DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                        startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
                    }
                } else {
                    startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
                }
            }).addOnFailureListener(e -> {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                    FileLogs.appendLog(this,TAG, "I", "Remote - service Error " + e.getMessage());
                }
                startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
            });

            //
            try {
                if (LocationRequestUpdateService.locationManager != null) {
                    String locationProvider = LocationRequestUpdateService.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ? LocationManager.GPS_PROVIDER :
                            LocationManager.NETWORK_PROVIDER;
                    FileLogs.appendLog(this,TAG, "I", "Remote-CoreTrackingLocation get provider location now");
                    @SuppressLint("MissingPermission")
                    Location location = LocationRequestUpdateService.locationManager.getLastKnownLocation(locationProvider);
                    if (location != null) {
                        //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
                        if (location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
                            FileLogs.appendLog(this,"LocationProcess", "I", "From " + locationProvider + " Provider Get Last Location Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                            processLocationData(location);
                        } else {
                            FileLogs.appendLog(this,TAG, "I", "Remote - **** From " + locationProvider + " Provider Location accuracy larger than " + DETECT_LOCATION_ACCURACY + " Lat = " + location.getLatitude() + " - Lng= " + location.getLongitude());
                            startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
                        }
                    } else {
                        startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
                    }
                } else {
                    FileLogs.appendLog(this,TAG, "I", "Remote-CoreTrackingLocation get provider location Fail");
                }
            }catch (Exception ex){
                FileLogs.appendLog(this,TAG, "I", "Remote-CoreTrackingLocation get provider location Error " + Log.getStackTraceString(ex));
            }

        }
    }

    private void startAlarmLocationTrigger(int delayInMs){
        WorkManagerHelper.startLocationTriggerWorkerOnetimeRequest(this,delayInMs/1000, ExistingWorkPolicy.KEEP.ordinal());
    }

    private static void cancelLocationTriggerAlarm(Context context) {
        Log.i(TAG, "Cancel Location Trigger Interval Worker");
        FileLogs.appendLog(context,TAG,"I","Cancel Location Trigger worker");
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
                FileLogs.appendLog(this,TAG,"I","Remote-CoreTrackingLocation receive location intent and process now");
                FileLogs.appendLog(this,"LocationProcess","I","From "+ intent.getStringExtra(BUNDLE_EXTRA_LOCATION_SOURCE)+" Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                processLocationData(location);
                return true;
            }
        }
        return false;
    }

    private void processLocationData(Location location){
        boolean isMove = checkUserLocationData(location);
        if(isMove){
            SbLog.i(TAG,"***User moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.appendLog(this,TAG,"I","Remote-***User moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            //store location info local db
            FileLogs.appendLog(this,"LocationProcess","I","Save Location At Moving Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            //store location
            //UtilsFn.saveLocation(location,this);
            //check if location request update still alive, then start alarm
            startAlarmLocationTrigger(INTERVAL_SLOW_MOVE_IN_MS);
        }else{
            long lastStayMoment = SharedPreferencesHandler.getLastMomentGPSNotChange(this);
            //check if user don't move for long time => user STILL
            if(System.currentTimeMillis() - lastStayMoment >= TIMEOUT_STAY_LOCATION){
                SbLog.i(TAG,"***User Stay for long time Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                FileLogs.appendLog(this,TAG,"I","Remote-***User stay for certain time Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                getSnapshotCurrentActivity(this,location);
            }else{
                Log.i(TAG,"***User Stay a bit Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                FileLogs.appendLog(this,TAG,"I","Remote-***User stay a bit Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                //check if location request update still alive, then start alarm
                startAlarmLocationTrigger(INTERVAL_VERY_SLOW_MOVE_IN_MS);
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
            updateLastLocation((float) location.getLatitude(),(float) location.getLongitude(),System.currentTimeMillis());
            return true;
        }else{
            float distance = getMetersFromLatLong(lastLat,lastLng, (float) location.getLatitude(), (float) location.getLongitude());
            //seem not move
            if(distance <= STAY_DISTANCE_IN_MET){
                return false;
            }else{
                //user move
                updateLastLocation((float) location.getLatitude(),(float) location.getLongitude(),System.currentTimeMillis());
                return true;
            }
        }
    }

    private float getMetersFromLatLong(float lat1, float lng1, float lat2, float lng2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lng1);
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lng2);
        float distanceInMeters = loc1.distanceTo(loc2);
        return distanceInMeters;
    }

    private void updateLastLocation(float lat,float lng,long time){
        SharedPreferencesHandler.setLastLatLocation(this, lat);
        SharedPreferencesHandler.setLastLngLocation(this, lng);
        SharedPreferencesHandler.setLastMomentGPSNotChange(this, time);
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
                    FileLogs.appendLog(context,TAG,"I","Remote-Get Snapshot Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    //check if STILL now, so cancel tracking
                    if(probableActivity.getType() == DetectedActivity.STILL && confidence >= STILL_CONFIDENCE){
                        //user still, so cancel tracking location alarm
                        FileLogs.appendLog(context,TAG,"I","Remote-User STILL now, Cancel location trigger interval worker");
                        //stop interval check location work
                        cancelLocationTriggerAlarm(context);
                        //stop request update location service
                        Map<String,Object> bundle = new HashMap<>();
                        bundle.put("action", "STOP");
                        ServiceHelper.startLocationRequestUpdateService(context,bundle);
                        //stop request update geo fencing service
                        //ModulePresenter.startGeofencingRequestUpdateService(context,bundle);
                        //
                        updateLastLocation((float) location.getLatitude(),(float) location.getLongitude(),System.currentTimeMillis());
                        //store location info local db
                        FileLogs.appendLog(context,"LocationProcess","I","Save Location At Still Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                        //save location
                        //UtilsFn.saveLocation(location,mContext);
                    }else{
                        FileLogs.appendLog(context,TAG,"I","Remote-User NOT STILL now, keep tracking location by interval worker trigger");
                        //seem user not move, so track again after quite long time
                        startAlarmLocationTrigger(INTERVAL_CHECK_STILL_IN_MS);
                    }
                })

                .addOnFailureListener(e -> {
                    SbLog.e(TAG, "Could not detect activity: " + e);
                    FileLogs.appendLog(context,TAG,"E","Remote-Get Snapshot could not detect activity: " + e);
                });
    }
}
