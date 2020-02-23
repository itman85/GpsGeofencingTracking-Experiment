package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.MainActivity;
import phannguyen.sample.gpsgeofencingtrackingexperiment.R;
import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.storage.SharedPreferencesHandler;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_RESULT;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.BUNDLE_EXTRA_LOCATION_SOURCE;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.DETECT_LOCATION_ACCURACY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.FASTEST_INTERVAL;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.UPDATE_INTERVAL;

// For Android 8+
public class LocationRequestUpdateForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = "LocationRequestUpdateFgSrvOreo";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private HandlerThread mWorkerThread;
    private int serviceRunCount;// this help to prevent onHandleWork call multiple time while it running

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // This service created and destroyed one time, onStartCommand will called multiple times
        // If service running, next call start will call onStartCommand
        SbLog.i(TAG,"Location Request Update Service initService");
        FileLogs.writeLog(this,TAG,"I","Location Request Update Service initService");
        FileLogs.writeLogByDate(this,TAG,"I","Location Request Update Service initService");
        serviceRunCount = 0;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);// less drain battery
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceRunCount++;
        // Exit if service already running
        if(serviceRunCount>1)
            return START_STICKY;// re-create service (with null intent) if it get killed by OS
        SbLog.i(TAG,"Location Request Update Service handleStartCommand");
        FileLogs.writeLog(this,TAG,"I","*** Location Request Update Service handleStartCommand");
        FileLogs.writeLogByDate(this,TAG,"I","*** Location Request Update Service handleStartCommand");
        // todo think about fake or make sense notification
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SB Test Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        // must call startForeground after service created within 5s
        // https://stackoverflow.com/questions/44425584/context-startforegroundservice-did-not-then-call-service-startforeground
        startForeground(1, notification);// todo id?
        //do heavy work on a background thread
        // Cancel Tracking activity worker, because tracking location now no matter what user activity
        CoreDetectActivityJobService.cancelActivityTriggerAlarm(this);
        // request update location
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, mWorkerThread.getLooper());
        //stopSelf();
        return START_STICKY;// re-create service (with null intent) if it get killed by OS
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SbLog.i(TAG," Location Request Update Service Destroy");
        FileLogs.writeLog(this,TAG,"I","*** Location Request Update Service Destroy");
        FileLogs.writeLogByDate(this,TAG,"I","*** Location Request Update Service Destroy");
        SharedPreferencesHandler.setLocationRequestUpdateStatus(this, false);
        removeLocationRequestUpdate();
    }

    @SuppressLint("MissingPermission")
    private void removeLocationRequestUpdate(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerThread.quitSafely();
        }else
            mWorkerThread.quit();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void onNewLocation(Location location) {
        //only accept location with accuracy less than DETECT_LOCATION_ACCURACY
        if (location != null && location.getAccuracy() < DETECT_LOCATION_ACCURACY) {
            CoreTrackingJobService.updateLastLocation(this,(float) location.getLatitude(),(float) location.getLongitude(),true);
            //FileLogs.writeLog(this,"Result","I",location.getLatitude() + ","+location.getLongitude());
            //let core tracking service process this location data
            //Map<String,Object> bundle = new HashMap<>();
            //bundle.put(BUNDLE_EXTRA_LOCATION_RESULT, location);
            //bundle.put(BUNDLE_EXTRA_LOCATION_SOURCE, "Fused");
            //ServiceHelper.startCoreLocationTrackingJobService(this,bundle);
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



}
