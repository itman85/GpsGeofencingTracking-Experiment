package phannguyen.sample.gpsgeofencingtrackingexperiment.helper;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.Map;

import phannguyen.sample.gpsgeofencingtrackingexperiment.service.CoreDetectActivityJobService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.service.CoreTrackingJobService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.service.LocationRequestUpdateBackgroundService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.service.LocationRequestUpdateForegroundService;

public class ServiceHelper {

    public static void startLocationRequestUpdateService(Context context, Map<String, Object> bundle){
        // Start normal service in bg only allow in android 7-
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent serviceIntent = new Intent(context, LocationRequestUpdateBackgroundService.class);
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    if (bundle.get(key) instanceof String)
                        serviceIntent.putExtra(key, (String) bundle.get(key));
                    else if (bundle.get(key) instanceof Boolean)
                        serviceIntent.putExtra(key, (Boolean) bundle.get(key));
                    else if (bundle.get(key) instanceof Integer)
                        serviceIntent.putExtra(key, (Integer) bundle.get(key));
                }
            }
            context.startService(serviceIntent);
        }else{
            // from android 8+ will start foreground service
            Intent serviceIntent = new Intent(context, LocationRequestUpdateForegroundService.class);
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    if (bundle.get(key) instanceof String)
                        serviceIntent.putExtra(key, (String) bundle.get(key));
                    else if (bundle.get(key) instanceof Boolean)
                        serviceIntent.putExtra(key, (Boolean) bundle.get(key));
                    else if (bundle.get(key) instanceof Integer)
                        serviceIntent.putExtra(key, (Integer) bundle.get(key));
                }
            }
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }

    public static void stopLocationRequestUpdateService(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent serviceIntent = new Intent(context, LocationRequestUpdateBackgroundService.class);
            context.stopService(serviceIntent);
        }else{
            Intent serviceIntent = new Intent(context, LocationRequestUpdateForegroundService.class);
            context.stopService(serviceIntent);
        }
    }

    public static void startCoreLocationTrackingJobService(Context context, Map<String, Object> bundle){
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

   /* public static void startLocationRequestUpdateForegroundService(Context context,Map<String, Object> bundle){
        Intent serviceIntent = new Intent(context, LocationRequestUpdateForegroundService.class);
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
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    public static void stopLocationRequestUpdateForegroundService(Context context){
        Intent serviceIntent = new Intent(context, LocationRequestUpdateForegroundService.class);
        context.stopService(serviceIntent);
    }*/

    public static void startCoreActivityTrackingJobService(Context context, Map<String, Object> bundle){
        Intent serviceIntent = new Intent(context, CoreDetectActivityJobService.class);
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
        CoreDetectActivityJobService.enqueueWork(context,serviceIntent);
    }
}
