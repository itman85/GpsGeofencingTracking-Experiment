package phannguyen.sample.gpsgeofencingtrackingexperiment.helper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import phannguyen.sample.gpsgeofencingtrackingexperiment.receiver.ActivityFenceSignalReceiver;

import static phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant.ACTIVITY_SIGNAL_RECEIVER_ACTION;

public class PendingIntentHelper {

    public static PendingIntent getFenceAwareNessPendingIntent(Context context){
        Intent intent = new Intent(context, ActivityFenceSignalReceiver.class);
        intent.setAction(ACTIVITY_SIGNAL_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
