package com.multitech.skydailer.constantvalues.internetcalls;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.multitech.skydailer.R;

import java.util.Locale;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

import static android.Manifest.permission.RECORD_AUDIO;
import static io.agora.rtc.Constants.VOICE_CHANGER_BABYBOY;
import static io.agora.rtc.Constants.VOICE_CHANGER_BABYGIRL;
import static io.agora.rtc.Constants.VOICE_CHANGER_ETHEREAL;
import static io.agora.rtc.Constants.VOICE_CHANGER_HULK;
import static io.agora.rtc.Constants.VOICE_CHANGER_OLDMAN;
import static io.agora.rtc.Constants.VOICE_CHANGER_ZHUBAJIE;

/**
 * A simple {@link Fragment} subclass.
 */
public class InternetCallFragment extends android.app.Fragment {

    View view;
    private ImageView img_mute;
    private ImageView img_loudspeaker;
    private ImageView img_end_call;
    private static final String LOG_TAG = InternetCallFragment.class.getSimpleName();

    public static final String[] voicesNames = {"Select Voice","OldMan","BabyGirl","BabyBoy","Ethereal","Hulk","ZhubAjie"};
    private Spinner voiceSpinner;

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;


    private RtcEngine mRtcEngine;// Tutorial Step 1
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        @Override
        public void onUserOffline(final int uid, final int reason) { // Tutorial Step 4
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid, reason);
                }
            });
        }

        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVoiceMuted(uid, muted);
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_internet_call, container, false);

        if (checkSelfPermission(RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
        } else {
            initAgoraEngineAndJoinChannel();
        }



        voiceSpinner = (Spinner)view.findViewById(R.id.voiceSpinner);
        voiceSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, voicesNames));

        voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                String languageName =voicesNames[voiceSpinner.getSelectedItemPosition()];
                switch (languageName) {
                    case "OldMan":
                        mRtcEngine.setLocalVoiceChanger(VOICE_CHANGER_OLDMAN);
                        break;
                    case "BabyGirl":
                        mRtcEngine.setLocalVoiceChanger(VOICE_CHANGER_BABYGIRL);
                        break;
                    case "BabyBoy":
                        mRtcEngine.setLocalVoiceChanger(VOICE_CHANGER_BABYBOY);
                        break;
                    case "Ethereal":
                        mRtcEngine.setLocalVoiceChanger(VOICE_CHANGER_ETHEREAL);
                        break;
                    case "Hulk":
                        mRtcEngine.setLocalVoiceChanger(VOICE_CHANGER_HULK);
                        break;
                    case "ZhubAjie":
                        mRtcEngine.setLocalVoiceChanger(VOICE_CHANGER_ZHUBAJIE);
                        break;
                    default:
                        System.out.println("INVALID COLOR CODE");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        img_mute=(ImageView)view.findViewById(R.id.img_mute);
        img_loudspeaker=(ImageView)view.findViewById(R.id.img_loudspeaker);
        img_end_call=(ImageView)view.findViewById(R.id.img_end_call);

        img_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLocalAudioMuteClicked(view);
            }
        });

        img_loudspeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onSwitchSpeakerphoneClicked(view);
            }
        });

        img_end_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onEncCallClicked(view);

            }
        });


        return view;
    }
    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        joinChannel();               // Tutorial Step 2
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(getActivity(),
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    getActivity().finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    // Tutorial Step 7
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 5
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.setEnableSpeakerphone(view.isSelected());
    }

    // Tutorial Step 3
    public void onEncCallClicked(View view) {

        getActivity().finish();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getActivity(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void joinChannel() {
        String accessToken = getString(R.string.agora_access_token);
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken, "#YOUR ACCESS TOKEN#")) {
            accessToken = null; // default, no token
        }

        mRtcEngine.joinChannel(accessToken, "voiceDemoChannel1", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 3
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 4
    private void onRemoteUserLeft(int uid, int reason) {
        showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        View tipMsg =view.findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
        tipMsg.setVisibility(View.VISIBLE);
    }

    // Tutorial Step 6
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
    }
}
