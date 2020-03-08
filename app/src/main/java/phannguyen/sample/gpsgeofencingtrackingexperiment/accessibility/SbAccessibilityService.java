package phannguyen.sample.gpsgeofencingtrackingexperiment.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import phannguyen.sample.gpsgeofencingtrackingexperiment.storage.SharedPreferencesHandler;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.ActivityFenceUtils;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.Constant;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.SbLog;

public class SbAccessibilityService extends AccessibilityService {
    private static final String TAG = "SbAccessibilityService";

    @Override
    public void onCreate() {
        super.onCreate();
        SbLog.i(TAG,"Created");
        FileLogs.writeLog(this,TAG,"I","Created");
    }

    // this run in bg thread
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event != null && event.getPackageName() != null) {
            SbLog.i(TAG,"Event type "+ eventTypeToString(event.getEventType()));
            SbLog.i(TAG,"Package "+ event.getPackageName());
            //FileLogs.writeLogInThread(this,TAG,"I","Event type "+ eventTypeToString(event.getEventType()));
            checkToRegisterFenceActivities(event.getEventType());
        }
    }

    @Override
    public void onInterrupt() {
        FileLogs.writeLog(this,TAG,"I","Interrupted");
        SbLog.i(TAG,"Interrupted");
    }

    @Override
    protected void onServiceConnected() {
        SbLog.i(TAG,"Connected");
        FileLogs.writeLog(this,TAG,"I","Connected");
        //super.onServiceConnected();
        // this same config in @xml/serviceconfig
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 1000;// todo this will limit bg delay?
        info.packageNames = null;
        setServiceInfo(info);
    }

    private String eventTypeToString(int eventType){
        switch (eventType){
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                return "TYPE WINDOWS CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE WINDOW CONTENT CHANGED";
            case AccessibilityEvent.WINDOWS_CHANGE_ACTIVE:
                return "WINDOWS CHANGE ACTIVE";
            case AccessibilityEvent.WINDOWS_CHANGE_BOUNDS:
                return "WINDOWS CHANGE BOUNDS";
            case AccessibilityEvent.WINDOWS_CHANGE_ADDED:
                return "WINDOWS CHANGE ADDED";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE VIEW SCROLLED";

            default:
                return "Other Type "+eventType;
        }
    }

    private void checkToRegisterFenceActivities(int eventType){
        // measure time read data from share prefs, this probably happen every often -> ~ 15ms 1st time, 0 - 5 ms from 2nd time
        if( eventType == AccessibilityEvent.WINDOWS_CHANGE_ADDED ) {
            FileLogs.writeLog(this,TAG,"I","Window added check last time checking service");
            //long s1 = System.currentTimeMillis();
            long lastTimeCheck = SharedPreferencesHandler.getLastTimeCheckRegisterActivity(this);
            //long s2 = System.currentTimeMillis();
            //SbLog.i(TAG,"Time to read shareprefs "+(s2-s1));
            if (System.currentTimeMillis() - lastTimeCheck > Constant.TIME_PERIOD_TO_CHECK_REGISTER_ACTIVITY_IN_SECOND * 1000) {
                FileLogs.writeLog(this,TAG,"I","Long time has not check register fencing activites, so check now");
                // update last time checking main service
                SharedPreferencesHandler.setLastTimeCheckRegisterActivity(this, System.currentTimeMillis());
                //
                ActivityFenceUtils.checkActivitiesOffThenRegisterAgain(this);
            }
        }
    }

}

