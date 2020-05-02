package phannguyen.sample.gpsgeofencingtrackingexperiment.geofencing;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import phannguyen.sample.gpsgeofencingtrackingexperiment.models.GeoFencingPlaceModel;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.TestUtils;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.GEO_ID_PLIT_CHAR;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.KEY_ADD_NEW_LIST;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.KEY_REMOVE_LIST;

public class GeofencingRequestUpdateJobIntentService extends JobIntentService {

    private static final int JOB_ID = 574;

    private static final String TAG = "GeoFencingReqSv";

    private GeofencingClient geofencingClient;
    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofencingRequestUpdateJobIntentService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SbLog.i(TAG, "Geo fencing request service created");
        FileLogs.writeLog(this,TAG,"I","Geo fencing request service created");
        geofencingClient = LocationServices.getGeofencingClient(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if((intent==null) || (intent!=null && intent.hasExtra("action") && "START".equals(intent.getStringExtra("action")))) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                SbLog.e(TAG, "No permission for geo fencing");
                FileLogs.writeLog(this,TAG,"I","No permission for geo fencing");
                stopSelf();
            } else {
                //get add/remove from intent
                List<String> addNewGeoSetting = new ArrayList<>();
                List<String> removeGeoSetting = new ArrayList<>();
                extractGeoDataFromIntent(intent, addNewGeoSetting, removeGeoSetting);
                //1. call remove if there are list of remove points
                if (!removeGeoSetting.isEmpty()) {
                    SbLog.i(TAG, "Geo fencing request remove points now");
                    FileLogs.writeLog(this,TAG,"I","Geo fencing request remove points now");
                    removeGeofencingPoints(removeGeoSetting);
                }
                if (!addNewGeoSetting.isEmpty()) {
                    SbLog.i(TAG, "Geo fencing request add points now");
                    FileLogs.writeLog(this,TAG,"I","Geo fencing request add points now");
                    //2. call add geo fencing, will update if this request existing
                    geofencingClient.addGeofences(getGeofencingRequest(addNewGeoSetting), getGeofencePendingIntent())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    SbLog.i(TAG, "Geo fencing request register successfully");
                                    FileLogs.writeLog(this,TAG,"I","Geo fencing request register successfully");
                                    // geo fencing only need to register one time, so after register successfully stop service now
                                    //stop service
                                    stopSelf();
                                } else {
                                    SbLog.e(TAG, "Geo fencing request register fail");
                                    FileLogs.writeLog(this,TAG,"I","Geo fencing request register fail, so stop service now");
                                    stopSelf();
                                }
                            });
                }
            }
        }else if(intent!=null &&  intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))) {
            SbLog.e(TAG, "***Geo fencing request remove...");
            FileLogs.writeLog(this,TAG,"I","***Geo fencing request remove...");
            stopGeofencingMonitoring();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SbLog.i(TAG, "Geo fencing request service destroy");
        FileLogs.writeLog(this,TAG,"I","***Geo fencing request service destroy");
    }

    public void extractGeoDataFromIntent(Intent intent, List<String> addNewGeoSetting, List<String> removeGeoSetting) {
        addNewGeoSetting.add("ok");//test data
        if (intent!=null && intent.hasExtra(KEY_REMOVE_LIST)) {
            String tempRemove = intent.getStringExtra(KEY_REMOVE_LIST);
            String[] tempArray = tempRemove.split(GEO_ID_PLIT_CHAR);
            for (String value:tempArray){
                removeGeoSetting.add(value);
            }
        }
        if (intent!=null && intent.hasExtra(KEY_ADD_NEW_LIST)) {
            String tempAdd = intent.getStringExtra(KEY_ADD_NEW_LIST);
            String[] tempArray = tempAdd.split(GEO_ID_PLIT_CHAR);
            for (String value:tempArray){
                addNewGeoSetting.add(value);
            }
        }
    }
    private GeofencingRequest getGeofencingRequest(List<String> addedIdGeoPoints) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(createGeofenceObjectsList(addedIdGeoPoints));
        return builder.build();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    //Stopping geofence monitoring when it is no longer needed or desired can help save battery power and CPU cycles on the device
    private void stopGeofencingMonitoring(){
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SbLog.i(TAG,"Remove Geofencing Request successful");
                        FileLogs.writeLog(this,TAG,"I","Remove Geofencing Request successful");
                    }else{
                        SbLog.i(TAG,"Remove Geofencing Request Fail");
                        FileLogs.writeLog(this,TAG,"I","Remove Geofencing Request Fail");
                    }
                    stopSelf();
                });
    }

    //remove geo points which no longer need to monitor
    private void removeGeofencingPoints(List<String> pointsId){
        geofencingClient.removeGeofences(pointsId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SbLog.i(TAG,"Location alters have been removed");
                FileLogs.writeLog(this,TAG,"I","Location alters have been removed");
            }else{
                SbLog.i(TAG,"Location alters could not be removed");
                FileLogs.writeLog(this,TAG,"I","Location alters could not be removed");
            }
        });
    }


    private List<Geofence> createGeofenceObjectsList(List<String> addedIdGeoPoints){
        List<Geofence> geofencesList = new ArrayList<>();
        for(GeoFencingPlaceModel geoPlace: TestUtils.createListGeoFencingPlaces()){
            geofencesList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(geoPlace.getName())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            geoPlace.getLat(),
                            geoPlace.getLng(),
                            geoPlace.getRadius()
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
        return geofencesList;
    }
}
