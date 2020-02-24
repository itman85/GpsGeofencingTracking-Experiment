package phannguyen.sample.gpsgeofencingtrackingexperiment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.work.ExistingWorkPolicy;

import com.google.android.gms.awareness.fence.FenceState;

import java.util.HashMap;
import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ACTIVITY_SIGNAL_RECEIVER_ACTION;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.FAST_ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_SLOW_MOVE_IN_MS;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.NOT_MOVE_ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.SLOW_ACTIVITY_FENCE_KEY;

public class ActivityFenceSignalReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivitySignalRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        SbLog.i(TAG, "Activity Fence onReceive");
        FileLogs.writeLog(context, TAG, "I", "Activity Fence Signal onReceive");
        FileLogs.writeLogByDate(context, TAG, "I", "Activity Fence Signal onReceive");
        if (!TextUtils.equals(ACTIVITY_SIGNAL_RECEIVER_ACTION, intent.getAction())) {
            SbLog.i(TAG, "Received an unsupported action in ActivityFenceSignalReceiver: action="
                    + intent.getAction());
            return;
        }

        // The state information for the given fence is em
        FenceState fenceState = FenceState.extract(intent);

        if (TextUtils.equals(fenceState.getFenceKey(), FAST_ACTIVITY_FENCE_KEY)) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    SbLog.i(TAG,"User Move Fast in Vehicle");
                    FileLogs.writeLog(context, TAG, "I", "User Move Fast in Vehicle");
                    // user move fast, so start tracking now
                    startLocationTrackingService(context);
                    break;
                default:
                    SbLog.i(TAG,"User Move Fast Unknown");
                    //FileLogs.writeLog(context, TAG, "I", "User Move Fast Unknown");
                    //stopLocationTrackingService(context);
            }
            //Log.i(TAG,"Fence state: " + fenceStateStr);
            //Utils.writeLog(TAG,"I","Fence state: " + fenceStateStr);
        }else if(TextUtils.equals(fenceState.getFenceKey(), SLOW_ACTIVITY_FENCE_KEY)){
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    SbLog.i(TAG,"User Slow Move");
                    FileLogs.writeLog(context, TAG, "I", "User Move Slow On Foot");
                    // user move slow on foot, so let check after delay time
                    WorkManagerHelper.startActivityTriggerWorkerOnetimeRequest(context,INTERVAL_SLOW_MOVE_IN_MS/1000, ExistingWorkPolicy.REPLACE.ordinal());
                    break;
                default:
                    SbLog.i(TAG,"User Move Slow Unknown");
                    //FileLogs.writeLog(context, TAG, "I", "User Move Slow Unknown");
                    //stopLocationTrackingService(context);
            }
        }else if(TextUtils.equals(fenceState.getFenceKey(), NOT_MOVE_ACTIVITY_FENCE_KEY)){
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    SbLog.i(TAG,"User Still");
                    FileLogs.writeLog(context, TAG, "I", "User Still");
                    // user not move, so check activity after delay
                    WorkManagerHelper.startActivityTriggerWorkerOnetimeRequest(context,INTERVAL_SLOW_MOVE_IN_MS/1000, ExistingWorkPolicy.REPLACE.ordinal());
                    break;
                default:
                    SbLog.i(TAG,"User Still Unknown");
                    //FileLogs.writeLog(context, TAG, "I", "User Still Unknown");
                    //stopLocationTrackingService(context);
            }
        }
    }

    private void startLocationTrackingService(Context context) {
        SbLog.i(TAG, "User MOVE FAST SIGNAL - Start Location Request Update Service");
        FileLogs.writeLog(context, TAG, "I", "USER MOVE FAST SIGNAL - Start Location&Geofencing Request Update Service");
        FileLogs.writeLogByDate(context, TAG, "I", "USER MOVE FAST SIGNAL - Start Location&Geofencing Request Update Service");
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("action", "START");
        ServiceHelper.startLocationRequestUpdateForegroundService(context, bundle);
    }

    private void stopLocationTrackingService(Context context) {
        SbLog.i(TAG, "USER NOT MOVE SIGNAL");
        FileLogs.writeLog(context, TAG, "I", "USER NOT MOVE SIGNAL");
        FileLogs.writeLogByDate(context, TAG, "I", "USER NOT MOVE SIGNAL");
        ServiceHelper.stopLocationRequestUpdateForegroundService(context);
        //let core location tracking decide to stop tracking or not
        //WorkManagerHelper.startLocationTriggerWorkerOnetimeRequest(context,INTERVAL_VERY_SLOW_MOVE_IN_MS/1000, ExistingWorkPolicy.REPLACE.ordinal());


    }
}
