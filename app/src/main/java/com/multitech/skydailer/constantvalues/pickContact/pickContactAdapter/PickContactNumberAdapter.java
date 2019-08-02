package com.multitech.skydailer.constantvalues.pickContact.pickContactAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.multitech.skydailer.R;


public class PickContactNumberAdapter extends CursorAdapter {
	
	public PickContactNumberAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((TextView)view.findViewById(R.id.pick_contact_name)).setText(cursor.getString(1));
		String number = cursor.getString(2);
		((TextView)view.findViewById(R.id.pick_phone_number)).setText(number);
		view.setTag(number);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.pick_number_item, parent, false);
		return view;
	}
	
}
