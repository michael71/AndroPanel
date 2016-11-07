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

	@Override
	public void doDraw(Canvas canvas) {

		// read data from SX bus and paint position of turnout accordingly
		// draw a line and not a bitmap
		if ((enableEdit) || (sxAdr == INVALID_INT)) {
			canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,greenPaint);	
			canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,redPaint);	
		} else {

				if (state == STATE_CLOSED) {
					canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,bgPaint);
					canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,linePaint2);
				} else if (state == STATE_THROWN){
					canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,bgPaint);
					canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,linePaint2);
				} else if (state == STATE_UNKNOWN){
					canvas.drawLine(x*prescale,y*prescale,xt*prescale,yt*prescale,bgPaint);
					canvas.drawLine(x*prescale,y*prescale,x2*prescale,y2*prescale,bgPaint);
				}
	
		}

		if (drawSXAddresses) doDrawSXAddresses(canvas);
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
		
		/* aus sxi-interface:
		    synchronized void sendAccessory(int adr, int bit, int data) {
		  
		        int d = sxData[adr];
		        Byte[] b = {(byte) (adr + 128), 0};  // bit 7 muss gesetzt sein zum Schreiben
		        if (data == 1) {  // set bit
		            d |= (1 << (bit-1));  // sx bit von 1 bis 8
		        } else {
		            // reset bit
		            d = d & ~(1 << (bit-1));  // sx bit von 1 bis 8
		        }
		        b[1] = (byte) (d);
		        sxi.send(b);
		    }  */
		
		//sxData[sxAdr] = d;
		state = STATE_UNKNOWN; // until updated via SX message
		client.sendCommand(sxAdr,d);  // ==> send changed data over network to interface
		if (DEBUG) Log.d(TAG,"toggle sxAdr "+sxAdr);
	}

 }
