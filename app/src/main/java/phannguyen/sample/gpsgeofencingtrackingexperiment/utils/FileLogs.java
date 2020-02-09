package phannguyen.sample.gpsgeofencingtrackingexperiment.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogs {
    public static final String TAG = "FileLogs";
    private static final boolean IS_WRITE_LOG_FOR_DEBUG = true;

    public static void writeLogInThread(Context context, String tag, String type, String text)
    {
        if(!IS_WRITE_LOG_FOR_DEBUG) return;

        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Thread task = new Thread(() -> {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String dateFormat = s.format(new Date());
                // external storage folder for this app package.
                File logfolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
                //File logfolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Test_Service");
                //File file = context.getApplicationContext().getDir("service_test", Context.MODE_PRIVATE);
                if (!logfolder.exists()) {
                    logfolder.mkdir();
                }

                File logFile = new File( logfolder.getAbsolutePath() + "/" + tag + ".txt");
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        SbLog.e(TAG,e);
                    }
                }
                try {
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(dateFormat + "  " + type + "/" + tag + ": " + text);
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {
                    SbLog.e(TAG,e);
                }
            });
            task.start();
        }

    }

    public static void writeLogNoThread(Context context, String tag, String type, String text)
    {
        if(!IS_WRITE_LOG_FOR_DEBUG) return;

        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateFormat = s.format(new Date());
            // external storage folder for this app package.
            File logfolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
            //File logfolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Test_Service");
            //File file = context.getApplicationContext().getDir("service_test", Context.MODE_PRIVATE);
            if (!logfolder.exists()) {
                logfolder.mkdir();
            }

            File logFile = new File( logfolder.getAbsolutePath() + "/" + tag + ".txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    SbLog.e(TAG,e);
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(dateFormat + "  " + type + "/" + tag + ": " + text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                SbLog.e(TAG,e);
            }
        }

    }

    public static void writeDayLogNoThread(Context context,String tag, String type, String text)
    {
        if(!IS_WRITE_LOG_FOR_DEBUG) return;

        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateFormat = s.format(new Date());
            // external storage folder for this app package.
            File logfolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Days");
            //File logfolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Test_Service");
            //File file = context.getApplicationContext().getDir("service_test", Context.MODE_PRIVATE);
            if (!logfolder.exists()) {
                logfolder.mkdir();
            }

            SimpleDateFormat s1 = new SimpleDateFormat("ddMMyyyy");
            String dateFormat1 = s1.format(new Date());

            File logFile = new File( logfolder.getAbsolutePath() + "/" + dateFormat1 + ".txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    SbLog.e(TAG,e);
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(dateFormat + "  " + type + "/" + tag + ": " + text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                SbLog.e(TAG,e);
            }
        }

    }

    /**
     * Write log by date in io thread
     * @param context
     * @param tag
     * @param type
     * @param text
     */
    public static void writeDayLogInThread(Context context,String tag, String type, String text)
    {
        if(!IS_WRITE_LOG_FOR_DEBUG) return;

        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            //Log.i(TAG,"Permission days granted "+ tag + " in thread");
            Thread task = new Thread(() -> {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                String dateFormat = s.format(new Date());
                // external storage folder for this app package.
                File logfolder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Days");
                if (!logfolder.exists()) {
                    logfolder.mkdir();
                }

                //Log.i(TAG,"Folder Days created "+logfolder.getAbsolutePath());
                SimpleDateFormat s1 = new SimpleDateFormat("ddMMyyyy");
                String dateFormat1 = s1.format(new Date());

                File logFile = new File( logfolder.getAbsolutePath() + "/" + dateFormat1 + ".txt");
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        Log.e(TAG,e.getMessage());
                    }
                }
                //Log.i(TAG,"File Days created "+logFile.getAbsolutePath());
                try {
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    //Log.i(TAG,"File Days open to write");
                    buf.append(dateFormat + "  " + type + "/" + tag + ": " + text);
                    //Log.i(TAG,"File Days write "+dateFormat + "  " + type + "/" + tag + ": " + text);
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                }
            });
            task.start();

        }

    }


    public static void writeLog(Context context, String tag, String type, String text){
        // android >= 8 will write log in thread, android 7- write log in main thread
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            writeLogInThread(context,tag,type,text);
        }else
            writeLogNoThread(context, tag, type, text);// todo dont know why in android 7 write log in thread not work
    }

    public static void writeLogByDate(Context context, String tag, String type, String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            writeDayLogInThread(context,tag,type,text);
        }else
            writeDayLogNoThread(context, tag, type, text);// todo dont know why in android 7 write log in thread not work
    }
}
