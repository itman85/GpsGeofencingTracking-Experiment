package phannguyen.sample.gpsgeofencingtrackingexperiment.helper;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.FileUtils;

import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.service.CoreTrackingJobService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.service.LocationRequestUpdateService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.FileLogs;

public class ServiceHelper {

    public static void startLocationRequestUpdateService(Context context, Map<String, Object> bundle){
        FileLogs.appendLog(context,"MainAppProcessCallbackImpl","I","2,3-App-Invoke method startLocationRequestUpdateService");
        Intent serviceIntent = new Intent(context, LocationRequestUpdateService.class);
        if(bundle!=null) {
            for (String key : bundle.keySet()) {
                if(bundle.get(key) instanceof String)
                    serviceIntent.putExtra(key,(String)bundle.get(key));
                else if(bundle.get(key) instanceof Boolean)
                    serviceIntent.putExtra(key,(Boolean) bundle.get(key));
                else if(bundle.get(key) instanceof Integer)
                    serviceIntent.putExtra(key,(Integer) bundle.get(key));
            }
        }
        context.startService(serviceIntent);
    }

    public static void startCoreLocationTrackingJobService(Context context, Map<String, Object> bundle){
        FileLogs.appendLog(context,"MainAppProcessCallbackImpl","I","4,5-App-Invoke method startCoreLocationTrackingJobService");
        Intent serviceIntent = new Intent(context, CoreTrackingJobService.class);
        if(bundle!=null) {
            for (String key : bundle.keySet()) {
                if(bundle.get(key) instanceof String)
                    serviceIntent.putExtra(key,(String)bundle.get(key));
                else if(bundle.get(key) instanceof Boolean)
                    serviceIntent.putExtra(key,(Boolean) bundle.get(key));
                else if(bundle.get(key) instanceof Integer)
                    serviceIntent.putExtra(key,(Integer) bundle.get(key));
                else if(bundle.get(key) instanceof Location)
                    serviceIntent.putExtra(key,(Location)bundle.get(key));
            }
        }
        CoreTrackingJobService.enqueueWork(context,serviceIntent);
    }
}
