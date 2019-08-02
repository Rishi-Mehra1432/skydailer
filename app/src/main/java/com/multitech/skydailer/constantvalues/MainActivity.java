package com.multitech.skydailer.constantvalues;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;

import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.multitech.skydailer.R;
import com.multitech.skydailer.constantvalues.CallRecod.CallRecorderFragment;
import com.multitech.skydailer.constantvalues.internetcalls.InternetCallFragment;
import com.multitech.skydailer.constantvalues.logentry.logadapter.LogEntryAdapter;
import com.multitech.skydailer.constantvalues.mainfragmenttabs.MainFragment;
import com.multitech.skydailer.constantvalues.pickContact.model.Contact_Model;
import com.multitech.skydailer.constantvalues.speedDail.SpeedDialFragmnt;
import com.multitech.skydailer.constantvalues.voicechange.VoiceChangerFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;


public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    ImageView backButton, popup_option;
    public static TextView headerTxt;
    public static ArrayList<Contact_Model> arrayList;



    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);


        backButton = (ImageView) findViewById(R.id.back_button);
        headerTxt = (TextView) findViewById(R.id.header_txt);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        popup_option = (ImageView) findViewById(R.id.popup_txt);
        popup_option.setVisibility(View.VISIBLE);
        headerTxt.setText(R.string.app_name);


        MainFragment homeFragment = new MainFragment();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).addToBackStack("HomeFragment").commit();

        //    Read Contacts from mobile phone
        arrayList = readContacts();
        findViewById(R.id.popup_txt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });



    }


    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.dialer_options);
        popup.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.dialer_call_home:
                MainFragment homeFragment = new MainFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).addToBackStack("HomeFragment").commit();
                return true;
            case R.id.dialer_call_profile:
              /*  ProfileFragment profileFragment = new ProfileFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).addToBackStack("ProfileFragment").commit();
                return true;*/

            case R.id.Agora_Call:
                 startActivity(new Intent(MainActivity.this,VoiceChatViewActivity.class));
                //InternetCallFragment internetCallFragment =new InternetCallFragment();
             //   getFragmentManager().beginTransaction().replace(R.id.fragment_container, internetCallFragment).addToBackStack("InternetCallFragment").commit();
                return true;
            case R.id.clear_call_log:
                clearCallLogDialog();
                return true;
            case R.id.fast_dial_preferences:

                SpeedDialFragmnt speedDialFragmnt = new SpeedDialFragmnt();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, speedDialFragmnt).addToBackStack("SpeedDialFragmnt").commit();

                return true;
            case R.id.dialer_preferences:
                startActivity(new Intent(MainActivity.this, DialerPreferencesActivity.class));
                return true;
            case R.id.dialer_call_recorder:

                CallRecorderFragment callRecorderFragment = new CallRecorderFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, callRecorderFragment).addToBackStack("CallRecorderFragment").commit();

                return true;
            case R.id.dialer_voice_changer:
                VoiceChangerFragment voiceChangerFragment = new VoiceChangerFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, voiceChangerFragment).addToBackStack("CallRecorderFragment").commit();


                return true;
            case R.id.dialer_profile:

                /*FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(MainActivity.this, PhoneLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);*/

                return true;

            default:
                return false;
        }
    }


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>read contacts

    // Method that return all contact details in array format
    private ArrayList<Contact_Model> readContacts() {
        ArrayList<Contact_Model> contactList = new ArrayList<Contact_Model>();

        Uri uri = ContactsContract.Contacts.CONTENT_URI; // Contact URI
        Cursor contactsCursor = getContentResolver().query(uri, null, null,
                null, ContactsContract.Contacts.DISPLAY_NAME + " ASC "); // Return
        // all
        // contacts
        // name
        // containing
        // in
        // URI
        // in
        // ascending
        // order
        // Move cursor at starting
        if (contactsCursor.moveToFirst()) {
            do {
                long contctId = contactsCursor.getLong(contactsCursor
                        .getColumnIndex("_ID")); // Get contact ID
                Uri dataUri = ContactsContract.Data.CONTENT_URI; // URI to get
                // data of
                // contacts
                Cursor dataCursor = getContentResolver().query(dataUri, null,
                        ContactsContract.Data.CONTACT_ID + " = " + contctId,
                        null, null);// Retrun data cusror represntative to
                // contact ID

                // Strings to get all details
                String displayName = "";
                String nickName = "";
                String homePhone = "";
                String mobilePhone = "";
                String workPhone = "";
                String photoPath = "" + R.drawable.userimage; // Photo path
                byte[] photoByte = null;// Byte to get photo since it will come

                // Now start the cusrsor
                if (dataCursor.moveToFirst()) {
                    displayName = dataCursor
                            .getString(dataCursor
                                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));// get
                    // the
                    // contact
                    // name
                    do {
                        if (dataCursor
                                .getString(
                                        dataCursor.getColumnIndex("mimetype"))
                                .equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)) {
                            nickName = dataCursor.getString(dataCursor
                                    .getColumnIndex("data1")); // Get Nick Name

                        }

                        if (dataCursor
                                .getString(
                                        dataCursor.getColumnIndex("mimetype"))
                                .equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {

                            // In this get All contact numbers like home,
                            // mobile, work, etc and add them to numbers string
                            switch (dataCursor.getInt(dataCursor
                                    .getColumnIndex("data2"))) {
                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                    homePhone = dataCursor.getString(dataCursor
                                            .getColumnIndex("data1"));

                                    break;

                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                    workPhone = dataCursor.getString(dataCursor
                                            .getColumnIndex("data1"));

                                    break;

                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                    mobilePhone = dataCursor.getString(dataCursor
                                            .getColumnIndex("data1"));

                                    break;

                            }
                        }


                        if (dataCursor
                                .getString(
                                        dataCursor.getColumnIndex("mimetype"))
                                .equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                            photoByte = dataCursor.getBlob(dataCursor
                                    .getColumnIndex("data15")); // get photo in
                            // byte

                            if (photoByte != null) {

                                // Now make a cache folder in file manager to
                                // make cache of contacts images and save them
                                // in .png
                                Bitmap bitmap = BitmapFactory.decodeByteArray(
                                        photoByte, 0, photoByte.length);
                                File cacheDirectory = this.getCacheDir();

                                File tmp = new File(cacheDirectory.getPath()
                                        + "/_androhub" + contctId + ".png");
                                try {
                                    FileOutputStream fileOutputStream = new FileOutputStream(
                                            tmp);
                                    bitmap.compress(Bitmap.CompressFormat.PNG,
                                            100, fileOutputStream);
                                    fileOutputStream.flush();
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    // TODO: handle exception
                                    e.printStackTrace();
                                }
                                photoPath = tmp.getPath();// finally get the
                                // saved path of
                                // image
                            }

                        }

                    } while (dataCursor.moveToNext()); // Now move to next
                    // cursor

                    contactList.add(new Contact_Model(Long.toString(contctId),
                            displayName, mobilePhone, photoPath));// Finally ad
                    // items to
                    // array list
                }

            } while (contactsCursor.moveToNext());
        }
        return contactList;
    }


    private void clearCallLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getResources().getString(R.string.clear_call_log_question));
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    public void onClick(DialogInterface di, int which) {
                        clearCallLog();
                    }
                });
        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int which) {

                    }
                });

        builder.create().show();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void clearCallLog() {
        if (checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        getContentResolver().delete(CallLog.Calls.CONTENT_URI, null, null);
        TypedValue outValue = new TypedValue();
        int defaultContactImageId = outValue.resourceId;
        AsyncContactImageLoader mAsyncContactImageLoader;
        mAsyncContactImageLoader = new AsyncContactImageLoader(MainActivity.this, getResources().getDrawable(defaultContactImageId,  getTheme()));
        LogEntryAdapter logEntryAdapter = new LogEntryAdapter(MainActivity.this, null, mAsyncContactImageLoader);
        logEntryAdapter.update();
    }

}
