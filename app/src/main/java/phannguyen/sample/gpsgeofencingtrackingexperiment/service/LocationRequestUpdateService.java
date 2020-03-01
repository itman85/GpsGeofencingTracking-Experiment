package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

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

public class LocationRequestUpdateService extends Service implements LocationListener {

    private static final String TAG = "LocationRequestUpdateService";


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    //private Handler mServiceHandler;
    private boolean isStartTracking;
    //public static LocationManager locationManager;
    private String locationProvider;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SbLog.i(TAG,"Location Request Update Service initService");
        FileLogs.writeLog(this,TAG,"I","Location Request Update Service initService");
        FileLogs.writeLogByDate(this,TAG,"I","Location Request Update Service initService");
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
        /*HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());*/
        isStartTracking = false;

        /*locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)? LocationManager.GPS_PROVIDER:
                LocationManager.NETWORK_PROVIDER;*/
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileLogs.writeLog(this,TAG,"I","*** Location Request Update Service handleStartCommand");
        FileLogs.writeLogByDate(this,TAG,"I","*** Location Request Update Service handleStartCommand");
        boolean isOnTracking = false;
        //for case this service is killed by OS, it will restart with null intent, then request location update again
        if(intent==null){
            isOnTracking = SharedPreferencesHandler.getLocationRequestUpdateStatus(this);
            if(isOnTracking){
                isStartTracking  = false;//restart request update location incase this service killed by OS
                FileLogs.writeLog(this,TAG,"I","Service restart Null Intent,Still on tracking so restart update location");
            }else{
                //no location request update live now, so kill this service
                FileLogs.writeLog(this,TAG,"I","Service restart Null Intent,Not on tracking so Stop now");
                stopSelf();//https://stackoverflow.com/questions/8279199/can-i-call-stopself-in-service-onstartcommand
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
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    //locationManager.requestLocationUpdates(locationProvider,INTERVAL_SLOW_MOVE_IN_MS,STAY_DISTANCE_IN_MET,this);
                    isStartTracking = true;
                    SharedPreferencesHandler.setLocationRequestUpdateStatus(this, true);
                }
            }
        }else if(intent!=null && intent.hasExtra("action") && "STOP".equals(intent.getStringExtra("action"))){
            //Log.e(TAG, "*** Remove Request location update");
            FileLogs.writeLog(this,TAG,"I","*** Remove Request location update,STOP update location now");
            FileLogs.writeLogByDate(this,TAG,"I","*** Remove Request location update,STOP update location now");
            removeLocationRequestUpdate();
            isStartTracking = false;
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileLogs.writeLog(this,TAG,"I","*** Location Request Update Service Destroy");
        FileLogs.writeLogByDate(this,TAG,"I","*** Location Request Update Service Destroy");
        SharedPreferencesHandler.setLocationRequestUpdateStatus(this, false);
        //mServiceHandler.removeCallbacksAndMessages(null);
        //locationManager = null;
    }

    @SuppressLint("MissingPermission")
    private void removeLocationRequestUpdate(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        //locationManager.removeUpdates(this);
    }

    private void onNewLocation(Location location) {
        //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
        if (location != null && location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
            CoreTrackingJobService.updateLastLocation(this,(float) location.getLatitude(),(float) location.getLongitude());
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
