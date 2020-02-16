package phannguyen.sample.gpsgeofencingtrackingexperiment.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import phannguyen.sample.gpsgeofencingtrackingexperiment.receiver.ScreenStateReceiver;

// this service only for android 7-
public class RegisterTriggerService extends Service {
    boolean isRegistered;
    private ScreenStateReceiver mScreenReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mScreenReceiver = new ScreenStateReceiver();
        isRegistered = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isRegistered){
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mScreenReceiver, intentFilter);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenReceiver);
    }
}
