package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.xmlpull.v1.XmlSerializer;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

/**
 * WriteConfig - Utility to save Panel Config
 *
 * @author Michael Blank
 * 
 * @version 1.0
 */


public class WriteConfig {
	
    /** 
     * writeConfigToXML 
     * 
     * saves all PanelElements (including deducted elements) to an XML file
     *
     * @param 
     * @return true, if succeeds - false, if not.
     */

	public static boolean writeToXML() {

		
		boolean mExternalStorageWriteable = false;
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
            try {
            	File dir = new File(Environment.getExternalStorageDirectory(),DIRECTORY);
            	dir.mkdir();  // make DIRECTORY - if needed
            	File from = new File(Environment.getExternalStorageDirectory(),DIRECTORY+configFilename);
            	String suffix = Utils.getDateTime();
            	File to = new File(Environment.getExternalStorageDirectory(),DIRECTORY+configFilename+"."+suffix);
            	from.renameTo(to);
            } catch (Exception e) {
				Log.e(TAG,"Error in renaming old config file: "+e.getMessage());	
            }
            FileWriter fWriter=null;
			try{
				fWriter = new FileWriter(Environment.getExternalStorageDirectory()+"/"+DIRECTORY+configFilename);
				fWriter.write(writeXml());
				fWriter.flush();
				fWriter.close();

				if (DEBUG) Log.d(TAG,"Config File saved!");
				configHasChanged = false; // reset flag

			} catch (Exception e) {
				Log.e(TAG,"Exception: "+e.getMessage());
				return false;
			} finally {
				if (fWriter != null) {
					try {
						fWriter.close();
					} catch (IOException e) {
						Log.e(TAG,"could not close output file!");
					}
				}
			}
		} else {
			Log.e(TAG,"external storage not writeable!");
			return false;
		}
		return true;
	}

	private static String writeXml(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.text("\n");
			serializer.startTag("", "layout-config");   // namespace ="" always
			serializer.text("\n");
			serializer.startTag("", "panel");
			serializer.attribute("", "name", panelName);
			serializer.text("\n");
			for (PanelElement pe: panelElements){
				if (DEBUG) Log.d(TAG,"writing panel element "+pe.toString());
				serializer.startTag("", pe.getType());
				if (DEBUG) Log.d(TAG," type="+pe.type);
				if (pe.name.length()>0) {
					serializer.attribute("", "name", ""+pe.name);
				}

				serializer.attribute("", "x", ""+pe.x);
				serializer.attribute("", "y", ""+pe.y);
				if (pe.x2 != INVALID_INT) {	// save only valid attributes
					serializer.attribute("", "x2", ""+pe.x2);
					serializer.attribute("", "y2", ""+pe.y2);
				}
				if (pe.xt != INVALID_INT) {
					serializer.attribute("", "xt", ""+pe.xt);
					serializer.attribute("", "yt", ""+pe.yt);
				}
				if (pe.getSxAdr() != INVALID_INT) {
					serializer.attribute("", "sxadr", ""+pe.getSxAdr());
					serializer.attribute("", "sxbit", ""+pe.getSxBit());
				}
				serializer.endTag("",  pe.getType());
				serializer.text("\n");
			}
			serializer.endTag("", "panel");
			serializer.text("\n");
			serializer.endTag("", "layout-config");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

}

