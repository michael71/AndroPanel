package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;


public class SXPanelElement extends PanelElement {

	protected static final int STATE_CLOSED = 0;
	protected static final int STATE_THROWN = 1;
	protected static final int STATE_UNKNOWN = 2;
	
	protected int state;
	protected int sxAdr=INVALID_INT;
	protected int sxBit=1;	
	protected long lastToggle=0L;

	public SXPanelElement() {
		super(null,0,0);
	}

	public SXPanelElement(String type, int x, int y, String name, int adr, int bit) {
		super(null, x, y);
		this.type=type;
		this.state = STATE_CLOSED;
		this.sxAdr = adr;
		this.sxBit = bit;
		update();
	}

	@Override
	public int getSxAdr() {
		return sxAdr;
	}

	public void setSxAdr(int sxAdr) {
		this.sxAdr = sxAdr;
		update();
	}

	@Override
	public int getSxBit() {
		return sxBit;
	}
	public void setSxBit(int sxBit) {
		this.sxBit = sxBit;
		update();
	}


	@Override
	public void update() {
		state = AndroPanelApplication.getSxBit(sxAdr, sxBit) ; 
	}

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

		if ((xs >= (x-w)) && (xs <=(x+w)) && (ys>=(y-h)) && (ys<=(y+h))) {
			if (DEBUG) Log.i(TAG,"selected adr="+sxAdr+" /"+sxBit+" "+type+"  x="+x+xoff+" y="+y+yoff+" w="+w+" h"+y+" (xs,ys)=("+xs+","+ys+")");
			return true;
		} else {
			return false;
		}

	}

	protected void doDrawSXAddresses(Canvas canvas) {

		Rect bounds = new Rect();
		String txt;
		if (sxAdr == INVALID_INT) {
			txt ="???";
		} else {
			txt = ""+sxAdr+"/"+sxBit;
		}
		sxAddressPaint.getTextBounds(txt, 0, txt.length(), bounds);
		int text_height =  bounds.height();
		int text_width =  bounds.width();

		canvas.drawRect((x-2)*prescale, (y-2)*prescale-text_height,
				((x+2)*prescale+text_width), y*prescale+2, sxAddressBGPaint);  // dark rectangle
				canvas.drawText(txt,x*prescale, y*prescale, sxAddressPaint);   // the numbers

	}

}
