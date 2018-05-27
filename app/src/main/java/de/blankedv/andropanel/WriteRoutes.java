package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.DEBUG;
import static de.blankedv.andropanel.AndroPanelApplication.DIRECTORY;

import static de.blankedv.andropanel.AndroPanelApplication.TAG;
import static de.blankedv.andropanel.AndroPanelApplication.panelName;
import static de.blankedv.andropanel.AndroPanelApplication.configFilename;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class WriteRoutes {
	/**
	 * writeRoutes
	 * 
	 * saves all Routes to an XML file
	 * 
	 * @param routes
	 * 
	 * @param
	 * @return true, if succeeds - false, if not.
	 */

	public static boolean writeToXML(ArrayList<Route> routes) {

		String routeFilename = "routes." + configFilename  /*Utils.getDateTime() + */ ;
		boolean mExternalStorageWriteable;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong.
			mExternalStorageWriteable = false;
		}

		if (mExternalStorageWriteable) {

			FileWriter fWriter = null;
			try {
				fWriter = new FileWriter(
						Environment.getExternalStorageDirectory() + "/"
								+ DIRECTORY + routeFilename);
				String content = writeXml(routes);
				if (DEBUG) Log.d("ROUTE",content);
				fWriter.write(content);
				fWriter.flush();
				fWriter.close();

				if (DEBUG)
					Log.d(TAG, "Route File saved!");

			} catch (Exception e) {
				Log.e(TAG, "Exception: " + e.getMessage());
				return false;
			} finally {
				if (fWriter != null) {
					try {
						fWriter.close();
					} catch (IOException e) {
						Log.e(TAG, "could not close output file!");
					}
				}
			}
		} else {
			Log.e(TAG, "external storage not writeable!");
			return false;
		}
		return true;
	}

	private static String writeXml(ArrayList<Route> routes) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		// ArrayList<TurnoutAndState> ts;
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.text("\n");
			serializer.startTag("", "routes-list"); // namespace ="" always
			serializer.text("\n");
			serializer.startTag("", "routes");
			serializer.attribute("", "name", panelName);
			serializer.text("\n");
			if (DEBUG)
				Log.d(TAG, "n-routes=" +routes.size());
			for (Route rt : routes) {
				if (DEBUG)
					Log.d(TAG, "writing route from [" + rt.start.x + ","
							+ rt.start.y + "] to [" + rt.stop.x + ","
							+ rt.stop.y + "]");
				serializer.startTag("", "route");

				serializer.attribute("", "startx", "" + rt.start.x);
				serializer.attribute("", "starty", "" + rt.start.y);
				if (rt.stop != null) {
					serializer.attribute("", "endx", "" + rt.stop.x);
					serializer.attribute("", "endy", "" + rt.stop.y);
				}
				if (rt.nTurnout > 0) {
					if (DEBUG)
						Log.d(TAG, "route has " + rt.nTurnout + " turnouts.");
					for (int i = 0; i < rt.nTurnout; i++) {
						if (DEBUG)
							Log.d(TAG, "i=" + i);
						if (rt.turnout[i] != null) {
							if (DEBUG)
								Log.d(TAG, "route-turnout at ["
										+ rt.turnout[i].x + ","
										+ rt.turnout[i].y + "] state="
										+ rt.turnoutState[i]);
							serializer.startTag("", "turnout");
							try {
								serializer.attribute("", "x", ""
										+ rt.turnout[i].x);
								serializer.attribute("", "y", ""
										+ rt.turnout[i].y);
								serializer.attribute("", "state", ""
										+ rt.turnoutState[i]);
							} catch (Exception e) {
								Log.e(TAG,"error"+e.getMessage());
							}
							serializer.endTag("", "turnout");
						}

						
					}
				}

				serializer.endTag("", "route");
				serializer.text("\n");
			}
			serializer.endTag("", "routes");
			serializer.text("\n");
			serializer.endTag("", "routes-list");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
