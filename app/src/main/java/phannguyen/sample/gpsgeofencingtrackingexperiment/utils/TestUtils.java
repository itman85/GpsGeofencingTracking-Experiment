package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;

public class TestUtils {

    public static void startLocationTrackingService(Context context) {
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("action", "START");
        ServiceHelper.startLocationRequestUpdateService(context, bundle);
    }

    public static void stopLocationTrackingService(Context context) {
        ServiceHelper.stopLocationRequestUpdateService(context);
    }

    public static long getBatteryCapacity(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager mBatteryManager = (BatteryManager) ctx.getSystemService(Context.BATTERY_SERVICE);
            //Long chargeCounter = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
            Long capacity = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            /*if (chargeCounter != null && capacity != null) {
                long value = (long) (((float) chargeCounter / (float) capacity) * 100f);
                return value;
            }*/
            return capacity;
        }

        return 0;
    }

}
