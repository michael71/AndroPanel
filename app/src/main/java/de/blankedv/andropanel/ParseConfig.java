package de.blankedv.andropanel;

import java.io.File;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
import android.content.SharedPreferences;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import static de.blankedv.andropanel.AndroPanelApplication.*;

import static de.blankedv.andropanel.SXPanelElement.*;
import static de.blankedv.andropanel.Route.MAX_DEPTH;
/**
 * Parse Configuration from XML file
 * @author mblank
 *
 */
public class ParseConfig {

	 public static int panelXmin, panelXmax, panelYmin, panelYmax;
	 public static ArrayList<Route> routelist = new ArrayList<Route>();
	 
	 public static final String MYTAG = "ROUTE";
	 private static int routenumber = 0;
	 private static final int STEP = 2; // raster

 /** 
     * 
     * read all PanelElements from a configuration (XML) file
     * and add deducted turnouts if needed
     * results will be put into global variable "panelElements"
     * 
     * all dimension will be scaled with the global value of "prescale"
     * 
     * @param context
     * @return true, if succeeds - false, if not.
     * 
     */
	public static boolean readConfigFromFile(Context context)  {
		DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		long time0=System.currentTimeMillis(); 
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); 
		configFilename = prefs.getString(KEY_CONFIG_FILE,DEMO_FILE);	
		
		
		
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			Log.e(MYTAG,"ParserConfigException Exception - "+e1.getMessage());
			Toast.makeText(context, "ParserConfigException", Toast.LENGTH_LONG).show();
			return false;
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
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			mExternalStorageAvailable = false;
		}

		if (mExternalStorageAvailable) {
			try{
				File f = new File(Environment.getExternalStorageDirectory()+"/"+DIRECTORY+configFilename);
				FileInputStream fis;
				InputStream demoIs=null;
				if (!f.exists())  {
					Toast.makeText(context, "No Panel Config file found ("+DIRECTORY+configFilename+") - creating demo data", Toast.LENGTH_SHORT).show();
					demoIs = context.getAssets().open(DEMO_FILE);
					configHasChanged = true;
					try {
						doc = builder.parse(demoIs);
						panelElements = parseDoc(doc);
					} catch (SAXException e) {
						Log.e(MYTAG,"SAX Exception - "+e.getMessage());
					}
				} else {
					fis = new FileInputStream(f);
					try {
						doc = builder.parse(fis);
						panelElements = parseDoc(doc);
					} catch (SAXException e) {
						Log.e(MYTAG,"SAX Exception - "+e.getMessage());
					}
				}
				
				if (demoIs !=null) demoIs.close();
				


			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e){
				e.printStackTrace();
			}

		} else {
			Toast.makeText(context, "ERROR:External storage not readable", Toast.LENGTH_LONG).show();
			return false;
		}
		Log.d(MYTAG,"config loaded from "+configFilename+" in "+(System.currentTimeMillis()-time0)/1000f+" secs");
		
		// calc min xy and max xy of panel
		calcMinMax();
		
		// finally calculate routes
		routes = findRoutes();	
		WriteRoutes.writeToXML(routes);
		
		return true;
	}


	private static ArrayList<PanelElement> parseDoc(Document doc) {
		// assemble new ArrayList of tickets.
		ArrayList<PanelElement> pes = new ArrayList<PanelElement>();
		NodeList items ;
		Element root= doc.getDocumentElement();
		
		items = root.getElementsByTagName("panel");
		panelName = parsePanelName(items.item(0));
		if (DEBUG) Log.d(MYTAG,"config: panel name="+panelName);


		// look for TrackElements  - this is the lowest layer
		items = root.getElementsByTagName("track");
		if (DEBUG) Log.d(MYTAG,"config: "+items.getLength()+" track");
		for (int i=0;i<items.getLength();i++){
		    pes.add(parseTrack(items.item(i)));
     	}
		
		// look for existing and known turnouts - on top of track
		items = root.getElementsByTagName("turnout");
		if (DEBUG) Log.d(MYTAG,"config: "+items.getLength()+" turnouts");
		for (int i=0;i<items.getLength();i++){	
			pes.add(parseTurnout(items.item(i)));
		}

		// check for intersection of track, if new, add a turnout with unknown SX address
		for (int i=0; i<pes.size(); i++) {
			PanelElement p = pes.get(i);

			for (int j=i+1; j<pes.size(); j++) {
				PanelElement q = pes.get(j);

				PanelElement turnout =  LinearMath.trackIntersect(p, q);
				
				if (turnout != null) {
					// there is an intersection => make new turnoout
					if (DEBUG) Log.d(MYTAG,"(i,j)=("+i+","+j+") new? turnout found at x="+
										turnout.x+" y="+turnout.y+" x2(cl)="+turnout.x2+" y2(cl)="+turnout.y2+" xt="+turnout.xt+" yt="+turnout.yt);
	
					// check whether this turnout is already known 
					boolean known = false;
					for (PanelElement e: pes ) {
						if ((e.getType().equals("turnout") )&&(e.x == turnout.x) && (e.y == turnout.y)) {  // at same position => match
							known = true;
							break;
						}
					}
					if (!known) {
						configHasChanged = true;
						pes.add(new TurnoutElement(turnout));  // with unknown SX address
					}
				}
			}
		}
				
		// look for sensors 
		// SENSORS als LETZTE !!!! important (sind damit immer "on top")
	
		items = root.getElementsByTagName("sensor");
		if (DEBUG) Log.d(MYTAG,"config: "+items.getLength()+" sensors");
		for (int i=0;i<items.getLength();i++){ 
			pes.add(parseSensor(items.item(i)));
		}
		
		return pes; 
	}

	
	
	private static TurnoutElement parseTurnout(Node item) {

		TurnoutElement pe = new TurnoutElement();
		pe.type="turnout";
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG) Log.d(MYTAG,theAttribute.getNodeName() + "=" + theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("name")) {
				pe.name=theAttribute.getNodeValue();
			} else if (theAttribute.getNodeName().equals("x")) {
				pe.x=Integer.parseInt(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("y")) {
				pe.y=Integer.parseInt(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("x2")) {
				pe.x2=Integer.parseInt(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("y2")) {
				pe.y2=Integer.parseInt(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("xt")) {
				pe.xt=Integer.parseInt(theAttribute.getNodeValue()); 
			} else if (theAttribute.getNodeName().equals("yt")) {
				pe.yt=Integer.parseInt(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("sxadr")) {
				pe.setSxAdr(Integer.parseInt(theAttribute.getNodeValue()));
			} else if (theAttribute.getNodeName().equals("sxbit")) {
				pe.setSxBit(Integer.parseInt(theAttribute.getNodeValue()));
			} else {
				if (DEBUG) Log.d(MYTAG,"unknown attribute "+theAttribute.getNodeName()+" in config file");
			}
		}

		return  pe;

	}
	
	private static String parsePanelName(Node item) {					
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			if (DEBUG) Log.d(MYTAG,theAttribute.getNodeName() + "=" + theAttribute.getNodeValue());
				
			if (theAttribute.getNodeName().equals("name")) {
				String name=theAttribute.getNodeValue();
				return name;
				
			}
		}
		return "";
	}
	
	private static SensorElement parseSensor(Node item) {
		// ticket node can be Incident oder UserRequest
		SensorElement pe = new SensorElement();
        pe.type="sensor";
        pe.x2=INVALID_INT;  // to be able to distinguish between different types of sensors (LAMP or dashed track)
			NamedNodeMap attributes = item.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node theAttribute = attributes.item(i);
				// if (DEBUG) Log.d(MYTAG,theAttribute.getNodeName() + "=" + theAttribute.getNodeValue());
				if (theAttribute.getNodeName().equals("name")) {
					pe.name=theAttribute.getNodeValue();
				} else if (theAttribute.getNodeName().equals("x")) {
					pe.x=getValue(theAttribute.getNodeValue());
				} else if (theAttribute.getNodeName().equals("y")) {
					pe.y=getValue(theAttribute.getNodeValue());
				} else if (theAttribute.getNodeName().equals("x2")) {
					pe.x2=getValue(theAttribute.getNodeValue());
				} else if (theAttribute.getNodeName().equals("y2")) {
					pe.y2=getValue(theAttribute.getNodeValue());
				} else if (theAttribute.getNodeName().equals("icon")) {
					pe.setType(theAttribute.getNodeValue()); 
				} else if (theAttribute.getNodeName().equals("sxadr")) {
					pe.setSxAdr(Integer.parseInt(theAttribute.getNodeValue()));
				} else if (theAttribute.getNodeName().equals("sxbit")) {
					pe.setSxBit(Integer.parseInt(theAttribute.getNodeValue()));
				} else {
					if (DEBUG) Log.d(MYTAG,"unknown attribute "+theAttribute.getNodeName()+" in config file");
				}
			}

		return  pe;

	}
	
	private static int getValue(String s) {
		float b= Float.parseFloat(s);
		return (int)b;
	}
	

	
	private static PanelElement parseTrack(Node item) {
		// ticket node can be Incident oder UserRequest
		PanelElement pe = new PanelElement();
        pe.type="track";
		NamedNodeMap attributes = item.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node theAttribute = attributes.item(i);
			// if (DEBUG) Log.d(MYTAG,theAttribute.getNodeName() + "=" + theAttribute.getNodeValue());
			if (theAttribute.getNodeName().equals("x")) {
				pe.x=getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("y")) {
				pe.y=getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("x2")) {
				pe.x2=getValue(theAttribute.getNodeValue());
			} else if (theAttribute.getNodeName().equals("y2")) {
				pe.y2=getValue(theAttribute.getNodeValue());
			} else {
				if (DEBUG) Log.d(MYTAG,"unknown attribute "+theAttribute.getNodeName()+" in config file");
			}
		}

		if (pe.x2 < pe.x) {  // swap 1/2, x2 must always be >x
			int tmp = pe.x;
			pe.x=pe.x2;
			pe.x2=tmp;
			tmp = pe.y;
			pe.y=pe.y2;
			pe.y2=tmp;		
		}
		return  pe;

	}

	private static String getConcatNodeValues(Node prop) {
		// behaves well for non-existing nodes and for node values which are 
		// broken into several values because of special characters like '"' 
		// needed for the android code - this problem only exists in
		// Android xml library and not on the PC !!
		if (prop.hasChildNodes()) { // false for optional attributes
			StringBuilder text = new StringBuilder();
			NodeList chars = prop.getChildNodes();
			for (int k=0;k<chars.getLength();k++){
				text.append(chars.item(k).getNodeValue());
			}
			return text.toString().trim();
		} else {
			return ("");  // return empty string if empty
		}
	}

	private static void calcMinMax() {
		// reset min/max calculation
		panelXmin = panelYmin = 100000;
		panelXmax = panelYmax = -100000;
		for (PanelElement pe: panelElements) {
			if ((pe.x != INVALID_INT) && (pe.x < panelXmin)) panelXmin = pe.x;
			if ((pe.x2 != INVALID_INT) &&(pe.x2 < panelXmin)) panelXmin = pe.x2;
			if ((pe.x != INVALID_INT) &&(pe.x > panelXmax)) panelXmax = pe.x;
			if ((pe.x2 != INVALID_INT) &&(pe.x2 > panelXmax)) panelXmax = pe.x2;
			if ((pe.y != INVALID_INT) &&(pe.y < panelYmin)) panelYmin = pe.y;
			if ((pe.y2 != INVALID_INT) &&(pe.y2 < panelYmin)) panelYmin = pe.y2;
			if ((pe.y != INVALID_INT) &&(pe.y > panelYmax)) panelYmax = pe.y;
			if ((pe.y2 != INVALID_INT) &&(pe.y2 > panelYmax)) panelYmax = pe.y2;
		}
		// add 20 pixel
		//panelXmax += RASTER/prescale;
		//panelYmax += RASTER/prescale;
		if (DEBUG) Log.d(MYTAG,"x-range=["+panelXmin+","+panelXmax+"] y-range=["+panelYmin+","+panelYmax+"]");
	}
	

	private static ArrayList<Route> findRoutes() {

		for (PanelElement startSensor : panelElements) {
			if (startSensor.type == "sensor") {

				int x = startSensor.x;
				int y = startSensor.y;
				
				if (DEBUG)
					Log.d(MYTAG, "ALL ROUTES ---------- for sensor " + startSensor.name+ " at x,y="+x+","+y+"------------------");
				// find a route to all other sensors to the right
				Route r = new Route(startSensor);
				
				// now find a route to another sensor (=> to the right)
				// first look for next Turnout or Sensor or EndOfTrack
				
				// get slope of current track
				SlopeEndX sax = getSlopeAndEndX(x,y);
				
				findTurnout(x+2, y+2*sax.slope, sax, 0, r); 
			}
		}

	    return routelist;
	}
	
	/**
	 * 
	 */
	private static SlopeEndX getSlopeAndEndX(int x, int y)  {
		PanelElement pe=getTrack(x,y);
		SlopeEndX ret = new SlopeEndX();
		if (pe == null) {
			Log.d(MYTAG,"no track found at ["+x+","+y+"]");
			return ret;
		} else {
			ret.endX = pe.x2;
			if ((pe.y2-pe.y) == 0) {
				ret.slope=0;
				return ret;
			} else if ((pe.y2-pe.y) > 0) {
				ret.slope=1;
				return ret;
			} else {
				ret.slope=-1;
				return ret;
			}
		}
	}
	
	/**
	 * is a Point (x,y) (which is not a turnout point) on a Track line? 
	 * if yes, return the track element 
	 * (if it is not a turnout => only 1 track is possible)
	 */
	private static PanelElement getTrack(int x, int y) {
		for (PanelElement pe:panelElements) {
			if (pe.type == "track") {
				if ((DEBUG) && (x> 800) && (x < 840 )&& ( y > 320) && (y< 340)) {
					Log.d(MYTAG,"getTrack x,y=[:"+x+", "+y+" mult "+(pe.x2-pe.x) +" "
				+ (y-pe.y) + "==?" + (pe.y2-pe.y) + " "+ (x-pe.x));
				}
				// x,y on track line?
				if( (pe.x2-pe.x)*(y-pe.y) == (pe.y2-pe.y)*(x-pe.x)) {
					// notwendige, aber nicht hinreichende Bedingung
					// check bounds also
					if ( (x < pe.x) || (x > pe.x2) || (y < pe.y) || (y > pe.y2)) { // out of track area
						// continue with search loop, no matching track
					} else {
						if (DEBUG) Log.d(MYTAG,"getTrack from ["+pe.x+","+pe.y+"] to ["+pe.x2+","+pe.y2+"]");
						return pe;
					}
				}
			}
		}
		Log.e(MYTAG,"no track found for x,y="+x+","+y);

		return null;
	}
	
	/**
	 * find a turnout, start at point (x,y) with slope slx.slope until endX=slx.LastX
	 *     iteration step-number is n and Route is r
	 * @param x start at point (x,y)
	 * @param y start at point (x,y)
	 * @param slx (slope,endX)
	 * @param n iteration step-number
	 * @param r Route
	 */
	private static void findTurnout(int x, int y, SlopeEndX slx, int n, Route r) {
		int slope = slx.slope;
		int endX = slx.endX;
		
		
	    if (DEBUG) Log.d(MYTAG,"findTurnout( ["+x+","+y+"]  slope="+slope+" endX="+endX+" n="+n+" r#="+routenumber+")");
        if ( n == (MAX_DEPTH-1)) {
        	Log.e(MYTAG,"findTurnout: something went wrong in recursion, n="+n);
        	return;
        }
        if ( slx.endX <= x) {
        	Log.e(MYTAG,"findTurnout: endX="+slx.endX+" is to left of x="+x);
        	return;
        }
		// find nearest turnout on right side (=increasing x)
      // and check: do we have reached the target ?

		
		while (x <= endX) {
			//if (DEBUG) Log.d(MYTAG,"XY,"+x+","+y+","+routenumber);
			// TODO Behandlung von Track-Ende, der unter neuem Winkel weitergeht
			// is this the start of a new track? 
			//for (PanelElement newtrack:panelElements) {
			//	if ( (x == newtrack.x) && (y == newtrack.y) ) {
			//		finder.result = NEW_TRACK;
			//		finder.pe = newtrack;
			//		return finder;
			//	}
			//}
			if (isSensor(x,y)) {
				// this is the end of our search  - add current route to routelist;
				r.stop = getSensor(x,y);
				routelist.add(r);
				if (DEBUG) Log.d(MYTAG,"sensor reached at ["+x+","+y+"] - r#="+routenumber+" ends ------------------------");
				if (DEBUG) logRoute(r);
				routenumber++;
				return;
			}

			// run through all turnouts to find a matching one
			for (PanelElement te : panelElements) {
				if (te.type == "turnout") {
					// Weiche entgegen Fahrtrichung? nach (xc,yc) und (xt,yt) suchen
					if ((x == te.x2) && (y == te.y2)) {  // end of closed position
						// we found a turnout and it should be closed
						Route r4=r.clone();
						r4.addTurnout(te,STATE_CLOSED);

						if (DEBUG) Log.d(MYTAG,"found closed turnout at ["+te.x+","+te.y+
								"] - added to route#="+routenumber);
						continueBehindTurnout(te,r4);
	
						return;
						
					// Weiche entgegen Fahrtrichung?
					} else if ((x == te.xt) && (y == te.yt)) {  // end of thrown position
						// we found a turnout and it should be THROWN
						Route r4=r.clone();
						r4.addTurnout(te,STATE_THROWN);

						if (DEBUG) Log.d(MYTAG,"found thrown turnout at ["+te.x+","+te.y+
								"] - added to route#="+routenumber);
						continueBehindTurnout(te,r4);
	
						return;
						
					// Weiche in Fahrtrichtung? beiden Wegen folgen
					}	else if ((x == te.x) && (y == te.y)) { 
						if (DEBUG) Log.d(MYTAG,"found turnout at ["+x+","+y+"]");
						if (te.xt < x)  { // wir haben den Turnout bereits hinter uns gelassen
							if (DEBUG) Log.d(MYTAG,"Error in findTurnout: already behind turnout at ["+x+","+y+"], r#="+routenumber);
							
						} else {  // clone and follow the two possible routes .
						Route r1  = r.clone();  // thrown route
						Route r2  = r.clone();  // closed route
						
						// follow thrown route
						r1.addTurnout(te,STATE_THROWN);	

						if (DEBUG) Log.d(MYTAG,"added turnout and following THROWN - r#="+routenumber);

						SlopeEndX s1x = getSlopeAndEndX(te.xt,te.yt);
						findTurnout(te.xt+STEP,te.yt+STEP*s1x.slope, s1x, r1.nTurnout,  r1);
						
						// same for closed				
						r2.addTurnout(te,STATE_CLOSED);
						if (DEBUG) Log.d(MYTAG,"added turnout and following CLOSED - r#="+routenumber);

						s1x = getSlopeAndEndX(te.x2,te.y2);	
						findTurnout(te.x2+STEP,te.y2+STEP*s1x.slope,  s1x, r2.nTurnout,  r2);
						
						return;
						}
					}
				}

			}
			x += STEP;
			y += slope * STEP;
		}
		// correct for last increase
		x -= STEP;
		y -= slope * STEP;


		if (DEBUG)
			Log.d(MYTAG, "checking for new starting track at [" + x + "," + y
					+ "]");
		boolean foundNewTrack = false;
		for (PanelElement te : panelElements) {
			
			if ((te.type == "track") && (te.x == x) && (te.y == y)) {
				Log.d(MYTAG, "new track starts here!");
				// calculate slop and endX of this new track
				SlopeEndX s2x = new SlopeEndX();
				s2x.endX = te.x2;
				if ((te.y2 - te.y) == 0) {
					s2x.slope = 0;
				} else if ((te.y2 - te.y) > 0) {
					s2x.slope = 1;

				} else {
					s2x.slope = -1;

				}
				foundNewTrack = true;
				findTurnout(te.x+STEP , te.y+STEP*s2x.slope, s2x, r.nTurnout, r);  // start on new track (not on old one)
				break;
			}
		}
		if ((!foundNewTrack) && (DEBUG) )
			Log.d(MYTAG, "no more track elements at [" + x + "," + y
					+ "] r#=" + routenumber);
		//
		return;
	}
	
	private static void logRoute(Route r) {
		Log.d(MYTAG,"startoute----------------- r#="+routenumber+" ----------------------------------------");
		Log.d(MYTAG,r.toString());
		Log.d(MYTAG,"endroute-----------------------------------------------------------");

	}
	private static boolean isSensor(int x, int y) {
    	for (PanelElement se:panelElements) {
    		if ( (se.type == "sensor") && (se.x == x) && (se.y == y)) return true;
    	}
    	return false;
    }
    
    private static PanelElement getSensor(int x, int y) {
       	for (PanelElement se:panelElements) {
    		if ( (se.type == "sensor") && (se.x == x) && (se.y == y)) return se;
    	}
    	return null;
    	
    }
    
    private static void continueBehindTurnout(PanelElement te, Route r4) {
    	if (DEBUG) Log.d(MYTAG,"continue behind turnout at ["+te.x+","+te.y);
		// continue after turnout te
		PanelElement tr = new PanelElement();
		if ((tr=getTrack(te.x+2,te.y)) != null) {
			// continue with slope 0 behind turnout
			if (DEBUG) Log.d(MYTAG,"slope=0");
			findTurnout(te.x+2,te.y, new SlopeEndX(0,tr.x2), r4.nTurnout,  r4);
		} else if  ((tr=getTrack(te.x+2,te.y+2)) != null) {
			if (DEBUG) Log.d(MYTAG,"slope=1");
			// continue with slope 1 behind turnout
			findTurnout(te.x+2,te.y+2, new SlopeEndX(1,tr.x2), r4.nTurnout,  r4);
		} else if  ((tr=getTrack(te.x+2,te.y-2)) != null) {
			if (DEBUG) Log.d(MYTAG,"slope=-1");
			// continue with slope -1 behind turnout
			findTurnout(te.x+2,te.y-2, new SlopeEndX(-1,tr.x2), r4.nTurnout,  r4);
		} else {
			Log.d(MYTAG,"Error: cannot continue at ["+te.x+","+te.y);
		}
    }
   
}
