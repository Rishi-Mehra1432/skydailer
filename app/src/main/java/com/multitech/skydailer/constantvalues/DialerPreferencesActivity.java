package com.multitech.skydailer.constantvalues;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;

import com.multitech.skydailer.R;


public class DialerPreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private boolean restartTriggered;
	private SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		ListPreference themePreference = (ListPreference) findPreference("theme");
		themePreference.setSummary(themePreference.getEntry());
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
		Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
		root.addView(bar, 0); // insert at top
		bar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});


	}
	
	@Override
	protected void onResume() {
		super.onResume();
		preferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		preferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onBackPressed() {
		if (restartTriggered) {
			startActivity(new Intent(this, MainActivity.class));
			finishAffinity();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Preference pref = findPreference(key);
		if (pref instanceof ListPreference) {
			ListPreference listPreference = (ListPreference) pref;
			listPreference.setSummary(listPreference.getEntry());
		}
	
		if ("theme".equals(key)) {
			restartTriggered = true;
		}
	}
}
