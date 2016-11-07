package de.blankedv.andropanel;

import android.graphics.Canvas;

import android.graphics.Point;
import static de.blankedv.andropanel.AndroPanelApplication.*;

public class PanelElement {

	protected String name="";
    protected String type="";
	protected int x; // starting point
	protected int y;
	protected int x2=INVALID_INT;   // endpoint - x2 always >x
	protected int y2=INVALID_INT;
	protected int xt=INVALID_INT;   // "thrown" position for turnout
	protected int yt=INVALID_INT;


	public PanelElement(String type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
		name="";
	}
	
	public PanelElement(String type, Point poi) {
		this.type = type;
		this.x = poi.x;
		this.y = poi.y;
		name="";
	}
	
	public PanelElement(String type, Point poi,  Point closed, Point thrown) {
		this.type = type;
		this.x = poi.x;
		this.y = poi.y;
		this.x2 = closed.x;
		this.y2 = closed.y;
		this.xt = thrown.x;
		this.yt = thrown.y;
		name="";
	}
	
	public PanelElement() {
	}


	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public void doDraw(Canvas canvas) {
     
		if (y==y2) { // horizontal line
			// draw a line and not a bitmap
			canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale, linePaint);	
		} else {  // diagonal, draw with round stroke
			canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale, linePaint2);
		}

	}

	public boolean isSelected(float mLastTouchX, float mLastTouchY) {
		return false;
	}

	public void toggle() {
		// do nothing for non changing element		
	}
	
	public int getSxAdr() {
		return INVALID_INT;
	}
	
	public int getSxBit() {
		return INVALID_INT;
	}

    public int getInverted() {
        return INVALID_INT;
    }

	
	public void update() {
		// makes sense only for SX panel elements
	}

}
