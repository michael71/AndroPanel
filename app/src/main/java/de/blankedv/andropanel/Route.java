package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.DEBUG;
import static de.blankedv.andropanel.ParseConfig.MYTAG;
import android.util.Log;

/**
 * describes a route from one sensor element to another sensor element
 * as a number of turnouts plus their state 
 * 
 * @author mblank
 *
 */
public class Route {
	public static int MAX_DEPTH=10;
	
	public PanelElement start;
	public PanelElement stop;
	public PanelElement[] turnout = new PanelElement[MAX_DEPTH];
	public int[] turnoutState = new int[MAX_DEPTH];
	public int nTurnout;   // actual depth

	public Route(PanelElement start) {
		super();
		this.start = start;
		this.stop = null;
		this.nTurnout = 0;
	}
	
	@SuppressWarnings("MethodDoesntCallSuperMethod")
	public Route clone() {
		// clone a route
		Route clone = new Route(this.start);
		clone.stop = this.stop;
		clone.nTurnout = this.nTurnout;
		for (int i=0; i<MAX_DEPTH; i++) {
			clone.turnout[i] = this.turnout[i];
		    clone.turnoutState[i] = this.turnoutState[i];
		}
		return clone;
		
	}

	public boolean addTurnout(PanelElement tu, int state) {
		if (nTurnout < (MAX_DEPTH -1)) {
		    turnout[nTurnout]=tu;
		    turnoutState[nTurnout] = state;
			if (DEBUG) Log.d(MYTAG,"added turnout#="+nTurnout+" with state="+state+" to route");
		    nTurnout++;
		    return true;
		} else {
			if (DEBUG) Log.d(MYTAG,"Error. too many turnouts - nturnout#="+nTurnout);
			return false;  // more turnouts than MAX_DEPTH
		}
	}
	
	public void stop(PanelElement stop) {
		this.stop = stop;
	}
	
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	public String toString () {
    	StringBuilder sb= new StringBuilder();
		sb.append("route from [" + start.x + "," + start.y
				+ "] to [" + stop.x + "," + stop.y + "]\n");

		sb.append("route has " + nTurnout + " turnouts.\n");
		for (int i = 0; i < nTurnout; i++) {
			sb.append("i=" + i+" ");
			if (turnout[i] != null) {
				sb.append("route-turnout at [" + turnout[i].x + ","
							+ turnout[i].y + "] state=" + turnoutState[i]+"\n");
			} else {
				sb.append("route-turnout == null\n");
			}
		} 
		return sb.toString();
	}


}
