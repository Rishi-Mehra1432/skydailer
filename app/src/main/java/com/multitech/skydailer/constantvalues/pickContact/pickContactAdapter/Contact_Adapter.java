package com.multitech.skydailer.constantvalues.pickContact.pickContactAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.multitech.skydailer.R;
import com.multitech.skydailer.constantvalues.pickContact.model.Contact_Model;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Contact_Adapter extends BaseAdapter {
	private Context context;
	private ArrayList<Contact_Model> arrayList;

	public Contact_Adapter(Context context, ArrayList<Contact_Model> arrayList) {
		this.context = context;
		this.arrayList = arrayList;
	}

	@Override
	public int getCount() {

		return arrayList.size();
	}

	@Override
	public Contact_Model getItem(int position) {

		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Contact_Model model = arrayList.get(position);
		ViewHodler holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.contact_custom_view,parent, false);
			holder = new ViewHodler();
			holder.contactImage = (CircleImageView) convertView
					.findViewById(R.id.contactImage);
			holder.contactName = (TextView) convertView
					.findViewById(R.id.contactName);

			holder.contactNumber = (TextView) convertView
					.findViewById(R.id.contactNumber);

			convertView.setTag(holder);
		} else {
			holder = (ViewHodler) convertView.getTag();
		}

		// Set items to all com.example.constantValues.view
		if (!model.getContactName().equals("")
				&& model.getContactName() != null) {
			holder.contactName.setText(model.getContactName());
		} else {
			holder.contactName.setText("No Name");
		}


		if (!model.getContactNumber().equals("")
				&& model.getContactNumber() != null) {
			holder.contactNumber.setText( model.getContactNumber());
		} else {
		//	holder.contactNumber.setText("No Contact Number");
		}



		// Bitmap for imageview
		Bitmap image = null;
		if (!model.getContactPhoto().equals("")
				&& model.getContactPhoto() != null) {
			image = BitmapFactory.decodeFile(model.getContactPhoto());// decode
																		// the
																		// image
																		// into
																		// bitmap
			if (image != null)
				holder.contactImage.setImageBitmap(image);// Set image if bitmap
															// is not null
			else {
			//	image = BitmapFactory.decodeResource(context.getResources(), R.drawable.androhub_logo);// if bitmap is null then set
													// default bitmap image to
													// contact image
				holder.contactImage.setImageResource(R.drawable.userimage);
			}
		} else {
		//	image = BitmapFactory.decodeResource(context.getResources(), R.drawable.androhub_logo);
			holder.contactImage.setImageBitmap(image);
		}
		return convertView;
	}

	// View holder to hold views
	private class ViewHodler {
		CircleImageView contactImage;
		TextView contactName, contactNumber, contactEmail, contactOtherDetails;
	}
}
