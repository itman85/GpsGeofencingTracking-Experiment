package phannguyen.sample.gpsgeofencingtrackingexperiment.helper;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.worker.LocationTrackingIntervalWorker;
import phannguyen.sample.gpsgeofencingtrackingexperiment.worker.RegisterActivityFenceSignalWorker;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.LOCATION_TRACKING_INTERVAL_WORKER_TAG;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.LOCATION_TRACKING_INTERVAL_WORKER_UNIQUE_NAME;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.REGISTER_ACTIVTY_WORKER_TAG;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.REGISTER_USER_ACTIVITY_INTERVAL_WORKER_UNIQUE_NAME;

public class WorkManagerHelper {

    public static void startOneTimeRegisterUserActivityForTrackingLocationWorker(Context context,int workPolicyVal, int intervalTimeInSecond){
        ExistingWorkPolicy workPolicy = ExistingWorkPolicy.REPLACE;//replace by new request
        switch (workPolicyVal){
            case 0:
                workPolicy = ExistingWorkPolicy.REPLACE;
                break;
            case 1:
                workPolicy = ExistingWorkPolicy.KEEP;
                break;
            default:
                break;
        }
        OneTimeWorkRequest registerActivityWork=
                new OneTimeWorkRequest.Builder(RegisterActivityFenceSignalWorker.class)
                        .setInitialDelay(intervalTimeInSecond, TimeUnit.SECONDS)
                        .addTag(REGISTER_ACTIVTY_WORKER_TAG)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();
        WorkManager.getInstance(context).enqueueUniqueWork(REGISTER_USER_ACTIVITY_INTERVAL_WORKER_UNIQUE_NAME,workPolicy,registerActivityWork);
    }

    public static void startLocationTriggerWorkerOnetimeRequest(Context context,int delayInSecond, int workPolicyVal){
        ExistingWorkPolicy workPolicy = ExistingWorkPolicy.REPLACE;//replace by new request
        switch (workPolicyVal){
            case 0:
                workPolicy = ExistingWorkPolicy.REPLACE;
                break;
            case 1:
                workPolicy = ExistingWorkPolicy.KEEP;
                break;
            case 2:
                workPolicy = ExistingWorkPolicy.APPEND;
                break;
        }
        OneTimeWorkRequest locationIntervalWork=
                new OneTimeWorkRequest.Builder(LocationTrackingIntervalWorker.class)
                        .setInitialDelay(delayInSecond, TimeUnit.SECONDS)
                        .addTag(LOCATION_TRACKING_INTERVAL_WORKER_TAG)// Use this when you want to add initial delay or schedule initial work to `OneTimeWorkRequest` e.g. setInitialDelay(2, TimeUnit.HOURS)
                        .build();
        WorkManager.getInstance(context).enqueueUniqueWork(LOCATION_TRACKING_INTERVAL_WORKER_UNIQUE_NAME,workPolicy,locationIntervalWork);
    }

    public static void cancelLocationTriggerWorkerOnetimeRequest(Context context){
        FileLogs.writeLog(context,"MainAppProcessCallbackImpl","I","4-App-Invoke method cancelLocationTriggerWorkerOnetimeRequest");
        WorkManager.getInstance(context).cancelUniqueWork(LOCATION_TRACKING_INTERVAL_WORKER_UNIQUE_NAME);
    }
}
