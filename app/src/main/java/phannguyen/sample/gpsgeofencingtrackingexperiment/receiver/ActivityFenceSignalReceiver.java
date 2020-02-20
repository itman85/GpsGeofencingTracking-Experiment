package phannguyen.sample.gpsgeofencingtrackingexperiment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;

import com.google.android.gms.awareness.fence.FenceState;

import java.util.HashMap;
import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ACTIVITY_SIGNAL_RECEIVER_ACTION;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.INTERVAL_VERY_SLOW_MOVE_IN_MS;

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

        if (TextUtils.equals(fenceState.getFenceKey(), ACTIVITY_FENCE_KEY)) {
            switch (fenceState.getCurrentState()) {
                case FenceState.FALSE:
                    SbLog.i(TAG,"User Move");
                    // user move
                    startLocationTrackingService(context);
                    break;
                case FenceState.TRUE:
                    //user still
                    SbLog.i(TAG,"User Still");
                    stopLocationTrackingService(context);
                    //startLocationTrackingService(context);
                    break;
                default:
                    stopLocationTrackingService(context);
            }
            //Log.i(TAG,"Fence state: " + fenceStateStr);
            //Utils.writeLog(TAG,"I","Fence state: " + fenceStateStr);
        }
    }

    private void startLocationTrackingService(Context context) {
        SbLog.i(TAG, "User MOVE SIGNAL - Start Location Request Update Service");
        FileLogs.writeLog(context, TAG, "I", "USER MOVE SIGNAL - Start Location&Geofencing Request Update Service");
        FileLogs.writeLogByDate(context, TAG, "I", "USER MOVE SIGNAL - Start Location&Geofencing Request Update Service");
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("action", "START");
        ServiceHelper.startLocationRequestUpdateService(context, bundle);
    }

    private void stopLocationTrackingService(Context context) {
        SbLog.i(TAG, "USER NOT MOVE SIGNAL");
        FileLogs.writeLog(context, TAG, "I", "USER NOT MOVE SIGNAL");
        FileLogs.writeLogByDate(context, TAG, "I", "USER NOT MOVE SIGNAL");
        //let core location tracking decide to stop tracking or not
        WorkManagerHelper.startLocationTriggerWorkerOnetimeRequest(context,INTERVAL_VERY_SLOW_MOVE_IN_MS/1000, ExistingWorkPolicy.REPLACE.ordinal());


    }
}
