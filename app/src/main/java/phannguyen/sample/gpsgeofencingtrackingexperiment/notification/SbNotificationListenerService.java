package phannguyen.sample.gpsgeofencingtrackingexperiment.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SbNotificationListenerService extends NotificationListenerService {
    public static String TAG = "Notifications";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SbLog.i(TAG,"ID:" + sbn.getId());
        SbLog.i(TAG,"Posted by:" + sbn.getPackageName());

        FileLogs.writeLog(getApplicationContext(), TAG, "I","Notification Posted by:" + sbn.getPackageName());
        FileLogs.writeLog(getApplicationContext(), TAG, "I","Notification ID:" + sbn.getId());
        FileLogs.writeLog(getApplicationContext(), TAG, "I","Notification Title:"+ sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
        FileLogs.writeLog(getApplicationContext(), TAG, "I","Notification Title Big:"+ sbn.getNotification().extras.getString(Notification.EXTRA_TITLE_BIG));
        FileLogs.writeLog(getApplicationContext(), TAG, "I","Notification Content:"+ sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (NotiHelper.isAppInvolvedInSystemNotification(getApplicationContext(), sbn.getNotification())) {
                FileLogs.writeLog(getApplicationContext(), TAG, "I","***This notification about the app, so cancel it");
                cancelNotification(sbn.getKey());
            }
        }
        FileLogs.writeLog(getApplicationContext(), TAG, "I","###");
    }
}
