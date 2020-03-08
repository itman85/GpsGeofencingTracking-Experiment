package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;

import java.util.Arrays;
import java.util.Date;

import phannguyen.sample.gpsgeofencingtrackingexperiment.MainActivity;
import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;

import static android.provider.Settings.System.DATE_FORMAT;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.FAST_ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.NOT_MOVE_ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.SLOW_ACTIVITY_FENCE_KEY;

public class ActivityFenceUtils {
    public static String TAG = "ActivityFenceUtils";
    /**
     * Will check activities that already registered are off or not, if off then register again
     * For testing, need to restart device to make sure android remove fence keys
     * @param context
     */
    public static void checkActivitiesOffThenRegisterAgain(Context context){
        Awareness.getFenceClient(context)
                .queryFences(FenceQueryRequest.forFences(Arrays.asList(FAST_ACTIVITY_FENCE_KEY,
                        NOT_MOVE_ACTIVITY_FENCE_KEY,
                        SLOW_ACTIVITY_FENCE_KEY)))
                .addOnSuccessListener(response -> {
                    FenceStateMap map = response.getFenceStateMap();
                    int count = 0;
                    for (String fenceKey : map.getFenceKeys()) {
                        SbLog.i(TAG,"Fencing activity is registered : "+fenceKey);
                        FileLogs.writeLog(context,TAG,"I","Fencing activity is registered : "+fenceKey);
                        FenceState fenceState = map.getFenceState(fenceKey);
                        SbLog.i(TAG, "Fence " + fenceKey + ": "
                                + fenceState.getCurrentState()
                                + ", was="
                                + fenceState.getPreviousState()
                                + ", lastUpdateTime="
                                + DATE_FORMAT.format(
                                String.valueOf(new Date(fenceState.getLastFenceUpdateTimeMillis()))));
                        if(fenceKey.equals(FAST_ACTIVITY_FENCE_KEY))
                            count++;
                        else if(fenceKey.equals(NOT_MOVE_ACTIVITY_FENCE_KEY))
                            count++;
                        else if(fenceKey.equals(SLOW_ACTIVITY_FENCE_KEY))
                            count++;
                    }
                    if(count!=3){
                        SbLog.i(TAG,"Fencing activities are missed, need to register again");
                        FileLogs.writeLog(context,TAG,"I","Fencing activities are missed, need to register again");
                        WorkManagerHelper.startOneTimeRegisterUserActivityForTrackingLocationWorker(context,0,5);
                    }else{
                        SbLog.i(TAG,"Fencing activities are registered. No need to register");
                        FileLogs.writeLog(context,TAG,"I","Fencing activities are registered. No need to register");
                    }
                })
                .addOnFailureListener(e -> {
                    SbLog.e(TAG,"Could not query fencing activities, so register again");
                    FileLogs.writeLog(context,TAG,"I","Could not query fencing activities, so register again");
                    WorkManagerHelper.startOneTimeRegisterUserActivityForTrackingLocationWorker(context,0,5);
                });
    }
}
