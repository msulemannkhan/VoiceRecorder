package com.paykio.khubaibraza.voicrecorder;

/**
 * Created by Khubaib raza on 18/02/2018.
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
        import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
/**
 * Created by Khubaib raza on 18/02/2018.
 */

public class CallsFragment extends android.support.v4.app.Fragment {

    Button buttonStart, buttonStop, buttonPlayLastRecordAudio, buttonStopPlayingRecording ;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    View view;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calls_fragment, container, false);
        buttonStart = (Button) rootView.findViewById(R.id.button);
        buttonStop = (Button) rootView.findViewById(R.id.button2);
        buttonPlayLastRecordAudio = (Button) rootView.findViewById(R.id.button3);
        buttonStopPlayingRecording = (Button)rootView.findViewById(R.id.button4);

        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);

        random = new Random();

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    AudioSavePathInDevice = Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/" + CreateRandomAudioFileName(5) + "AudioRecording.3gp";



                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    Toast.makeText(CallsFragment.this.getActivity(), "Recording started", Toast.LENGTH_LONG).show();
                }
                else {

                    requestPermission();

                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaRecorder.stop();

                buttonStop.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);

                Toast.makeText(getActivity(), "Recording Completed", Toast.LENGTH_LONG).show();

            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException, SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();

                Toast.makeText(CallsFragment.this.getActivity(), "Recording Playing", Toast.LENGTH_LONG).show();

            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if(mediaPlayer != null){

                    mediaPlayer.stop();
                    mediaPlayer.release();

                    MediaRecorderReady();

                }

            }
        });
        return rootView;
    }

//    public void saveFileToCard(byte[] data,String filename) {
//        File file = new File(Environment.getExternalStorageDirectory(), filename);
//        FileOutputStream fos;
//        try {
//            fos = new FileOutputStream(file);
//            fos.write(data);
//            fos.flush();
//            fos.close();
//        } catch (FileNotFoundException e) {
//            // handle exception
//        } catch (IOException e) {
//            // handle exception
//        }
//    }
    public void MediaRecorderReady(){

        mediaRecorder=new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        mediaRecorder.setOutputFile(AudioSavePathInDevice);

        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

    }

    public String CreateRandomAudioFileName(int string){

        StringBuilder stringBuilder = new StringBuilder( string );

        int i = 0 ;
        while(i < string ) {

            stringBuilder.append(RandomAudioFileName.charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {

                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {

                        Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getActivity(),"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int result = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), RECORD_AUDIO);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
}
