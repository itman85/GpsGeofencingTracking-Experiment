package phannguyen.sample.gpsgeofencingtrackingexperiment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.service.CoreTrackingJobService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ACTIVITY_SIGNAL_RECEIVER_ACTION;

public class ActivityFenceSignalReceiver extends BroadcastReceiver {
    private static final String TAG = "ActivitySignalRc";

    @Override
    public void onReceive(Context context, Intent intent) {
        SbLog.i(TAG,"Activity Fence onReceive");
        FileLogs.appendLog(context,TAG,"I","Activity Fence onReceive");
        if (!TextUtils.equals(ACTIVITY_SIGNAL_RECEIVER_ACTION, intent.getAction())) {
            SbLog.i(TAG,"Received an unsupported action in ActivityFenceSignalReceiver: action="
                    + intent.getAction());
            return;
        }

        // The state information for the given fence is em
        FenceState fenceState = FenceState.extract(intent);

        if (TextUtils.equals(fenceState.getFenceKey(), ACTIVITY_FENCE_KEY)) {
            String fenceStateStr;
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    fenceStateStr = "true";
                    stopLocationTrackingService(context);
                    break;
                case FenceState.FALSE:
                    startLocationTrackingService(context);
                    fenceStateStr = "false";
                    break;
                case FenceState.UNKNOWN:
                    fenceStateStr = "unknown";
                    stopLocationTrackingService(context);
                    break;
                default:
                    fenceStateStr = "unknown value";
                    stopLocationTrackingService(context);
            }
            //Log.i(TAG,"Fence state: " + fenceStateStr);
            //Utils.appendLog(TAG,"I","Fence state: " + fenceStateStr);
        }
    }

    private void startLocationTrackingService(Context context){
        Log.i(TAG,"User MOVE SIGNAL - Start Location Request Update Service");
        FileLogs.appendLog(context,TAG,"I","Remote - USER MOVE SIGNAL - Start Location&Geofencing Request Update Service");
        Map<String,Object> bundle = new HashMap<>();
        bundle.put("action", "START");
        ServiceHelper.startLocationRequestUpdateService(context,bundle);
    }

    private void stopLocationTrackingService(Context context){
        SbLog.i(TAG,"USER NOT MOVE SIGNAL");
        FileLogs.appendLog(context,TAG,"I","USER NOT MOVE SIGNAL");
        //let location tracking decide to stop tracking or not
        //LocationTrackingJobIntentService.cancelLocationTriggerAlarm(context);

    }
}
