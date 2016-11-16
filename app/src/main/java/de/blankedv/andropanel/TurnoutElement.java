package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint; 
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.Log;


public class TurnoutElement extends SXPanelElement {

	// for turnouts which can be interactivly set from panel

	private boolean isPartOfDoubleslip = false;
	private TurnoutElement doubleslipSecondTurnout = null;

	public TurnoutElement(String type, int x, int y, String name, int adr, int bit) {
		super(type, x, y, name,  adr, bit);	
	} 

 	public TurnoutElement() {
 		sxAdr = INVALID_INT;
		sxBit = 1;
		state = STATE_CLOSED;
        inverted = 0;
    }

	public TurnoutElement(PanelElement turnout) {
		type = turnout.type;
		x = turnout.x;    // center of turnout
		y = turnout.y;
		x2= turnout.x2;   // => closed 
		y2= turnout.y2;
		xt= turnout.xt;   // thrown
		yt= turnout.yt;
		sxAdr = INVALID_INT;
		sxBit = 1;
		state = STATE_CLOSED;
        inverted = turnout.getInverted();
    }

    public void setSecondTurnout(TurnoutElement t2) {
        doubleslipSecondTurnout = t2;
        isPartOfDoubleslip = true;
    }
	@Override
	public void doDraw(Canvas canvas) {

		if (isPartOfDoubleslip) {
			// draw other turnout
            drawTurnout(canvas, doubleslipSecondTurnout);
			if (drawSXAddresses) doDrawSXAddresses(canvas, doubleslipSecondTurnout);
		} else {
			// read data from SX bus and paint position of turnout accordingly
			// draw a line and not a bitmap
            drawTurnout(canvas, this);
			if (drawSXAddresses) doDrawSXAddresses(canvas, this);
		}


	}

	private void drawTurnout(Canvas canvas, TurnoutElement t) {
		if ((enableEdit) || (sxAdr == INVALID_INT)) {
			canvas.drawLine(t.x * prescale, t.y * prescale, t.x2 * prescale, t.y2 * prescale, greenPaint);
			canvas.drawLine(t.x * prescale, t.y * prescale, t.xt * prescale, t.yt * prescale, redPaint);
		} else {

			if (state == STATE_CLOSED) {
				canvas.drawLine(t.x * prescale, t.y * prescale, t.xt * prescale, t.yt * prescale, bgPaint);
				canvas.drawLine(t.x * prescale, t.y * prescale, t.x2 * prescale, t.y2 * prescale, linePaint2);
			} else if (state == STATE_THROWN) {
				canvas.drawLine(t.x * prescale, t.y * prescale, t.x2 * prescale, t.y2 * prescale, bgPaint);
				canvas.drawLine(t.x * prescale, t.y * prescale, t.xt * prescale, t.yt * prescale, linePaint2);
			} else if (state == STATE_UNKNOWN) {
				canvas.drawLine(t.x * prescale, t.y * prescale, t.xt * prescale, t.yt * prescale, bgPaint);
				canvas.drawLine(t.x * prescale, t.y * prescale, t.x2 * prescale, t.y2 * prescale, bgPaint);
			}
		}
	}
	@Override
	public void toggle() {
		if (sxAdr == INVALID_INT) return; // do nothing if no sx address defined.
		
		if ((System.currentTimeMillis() - lastToggle) < 250) return;  // do not toggle twice within 250msecs
		
		lastToggle = System.currentTimeMillis();  // reset toggle timer
		
		int stateFromSX = AndroPanelApplication.getSxBit(sxAdr,sxBit);
		int d = AndroPanelApplication.getSxData(sxAdr);
		
		if (stateFromSX == 0) {
			d |= (1 << (sxBit-1));  // sx bit von 1 bis 8
		} else {
			d = d & ~(1 << (sxBit-1));  // sx bit von 1 bis 8
		}	
		
		state = STATE_UNKNOWN; // until updated via SX message
		client.sendCommand(sxAdr,d);  // ==> send changed data over network to interface
		if (DEBUG) Log.d(TAG,"toggle sxAdr "+sxAdr);
	}


	// for turnouts, use "Schwerpunkt as center"
	@Override
	public boolean isSelected(float x1, float y1) {
		// check only for active elements
		//	Bitmap bm = bitmaps.get(type+"_closed");
		//	int w = bm.getWidth();
		//	int h = bm.getHeight();
		int w = RASTER/3;   // RASTER defines sensitive area
		int h = RASTER/3;

		float xs = x1/prescale;  // reduces by overall dimension scaling factor
		float ys = y1/prescale;
		//if ((x1 >= (x+xoff)) && (x1 <=(x+xoff+w)) && (y1>=(y+yoff)) && (y1<=(y+yoff+h))) {

		int mx = (x+xt+x2)/3;
		int my = (y+yt+y2)/3;

		if ((xs >= (mx-w)) && (xs <=(mx+w)) && (ys>=(my-h)) && (ys<=(my+h))) {
			if (DEBUG) Log.i(TAG,"selected adr="+sxAdr+" /"+sxBit+" "+type+"  x="+x+xoff+" y="+y+yoff+" w="+w+" h"+y+" (xs,ys)=("+xs+","+ys+")");
			return true;
		} else {
			return false;
		}

	}

   	// for turnouts, use "Schwerpunkt as center"
	private void doDrawSXAddresses(Canvas canvas, TurnoutElement t) {

		Rect bounds = new Rect();
		String txt;
		if (t.sxAdr == INVALID_INT) {
			txt ="???";
		} else {
			txt = ""+t.sxAdr+"/"+t.sxBit;
		}
        //if (t.sxAdr == 0) {
        //    Log.e(TAG, "sxAdr = 0 element-at ="+t.x+"/"+t.y+" type"+t.getType());
        //}
		sxAddressPaint.getTextBounds(txt, 0, txt.length(), bounds);
		int text_height =  bounds.height();
		int text_width =  bounds.width();

		int mx = (t.x+t.xt+t.x2)/3;
		int my = (t.y+t.yt+t.y2)/3;

		canvas.drawRect((mx-2)*prescale, (my-2)*prescale-text_height,
				((mx+2)*prescale+text_width), my*prescale+2, sxAddressBGPaint);  // dark rectangle
		canvas.drawText(txt,mx*prescale, my*prescale, sxAddressPaint);   // the numbers

	}
 }
