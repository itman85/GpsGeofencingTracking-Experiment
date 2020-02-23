package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

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

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.DETECT_LOCATION_ACCURACY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ERROR_TAG;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.FAR_DISTANCE_IN_MET;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_VERY_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.LOCATION_RESULT_TAG;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ONFOOT_CONFIDENCE;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.STILL_CONFIDENCE;

public class CoreDetectActivityJobService extends JobIntentService {

    private static final int JOB_ID = 1010;

    private static final String TAG = "CoreDetectActivityJobSrv";


    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        // if it already enqueued, so next one will be call onHandleWork as its turn and not call onCreate again.
        // so no matter how many enqueued, it only call onCreate and onDestroy one time and call onHandleWork multiple times
        // Destroy called only there no more task in queue or running
        enqueueWork(context, CoreDetectActivityJobService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SbLog.i(TAG,"Core Detect Activity Created");
        FileLogs.writeLog(this,TAG,"I","Create Core Detect Activity");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        SbLog.i(TAG,"CoreDetectActivity job service on handle");
        FileLogs.writeLog(this,TAG,"I","CoreDetectActivity job service on handle");
        FileLogs.writeLogByDate(this,TAG,"I","CoreDetectActivity job service on handle");
        // Quick get current location
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
                if(location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
                    FileLogs.writeLog(this,LOCATION_RESULT_TAG,"I","From Get Fused Last Location Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                    processLocationData(location);
                } else {
                    FileLogs.writeLog(this,LOCATION_RESULT_TAG, "I", "Location From Fused last location accuracy larger than " + DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
                    startAlarmActivityTrigger(INTERVAL_VERY_SLOW_MOVE_IN_MS,ExistingWorkPolicy.REPLACE.ordinal());
                }
            } else {
                startAlarmActivityTrigger(INTERVAL_VERY_SLOW_MOVE_IN_MS,ExistingWorkPolicy.REPLACE.ordinal());
            }
        }).addOnFailureListener(e -> {
            if (e != null) {
                SbLog.e(TAG, e.getMessage());
                FileLogs.writeLog(this,ERROR_TAG, "I", "service Error " + e.getMessage());
            }
            startAlarmActivityTrigger(INTERVAL_VERY_SLOW_MOVE_IN_MS,ExistingWorkPolicy.REPLACE.ordinal());
        });


    }

    private void startAlarmActivityTrigger(int delayInMs, int workPolicy){
        WorkManagerHelper.startActivityTriggerWorkerOnetimeRequest(this,delayInMs/1000, workPolicy);
    }

    public static void cancelActivityTriggerAlarm(Context context) {
        SbLog.i(TAG, "Cancel Activity Trigger Interval Worker");
        FileLogs.writeLog(context,TAG,"I","Cancel Activity Trigger worker");
        WorkManagerHelper.cancelActivityTriggerWorkerOnetimeRequest(context);
    }

    private void processLocationData(Location location){
        CoreTrackingJobService.updateLastLocation(this,(float)location.getLatitude(),(float)location.getLongitude(),false);
        //FileLogs.writeLog(this,"Result","D",location.getLatitude() + ","+location.getLongitude());
        Awareness.getSnapshotClient(this).getDetectedActivity()
                .addOnSuccessListener(dar -> {
                    ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                    // getMostProbableActivity() is good enough for basic Activity detection.
                    // To work within a threshold of confidence,
                    // use ActivityRecognitionResult.getProbableActivities() to get a list of
                    // potential current activities, and check the confidence of each one.
                    DetectedActivity probableActivity = arr.getMostProbableActivity();
                    int confidence = probableActivity.getConfidence();
                    String activityStr = probableActivity.toString();
                    SbLog.i(TAG,"Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    FileLogs.writeLog(this,TAG,"I","Get Snapshot Activity: " + activityStr
                            + ", Confidence: " + confidence + "/100");
                    //check if STILL now, so cancel tracking
                    if(probableActivity.getType() == DetectedActivity.ON_FOOT && confidence >= ONFOOT_CONFIDENCE){
                        //user on foot, so check if user move far from last location
                        if(checkIfUserMoveFar(location)){
                            //cancel activity trigger worker
                            cancelActivityTriggerAlarm(this);
                            SbLog.i(TAG,"User move far from last location,so start tracking now");
                            FileLogs.writeLog(this,TAG,"I","User move far from last location,so start tracking now");
                            FileLogs.writeLogByDate(this,TAG,"I","User move far from last location,so start tracking now");
                            ServiceHelper.startLocationRequestUpdateForegroundService(this,null);
                        }else{
                            FileLogs.writeLog(this,TAG,"I","User Probably moving around");
                            FileLogs.writeLogByDate(this,TAG,"I","User Probably moving around");
                            // todo think about solution increase delay from time to time
                            // this case probably user move around work play or house only
                            startAlarmActivityTrigger(10*60*1000, ExistingWorkPolicy.REPLACE.ordinal());
                        }

                    }else if(probableActivity.getType() == DetectedActivity.STILL && confidence >= STILL_CONFIDENCE){
                        //cancel activity trigger worker
                        cancelActivityTriggerAlarm(this);
                        SbLog.i(TAG,"User Still,so stop tracking now");
                        FileLogs.writeLog(this,TAG,"I","User STILL now, stop tracking");
                        FileLogs.writeLogByDate(this,TAG,"I","User STILL now, stop tracking");
                        ServiceHelper.stopLocationRequestUpdateForegroundService(this);
                    } else{
                        FileLogs.writeLog(this,TAG,"I","User NOT ON_FOOT now, keep tracking activity by interval worker trigger");
                        FileLogs.writeLogByDate(this,TAG,"I","User NOT ON_FOOT now, keep tracking activity by interval worker trigger");
                        //seem user not move, so track again after quite long time
                        startAlarmActivityTrigger(INTERVAL_SLOW_MOVE_IN_MS, ExistingWorkPolicy.REPLACE.ordinal());
                    }
                })

                .addOnFailureListener(e -> {
                    SbLog.e(TAG, "Could not detect activity: " + e);
                    FileLogs.writeLog(this,ERROR_TAG,"E","Get Snapshot could not detect activity: " + e.getMessage());
                });
    }

    /**
     * User on_foot but move far from last known location, so will track
     * @param location
     * @return
     */
    private boolean checkIfUserMoveFar(Location location){
        float lastLng = SharedPreferencesHandler.getLastLngLocation(this);
        float lastLat = SharedPreferencesHandler.getLastLatLocation(this);
        if(lastLat==0||lastLng==0){
            return true;
        }else{
            float distance = getMetersFromLatLong(lastLat,lastLng, (float) location.getLatitude(), (float) location.getLongitude());
            //seem not move
            if(distance <= FAR_DISTANCE_IN_MET){
                return false;
            }else{
                //user move far
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
}
