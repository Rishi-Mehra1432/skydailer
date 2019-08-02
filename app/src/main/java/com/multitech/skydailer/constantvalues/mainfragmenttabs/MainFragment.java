package com.multitech.skydailer.constantvalues.mainfragmenttabs;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.multitech.skydailer.R;
import com.multitech.skydailer.constantvalues.pickContact.ContactFragment;


public class MainFragment extends android.app.Fragment {
    View view;
    private TabLayout tabLayout;



    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        HomeFragment homeFragment = new HomeFragment();
        getFragmentManager().beginTransaction().replace(R.id.home_frag_container,homeFragment).commit();

        tabLayout=(TabLayout)view.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Recents"));
        tabLayout.addTab(tabLayout.newTab().setText("Contacts"));
        tabLayout.setTabTextColors(ColorStateList.valueOf(R.color.black));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:

                        HomeFragment homeFragment = new HomeFragment();
                        getFragmentManager().beginTransaction().replace(R.id.home_frag_container,homeFragment).commit();

                        break;
                    case 1:
                        ContactFragment contactFragment = new ContactFragment();
                        getFragmentManager().beginTransaction().replace(R.id.home_frag_container,contactFragment).commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;


    }

}
