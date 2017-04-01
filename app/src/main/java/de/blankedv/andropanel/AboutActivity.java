package de.blankedv.andropanel;

/*  (c) 2012, Michael Blank
 * 
 *  This file is part of SRCP Client.

    SRCP Client is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SRCP Client is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

*/


import android.app.Activity;
import static de.blankedv.andropanel.AndroPanelApplication.*;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static de.blankedv.andropanel.AndroPanelApplication.connString;

public class AboutActivity extends Activity {

	private Button cancel;
	private String vinfo="";
	private TextView versTv,connTo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);
		versTv = (TextView)findViewById(R.id.version);
		connTo = (TextView)findViewById(R.id.connected_to);
		
		int version = -1;
		String vName="";

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		String dsp = "\n Display:  " + metrics.widthPixels+ " x " + metrics.heightPixels + " Pixel," +
		 " \nDisplay densityDpi: " + metrics.densityDpi + "\nDisplay density:" + metrics.density + "   " + getDpiString(metrics.density);

		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			version = pInfo.versionCode;
			vName =pInfo.versionName;
			vinfo = "Version: "+vName + "  (" + version + ")" ;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		 
		versTv.setText(vinfo.toString() + dsp);
		
		if (connString.length() >0) { 
			connTo.setText("connected to: "+connString);
		}
		else
		{
			connTo.setText("currently not connected to any SXnet server");
		}

		cancel = (Button)findViewById(R.id.cancel);

		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  
				finish();
			}

		});
		
//		upload = (Button)findViewById(R.id.upload);
		
//		if ((DEBUG) && (debugFileEnabled) ){
//			upload.setVisibility(View.VISIBLE);
//
//			upload.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {  
//					app.uploadDebugFile();
//				}
//
//			});
//		}
//		else  {
//			// don't show upload button when there is no debug log file.
//			upload.setVisibility(View.GONE);
//		}
	}

	public static String getDpiString(double density) {

		if (density >= 4.0) {
			 return "xxxhdpi";
		} else if (density >= 3.0 && density < 4.0) {
			 return "xxhdpi";
		} else if (density >= 2.0) {
			return "xhdpi";
		} else if (density >= 1.5 && density < 2.0) {
			return "hdpi";
		} else if (density >= 1.3 && density < 1.5) {
			return "tvdpi";
		} else if (density >= 1.0 && density < 1.3) {
			return "mdpi";
		}
		return "?";
	}
}