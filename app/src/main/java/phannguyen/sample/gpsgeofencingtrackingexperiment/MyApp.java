package phannguyen.sample.gpsgeofencingtrackingexperiment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import io.paperdb.Paper;

public class MyApp extends Application implements Configuration.Provider{

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
    }
}
