package phannguyen.sample.gpsgeofencingtrackingexperiment.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;

public class LocationTrackingIntervalWorker extends Worker {
    private static final String TAG = "5.LocationIntervalWorker";
    public static final String KEY_RESULT = "locationResult";

    public LocationTrackingIntervalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        //Log.i(TAG,"Location Tracking Interval Worker Trigger");
        FileLogs.appendLog(this.getApplicationContext(),TAG,"I","App-Location Tracking Interval Worker Trigger");
        ServiceHelper.startCoreLocationTrackingJobService(getApplicationContext(),null);
        Data output = new Data.Builder()
                .putBoolean(KEY_RESULT, true)
                .build();
        return Result.success(output);
    }
}

