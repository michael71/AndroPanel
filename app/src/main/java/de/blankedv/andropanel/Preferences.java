package de.blankedv.andropanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;

import static android.R.attr.key;
import static de.blankedv.andropanel.AndroPanelApplication.*;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {


	private EditTextPreference ipPref, portPref;
//	private CheckBoxPreference showSXPref, enableZoomPref, enableEditPref, enableDemoPref;
	private ListPreference configFilenamePref, locosFilenamePref;
	private ListPreference lp;
	private ListPreference selectStylePref;

	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 

		addPreferencesFromResource(R.xml.preferences); 

		ipPref = (EditTextPreference)getPreferenceScreen().findPreference(KEY_IP);

//		showSXPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_SHOW_SX);
//		enableZoomPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_ENABLE_ZOOM);
//		enableEditPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_ENABLE_EDIT);   
//		enableDemoPref = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_ENABLE_DEMO);  
		configFilenamePref = (ListPreference)getPreferenceScreen().findPreference(KEY_CONFIG_FILE);  
		locosFilenamePref = (ListPreference)getPreferenceScreen().findPreference(KEY_LOCOS_FILE); 
		PreferenceCategory extCat = (PreferenceCategory) findPreference("extended_cat");

		String[] allfiles = allFiles();
		CharSequence[] entries = matchingFiles("panel",allfiles);
		CharSequence[] entryValues = entries;
		if (entries != null) configFilenamePref.setEntries(entries);
		if (entryValues != null) configFilenamePref.setEntryValues(entryValues);
		
		entries = matchingFiles("loco",allfiles);
		entryValues = entries;
		if (entries != null) locosFilenamePref.setEntries(entries);
		if (entryValues != null) locosFilenamePref.setEntryValues(entryValues);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
		
		configFilenamePref.setSummary("config loaded from "+prefs.getString(KEY_CONFIG_FILE,"-"));
	    locosFilenamePref.setSummary("locos loaded from "+prefs.getString(KEY_LOCOS_FILE,"-"));

		selectStylePref = (ListPreference)getPreferenceScreen().findPreference(KEY_STYLE_PREF);
		selectStylePref.setSummary("current selected style is "+selectedStyle);
		
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		// Let's do something if a preference value changes
		if (key.equals(KEY_IP)) {
			ipPref.setSummary("= "+sharedPreferences.getString(KEY_IP,""));
		} else  if (key.equals(KEY_SHOW_SX)) {
			drawSXAddresses = sharedPreferences.getBoolean(KEY_SHOW_SX,false);
		} else if (key.equals(KEY_SHOW_XY_VALUES)) {
			drawXYValues = sharedPreferences.getBoolean(KEY_SHOW_XY_VALUES,false);
		} else if (key.equals(KEY_ENABLE_ZOOM)) {
			zoomEnabled = sharedPreferences.getBoolean(KEY_ENABLE_ZOOM, false);
		}  else if (key.equals(KEY_ENABLE_DEMO)) {
			demoFlag = sharedPreferences.getBoolean(KEY_ENABLE_DEMO, false);
		} else if (key.equals(KEY_ENABLE_EDIT)) {
			enableEdit = sharedPreferences.getBoolean(KEY_ENABLE_EDIT, false);
		}  else  if (key.equals(KEY_CONFIG_FILE)) {
		    configFilenamePref.setSummary("config loaded from "+sharedPreferences.getString(KEY_CONFIG_FILE,"-"));
		}  else  if (key.equals(KEY_LOCOS_FILE)) {
		    locosFilenamePref.setSummary("locos loaded from "+sharedPreferences.getString(KEY_LOCOS_FILE,"-"));
		} else  if (key.equals( KEY_STYLE_PREF)) {
            selectedStyle = sharedPreferences.getString(KEY_STYLE_PREF, "US");
            Log.d(TAG, "selectedStyle = " + selectedStyle);
            selectStylePref.setSummary("current selected style is " + selectedStyle);
			reinitPaints = true;
        }

	}

	@Override
	protected void onResume() {
		super.onResume();

		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
		// Setup the initial values
		ipPref.setSummary("= "+prefs.getString(KEY_IP,""));

		// Set up a listener whenever a key changes            
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes            
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	}

    private String[] allFiles() {
    	boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			mExternalStorageAvailable = false;
		}

		if (mExternalStorageAvailable) {

			File dir = new File(Environment.getExternalStorageDirectory()+"/"+DIRECTORY);
			Log.d(TAG, "reading directory " + dir.getAbsolutePath());
			String[] allfiles = dir.list();

			return allfiles;
			
			
		} else {
            Log.d(TAG, "cannot read externalStorageDirectory ");
            return null;
        }

    }

    private String[] matchingFiles(String match, String[] all) {
		if (all == null) return null;
    	ArrayList<String> files = new ArrayList<String>();    	
    	for (String s : all) {
    	    int i = s.indexOf(match);
    	    if (i == 0) {
    	        // found a filename beginning with content of "match"
    	    	files.add(s);
    	    }
    	}
    //	return (String[]) files.toArray(); funktioniert nicht ...
    	int size = files.size();
    	if (size > 0) {
    		String[] fl = new String[size];
    		for (int i=0; i < size; i++) {
    			fl[i] = files.get(i);
    		}
    		return fl;
    	} else {
    		return null;
    	}
    }
    
 
}