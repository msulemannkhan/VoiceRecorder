package com.paykio.khubaibraza.voicrecorder;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Daniel on 12/28/2014.
 */
public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";
    private Handler handler;
    private String mFileName = null;
    private String mFilePath = null;
    private String mFileType = "mp3";
    private int mFileSize;
    private File f;

    private MediaRecorder mRecorder = null;
    private boolean isRecording =false;
    private DBHelper mDatabase;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private String valueList[];
    private SharedPreferences sharedPreferences;
    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
        valueList=new String[10];

//        ToDo: get value of shared preference
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);

        String value=sharedPreferences.getString(getResources().getString(R.string.RecordingFormat),"-1");//first
        valueList[0]=value;

        value=sharedPreferences.getString(getResources().getString(R.string.SampleRate),"-1");//second
        valueList[6]=value;

        value=sharedPreferences.getString(getResources().getString(R.string.EncoderBitrate),"-1");
        valueList[1]=value;

        value=sharedPreferences.getString(getResources().getString(R.string.AudioSource),"-1");
        valueList[2]=value;

//        value=sharedPreferences.getString(getResources().getString(R.string.StatusBar),"-1");
        valueList[3]="nothing";

        value=sharedPreferences.getString("path","-1");//set file path get though shared prefrence
        valueList[4]=value;

        value=sharedPreferences.getString(getResources().getString(R.string.Language),"-1");
        valueList[5]=value;


        if(Integer.parseInt(valueList[0])==1){
            mFileType="3gp";
        }
        else if(Integer.parseInt(valueList[0])==2){
            mFileType="m4a";
        }
        else if(Integer.parseInt(valueList[0])==3){
            mFileType="wav";
        }
        else {
            mFileType="mp3";
        }
//        value=sharedPreferences.getString(getResources().getString(R.string.SortRecording),"-1");
//        valueList[6]=value;

    }

    ResultReceiver rec;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Extract the receiver passed into the service
        rec = intent.getParcelableExtra("receiver");
        // create the Handler for visualizer update
        handler = new Handler();
        startRecording();
        Log.i("recorder", "onStartCommand execute hogya ha");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return super.bindService(service, conn, flags);
    }

    public void startRecording() {
        handler.post(updateVisualizer);
        setFileNameAndPath();
        sendNotification();
        isRecording = true;
        mRecorder = new MediaRecorder();

//        mRecorder.setAudioSource(Integer.parseInt("0"));
        Log.e(LOG_TAG, "Audio Source value = "+valueList[2]);
        if((valueList[2].equals("-1"))){
            valueList[2]="0";
        }
        mRecorder.setAudioSource(Integer.parseInt(valueList[2]));

        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFilePath);//set file path
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//confuse search in morning
        mRecorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            mRecorder.setAudioSamplingRate(Integer.parseInt(valueList[1]));
            //TODO: IMPORTANT APP FEATURE
            mRecorder.setAudioEncodingBitRate(Integer.parseInt(valueList[1]));
        }

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void setFileNameAndPath(){
        int count = 0;


        do{
            count++;

            mFileName = getString(R.string.default_file_name)
                    + "_" + (mDatabase.getCount() + count) + "." + mFileType;
            SharedPreferences sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("valueInput", mFileName);
            editor.commit();
            mFilePath=valueList[4];
            if((valueList[4].equals("-1"))){
                mFilePath = Environment.getExternalStorageDirectory()+"/VoiceRecorder";
            }

            mFilePath += "/" + mFileName;

            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }

    public void stopRecording() {
        handler.removeCallbacks(updateVisualizer);
        isRecording =false;
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;
       mFileSize = Integer.parseInt(String.valueOf(f.length()/1024));
        try {
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis, mFileType, mFileSize);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
    }


    // updates the visualizer every 50 milliseconds
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording) // if we are already recording
            {
                Log.i("CHUSSTAG::::::", "chuss mari ha main nay Thankyou lag nae rahi but");
                // get the current amplitude
                int x = mRecorder.getMaxAmplitude();

                Bundle bundle = new Bundle();
                bundle.putInt("resultValue", x);
                // Here we call send passing a resultCode and the bundle of extras
                rec.send(Activity.RESULT_OK, bundle);


                // update in 40 milliseconds
                handler.postDelayed(this, 40);
            }
        }
    };

    private void sendNotification(){

        CharSequence title = "Recording..";
        CharSequence message = "size seconds";// will mention seconds and space consume by recording

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(title);

        mBuilder.setContentText(message);
        mBuilder.setTicker(message);
        mBuilder.setWhen(System.currentTimeMillis());

        NotificationManager notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        mBuilder.setContentIntent(pendingIntent);
        notificationManager.notify(0, mBuilder.build());
    }

//    private void startTimer() {
//        mTimer = new Timer();
//        mIncrementTimerTask = new TimerTask() {
//            @Override
//            public void run() {
//                mElapsedSeconds++;
//                if (onTimerChangedListener != null)
//                    onTimerChangedListener.onTimerChanged(mElapsedSeconds);
//                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                mgr.notify(1, createNotification());
//            }
//        };
//        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
//    }

//    //TODO:
//    private Notification createNotification() {
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(getApplicationContext())
//                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
//                        .setContentTitle(getString(R.string.notification_recording))
//                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
//                        .setOngoing(true);
//
//        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
//                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));
//
//        return mBuilder.build();
//    }
}

