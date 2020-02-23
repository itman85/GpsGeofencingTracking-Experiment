package phannguyen.sample.gpsgeofencingtrackingexperiment.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;

public class ActivityTrackingIntervalWorker extends Worker {
    private static final String TAG = "ActivityTrackingIntervalWorker";
    public static final String KEY_RESULT = "locationResult";

    public ActivityTrackingIntervalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        //Log.i(TAG,"Location Tracking Interval Worker Trigger");
        FileLogs.writeLog(this.getApplicationContext(),TAG,"I","Activity Tracking Interval Worker Trigger");
        FileLogs.writeLogByDate(this.getApplicationContext(),TAG,"I","Activity Tracking Interval Worker Trigger");
        ServiceHelper.startCoreActivityTrackingJobService(getApplicationContext(),null);
        Data output = new Data.Builder()
                .putBoolean(KEY_RESULT, true)
                .build();
        return Result.success(output);
    }
}
