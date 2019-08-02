package com.multitech.skydailer.constantvalues.speedDail;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import com.multitech.skydailer.R;
import com.multitech.skydailer.constantvalues.DialerApp;
import com.multitech.skydailer.constantvalues.MainActivity;
import com.multitech.skydailer.constantvalues.pickContact.PickContactNumberActivity;
import com.multitech.skydailer.constantvalues.speedDail.speedDialAdapter.SpeedDialAdapter;

import static android.app.Activity.RESULT_OK;

public class SpeedDialFragmnt extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	
	private static final int PICK_CONTACT_NUMBER = 1;
	public static final String CONTACT_NUMBER = "ru.henridellal.dialer.contact_number";
	public static final String SPEED_DIAL_SLOT = "ru.henridellal.dialer.speed_dial_slot";
	
	private ListView list;
	private SpeedDialAdapter mAdapter;


	View view;


 	@Override
	public View onCreateView(LayoutInflater inflater,   ViewGroup container, Bundle savedInstanceState) {
		DialerApp.setTheme(getActivity());
		view =  inflater.inflate(R.layout.fragment_speed_dial, container, false);
		MainActivity.headerTxt.setText("Speed Dial");

		mAdapter = new SpeedDialAdapter(getActivity());
		list = view.findViewById(R.id.speed_dial_entries);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);



		return view;
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_CONTACT_NUMBER && resultCode == RESULT_OK && null != data) {
			String number = data.getStringExtra(CONTACT_NUMBER);
			String speedDialSlot = data.getStringExtra(SPEED_DIAL_SLOT);
			SpeedDial.setNumber(getActivity(), speedDialSlot, number);
			mAdapter.update();
		}
	}

	public SpeedDialAdapter getAdapter() {
		return mAdapter;
	}

	private void clearSpeedDialSlotDialog(final String order, String contactInfo) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(contactInfo);
		builder.setMessage(getResources().getString(R.string.remove_speed_dial_entry));
		builder.setPositiveButton(android.R.string.yes,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int which) {
					SpeedDial.clearSlot(getActivity(), order);
					getAdapter().update();
				}
			});
		builder.setNegativeButton(android.R.string.no,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int which) {

				}
			});

		builder.create().show();
	}

	private void openSpeedDialPreferenceDialog(final String order) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.speed_dial_pref_dialog, null);
		builder.setView(dialogView);
		((TextView)dialogView.findViewById(R.id.speed_dial_number_field)).setText(SpeedDial.getNumber(getActivity(), order));
		builder.setPositiveButton(android.R.string.ok,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int which) {
					SpeedDial.setNumber(getActivity(), order, ((TextView)dialogView.findViewById(R.id.speed_dial_number_field)).getText().toString());
					getAdapter().update();
				}
			});
		builder.setNeutralButton(R.string.pick_contact_number,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int which) {
					Intent intent = new Intent(getActivity(), PickContactNumberActivity.class);
					intent.putExtra(SPEED_DIAL_SLOT, order);
					startActivityForResult(intent, PICK_CONTACT_NUMBER);
				}
			});

		builder.create().show();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		if (position == 0) {
			if (Build.VERSION.SDK_INT >= 23) {
				Intent intent = new Intent(TelephonyManager.ACTION_CONFIGURE_VOICEMAIL);
				startActivity(intent);
			}
			return;
		}
		String order = ((TextView)view.findViewById(R.id.entry_order)).getText().toString();
		openSpeedDialPreferenceDialog(order);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
		if (position == 0) {
			return false;
		}
		String order = ((TextView)view.findViewById(R.id.entry_order)).getText().toString();
		if (SpeedDial.getNumber(getActivity(), order).length() != 0) {
			String speedDialData = ((TextView)view.findViewById(R.id.entry_title)).getText().toString();
			clearSpeedDialSlotDialog(order, speedDialData);
			return true;
		}
		return false;
	}
	
}
