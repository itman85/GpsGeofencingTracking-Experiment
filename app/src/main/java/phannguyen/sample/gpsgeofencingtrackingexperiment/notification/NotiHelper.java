package phannguyen.sample.gpsgeofencingtrackingexperiment.notification;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import phannguyen.sample.gpsgeofencingtrackingexperiment.R;

public class NotiHelper {
    private static final int APP_NAME_LIMIT_LENGTH = 15;

    public static boolean isNotificationServiceEnabled(Context context){
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAppInvolvedInSystemNotification(Context context, Notification notification) {
        if (notification != null) {
            CharSequence tickerText = notification.tickerText;
            String appName = context.getString(R.string.app_name);
            if (appName.length() > APP_NAME_LIMIT_LENGTH) {
                appName = appName.substring(0, APP_NAME_LIMIT_LENGTH);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String title1 = null, title2 = null, content = null;
                Bundle extras = notification.extras;
                if (extras != null) {
                    title1 = getTextFromNotification(extras, Notification.EXTRA_TITLE);
                    title2 = getTextFromNotification(extras, Notification.EXTRA_TITLE_BIG);
                    content = getTextFromNotification(extras, Notification.EXTRA_TEXT);
                }
                return (title1 != null && isContainAppNameText(appName, title1)) ||
                        (title2 != null && isContainAppNameText(appName, title2)) ||
                        (content != null && isContainAppNameText(appName, content));
            } else {
                return tickerText != null && isContainAppNameText(appName, tickerText.toString());
            }
        }
        return false;
    }

    private static boolean isContainAppNameText(String appName, String content) {
        return content.toLowerCase().contains(appName.toLowerCase());
    }

    private static String getTextFromNotification(Bundle bundle, String key) {
        if (bundle.containsKey(key)) {
            CharSequence textFromSequence = bundle.getCharSequence(key);
            if (textFromSequence != null) {
                return textFromSequence.toString();
            } else {
                return bundle.getString(key);
            }
        }
        return null;
    }
}
