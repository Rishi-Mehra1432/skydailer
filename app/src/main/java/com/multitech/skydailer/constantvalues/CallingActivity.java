package com.multitech.skydailer.constantvalues;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;

import com.android.internal.telephony.ITelephony;
import com.multitech.skydailer.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CallingActivity extends AppCompatActivity {

    private ImageView mute_btn;
    private  ImageView loudspeaker_btn;
    private  ImageView end_call_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        mute_btn = (ImageView)findViewById(R.id.mute_btn);
        loudspeaker_btn = (ImageView)findViewById(R.id.loudspeaker_btn);
        end_call_btn = (ImageView)findViewById(R.id.end_call_btn);

        end_call_btn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

                Method method = null;
                try {
                    Class clazz = Class.forName(telephonyManager.getClass().getName());
                    method = clazz.getDeclaredMethod("getITelephony");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                method.setAccessible(true);
                ITelephony telephonyService = null;
                try {
                    telephonyService = (ITelephony) method.invoke(telephonyManager);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                telephonyService.endCall();
                finish();
            }
        });


        loudspeaker_btn.setOnClickListener(new View.OnClickListener() {


            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {


            }
        });



    }

}
