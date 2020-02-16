package phannguyen.sample.gpsgeofencingtrackingexperiment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;
import phannguyen.sample.gpsgeofencingtrackingexperiment.receiver.ScreenStateReceiver;
import phannguyen.sample.gpsgeofencingtrackingexperiment.service.RegisterTriggerService;
import phannguyen.sample.gpsgeofencingtrackingexperiment.utils.TestUtils;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;
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
        });

        Button awareBtn = findViewById(R.id.awarenessBtn);
        awareBtn.setOnClickListener(v -> {
            //
            //startActivity(new Intent(MainActivity.this,AwarenessActivity.class));
            //Toast.makeText(MainActivity.this,"This button disable processing",Toast.LENGTH_LONG).show();

        });

        Button registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, RegisterTriggerService.class);
            startService(serviceIntent);
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
            permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
