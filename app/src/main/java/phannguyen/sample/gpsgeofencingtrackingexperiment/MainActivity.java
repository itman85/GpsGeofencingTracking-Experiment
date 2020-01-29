package phannguyen.sample.gpsgeofencingtrackingexperiment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import phannguyen.sample.gpsgeofencingtrackingexperiment.helper.WorkManagerHelper;

public class MainActivity extends AppCompatActivity {

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
            Toast.makeText(MainActivity.this,"This button disable processing",Toast.LENGTH_LONG).show();
        });

    }
}
