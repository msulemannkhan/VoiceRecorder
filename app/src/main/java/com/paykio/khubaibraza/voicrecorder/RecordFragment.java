package com.paykio.khubaibraza.voicrecorder;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.loaderspack.loaders.RippleLoader;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();
    public MyTestReceiver receiverForTest;
    private int position;

    private long mStartTime=0;
    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;

    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;

    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;

    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button

    VisualizerView visualizerView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }


    public void setupServiceReceiver() {
        receiverForTest = new MyTestReceiver(new Handler());
        // This is where we specify what happens when data is received from the service
        receiverForTest.setReceiver(new MyTestReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    int x = resultData.getInt("resultValue");
                //    Toast.makeText(getActivity(), "RECEIVED "+ x, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(getActivity(), resultValue, Toast.LENGTH_SHORT).show();
                    visualizerView.updateVisualizer(x); // update the VisualizeView
                    visualizerView.invalidate(); // refresh the VisualizerView
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission added
                return;
            } else {
                Toast.makeText(getContext(), "oh! you don't give permission to access storage" +
                        ".To backup your data you need to grant permission", Toast.LENGTH_LONG).show();
            }
        }

    }


    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);
        visualizerView = (VisualizerView) recordView.findViewById(R.id.visualizerView);
        setupServiceReceiver();

        mChronometer = recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = (TextView) recordView.findViewById(R.id.recording_status_text);

        mRecordButton = (FloatingActionButton) recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });
        if(isMyServiceRunning(RecordingService.class)){
            mChronometer.setBase(SystemClock.elapsedRealtime() + (( mStartTime - System.currentTimeMillis() )));
            mChronometer.start();
            mRecordButton.setImageResource(R.drawable.ic_tab_pla);
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (mRecordPromptCount == 0) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                    } else if (mRecordPromptCount == 1) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                    } else if (mRecordPromptCount == 2) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                        mRecordPromptCount = -1;
                    }

                    mRecordPromptCount++;
                }
            });
        }

        mPauseButton = (Button) recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseRecord(mPauseRecording);
                mPauseRecording = !mPauseRecording;
            }
        });
