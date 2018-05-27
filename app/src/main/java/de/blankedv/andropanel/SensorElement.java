package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.*;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.util.Log;

/**
 * holds a Sensor element on the panel
 * 
 * can draw dashed line as sensor (european style, [x2,y2] must be set)
 * or lamp type sensor (USS style, no [x2,y2])
 * 
 * can be activated (to set a "FAHRSTRASSE")
 * 
 * @author mblank
 *
 */
public class SensorElement extends SXPanelElement {
	
	private boolean activated = false;
	public SensorElement(String type, int x, int y, String name, int adr, int bit) {
		super(type, x, y, name,  adr, bit);		

	}
	
	public SensorElement() {
		super();
	}

	@Override
	public void doDraw(Canvas canvas) {
		if (x2 != INVALID_INT) {  // draw dashed line as sensor
			// read data from SX bus and set red/gray dashed line accordingly

			if (state ==  STATE_CLOSED) {
				canvas.drawLine(x * 2, y * 2, x2 * 2, y2
						* 2, linePaintGrayDash);
			} else {
				canvas.drawLine(x * 2, y * 2, x2 * 2, y2
						* 2, linePaintRedDash);
			}
		} else {
			// draw lamp type of sensor

			// read data from SX bus and set bitmap accordingly
			int h = 0, w = 0;
			Bitmap bm;
			StringBuilder bmName = new StringBuilder(type);
			if (activated == false) {
				if (state ==  STATE_CLOSED) {
					bmName.append("_off");
				} else {
					bmName.append("_on");
				} 
			} else {
				if (state ==  STATE_CLOSED) {
					bmName.append("_off_act");
				} else {
					bmName.append("_on_act");
				} 
			}

			bm = bitmaps.get(bmName.toString());
			if (bm == null)
				Log.e(TAG,
						"error, bitmap not found with name="
								+ bmName.toString());
			h = bm.getHeight() / 2;
			w = bm.getWidth() / 2;
			canvas.drawBitmap(bm, x * 2 - w, y * 2 - h, null); // center
																				// bitmap
		}
		if (drawSXAddresses)
			doDrawSXAddresses(canvas);
	}
	
	@Override
	public void toggle() {
		//if (sxAdr == INVALID_INT) return; // do nothing if no sx address defined.
		
		if ((System.currentTimeMillis() - lastToggle) < 250) return;  // do not toggle twice within 250msecs
		
		lastToggle = System.currentTimeMillis();  // reset toggle timer
		
		if (activated == false) {
			// save state
			activated = true;
			if (DEBUG) Log.d(TAG,"toggling sensor - activated.");
		} else {
			activated = false;
			if (DEBUG) Log.d(TAG,"toggling sensor - activated=false.");
		}
		

		if (DEBUG) Log.d(TAG,"toggling sensor - state="+state);
	}

 }

