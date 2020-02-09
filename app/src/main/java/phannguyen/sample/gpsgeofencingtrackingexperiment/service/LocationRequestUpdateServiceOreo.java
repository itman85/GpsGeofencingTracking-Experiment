package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class LocationRequestUpdateServiceOreo extends JobIntentService {
    private static final int JOB_ID = 1008;

    private static final String TAG = "LocationRequestUpdateSrvOreo";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, LocationRequestUpdateServiceOreo.class, JOB_ID, intent);
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }
}
