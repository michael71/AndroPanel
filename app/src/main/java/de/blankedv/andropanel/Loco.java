package de.blankedv.andropanel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;
import static de.blankedv.andropanel.AndroPanelApplication.*;


public class Loco {
	public int adr;
	public String name;
	public int mass;    // 1...5 (never == 0 !)
	public int vmax = 100;   // maximum speed in km/h
	
	// speed vars used for setting loco speed
	public int speed_act;    	// -31 ... +31, speed currently sent via SXnet
	public int speed_to_be;   // -31 ... +31,  speed after Mass Simulation
	
	// actual speed read from SX bus - used for display
	public int speed_from_sx;  
	
	private int last_sx = 999;   // used to avoid resending
	
	boolean lamp;
	public boolean lamp_to_be;
	boolean function;
	public boolean function_to_be;
	long lastToggleTime = 0;
	private long speedSetTime=0;   // last time the speed was set on interface

	public Bitmap lbm = null;
	
	private int massCounter=0;
	
	private boolean sendNewDataFlag=false;
	
	public Loco() {  // dummy loco
		this.adr = 22;
		this.name = "Lok 22";
		this.mass = 3 ;
        lastToggleTime=0; // no toggle so far
        // init other data from actual SX bus data
        initFromSX();

	}

	public Loco(String name) {  // dummy loco
		this.adr = 3;
		this.name = name;
		this.mass = 3 ;
        lastToggleTime=0; // no toggle so far
        // init other data from actual SX bus data
        initFromSX();
	}

	public Loco(String name, int adr, int mass, Bitmap lbm) {

		this.adr = adr;
		this.name = name;
		this.lbm = lbm;
		if ( (mass >=1) && (mass <=5) ) {
			this.mass = mass;
		} else {
			this.mass = 3 ;
		}
		lastToggleTime=0; // no toggle so far
		// init other data from actual SX bus data
		initFromSX();
	}

	public String getName() {
		return name;
	}

	public String getAdr() {
		return ""+adr;
	}


	public void initFromSX() {
		updateLocoFromSX();
		resetToBe();
	}

    public boolean isForward() {
		return speed_act >= 0;
    }

	private void resetToBe() {
		speed_to_be = speed_act = speed_from_sx;	
		function_to_be = function;
		lamp_to_be = lamp;
	}
	
	public void updateLocoFromSX() {
		int d = AndroPanelApplication.getSxData(adr);
		int s = d & 0x1f;
		if( (d & 0x20) != 0) s = -s;
		speed_from_sx = s;
	    lamp = ( (d & 0x40) != 0);
	    function = ( ( d & 0x80) != 0);		
	    if ((System.currentTimeMillis() - lastToggleTime) > 2000) {
	    	// safe to update "to-be" state as "as-is" state
	    	lamp_to_be = lamp;
	    	function_to_be =function;
	    }
	}
	
	
	public void timer() {
		massSimulation();
		if (sendNewDataFlag) {  // if anything changed, send new value
			sendNewDataFlag = false;
			
			// calc SX byte from speed, lamp, function			
			int sx = 0;
			if (lamp_to_be)
				sx |= 0x40;
			if (function_to_be)
				sx |= 0x80;
			if (speed_act < 0) {
				sx |= 0x20;
				sx += (-speed_act);
			} else {
				sx += speed_act;
			}
			if (sx != last_sx) { // avoid sending the same message again
				speedSetTime = System.currentTimeMillis();   // we are actively controlling the loco
				last_sx = sx;

                if (demoFlag) {
                    Message m = Message.obtain();
                    m.what = SX_FEEDBACK_MESSAGE;
                    m.arg1 = adr;
                    m.arg2 = sx;
                    handler.sendMessage(m);  // send SX data to UI Thread via Message
                    return;
                }

				String command = "SX "+adr+" "+sx;
				Boolean success = sendQ.offer(command);
				if ( !success  && (DEBUG)) Log.d(TAG,"loco sendCommand failed, queue full")	;

			}
		}
	}
	
	private synchronized void massSimulation() {
		// depending on "mass", do more or less often
		if (massCounter < mass) {
			massCounter ++;
			return;
		}
		massCounter=0; //reset
		// bring actual speed and speed_to_be closer together
		
		if (speed_to_be != speed_act) {
			if (speed_to_be > speed_act) {
				speed_act++; 
			}
			if (speed_to_be < speed_act) {
				speed_act--; 
			}
			if (DEBUG) Log.d(TAG,"massSim: to-be="+speed_to_be+" act="+speed_act);
			sendNewDataFlag=true;
		}
		
		if (!isActive()) {
			resetToBe();
		}
	}
	

	public void stopLoco() {
		speed_to_be = speed_act = 0;
		sendNewDataFlag=true;
		speedSetTime = System.currentTimeMillis();
	}
	
	public void setSpeed(int s) {
		speed_to_be = s;
		// limit range
		if (speed_to_be < -31 ) speed_to_be = -31;
		if (speed_to_be >  31 ) speed_to_be =  31;
		speedSetTime = System.currentTimeMillis();
	}
	
	/** increase loco speed by one
	 * 
	 */
	public void incrLocoSpeed() {		
		speed_to_be += 1;
		if (speed_to_be < -31 ) speed_to_be = -31;
		if (speed_to_be >  31 )   speed_to_be =  31;
		if (Math.abs(speed_act - speed_to_be) <= 1) {
			speed_act = speed_to_be;		
		}
		sendNewDataFlag=true;
		speedSetTime = System.currentTimeMillis();
	}
	
	/** increase loco speed by one
	 * 
	 */
	public void decrLocoSpeed() {
		speed_to_be += -1;
		if (speed_to_be < -31 ) speed_to_be = -31;
		if (speed_to_be >  31 ) speed_to_be =  31;
		if (Math.abs(speed_act - speed_to_be) <= 1) {
			speed_act = speed_to_be;
		}
		sendNewDataFlag=true;
		speedSetTime = System.currentTimeMillis();
	}

	public void toggleLocoLamp() {
		if ((System.currentTimeMillis() - lastToggleTime) > 250) {  // entprellen
			if (lamp_to_be) {
				lamp_to_be = false;
			} else {
				lamp_to_be = true;
			}
			lastToggleTime = System.currentTimeMillis();
			if (DEBUG) Log.d(TAG,"loco touched: toggle lamp_to_be");
			sendNewDataFlag=true;
		}
	}
	
	public void toggleFunc() {
		if ((System.currentTimeMillis() - lastToggleTime) > 250) {  // entprellen
			if (function_to_be) {
				function_to_be = false;
			} else {
				function_to_be = true;
			}
			if (DEBUG) Log.d(TAG,"loco touched: toggle func");
			lastToggleTime = System.currentTimeMillis();
			sendNewDataFlag=true;
		}
	}
	
	private boolean isActive() {
		// are the loco-controls touched in the last 5 seconds
		if( ((System.currentTimeMillis() - speedSetTime) <5000) ||
			((System.currentTimeMillis() - lastToggleTime) < 5000) ) {
			return true;
		} else {
			return false;
		}

	}


	public String longString() {
		return name+" ("+adr+")(m="+mass+")";
	}
	
	
}
