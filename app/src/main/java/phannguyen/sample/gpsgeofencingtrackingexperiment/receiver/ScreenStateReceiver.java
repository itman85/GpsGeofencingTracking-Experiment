package phannguyen.sample.gpsgeofencingtrackingexperiment.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenStateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        SbLog.i(TAG,"Screen On/Off Change");
        //FileLogs.writeLog(context,TAG,"I","Screen On/Off Change");
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            SbLog.i(TAG, "Screen ON");
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            SbLog.i(TAG, "Screen OFF");
        }
    }
}
