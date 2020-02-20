package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.storage.SharedPreferencesHandler;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_RESULT;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_SOURCE;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.DETECT_LOCATION_ACCURACY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.FASTEST_INTERVAL;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.STAY_DISTANCE_IN_MET;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.UPDATE_INTERVAL;

public class LocationRequestUpdateServiceOreo extends JobIntentService implements LocationListener {
    private static final int JOB_ID = 1008;

    private static final String TAG = "LocationRequestUpdateSrvOreo";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    //private Handler mServiceHandler;
    private HandlerThread mWorkerThread;
    private boolean isStartTracking;
    //public static LocationManager locationManager;
    private String locationProvider;

    private static CountDownLatch mLatch;

    private int serviceRunCount;// this help to prevent onHandleWork call multiple time while it running or enqueued


    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        SbLog.i(TAG,"Enqueue Location request update service");
        enqueueWork(context, LocationRequestUpdateServiceOreo.class, JOB_ID, intent);// let task in queue first before release await from previous task in order it wont re-create service
        if(intent!=null && intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))) {
            SbLog.i(TAG,"Release pending current location request update service");
            if(mLatch!=null)
                mLatch.countDown();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        SbLog.i(TAG,"Location Request Update Service initService");
        FileLogs.writeLog(this,TAG,"I","Location Request Update Service initService");
        FileLogs.writeLogByDate(this,TAG,"I","Location Request Update Service initService");

        serviceRunCount = 0;
        mLatch = new CountDownLatch(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();
        //mServiceHandler = new Handler(handlerThread.getLooper());
        isStartTracking = false;

        /*locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)? LocationManager.GPS_PROVIDER:
                LocationManager.NETWORK_PROVIDER;*/
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        serviceRunCount++;
        SbLog.i(TAG,"Location Request Update Service handleStartCommand @"+serviceRunCount);
        FileLogs.writeLog(this,TAG,"I","*** Location Request Update Service handleStartCommand");
        FileLogs.writeLogByDate(this,TAG,"I","*** Location Request Update Service handleStartCommand");
        boolean isOnTracking = false;
        //for case this service is killed by OS, it will restart with null intent, then request location update again
        if(intent==null){
            // check if still on tracking from previous task, so continue tracking on this task
            isOnTracking = SharedPreferencesHandler.getLocationRequestUpdateStatus(this);
            if(isOnTracking){
                isStartTracking  = false;//restart request update location incase this service killed by OS
                FileLogs.writeLog(this,TAG,"I","Service restart Null Intent,Still on tracking so restart update location");
            }else{
                //no location request update live now, so kill this service
                FileLogs.writeLog(this,TAG,"I","Service restart Null Intent,Not on tracking so Stop now");
                return;//stop service
            }
        }

        if(isOnTracking || (intent!=null && intent.hasExtra("action") && "START".equals(intent.getStringExtra("action")))) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Log.e(TAG, "No location permission granted");
                FileLogs.writeLog(this,TAG,"E","No location permission granted");
            } else {
                //Log.i(TAG, "Request location update");
                if(!isStartTracking) {
                    FileLogs.writeLog(this,TAG,"I","Start Request location update now");
                    FileLogs.writeLogByDate(this,TAG,"I","Start Request location update now");
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, mWorkerThread.getLooper()); // todo see if handle update in other thread?
                    //locationManager.requestLocationUpdates(locationProvider,INTERVAL_SLOW_MOVE_IN_MS,STAY_DISTANCE_IN_MET,this,mWorkerThread.getLooper()); // todo see if handle update in other thread?
                    isStartTracking = true;
                    SharedPreferencesHandler.setLocationRequestUpdateStatus(this, true);
                }
            }
        }else if(intent!=null && intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))){
            SbLog.i(TAG, "*** Remove Request location update");
            FileLogs.writeLog(this,TAG,"I","*** Remove Request location update,STOP update location now");
            FileLogs.writeLogByDate(this,TAG,"I","*** Remove Request location update,STOP update location now");
            removeLocationRequestUpdate();
            isStartTracking = false;
            return;
        }
        //onHandleWork called multiple times so from second time will exit
        if(serviceRunCount>1)
            return;
        try {
            mLatch.await();// keep first task live long util STOP signal come
        } catch (InterruptedException e) {
            SbLog.e(TAG,e);
        }
        SbLog.i(TAG,"Finish handle Job @"+serviceRunCount);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SbLog.i(TAG," Location Request Update Service Destroy");
        FileLogs.writeLog(this,TAG,"I","*** Location Request Update Service Destroy");
        FileLogs.writeLogByDate(this,TAG,"I","*** Location Request Update Service Destroy");
        SharedPreferencesHandler.setLocationRequestUpdateStatus(this, false);
        removeLocationRequestUpdate();
        //locationManager = null;
        mLatch = null;
    }


    @SuppressLint("MissingPermission")
    private void removeLocationRequestUpdate(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        //locationManager.removeUpdates(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerThread.quitSafely();
        }else
            mWorkerThread.quit();
    }

    private void onNewLocation(Location location) {
        //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
        if (location != null && location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
            CoreTrackingJobService.updateLastLocation(this,(float) location.getLatitude(),(float) location.getLatitude(),true);
            //let core tracking service process this location data
            Map<String,Object> bundle = new HashMap<>();
            bundle.put(BUNDLE_EXTRA_LOCATION_RESULT, location);
            bundle.put(BUNDLE_EXTRA_LOCATION_SOURCE, "Fused");
            ServiceHelper.startCoreLocationTrackingJobService(this,bundle);
            // use the Location
            //Log.i(TAG,"***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLog(this,TAG,"I","Last Fused location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLogByDate(this,TAG,"I","Last Fused location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            SbLog.i(TAG,"Last Fused location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
        }else{
            FileLogs.writeLog(this,TAG,"I","Fused Location accuracy larger than "+DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLogByDate(this,TAG,"I","Fused Location accuracy larger than "+DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            SbLog.i(TAG,"Fused Location accuracy larger than "+DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
        }

    }

    ////////////////////////////////////////////
    // for location manager
    @Override
    public void onLocationChanged(Location location) {
        //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
        if (location != null && location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
            //let core tracking service process this location data
            Map<String,Object> bundle = new HashMap<>();
            bundle.put(BUNDLE_EXTRA_LOCATION_RESULT, location);
            bundle.put(BUNDLE_EXTRA_LOCATION_SOURCE, locationProvider);
            ServiceHelper.startCoreLocationTrackingJobService(this,bundle);
            // use the Location
            //Log.i(TAG,"***Last location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLog(this,TAG,"I","Last "+ locationProvider +" Provider location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLogByDate(this,TAG,"I","Last "+ locationProvider +" Provider location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            SbLog.i(TAG,"Last "+ locationProvider +" Provider location is Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
        }else{
            FileLogs.writeLog(this,TAG,"I",locationProvider + " Provider Location accuracy larger than "+ DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            FileLogs.writeLogByDate(this,TAG,"I",locationProvider + " Provider Location accuracy larger than "+ DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
            SbLog.i(TAG,locationProvider + " Provider Location accuracy larger than "+ DETECT_LOCATION_ACCURACY + " Lat = "+location.getLatitude() + " - Lng= "+location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
