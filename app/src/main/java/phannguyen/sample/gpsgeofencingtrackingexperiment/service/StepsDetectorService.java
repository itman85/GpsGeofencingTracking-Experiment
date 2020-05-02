package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

/**
 * https://developer.android.com/guide/topics/sensors/sensors_motion
 */
public class StepsDetectorService extends JobIntentService {

    private static final int JOB_ID = 1009;

    private static final String TAG = "StepsDetectorService";

    private SensorManager mSensorManager;

    private float initialStep = 0f;

    private static CountDownLatch mLatch;

    private int serviceRunCount;// this help to prevent onHandleWork call multiple time while it running or enqueued


    public static void enqueueWork(Context context, Intent intent) {
        SbLog.i(TAG,"Enqueue Location request update service");
        enqueueWork(context, StepsDetectorService.class, JOB_ID, intent);// let task in queue first before release await from previous task in order it wont re-create service

    }

    @Override
    public void onCreate() {
        super.onCreate();
        SbLog.i(TAG,"Create Steps Count service Job");
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLatch = new CountDownLatch(1);
        serviceRunCount = 0;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        serviceRunCount++;
        if(serviceRunCount>1){
            FileLogs.writeLog(StepsDetectorService.this, TAG, "I", "Steps Count service Job running, it busy now, so skip this handle");
            return;
        }
        SbLog.i(TAG,"Start handle Steps Count service Job");
        FileLogs.writeLog(StepsDetectorService.this, TAG, "I", "Start handle Steps Count service Job");
        Sensor stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.requestTriggerSensor(listener, stepCounter);
        // keep this service alive until step count reach threshold
        try {
            mLatch.await();// keep first task live long util STOP signal come
        } catch (InterruptedException e) {
            SbLog.e(TAG,e);
            FileLogs.writeLog(StepsDetectorService.this, TAG, "I", Log.getStackTraceString(e));
        }
        mLatch = null;
        SbLog.i(TAG,"Finish handle Steps Count service Job");
        FileLogs.writeLog(StepsDetectorService.this, TAG, "I", "Finish handle Steps Count service Job");

    }

    private TriggerEventListener listener = new TriggerEventListener(){
        @Override
        public void onTrigger(TriggerEvent event) {
            //handle step count here
            if(initialStep == 0f)
                initialStep = event.values[0];//Number of steps taken by the user since the last reboot while the sensor was activated.
            FileLogs.writeLog(StepsDetectorService.this, TAG, "I", "Steps Count so far = "+ event.values[0]);
            //check if user walk/run over 500steps
            if(event.values[0] - initialStep >= 50){
                //start tracking location
                FileLogs.writeLog(StepsDetectorService.this, TAG, "I", "USER Walking too long - Start Location&Geofencing Request Update Service");
                FileLogs.writeLogByDate(StepsDetectorService.this, TAG, "I", "USER Walking too long - Start Location&Geofencing Request Update Service");
                Map<String, Object> bundle = new HashMap<>();
                bundle.put("action", "START");
                ServiceHelper.startLocationRequestUpdateService(StepsDetectorService.this, bundle);
                if(mLatch!=null)
                    mLatch.countDown();
            }

        }
    };

    public static void cancelStepDetectorService(){
        if(mLatch!=null)
            mLatch.countDown();
    }
}
