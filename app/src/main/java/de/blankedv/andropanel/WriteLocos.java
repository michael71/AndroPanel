package de.blankedv.andropanel;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import static de.blankedv.andropanel.AndroPanelApplication.*;


/**
 * WriteConfig - Utility to save Panel Config
 *
 * @author Michael Blank
 * 
 * @version 1.0
 */


public class WriteLocos {
	
    /** 
     * writeConfigToXML 
     * 
     * saves all Locos to an XML file
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
            	File from = new File(Environment.getExternalStorageDirectory(),DIRECTORY+ locoConfigFilename);
            	String suffix = Utils.getDateTime();
            	File to = new File(Environment.getExternalStorageDirectory(),DIRECTORY+ locoConfigFilename +"."+suffix);
            	from.renameTo(to);
            } catch (Exception e) {
				Log.e(TAG,"Error in renaming old loco config file: "+e.getMessage());
            }
            FileWriter fWriter=null;
			try{
                String fname = Environment.getExternalStorageDirectory()+"/"+DIRECTORY+ locoConfigFilename;
                if (DEBUG) Log.d(TAG,"writing locos to "+fname);
                fWriter = new FileWriter(fname);
				fWriter.flush();
				fWriter.close();

				if (DEBUG) Log.d(TAG,"Loco Config File saved!");
				locoConfigHasChanged = false; // reset flag

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

	/*
	example file

	<loco-config>
	<locolist name="demo-loco-list">
	<loco adr="22" name="Lok22" mass="2"/><loco adr="97" name="SchönBB" mass="2"/>
	<loco adr="44" name="CSX4416" mass="4"/><loco adr="27" name="ET423-1" mass="2"/>
	</locolist></loco-config>
	 */
	private static String writeXml(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.text("\n");
			serializer.startTag("", "loco-config");   // namespace ="" always
			serializer.text("\n");
			serializer.startTag("", "locolist");
			serializer.attribute("", "name", locolistName);
			serializer.text("\n");
			for (Loco l: locolist){
	 			if (DEBUG) Log.d(TAG,"writing loco ");
				serializer.startTag("","loco");
				serializer.attribute("", "adr", ""+l.adr);
				serializer.attribute("", "name", ""+l.name);
				serializer.attribute("", "mass", ""+l.mass);
				serializer.attribute("", "vmax", ""+l.vmax);
				serializer.endTag("",  "loco");
				serializer.text("\n");

			}

			serializer.endTag("", "locolist");
			serializer.text("\n");
			serializer.endTag("", "loco-config");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

}