//        RippleLoader ripple = new RippleLoader(getActivity());
//        ripple.setCircleInitialRadius(80);
//        ripple.setCircleColor(getResources().getColor(R.color.red));
//        ripple.setFromAlpha(1.0f);
//        ripple.setToAlpha(0.01f);
//        ripple.setAnimationDuration(1000);
//        ripple.setInterpolator(new DecelerateInterpolator());
//        ripple.setStartLoadingDefault(true);
//
//        container.addView(ripple);
        return recordView;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Recording Start/Stop
    //TODO: recording pause
    private void onRecord(boolean start){

        Intent intent = new Intent(getActivity(), RecordingService.class);

        if (start) {
            // start recording
            mRecordButton.setImageResource(R.drawable.ic_tab_pla);
            //mPauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(),R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());

            String value=sharedPreferences.getString("path","-1");//first
            File folder;
            folder= new File(value);

            if(value.equals("-1"))
            {
               folder= new File(Environment.getExternalStorageDirectory()+"/VoiceRecorder");
            }

            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }

            //start Chronometer
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mStartTime = System.currentTimeMillis();
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (mRecordPromptCount == 0) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                    } else if (mRecordPromptCount == 1) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                    } else if (mRecordPromptCount == 2) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                        mRecordPromptCount = -1;
                    }

                    mRecordPromptCount++;
                }
            });

            if(!(sharedPreferences.getString("AudioSource","-1").equals("4"))) {
                intent.putExtra("receiver", receiverForTest);
                //start RecordingService
                getActivity().startService(intent);
                //keep screen on while recording
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                mRecordPromptCount++;
            }
            else {// becuase microphone is not working its key value is 4
                Toast.makeText(getActivity(),"another app is Recording ?", Toast.LENGTH_SHORT).show();
                mRecordButton.setImageResource(R.drawable.ic_tab_rec);
                //mPauseButton.setVisibility(View.GONE);
                mChronometer.stop();
                mStartTime = 0;
                mChronometer.setBase(SystemClock.elapsedRealtime());
                timeWhenPaused = 0;
                mRecordingPrompt.setText(getString(R.string.record_prompt));
            }

        } else {
            //Todo:(44):when user click on stop recoring open 2nd fragment of list
            //stop recording
            showEditDialog();
            mRecordButton.setImageResource(R.drawable.ic_tab_rec);
            //mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mStartTime = 0;
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            mRecordingPrompt.setText(getString(R.string.record_prompt));

            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    private void showEditDialog() {
//        FragmentManager fm = getSupportFragmentManager();
        EditNameDialogFragment editNameDialogFragment = EditNameDialogFragment.newInstance("Some Title");
        editNameDialogFragment.show(getFragmentManager(), "fragment_edit_name");
    }

    //TODO: implement pause recording
    private void onPauseRecord(boolean pause) {
        if (pause) {
            //pause recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_tab_rec ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
            mStartTime = 0;
        } else {
            //resume recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_tab_pla ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}


// Defines a generic receiver used to pass data to Activity from a Service
class MyTestReceiver extends ResultReceiver {
    private Receiver receiver;

    // Constructor takes a handler
    public MyTestReceiver(Handler handler) {
        super(handler);
    }

    // Setter for assigning the receiver
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    // Defines our event interface for communication
    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    // Delegate method which passes the result to the receiver if the receiver has been assigned
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }
}


class VisualizerView extends View {
    private int[] mBytes;
    private Paint mBarPaint;
    private Paint mBarPaintSecondary;
    private Paint mGridPaint;
    private Paint mGuidelinePaint;

    private int mPadding;
    private int spacing;
    private int dataCount;
    private float mAnimationFraction;
    static int chuss = 0;
    static int[] data = new int[66];
    static int pointer = 1;

    public void updateVisualizer(int x) {
        if (chuss == 0){
            init();
            chuss++;
        }


        final int height = getHeight();
        final int width = getWidth();
        final float gridLeft = mPadding;
        final float gridBottom = height - mPadding;
        final float gridTop = mPadding;
        final float gridRight = width - mPadding;


        dataCount = mBytes.length;
        float gridHeight = 1;
        float mColumnSpacing = 1;

        // Draw Bars

        float totalColumnSpacing = spacing * (dataCount + 1);
        float columnWidth = (gridRight - gridLeft - totalColumnSpacing) / dataCount;
        float columnLeft = gridLeft + spacing;
        float columnRight = columnLeft + columnWidth;

        if(pointer < (mBytes.length)-1) {
            mBytes[pointer] = x;
            ++pointer;
        }else{
            for(int i=0; i<(mBytes.length-1); ++i){
                mBytes[i] = mBytes[i+1];
            }
            mBytes[pointer] = x;
        }

        invalidate();
    }



    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        mBytes = new int[66];
        mBytes[20]= 50;
        mBarPaint = new Paint();
        mBarPaint.setStyle(Paint.Style.FILL);
        mBarPaint.setColor(getResources().getColor(R.color.primary));


        mBarPaintSecondary = new Paint();
        mBarPaintSecondary.setStyle(Paint.Style.FILL);
        mBarPaintSecondary.setColor(getResources().getColor( R.color.primary));

        mGridPaint = new Paint();
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setColor(getResources().getColor( R.color.primary));
        mGridPaint.setStrokeWidth(15f);

        mGuidelinePaint = new Paint();
        mGuidelinePaint.setStyle(Paint.Style.STROKE);
        mGuidelinePaint.setColor(getResources().getColor( R.color.primary));
        mGuidelinePaint.setStrokeWidth(66f);

        mPadding = 5;
        spacing = 1;
    }

    @Override
    public int getVerticalScrollbarWidth() {
        return super.getVerticalScrollbarWidth();
    }

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public void calculateAmplitude(){

    }

    public int getMaxFromArray(int[] array){
        int max= array[0];
        for(int i=0; i<array.length; ++i){
            if(array[i] > max){
                max = array[i];
            }
        }
        return max;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mBytes == null) {
            return;
        }
        makeAvg(mBytes);
        final int height = getHeight();
        final int width = getWidth();

        final float gridLeft = mPadding;
        final float gridBottom = height - mPadding;
        final float gridTop = mPadding;
        final float gridRight = width - mPadding;


        dataCount = mBytes.length;
        float gridHeight = 1;
        float mColumnSpacing = 1;

        // Draw Bars

        float totalColumnSpacing = spacing * (dataCount + 1);
        float columnWidth = (gridRight - gridLeft - totalColumnSpacing) / dataCount;
        float columnLeft = gridLeft + spacing;
        float columnRight = columnLeft + columnWidth;

        if(pointer > 64)
            Log.i("","");
        for (float percentage : mBytes) {
            if(percentage < 0)
                percentage *= -1;
                Log.i("percentage", "percentage: " + percentage);
            // Calculate top of column based on percentage.
            float top =  (height/2) -((percentage * (height/2)/getMaxFromArray(mBytes)));
            if(((height/2) - top) < 50){
                top = top - 50;
            }
            if(top < 0)
                top *= -1;
            if(color){
                canvas.drawRect(columnLeft, top, columnRight, gridBottom/2, mBarPaint);
                canvas.drawRect(columnLeft, gridBottom/2, columnRight, (gridBottom/2)+(gridBottom/2 -  top), mBarPaint);
                color = false;
            }else{
                canvas.drawRect(columnLeft, top, columnRight, gridBottom/2, mBarPaintSecondary);
                canvas.drawRect(columnLeft, gridBottom/2, columnRight, (gridBottom/2)+(gridBottom/2 -  top), mBarPaintSecondary);
                color = true;
            }


            // Shift over left/right column bounds
            columnLeft = columnRight + mColumnSpacing;
            columnRight = columnLeft + columnWidth;
        }

    }

    boolean color = false;

    int[] makeAvg(int[] array){
        for(int i =0; i< array.length; ++i){
//            if(array[i] <40){
//                array[i] = 40;
//            }
        //    Toast.makeText(getContext(), "value: "+ array[i], Toast.LENGTH_SHORT).show();
        }
        return  array;
    }

}