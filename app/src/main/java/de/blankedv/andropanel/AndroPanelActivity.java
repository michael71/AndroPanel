package de.blankedv.andropanel;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import static de.blankedv.andropanel.AndroPanelApplication.*;
import static de.blankedv.andropanel.ParseConfig.*; // panelXmin etc from xml panel file

/**
 * AndroPanelActivity is the activity of the sx net panel
 * 
 * @author mblank
 * 
 */
public class  AndroPanelActivity extends Activity {  //implements ServiceListener {
	Builder builder;

	public static PopupWindow popUp;
	public static LinearLayout layout;
	TextView tv;
	LayoutParams params;

	Button but;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ActionBar actionBar = getActionBar();
		// actionBar.show();
		if (DEBUG)
			Log.d(TAG, "onCreate AndroPanelActivity");
		popUp = new PopupWindow(this);
		layout = new LinearLayout(this);
		appContext = this;
		tv = new TextView(this);

		but = new Button(this);
		but.setText("Click Me");

		params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		tv.setText("popup window with sx.address...");
		layout.addView(tv, params);
		popUp.setContentView(layout);

		ParseConfig.readConfigFromFile(this);
		recalcScale();

		loadLocos();

		setContentView(new Panel(this));

		getDisplayInfo();

		builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to exit?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								shutdownSXClient();
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		if (client == null) {
			startSXNetCommunication();
		}

		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				counter++;
				selectedLoco.timer();
                if (restartCommFlag) {
                    restartCommFlag = false;
                    startSXNetCommunication();
                }
            }
		}, 100, 100);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (DEBUG)
			Log.d(TAG, "onBackPressed - AndroPanelActivity");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG)
			Log.d(TAG, "onStop - AndroPanelActivity");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG)
			Log.d(TAG, "onPause - AndroPanelActivity");
		// firstStart=false; // flag to avoid re-connection call during first
		// start
		// sendQ.add(DISCONNECT);
		((AndroPanelApplication) getApplication()).saveZoomEtc();
		if (configHasChanged) {
			WriteConfig.writeToXML();
		}
		if (locoConfigHasChanged) {
			WriteLocos.writeToXML();
		}
	}

	public void shutdownSXClient() {
		Log.d(TAG, "AndroPanelActivity - shutting down SXnet Client.");
		if (client != null)
			client.shutdown();
		if (client != null)
			client.disconnectContext();
		client = null;

	}

	public void getDisplayInfo() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Log.d(TAG, "Display in px is " + metrics.widthPixels+ " x " + metrics.heightPixels);

		Log.d(TAG, "Display density is " + metrics.densityDpi);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (DEBUG)
			Log.d(TAG, "onResume - AndroPanelActivity");

		if (reinitPaints) {
			LinePaints.init(appContext);
		}



		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String cfFilename = prefs.getString(KEY_CONFIG_FILE, "-");
		if (cfFilename != configFilename) {
			// reload, if a new panel config file selected
			if (DEBUG)
				Log.d(TAG, "onResume - reloading panel config.");
			ParseConfig.readConfigFromFile(this); // reload config File with scaling
			((AndroPanelApplication) getApplication()).loadZoomEtc();
			recalcScale();
		} else {
			((AndroPanelApplication) getApplication()).loadZoomEtc(); // reload
            // settings without scaling
		}
		String lfFilename = prefs.getString(KEY_LOCOS_FILE, "-");
		if (lfFilename != locoConfigFilename) {
			// reload, if a new loco config file was selected
			if (DEBUG) {
                Log.d(TAG, "onResume - reloading loco config file.");
            }
			loadLocos();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
 		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_settings: // call preferences activity
			startActivity(new Intent(this, Preferences.class));
			return (true);
		case R.id.menu_reconnect:
			startSXNetCommunication();
			return (true);
		case R.id.menu_center:
			recalcScale();
			return (true);
		case R.id.menu_about:
			startActivity(new Intent(this, AboutActivity.class));
			return (true);
		case R.id.menu_quit:
			AlertDialog alert = builder.create();
			alert.show();
			return (true);
		default:
			return super.onOptionsItemSelected(item);
		}
		
		
		
	}

	public void startSXNetCommunication() {
		Log.d(TAG, "AndroPanelActivity - startSXNetCommunication.");
		if (client != null) {
			client.shutdown();
			try {
				Thread.sleep(100); // give client some time to shut down.
			} catch (InterruptedException e) {
				if (DEBUG)
					Log.e(TAG, "could not sleep...");
			}
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);


        String ip = prefs.getString(KEY_IP, "192.168.178.31");


		//locolist.selectedLoco.adr = Integer
		//		.parseInt(prefs.getString(KEY_LOCO_ADR, "22"));
		drawSXAddresses = prefs.getBoolean(KEY_SHOW_SX, false);
		drawXYValues = prefs.getBoolean(KEY_SHOW_XY_VALUES, false);

		client = new SXnetClientThread(this, ip, SXNET_PORT);
		client.start();
		requestSXdata();
	}

	private void recalcScale() {
		Float sc;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		if (DEBUG)
			Log.i(TAG, "metrics - w=" + width + "  h=" + height);

		sc = (width * 0.4f) / ((panelXmax - panelXmin) * 1.0f);
		// Don't let the object get too small or too large.
		scale = Math.max(0.4f, Math.min(sc, 3.0f));

		xoff = ((width - (panelXmax - panelXmin) * scale * 2)) / 2;
		// for y we need to correct for the upper 20% of the display which is
		// control area
		// and not scaled
		yoff = (((height * 0.8f) - ((panelYmax - panelYmin) * scale * 2)) / 2)
				+ (height / (10 * scale * 2));

		Log.d(TAG, "new scale=" + scale + " xoff=" + xoff + " yoff=" + yoff);
	}

	private void loadLocos() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int lastLocoAddress = Integer.parseInt(prefs.getString(KEY_LOCO_ADR, "22"));  // last used loco address

        locoConfigFilename = prefs.getString(KEY_LOCOS_FILE, DEMO_LOCOS_FILE);
		
		ParseLocos.readLocosFromFile(this,locoConfigFilename);
		
		if (locolist == null) {
			Toast.makeText(this, "could not read loco list xml file or errors in file", Toast.LENGTH_LONG ).show();
            Log.e(TAG,  "could not read loco list xml file or errors in file: "+locoConfigFilename);
		} else {
			// if last loco (from stored loco_address) is in list then use this loco
			for (Loco loco : locolist) {
				if (loco.adr == lastLocoAddress) {
					selectedLoco = loco; // update from file
					selectedLoco.initFromSX();
				}
			}
		
		}
		
		if (selectedLoco == null) { // use first loco in list or use default
			if ( locolist.size() >= 1) {
				selectedLoco = locolist.get(0);  // first loco in xml file
			} else {
				// as a default use a "dummy loco" 
				int locoMass = Integer
						.parseInt(prefs.getString(KEY_LOCO_MASS, "3"));
				String locoName = prefs.getString(KEY_LOCO_NAME, "default loco 22");
				selectedLoco = new  Loco (locoName, lastLocoAddress, locoMass, null);
				selectedLoco.initFromSX();
			}
		}
	}

}