package phannguyen.sample.gpsgeofencingtrackingexperiment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(v -> {
            WorkManagerHelper.startOneTimeRegisterUserActivityForTrackingLocationWorker(MainActivity.this,0,500);
        });

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(v -> {

        });

        Button awareBtn = findViewById(R.id.awarenessBtn);
        awareBtn.setOnClickListener(v -> {
            //
            //startActivity(new Intent(MainActivity.this,AwarenessActivity.class));
            //Toast.makeText(MainActivity.this,"This button disable processing",Toast.LENGTH_LONG).show();
            askLocationPermission();
        });

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
}
