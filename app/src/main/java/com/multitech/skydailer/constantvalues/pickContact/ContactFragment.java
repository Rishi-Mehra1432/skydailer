package com.multitech.skydailer.constantvalues.pickContact;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.multitech.skydailer.R;
import com.multitech.skydailer.constantvalues.MainActivity;
import com.multitech.skydailer.constantvalues.pickContact.model.Contact_Model;
import com.multitech.skydailer.constantvalues.pickContact.pickContactAdapter.Contact_Adapter;


import static com.multitech.skydailer.constantvalues.MainActivity.arrayList;




public class ContactFragment extends android.app.Fragment {

   View view;
   private ListView contactList;

    private static Contact_Adapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contact, container, false);
        MainActivity.headerTxt.setText("Contact List");

        contactList = (ListView)view.findViewById(R.id.contact_list);

        // If array list is not null and is contains value
        if (arrayList != null && arrayList.size() > 0) {

            // then set total contacts to subtitle

            adapter = null;
            if (adapter == null) {
                adapter = new Contact_Adapter(getActivity(), arrayList);
                contactList.setAdapter(adapter);// set adapter
            }
            adapter.notifyDataSetChanged();
        } else {
            // If adapter is null then show toast
            Toast.makeText(getActivity(), "There are no contacts.",Toast.LENGTH_LONG).show();
        }

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact_Model contact_model = adapter.getItem(position);
                String number = contact_model.getContactNumber();
                callNumber(number);
            }
        });

        return  view;
    }


    private void callNumber(String number) {
        if (TextUtils.isEmpty(number) || null == number) {
            return;
        }

        Uri uri = Uri.parse("tel:" + Uri.encode(number));
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        startActivity(intent);
        getActivity().finish();
    }


}
