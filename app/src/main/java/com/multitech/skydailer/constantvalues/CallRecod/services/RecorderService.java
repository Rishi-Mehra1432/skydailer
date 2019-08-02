package com.multitech.skydailer.constantvalues.CallRecod.services;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.IBinder;

import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.multitech.skydailer.constantvalues.CallRecod.CommonMethods;

import java.io.IOException;

/**
 * Created by VS00481543 on 30-10-2017.
 */

public class RecorderService extends Service {

    MediaRecorder recorder;
    static final String TAGS=" Inside Service";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent,int flags,int startId)
    {
        recorder = new MediaRecorder();
        recorder.reset();

        String phoneNumber=intent.getStringExtra("number");
        Log.d(TAGS, "Phone number in service: "+phoneNumber);

        String time=new CommonMethods().getTIme();

        String path=new CommonMethods().getPath();

        String rec=path+"/"+phoneNumber+"_"+time+".mp4";

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.RECORD_AUDIO},
                  99);

        } else {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        }


        recorder.setOutputFile(rec);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();

        Log.d(TAGS, "onStartCommand: "+"Recording started");

        return START_NOT_STICKY;
    }

    public void onDestroy()
    {
        super.onDestroy();

        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder=null;

        Log.d(TAGS, "onDestroy: "+"Recording stopped");

    }
}
