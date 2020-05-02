package phannguyen.sample.gpsgeofencingtrackingexperiment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import phannguyen.sample.gpsgeofencingtrackingexperiment.accessibility.SbAccessibilityService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.notification.NotiHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.receiver.ScreenStateReceiver;
import phannguyen.sample.gpsgeofencingtrackingexperiment.service.RegisterTriggerService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.TestUtils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    private static final int NOTI_LISTENER_REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(v -> {
            WorkManagerHelper.startOneTimeRegisterUserActivityForTrackingLocationWorker(MainActivity.this,0,5);
            //TestUtils.startLocationTrackingService(MainActivity.this);
        });

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(v -> {
           // throw new RuntimeException("Test Crash"); // Force a crash
            TestUtils.stopLocationTrackingService(MainActivity.this);
        });

        Button awareBtn = findViewById(R.id.awarenessBtn);
        awareBtn.setOnClickListener(v -> {
            //
            //startActivity(new Intent(MainActivity.this,AwarenessActivity.class));
            //Toast.makeText(MainActivity.this,"This button disable processing",Toast.LENGTH_LONG).show();
            TestUtils.testSavePaperValue();
            TestUtils.testReadPaperValue();
        });

        Button registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(v -> {
           /* Intent serviceIntent = new Intent(MainActivity.this, RegisterTriggerService.class);
            startService(serviceIntent);*/
            checkAccessibilityService();
        });

        Button ignoreBatteryBtn = findViewById(R.id.ignoreBatteryBtn);
        ignoreBatteryBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                }
            }else{
                Toast.makeText(MainActivity.this,"Only support from android 6",Toast.LENGTH_LONG).show();
            }
        });

        Button notiBtn = findViewById(R.id.notiBtn);
        notiBtn.setOnClickListener(v -> {
            if (!NotiHelper.isNotificationServiceEnabled(this)) {
                requestNotificationListenerService();
            }
        });

        Button geofencing = findViewById(R.id.geofencingBtn);
        geofencing.setOnClickListener(v -> {
            // start register geo fencing service (only need to register one time)
            TestUtils.startGeofencingTrackingService(MainActivity.this);
        });

        askPermission();

    }

    private void askPermission(){
        List<String> permissionList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION); // this will for android 10 only, will add option "allow all time"
        }

        // require for android 10+
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q &&
                checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }

        if(!permissionList.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] itemsArray = new String[permissionList.size()];
            itemsArray = permissionList.toArray(itemsArray);
            this.requestPermissions(
                    itemsArray,
                    MY_PERMISSIONS_REQUEST_LOCATION);

        }
    }
    private void askLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    private void checkAccessibilityService(){
         if(!isAccessServiceEnabled(MainActivity.this, SbAccessibilityService.class)){
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Make sure Accessibility Service is enabled in Device Settings (Accessibility section).")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            startActivityForResult(intent, 0);
                        }).setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(
                            MainActivity.this,
                            "In order to use this app you must have Standalone ACCESSIBILITY SERVICE turn on, please reopen app and turn it on.",
                            Toast.LENGTH_LONG).show();
                }).show();

            }else{
                Toast.makeText(MainActivity.this,"Already enable this accessibility service",Toast.LENGTH_LONG).show();
            }
    }

    // todo test on other each version of android 5,6,7,8,9,10
    public boolean isAccessServiceEnabled(Context context, Class accessibilityServiceClass)
    {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString!= null && prefString.contains(context.getPackageName() + "/" + accessibilityServiceClass.getName());
    }

    //https://www.spiria.com/en/blog/mobile-development/hiding-foreground-services-notifications-in-android/
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void requestNotificationListenerService() {
        String notificationListenerSettings = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            notificationListenerSettings = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
        }

        Intent intent = new Intent(notificationListenerSettings);
        startActivityForResult(intent, NOTI_LISTENER_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOTI_LISTENER_REQUEST_CODE) {
            if (!NotiHelper.isNotificationServiceEnabled(this)) {
                requestNotificationListenerService();
            }
        }
    }
}
