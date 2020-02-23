package phannguyen.sample.gpsgeofencingtrackingexperiment.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResponse;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;
import java.util.Date;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.PendingIntentHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

import static android.provider.Settings.System.DATE_FORMAT;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.FAST_ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.NOT_MOVE_ACTIVITY_FENCE_KEY;
import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.SLOW_ACTIVITY_FENCE_KEY;

public class RegisterActivityFenceSignalWorker extends Worker {
    private static final String TAG = "RegisterActivityWorker";
    public static final String KEY_RESULT = "locationResult";
    private int count;
    public RegisterActivityFenceSignalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        count = 1;
    }

    @NonNull
    @Override
    public Result doWork() {
        SbLog.i(TAG,"Work time "+ count + " at " +System.currentTimeMillis());
        setupFences();
        Data output = new Data.Builder()
                .putInt(KEY_RESULT, count)
                .build();
        return Result.success(output);
        //return null;
    }

    private void setupFences() {
        // DetectedActivityFence will fire when it detects the user performing the specified
        // activity.  In this case it's walking.
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);

        AwarenessFence stayFence = DetectedActivityFence.during(DetectedActivityFence.STILL);

        AwarenessFence fastMovingFence = AwarenessFence.or(DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE),
                DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE));

        AwarenessFence slowMovingFence = DetectedActivityFence.during(DetectedActivityFence.ON_FOOT);

        AwarenessFence notMovingFence = AwarenessFence.not(AwarenessFence.and(DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE),
                DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE),DetectedActivityFence.during(DetectedActivityFence.ON_FOOT)));

        // There are lots of cases where it's handy for the device to know if headphones have been
        // plugged in or unplugged.  For instance, if a music app detected your headphones fell out
        // when you were in a library, it'd be pretty considerate of the app to pause itself before
        // the user got in trouble.
        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);

        // Combines multiple fences into a compound fence.  While the first two fences trigger
        // individually, this fence will only trigger its callback when all of its member fences
        // hit a true state.
        AwarenessFence walkingWithHeadphones = AwarenessFence.and(walkingFence, headphoneFence);

        // We can even nest compound fences.  Using both "and" and "or" compound fences, this
        // compound fence will determine when the user has headphones in and is engaging in at least
        // one form of exercise.
        // The below breaks down to "(headphones plugged in) AND (walking OR running OR bicycling)"
        AwarenessFence exercisingWithHeadphonesFence = AwarenessFence.and(
                headphoneFence,
                AwarenessFence.or(
                        walkingFence,
                        DetectedActivityFence.during(DetectedActivityFence.RUNNING),
                        DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE)));


        // Now that we have an interesting, complex condition, register the fence to receive
        // callbacks.
        SbLog.i(TAG, "Activity Fence now registered again.");
        FileLogs.writeLog(this.getApplicationContext(),TAG,"I","Activity Fence Register Now.");
        FileLogs.writeLogByDate(this.getApplicationContext(),TAG,"I","Activity Fence Register Now.");
        // Register the fence to receive callbacks.
        Awareness.getFenceClient(this.getApplicationContext()).updateFences(new FenceUpdateRequest.Builder()
                .addFence(FAST_ACTIVITY_FENCE_KEY, fastMovingFence, PendingIntentHelper.getFenceAwareNessPendingIntent(getApplicationContext()))
                .addFence(NOT_MOVE_ACTIVITY_FENCE_KEY, stayFence, PendingIntentHelper.getFenceAwareNessPendingIntent(getApplicationContext()))
                .addFence(SLOW_ACTIVITY_FENCE_KEY, slowMovingFence, PendingIntentHelper.getFenceAwareNessPendingIntent(getApplicationContext()))
                .build())
                .addOnSuccessListener(aVoid -> {
                    SbLog.i(TAG, "Activity Fence was successfully registered again.");
                    FileLogs.writeLog(this.getApplicationContext(),TAG,"I","Activity Fence was successfully registered again.");
                    FileLogs.writeLogByDate(this.getApplicationContext(),TAG,"I","Activity Fence was successfully registered again.");
                })
                .addOnFailureListener(e -> {
                    SbLog.e(TAG, "Activity Fence could not be registered again: " + e);
                    FileLogs.writeLog(this.getApplicationContext(),TAG,"E","Activity Fence could not be registered again: " + e);
                });
    }

    // query fence if not existed,so re-register
    protected void queryFence(final String fenceKey) {
        Awareness.getFenceClient(this.getApplicationContext())
                .queryFences(FenceQueryRequest.forFences(Arrays.asList(fenceKey)))
                .addOnSuccessListener(new OnSuccessListener<FenceQueryResponse>() {
                    @Override
                    public void onSuccess(FenceQueryResponse response) {
                        FenceStateMap map = response.getFenceStateMap();
                        for (String fenceKey : map.getFenceKeys()) {
                            FenceState fenceState = map.getFenceState(fenceKey);
                            Log.i(TAG, "Fence " + fenceKey + ": "
                                    + fenceState.getCurrentState()
                                    + ", was="
                                    + fenceState.getPreviousState()
                                    + ", lastUpdateTime="
                                    + DATE_FORMAT.format(
                                    String.valueOf(new Date(fenceState.getLastFenceUpdateTimeMillis()))));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Could not query fence: " + fenceKey);
                        return;
                    }
                });
    }
}
