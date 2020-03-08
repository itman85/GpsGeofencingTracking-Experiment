package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import phannguyen.sample.gpsgeofencingtrackingexperiment.BuildConfig;

public class Constant {
    public static  final String ACTIVITY_FENCE_KEY = "activity_fence_key";

    public static  final String FAST_ACTIVITY_FENCE_KEY = "fast_activity_fence_key";

    public static  final String SLOW_ACTIVITY_FENCE_KEY = "slow_activity_fence_key";

    public static  final String NOT_MOVE_ACTIVITY_FENCE_KEY = "not_moving_activity_fence_key";

    public static final String REGISTER_ACTIVTY_WORKER_TAG  = "register_activity_worker_tag";

    public static final String REGISTER_USER_ACTIVITY_INTERVAL_WORKER_UNIQUE_NAME  = "register_activity_interval_worker_unique_name";

    public static final String ACTIVITY_SIGNAL_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".ACTIVITY_SIGNAL_RECEIVER_ACTION";

    public static final String SIGNAL_KEY  = "signal";

    public static long UPDATE_INTERVAL = 120 * 1000;  /* 120 secs */

    public static long FASTEST_INTERVAL = 90* 1000; /* 90 sec */

    public static final int INTERVAL_MOVE_IN_MS = 5*60*1000;//5mins

    public static final int INTERVAL_WALK_IN_MS = 5*60*1000;//5mins

    public static final int INTERVAL_VERY_SLOW_MOVE_IN_MS = 120*1000;//2mins

    public static final int INTERVAL_SLOW_MOVE_IN_MS = 180*1000;//3min

    public static final int INTERVAL_STAY_LONG_IN_MS = 15*60*1000;//15min

    public static final int INTERVAL_STAY_SHORT_IN_MS = 5*60*1000;//5min

    public static final int STAY_DISTANCE_IN_MET = 50;//in this distance, device consider as not move

    public static final int FAR_DISTANCE_IN_MET = 200;//in this distance, device consider as move far

    public static final int DETECT_LOCATION_ACCURACY = 100;//in metter mean that the distance bwt of real location and result location <= 100m

    public static long TIMEOUT_STAY_LOCATION = 5*60*1000;  /* 5 MINS USER stay a location in 5 mins consider as STILL*/

    public static long UPDATE_STILL_TIME = 60*60*1000;  /* 60 MINS user STILL then update location*/

    public static long LONG_STAY_AROUND_TIME = 30*60*1000;  /* 30 MINS user stay and move around a place (house, workplace,...)*/

    public static long SHORT_STAY_AROUND_TIME = 5*60*1000;  /* 5 MINS user stay and move around a place (house, workplace,...)*/

    public static final int STILL_CONFIDENCE = 80;

    public static final int ONFOOT_CONFIDENCE = 90;

    public static final int FAST_MOVE_CONFIDENCE = 80;

    public static final int INTERVAL_CHECK_STILL_IN_MS = 3*60*1000;//3mins

    public static final int TIME_PERIOD_TO_CHECK_REGISTER_ACTIVITY_IN_SECOND = 12*60*60; // 12h

    public static final String BUNDLE_EXTRA_LOCATION_RESULT = "com.google.android.gms.location.EXTRA_LOCATION_RESULT";

    public static final String BUNDLE_EXTRA_LOCATION_SOURCE = "EXTRA_LOCATION_SOURCE";

    public static final String LOCATION_TRACKING_INTERVAL_WORKER_TAG  = "location_tracking_interval_worker_tag";

    public static final String ACTIVITY_TRACKING_INTERVAL_WORKER_TAG  = "activity_tracking_interval_worker_tag";

    public static final String LOCATION_TRACKING_INTERVAL_WORKER_UNIQUE_NAME  = "location_tracking_interval_worker_unique_name";

    public static final String ACTIVITY_TRACKING_INTERVAL_WORKER_UNIQUE_NAME  = "activity_tracking_interval_worker_unique_name";

    public static final String LOCATION_RESULT_TAG = "LocationResult";
    public static final String ERROR_TAG = "Error";

    public enum SIGNAL {
        MOVE("MOVE");

        private final String text;
        /**
         * @param text
         */
        SIGNAL(final String text) {
            this.text = text;
        }
        @Override
        public String toString() {
            return text;
        }
    }

    public enum LOCATION_CHANGE{
        STAYLONG,STAYSHORT,MOVINGSLOW,MOVING
    }
}
