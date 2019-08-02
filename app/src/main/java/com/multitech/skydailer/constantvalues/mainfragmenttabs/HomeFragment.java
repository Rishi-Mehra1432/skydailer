package com.multitech.skydailer.constantvalues.mainfragmenttabs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;

import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.multitech.skydailer.R;
import com.multitech.skydailer.constantvalues.AsyncContactImageLoader;
import com.multitech.skydailer.constantvalues.DialerApp;
import com.multitech.skydailer.constantvalues.MainActivity;
import com.multitech.skydailer.constantvalues.OnCallLogScrollListener;
import com.multitech.skydailer.constantvalues.contactEntry.contactAdapter.ContactsEntryAdapter;
import com.multitech.skydailer.constantvalues.logentry.logadapter.LogEntryAdapter;
import com.multitech.skydailer.constantvalues.speedDail.SpeedDial;

import java.text.DateFormat;

import kotlin.jvm.internal.Intrinsics;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class HomeFragment extends android.app.Fragment implements View.OnClickListener, View.OnLongClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, TextWatcher, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {
    View view;

    private static final int[] buttonIds = new int[]{
            R.id.btn_numpad_0, R.id.btn_numpad_1,
            R.id.btn_numpad_2, R.id.btn_numpad_3,
            R.id.btn_numpad_4, R.id.btn_numpad_5,
            R.id.btn_numpad_6, R.id.btn_numpad_7,
            R.id.btn_numpad_8, R.id.btn_numpad_9,
            R.id.btn_numpad_star, R.id.btn_numpad_hash,
            R.id.btn_add_contact, R.id.btn_remove_number,
            R.id.btn_toggle_numpad, R.id.btn_call};
    private static final int CALL_LOG_MODE = 0;
    private static final int CONTACTS_MODE = 1;
    private static final String BUNDLE_KEY_NUMBER = "number";
    private static final String[] PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_CALL_LOG
    };
    private static final String[] CALL_INFORMATION_PROJECTION = {
            CallLog.Calls._ID,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
    };

    private int mode;
    private AsyncContactImageLoader mAsyncContactImageLoader;
    private ContactsEntryAdapter contactsEntryAdapter;
    private EditText numberField;
    private ListView list;
    private LogEntryAdapter logEntryAdapter;
    private OnCallLogScrollListener onCallLogScrollListener;
    private TelephonyManager telephonyManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DialerApp.setTheme(getActivity());
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!preferences.getBoolean("privacy_policy", false)) {
            showPrivacyPolicyDialog(preferences.edit());
        }
        if (Build.VERSION.SDK_INT >= 23 && !hasRequiredPermissions()) {
            requestPermissions(PERMISSIONS, 0);
            for (int i = 0; i < 5; i++) {
                if (getActivity().checkSelfPermission(PERMISSIONS[i]) == PackageManager.PERMISSION_GRANTED) {
                    continue;
                } else {

                    getActivity().finish();
                }
            }
        }

        numberField = (EditText) view.findViewById(R.id.number_field);
        parseIntent(getActivity().getIntent());
        telephonyManager = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
        setButtonListeners();
        numberField.setCursorVisible(false);
        numberField.requestFocus();
        numberField.addTextChangedListener(this);
        numberField.setInputType(InputType.TYPE_NULL);
        list = (ListView) view.findViewById(R.id.log_entries_list);
        onCallLogScrollListener = new OnCallLogScrollListener(getActivity());
        list.setOnScrollListener(onCallLogScrollListener);
        TypedValue outValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.drawableContactImage, outValue, true);
        int defaultContactImageId = outValue.resourceId;

        mAsyncContactImageLoader = new AsyncContactImageLoader(getActivity(), getResources().getDrawable(defaultContactImageId, getActivity().getTheme()));
        logEntryAdapter = new LogEntryAdapter((MainActivity) getActivity(), null, mAsyncContactImageLoader);
        contactsEntryAdapter = new ContactsEntryAdapter((MainActivity) getActivity(), mAsyncContactImageLoader);
        list.setAdapter(logEntryAdapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        int keyboardType = getResources().getConfiguration().keyboard;
        if (keyboardType == Configuration.KEYBOARD_QWERTY) {
            numberField.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            contactsEntryAdapter.setRawFiltering(true);
            view.findViewById(R.id.btn_toggle_numpad).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.numpad).setVisibility(View.GONE);
        } else if (keyboardType == Configuration.KEYBOARD_12KEY) {
            view.findViewById(R.id.btn_toggle_numpad).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.numpad).setVisibility(View.GONE);
        }

        getLoaderManager().initLoader(0, null, this);
        getLoaderManager().initLoader(1, null, this);
        getLoaderManager().getLoader(0).forceLoad();
        getLoaderManager().getLoader(1).forceLoad();

        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putCharSequence(BUNDLE_KEY_NUMBER, numberField.getText());
        super.onSaveInstanceState(bundle);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onStart() {
        super.onStart();
        offerReplacingDefaultDialer();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void offerReplacingDefaultDialer() {
        Object var10000 = getActivity().getSystemService(TelecomManager.class);
        Intrinsics.checkExpressionValueIsNotNull(var10000, "getSystemService(TelecomManager::class.java)");
        if (Intrinsics.areEqual(((TelecomManager)var10000).getDefaultDialerPackage(), getActivity().getPackageName()) ^ true) {
            Intent var1 = (new Intent("android.telecom.action.CHANGE_DEFAULT_DIALER"))
                    .putExtra("android.telecom.extra.CHANGE_DEFAULT_DIALER_PACKAGE_NAME",
                            getActivity().getPackageName());
            getActivity().startActivity(var1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_numpad_0:
                addSymbolInNumber('0');
                break;
            case R.id.btn_numpad_1:
                addSymbolInNumber('1');
                break;
            case R.id.btn_numpad_2:
                addSymbolInNumber('2');
                break;
            case R.id.btn_numpad_3:
                addSymbolInNumber('3');
                break;
            case R.id.btn_numpad_4:
                addSymbolInNumber('4');
                break;
            case R.id.btn_numpad_5:
                addSymbolInNumber('5');
                break;
            case R.id.btn_numpad_6:
                addSymbolInNumber('6');
                break;
            case R.id.btn_numpad_7:
                addSymbolInNumber('7');
                break;
            case R.id.btn_numpad_8:
                addSymbolInNumber('8');
                break;
            case R.id.btn_numpad_9:
                addSymbolInNumber('9');
                break;
            case R.id.btn_numpad_star:
                addSymbolInNumber('*');
                break;
            case R.id.btn_numpad_hash:
                addSymbolInNumber('#');
                break;
            case R.id.btn_remove_number:
                removeSymbolInNumber();
                break;
            case R.id.btn_toggle_numpad:
                toggleNumpad();
                break;
            case R.id.btn_call:
                callNumber(numberField.getText().toString());
                break;
            case R.id.btn_add_contact:
                createContact(numberField.getText().toString());
                break;
          /*  case R.id.btn_options:
                showPopupMenu( com.example.constantValues.view.findViewById(R.id.btn_options));
                break;*/
        }
    }


    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            Object focusedViewTag = getCurrentFocus().getTag();
            String number;
            if (focusedViewTag instanceof LogEntryCache) {
                LogEntryCache tag = (LogEntryCache) focusedViewTag;
                number = tag.phoneNumber.getText().toString();
                if (number.length() == 0) {
                    number = tag.contactName.getText().toString();
                }
            } else if (focusedViewTag instanceof ContactsEntryCache) {
                ContactsEntryCache tag = (ContactsEntryCache) focusedViewTag;
                number = tag.phoneNumber.getText().toString();
            } else {
                number = numberField.getText().toString();
            }
            callNumber(number);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasRequiredPermissions() {
        for (int i = 0; i < 5; i++) {
            if (getActivity().checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < 5; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                getActivity().finish();
                break;
            }
        }
    }

    private void parseIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())
                || Intent.ACTION_DIAL.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                if (scheme != null && scheme.equals("tel")) {
                    String number = data.getSchemeSpecificPart();
                    if (number != null) {
                        dialNumber(number);
                    }
                }
            }
        }
    }

    private void removeSymbolInNumber() {
        StringBuilder text = new StringBuilder(numberField.getText());
        if (text.length() == 0)
            return;
        int selectionStart = numberField.getSelectionStart();
        int selectionEnd = numberField.getSelectionEnd();
        if (selectionStart != selectionEnd) {
            text.delete(selectionStart, selectionEnd);
            numberField.setText(text);
            numberField.setSelection(selectionStart);
        } else {
            if (selectionStart == 0)
                return;
            text.deleteCharAt(selectionEnd - 1);
            numberField.setText(text);
            numberField.setSelection(selectionStart - 1);
        }
        if (text.length() == 0) {
            numberField.setCursorVisible(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void addSymbolInNumber(char symbol) {
        StringBuilder text = new StringBuilder(numberField.getText());
        int selectionStart = numberField.getSelectionStart();
        int selectionEnd = numberField.getSelectionEnd();
        if (selectionStart != selectionEnd) {
            text.delete(selectionStart, selectionEnd);
        }
        text.insert(selectionStart, symbol);
        numberField.setText(text);
        numberField.setSelection(selectionStart + 1);
        if (!numberField.isCursorVisible()) {
            numberField.setCursorVisible(true);
        }
    }

    private void clearNumber() {
        numberField.setText("");
        numberField.setCursorVisible(false);
    }

    private void setButtonListeners() {
        for (int i = 0; i < buttonIds.length; i++) {
            view.findViewById(buttonIds[i]).setOnClickListener(this);
            view.findViewById(buttonIds[i]).setOnLongClickListener(this);
        }
    }

    public boolean isNumpadVisible() {
        View panel = view.findViewById(R.id.panel_number_input);
        return panel.getVisibility() == View.VISIBLE;
    }

    private void toggleNumpad() {
        if (isNumpadVisible()) {
            hideNumpad();
        } else {
            showNumpad();
        }
    }

    public void hideNumpad() {
        view.findViewById(R.id.panel_number_input).setVisibility(View.GONE);
        view.findViewById(R.id.numpad).setVisibility(View.GONE);
        //   view.findViewById(R.id.btn_call).setVisibility(View.INVISIBLE);
    }

    private void showNumpad() {
        view.findViewById(R.id.panel_number_input).setVisibility(View.VISIBLE);
        view.findViewById(R.id.numpad).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btn_call).setVisibility(View.VISIBLE);
    }

    private void openMessagingApp(String number) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:" + number));
        try {
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    private void callNumber(String number) {
        if (TextUtils.isEmpty(number) || null == number) {
            return;
        }

        Intent intent1 = new Intent(ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getActivity().getPackageName());
        if (intent1.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent1, 1102);
        } else {
            Log.w(getActivity().getLocalClassName(), "No Intent available to handle action");
        }
        Uri uri = Uri.parse("tel:" + Uri.encode(number));
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        startActivity(intent);
        getActivity().finish();
    }

    public void createContact(String number) {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION, ContactsContract.Contacts.CONTENT_URI);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMissingContactsAppDialog();
        }
    }

    private void dialNumber(String number) {
        showNumpad();
        numberField.setText(number);
        numberField.setCursorVisible(true);
    }

    private void setContactsMode() {
        if (mode == CONTACTS_MODE)
            return;

        mode = CONTACTS_MODE;
        list.setAdapter(contactsEntryAdapter);
    }

    private void setCallLogMode() {
        if (mode == CALL_LOG_MODE)
            return;


        mode = CALL_LOG_MODE;
        list.setAdapter(logEntryAdapter);
    }


    private void showDeviceId() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String deviceId;

        if (Build.VERSION.SDK_INT < 26) {
            if (checkSelfPermission(getActivity(),
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            deviceId = telephonyManager.getDeviceId();
        } else {
            switch (telephonyManager.getPhoneType()) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    deviceId = telephonyManager.getImei();
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    deviceId = telephonyManager.getMeid();
                    break;
                default:
                    deviceId = "null";
            }
        }
        builder.setMessage(deviceId);
        builder.create().show();
    }

    private void showPrivacyPolicyDialog(final SharedPreferences.Editor editor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.privacy_policy_title));
        builder.setMessage(getResources().getString(R.string.privacy_policy));
        builder.setPositiveButton(R.string.accept,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int which) {
                        editor.putBoolean("privacy_policy", true).commit();
                    }
                });
        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int which) {
                        getActivity().finish();
                    }
                });

        builder.create().show();
    }


    private void showInformationDialog(long id) {

        @SuppressLint("MissingPermission") Cursor cursor = getActivity().getContentResolver().query(CallLog.Calls.CONTENT_URI, CALL_INFORMATION_PROJECTION, CallLog.Calls._ID + "=?", new String[]{((Long) id).toString()}, null);
        if (cursor.getCount() == 0 || null == cursor)
            return;

        cursor.moveToNext();
        long date = cursor.getLong(1);
        long duration = cursor.getLong(2);

        DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.LONG);
        DateFormat timeInstance = DateFormat.getTimeInstance(DateFormat.MEDIUM);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String message = String.format("%1s: %2s, %3s\n%4s: %5s",
                getResources().getString(R.string.date), timeInstance.format(date),
                dateInstance.format(date), getResources().getString(R.string.duration),
                DateUtils.formatElapsedTime(duration));
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.create().show();
        cursor.close();
    }

    @SuppressLint("MissingPermission")
    private void deleteCallLogEntry(long id) {
        getActivity().getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + "=?", new String[]{((Long) id).toString()});
        logEntryAdapter.update();
    }

    private void deleteCallLogEntryDialog(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.delete_call_log_entry_question));
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int which) {
                        deleteCallLogEntry(id);
                    }
                });
        builder.setNegativeButton(android.R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int which) {

                    }
                });

        builder.create().show();
    }

    private void showLogEntryDialog(final int position, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String number = logEntryAdapter.getPhoneNumber(position);
        builder.setCancelable(true);
        builder.setTitle(number);
        String[] commands = new String[]{
                getResources().getString(R.string.show_info),
                getResources().getString(R.string.make_a_call),
                getResources().getString(R.string.send_message),
                getResources().getString(R.string.delete_log_entry),
                getResources().getString(R.string.copy_number)
        };
        ArrayAdapter dialogAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, commands);
        DialogInterface.OnClickListener onDialogItemClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int which) {
                switch (which) {
                    case 0:
                        showInformationDialog(id);
                        break;
                    case 1:
                        callNumber(number);
                        break;
                    case 2:
                        openMessagingApp(number);
                        break;
                    case 3:
                        deleteCallLogEntry(id);
                        break;
                    case 4:
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", number);
                        clipboard.setPrimaryClip(clip);
                        break;
                }
            }
        };
        builder.setAdapter(dialogAdapter, onDialogItemClick);
        builder.create().show();
    }

    public void showMissingContactsAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.contacts_app_is_missing));
        builder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int which) {
                    }
                });
        builder.create().show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView.getAdapter() instanceof LogEntryAdapter) {
            callNumber(logEntryAdapter.getPhoneNumber(position));
        } else if (adapterView.getAdapter() instanceof ContactsEntryAdapter) {
            callNumber(contactsEntryAdapter.getPhoneNumber(position));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView.getAdapter() instanceof LogEntryAdapter) {
            showLogEntryDialog(position, id);
            return true;
        }
        return false;
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String number = s.toString();
        if (start == 0 && before == 0 && count == 0) {
            return;
        } else if (TextUtils.isEmpty(s) && before > 0) {
            setCallLogMode();
            contactsEntryAdapter.resetFilter();
            list.setSelection(0);
        } else if (number.equals("*#06#")) {
            showDeviceId();
        } else if (number.startsWith("*#*#") && number.endsWith("#*#*")) {
            String secretCode = new StringBuilder(number).substring(4, number.length() - 4);
            getActivity().sendBroadcast(new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + secretCode)));
        } else {
            setContactsMode();
            contactsEntryAdapter.getFilter().filter(s);
            list.setSelection(0);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_numpad_0:
                addSymbolInNumber('+');
                break;
            case R.id.btn_numpad_1:
                if (checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return true;
                }
                String voiceMailNumber = telephonyManager.getVoiceMailNumber();
                if (null != voiceMailNumber) {
                    callNumber(voiceMailNumber);
                } else {
                    addSymbolInNumber('1');
                }
                break;
            case R.id.btn_numpad_2: callNumber(SpeedDial.getNumber(getActivity(), "2")); break;
            case R.id.btn_numpad_3: callNumber(SpeedDial.getNumber(getActivity(), "3")); break;
            case R.id.btn_numpad_4: callNumber(SpeedDial.getNumber(getActivity(), "4")); break;
            case R.id.btn_numpad_5: callNumber(SpeedDial.getNumber(getActivity(), "5")); break;
            case R.id.btn_numpad_6: callNumber(SpeedDial.getNumber(getActivity(), "6")); break;
            case R.id.btn_numpad_7: callNumber(SpeedDial.getNumber(getActivity(), "7")); break;
            case R.id.btn_numpad_8: callNumber(SpeedDial.getNumber(getActivity(), "8")); break;
            case R.id.btn_numpad_9: callNumber(SpeedDial.getNumber(getActivity(), "9")); break;
            case R.id.btn_remove_number: clearNumber(); break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 0) {
            return new CursorLoader(getActivity(), CallLog.Calls.CONTENT_URI, LogEntryAdapter.PROJECTION, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        } else {
            return new CursorLoader(getActivity(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, ContactsEntryAdapter.PROJECTION, ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER+"=1", null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == 0) {
            logEntryAdapter.swapCursor(data);
        } else {
            contactsEntryAdapter.setCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        if (id == 0) {
            logEntryAdapter.swapCursor(null);
        } else {
            contactsEntryAdapter.setCursor(null);
        }
    }





}
