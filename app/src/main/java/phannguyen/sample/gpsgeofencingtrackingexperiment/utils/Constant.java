package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import phannguyen.sample.gpsgeofencingtrackingexperiment.BuildConfig;

public class Constant {
    public static  final String ACTIVITY_FENCE_KEY = "activity_fence_key";

    public static final String REGISTER_ACTIVTY_WORKER_TAG  = "register_activity_worker_tag";

    public static final String REGISTER_USER_ACTIVITY_INTERVAL_WORKER_UNIQUE_NAME  = "register_activity_interval_worker_unique_name";

    public static final String ACTIVITY_SIGNAL_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".ACTIVITY_SIGNAL_RECEIVER_ACTION";

    public static final String SIGNAL_KEY  = "signal";

    public static long UPDATE_INTERVAL = 60 * 1000;  /* 60 secs */

    public static long FASTEST_INTERVAL = 30* 1000; /* 30 sec */

    public static final int INTERVAL_MOVE_IN_MS = 5*60*1000;//5mins

    public static final int INTERVAL_VERY_SLOW_MOVE_IN_MS = 120*1000;//2mins

    public static final int INTERVAL_SLOW_MOVE_IN_MS = 180*1000;//3min

    public static final int STAY_DISTANCE_IN_MET = 30;//in this distance, device consider as not move

    public static final int DETECT_LOCATION_ACCURACY = 100;//in metter mean that the distance bwt of real location and result location <= 100m

    public static long TIMEOUT_STAY_LOCATION = 5*60*1000;  /* 5 MINS USER stay a location in 5 mins consider as STILL*/

    public static long UPDATE_STILL_TIME = 60*60*1000;  /* 60 MINS user STILL then update location*/

    public static final int STILL_CONFIDENCE = 90;

    public static final int INTERVAL_CHECK_STILL_IN_MS = 3*60*1000;//3mins

    public static final String BUNDLE_EXTRA_LOCATION_RESULT = "com.google.android.gms.location.EXTRA_LOCATION_RESULT";

    public static final String BUNDLE_EXTRA_LOCATION_SOURCE = "EXTRA_LOCATION_SOURCE";

    public static final String LOCATION_TRACKING_INTERVAL_WORKER_TAG  = "location_tracking_interval_worker_tag";

    public static final String LOCATION_TRACKING_INTERVAL_WORKER_UNIQUE_NAME  = "location_tracking_interval_worker_unique_name";

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
}
