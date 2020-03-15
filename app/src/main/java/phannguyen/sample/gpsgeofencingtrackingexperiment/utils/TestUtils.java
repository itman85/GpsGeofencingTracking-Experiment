package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.ServiceHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.models.GeoFencingPlaceModel;

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

    public static void startGeofencingTrackingService(Context context) {
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("action", "START");
        ServiceHelper.startGeofencingnRequestUpdateService(context, bundle);
    }

    public static List<GeoFencingPlaceModel> createListGeoFencingPlaces(){
        List<GeoFencingPlaceModel> geoList = new ArrayList<>();
        geoList.add(new GeoFencingPlaceModel(10.776677, 106.683699,100,"CMT8_DBP"));
        geoList.add(new GeoFencingPlaceModel(10.775034, 106.686850,100,"CMT8_NDC"));
        geoList.add(new GeoFencingPlaceModel(10.772703, 106.691175,200,"CMT8_BTX"));

        geoList.add(new GeoFencingPlaceModel(10.775911, 106.682737,100,"NTHien_DBPhu"));
        geoList.add(new GeoFencingPlaceModel(10.771550, 106.685651,50,"NTHhien_VVTan"));
        geoList.add(new GeoFencingPlaceModel(10.770002, 106.688607,100,"BTXuan_TTTung"));
        geoList.add(new GeoFencingPlaceModel(10.767928, 106.695467,200,"NTHoc_THDao"));
        geoList.add(new GeoFencingPlaceModel(10.759683, 106.698477,100,"HDieu_KHoi"));
        geoList.add(new GeoFencingPlaceModel(10.753596, 106.702088,200,"CauKTe"));
        geoList.add(new GeoFencingPlaceModel(10.745254, 106.701887,100,"NHTho_D15"));
        geoList.add(new GeoFencingPlaceModel(10.745970, 106.708287,100,"Home"));

        geoList.add(new GeoFencingPlaceModel(10.778449, 106.679971,200,"Workplace"));
        geoList.add(new GeoFencingPlaceModel(10.761632, 106.689747,200,"THDao_TDXu"));
        geoList.add(new GeoFencingPlaceModel(10.756505, 106.685195,100,"THDao_NVCu"));
        geoList.add(new GeoFencingPlaceModel(10.769965, 106.694273,100,"NTNghia_LeLai"));
        geoList.add(new GeoFencingPlaceModel(10.777699, 106.681903,100,"VXoay_DanChu"));

        return geoList;
    }
}
