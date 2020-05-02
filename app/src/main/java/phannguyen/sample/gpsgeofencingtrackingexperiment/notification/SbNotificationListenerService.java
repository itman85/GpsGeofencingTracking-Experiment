package phannguyen.sample.gpsgeofencingtrackingexperiment.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;

import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SbNotificationListenerService extends NotificationListenerService {
    public static String TAG = "Notifications";
    public static String TAG1 = "NotificationMe";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SbLog.i(TAG,"ID:" + sbn.getId());
        SbLog.i(TAG,"Posted by:" + sbn.getPackageName());

        try {
            FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Posted by:" + sbn.getPackageName());
            FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification ID:" + sbn.getId());
            // todo java.lang.ClassCastException: android.text.SpannableString cannot be cast to java.lang.String
            FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Title:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TITLE));
            FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Title Big:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TITLE_BIG));
            FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Content:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TEXT));


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (NotiHelper.isAppInvolvedInSystemNotification(getApplicationContext(), sbn.getNotification())) {
                    FileLogs.writeLog(getApplicationContext(), TAG1, "I", "***This notification about the app, so cancel it");
                    FileLogs.writeLog(getApplicationContext(), TAG1, "I", "Notification Posted by:" + sbn.getPackageName());
                    FileLogs.writeLog(getApplicationContext(), TAG1, "I", "Notification ID:" + sbn.getId());
                    FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Title:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TITLE));
                    FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Title Big:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TITLE_BIG));
                    FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Content:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TEXT));

                    cancelNotification(sbn.getKey());
                } else if (sbn.getPackageName().equals("phannguyen.sample.gpsgeofencingtrackingexperiment")) {
                    FileLogs.writeLog(getApplicationContext(), TAG1, "I", "***This notification from this app, SORRY cannot cancel it");
                    FileLogs.writeLog(getApplicationContext(), TAG1, "I", "Notification Posted by:" + sbn.getPackageName());
                    FileLogs.writeLog(getApplicationContext(), TAG1, "I", "Notification ID:" + sbn.getId());
                    FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Title:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TITLE));
                    FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Title Big:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TITLE_BIG));
                    FileLogs.writeLog(getApplicationContext(), TAG, "I", "Notification Content:" + NotiHelper.getTextFromNotification(sbn.getNotification().extras, Notification.EXTRA_TEXT));

                }
            }
            FileLogs.writeLog(getApplicationContext(), TAG, "I", "###");
        }catch (Exception ex){
            FileLogs.writeLog(getApplicationContext(), TAG, "E", "Error " + Log.getStackTraceString(ex));
        }
    }
}
