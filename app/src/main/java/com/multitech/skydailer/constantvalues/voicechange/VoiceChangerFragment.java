package com.multitech.skydailer.constantvalues.voicechange;

import android.app.Fragment;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import com.multitech.skydailer.R;
import com.multitech.skydailer.constantvalues.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VoiceChangerFragment extends Fragment {
    View view;

    String[] freqText = {"11.025 KHz (Lowest)", "16.000 KHz", "22.050 KHz", "44.100 KHz (Highest)"};
    Integer[] freqset = {11025, 16000, 22050, 44100};
    private ArrayAdapter<String> adapter;

    Spinner spFrequency;
    Button startRec, stopRec, playBack;
    Boolean recording;


     @Override
    public View onCreateView(LayoutInflater inflater,   ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_voice_changer, container, false);
         MainActivity.headerTxt.setText("Change Voice");

         startRec = (Button)view.findViewById(R.id.startrec);
         stopRec = (Button)view.findViewById(R.id.stoprec);
         playBack = (Button)view.findViewById(R.id.playback);

         startRec.setOnClickListener(startRecOnClickListener);
         stopRec.setOnClickListener(stopRecOnClickListener);
         playBack.setOnClickListener(playBackOnClickListener);

         spFrequency = (Spinner)view.findViewById(R.id.frequency);
         adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, freqText);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spFrequency.setAdapter(adapter);

         stopRec.setEnabled(false);

        return view;
    }


    View.OnClickListener startRecOnClickListener
            = new View.OnClickListener(){

        @Override
        public void onClick(View arg0) {

            Thread recordThread = new Thread(new Runnable(){

                @Override
                public void run() {
                    recording = true;
                    startRecord();
                }

            });

            recordThread.start();
            startRec.setEnabled(false);
            stopRec.setEnabled(true);

        }};

    View.OnClickListener stopRecOnClickListener
            = new View.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            recording = false;
            startRec.setEnabled(true);
            stopRec.setEnabled(false);
        }};

    View.OnClickListener playBackOnClickListener
            = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            playRecord();
        }

    };

    private void startRecord(){

        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

        int selectedPos = spFrequency.getSelectedItemPosition();
        int sampleFreq = freqset[selectedPos];

        final String promptStartRecord =
                "startRecord()\n"
                        + file.getAbsolutePath() + "\n"
                        + (String)spFrequency.getSelectedItem();

        getActivity().runOnUiThread(new Runnable(){

            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        promptStartRecord,
                        Toast.LENGTH_LONG).show();
            }});

        try {
            file.createNewFile();

            OutputStream outputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

            int minBufferSize = AudioRecord.getMinBufferSize(sampleFreq,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            short[] audioData = new short[minBufferSize];

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleFreq,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            audioRecord.startRecording();

            while(recording){
                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                for(int i = 0; i < numberOfShort; i++){
                    dataOutputStream.writeShort(audioData[i]);
                }
            }

            audioRecord.stop();
            dataOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void playRecord(){

        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

        int shortSizeInBytes = Short.SIZE/Byte.SIZE;

        int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while(dataInputStream.available() > 0){
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            int selectedPos = spFrequency.getSelectedItemPosition();
            int sampleFreq = freqset[selectedPos];

            final String promptPlayRecord =
                    "PlayRecord()\n"
                            + file.getAbsolutePath() + "\n"
                            + (String)spFrequency.getSelectedItem();

            Toast.makeText(getActivity(),
                    promptPlayRecord,
                    Toast.LENGTH_LONG).show();

            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleFreq,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
