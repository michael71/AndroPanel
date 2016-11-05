package de.blankedv.andropanel;

public class RouteFinderResult {

	static final int SENSOR_FOUND=1;
	static final int END = 2;
	static final int TURNOUT_FOUND=3;
	static final int NEW_TRACK=4;
	
	public int result;
	public PanelElement pe;
	public int turnoutpos;

	public RouteFinderResult() {
		this.result = END;
	}

}
