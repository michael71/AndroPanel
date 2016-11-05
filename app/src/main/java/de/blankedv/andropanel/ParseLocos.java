package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.TAG;
import static de.blankedv.andropanel.AndroPanelApplication.DEBUG;
import static de.blankedv.andropanel.AndroPanelApplication.DIRECTORY;
import static de.blankedv.andropanel.AndroPanelApplication.DEMO_LOCOS_FILE;
import static de.blankedv.andropanel.AndroPanelApplication.INVALID_INT;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ParseLocos {

	/**
	 * 
	 * read all Locos from a configuration (XML) file results will be put into
	 * global variable "locos"
	 * 
	 * @param context
	 * @return true, if succeeds - false, if not.
	 * 
	 */
	public static LocoList readLocosFromFile(Context context, String filename) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		LocoList mylist = new LocoList();
		
//		SharedPreferences prefs = PreferenceManager
//				.getDefaultSharedPreferences(context);
//		locosFilename = prefs.getString(KEY_LOCOS_FILE, DEMO_LOCOS_FILE);

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			Log.e(TAG, "ParserLocosException Exception - " + e1.getMessage());
			Toast.makeText(context, "Parser Locos Exception - check file:"+filename, Toast.LENGTH_LONG)
					.show();
			return null;
		}
		Document doc;

		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = false;
		}

		if (mExternalStorageAvailable) {
			try {
				File f = new File(Environment.getExternalStorageDirectory()
						+ "/" + DIRECTORY + filename);
				FileInputStream fis;
				InputStream demoIs = null;
				if (!f.exists()) {
					Toast.makeText(
							context,
							"No Loco Config file found (" + DIRECTORY
									+ filename + ") - using demo locos",
							Toast.LENGTH_SHORT).show();
					demoIs = context.getAssets().open(DEMO_LOCOS_FILE);

					try {
						doc = builder.parse(demoIs);
						mylist = parseDoc(doc);
					} catch (SAXException e) {
						Log.e(TAG, "SAX Exception - " + e.getMessage());
					}

				} else {
					fis = new FileInputStream(f);
					try {
						doc = builder.parse(fis);
						mylist = parseDoc(doc);
					} catch (SAXException e) {
						Log.e(TAG, "SAX Exception - " + e.getMessage());
					}
				}
				if (demoIs != null) {
					demoIs.close();
					copyDemoFile(context);
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "loco config loaded from " + filename);
		} else {

			Toast.makeText(context, "ERROR:External storage not readable",
					Toast.LENGTH_LONG).show();
			return null;
		}

		return mylist;
	}

	private static LocoList parseDoc(Document doc) {
		// assemble new ArrayList of tickets.
		//ArrayList<Loco> ls = new ArrayList<Loco>();
		LocoList mylocolist = new LocoList();
		NodeList items;
		Element root = doc.getDocumentElement();

		items = root.getElementsByTagName("locolist");

		mylocolist.name = parseName(items.item(0));
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " locolists, name="
					+ mylocolist.name);

		// look for Locos
		items = root.getElementsByTagName("loco");
		if (DEBUG)
			Log.d(TAG, "config: " + items.getLength() + " locos");
		for (int i = 0; i < items.getLength(); i++) {
			Loco l = parseLoco(items.item(i));
			if (l != null) {
				mylocolist.locos.add(l);
			}
		}

		return mylocolist;
	}

	private static Loco parseLoco(Node item) {

		Loco l = new Loco();
		l.adr = INVALID_INT;
		l.mass = 3; //default
		l.name ="";

		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG) Log.d(TAG,theAttribute.getNodeName() + "=" +
			// theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("name")) {
				l.name = theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("adr")) {
				int adr = Integer.parseInt(theAttribute.getNodeValue());
				if ((adr >= 1) && (adr <= 111)) {
					l.adr=adr;
				} else {
					Log.e(TAG, "ParseLoco: Error in loco adr. "+adr+" is invalid. Setting adr=1.");
				}
			} else if (theAttribute.getNodeName().equals("mass")) {
				int mass = Integer.parseInt(theAttribute.getNodeValue());
				if ((mass >= 1) && (mass <= 12)) {
					l.mass=mass;
				} else {
					if (mass > 12) l.mass=12;
					if (mass < 1)  l.mass=1;
					Log.e(TAG, "ParseLoco: Error in loco mass. "+mass+" is invalid.");
				}
			} else {
				if (DEBUG)
					Log.e(TAG,
							"ParseLoco: unknown attribute " + theAttribute.getNodeName()
									+ " in config file");
			}
		}
        if (l.adr != INVALID_INT) {
        	return l;
        } else {
        	return null;
        }

	}

	private static String parseName(Node item) {
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			if (DEBUG) 	Log.d(TAG,	theAttribute.getNodeName() + "="
								+ theAttribute.getNodeValue());

			if (theAttribute.getNodeName().equals("name")) {
				String name = theAttribute.getNodeValue();
				return name;
			}
		}
		return "";
	}

	private static void copyDemoFile(Context context) {
		InputStream in;
		OutputStream out;
		try {
			in = context.getAssets().open(DEMO_LOCOS_FILE);
			out = new FileOutputStream(
					Environment.getExternalStorageDirectory() + "/" 
							+ DIRECTORY   // DIR.. contains trailing slash
							+ DEMO_LOCOS_FILE);
			copyFile(in, out);
			in.close();
			out.close();
		} catch (IOException e) {
			Log.e(TAG, "Failed to copy asset file: " + DEMO_LOCOS_FILE + " "
					+ e.getMessage());
		}

	}

	private static void copyFile(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
	// private static int getValue(String s) {
	// float b= Float.parseFloat(s);
	// return (int)b;
	// }

	// private static String getConcatNodeValues(Node prop) {
	// // behaves well for non-existing nodes and for node values which are
	// // broken into several values because of special characters like '"'
	// // needed for the android code - this problem only exists in
	// // Android xml library and not on the PC !!
	// if (prop.hasChildNodes()) { // false for optional attributes
	// StringBuilder text = new StringBuilder();
	// NodeList chars = prop.getChildNodes();
	// for (int k=0;k<chars.getLength();k++){
	// text.append(chars.item(k).getNodeValue());
	// }
	// return text.toString().trim();
	// } else {
	// return (""); // return empty string if empty
	// }
	// }

}
