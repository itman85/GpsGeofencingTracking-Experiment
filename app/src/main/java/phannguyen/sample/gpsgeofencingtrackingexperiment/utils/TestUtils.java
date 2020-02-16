package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;

public class TestUtils {

    public static void startLocationTrackingService(Context context) {
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("action", "START");
        ServiceHelper.startLocationRequestUpdateService(context, bundle);
    }
}
