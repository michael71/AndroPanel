package de.blankedv.andropanel;
import static de.blankedv.andropanel.Panel.controlAreaRect;
import static de.blankedv.andropanel.AndroPanelApplication.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * a button in the loco control area
 */
public class LocoButton {
    private float xrel, yrel;  // relative position in control area.
    private Bitmap bmON, bmOFF, bmDisabled;
    private int w = 10, h = 10; // half of the bitmap width and height

    // x and y are actual position of bitmap placing, NOT the center!
    public float x = 0, y = 0;
    private static Paint bg = new Paint();


    public LocoButton(float x2, float y2, Bitmap on, Bitmap off) {
        this.xrel = x2;
        this.yrel = y2;
        bmON = on;
        bmOFF = off;
        w = bmON.getWidth() / 2;
        h = bmON.getHeight() / 2;
        if (controlAreaRect != null) {
            recalcXY();
        }

    }

    public LocoButton(float x2, float y2, Bitmap on, Bitmap off, Bitmap disabled) {
        this.xrel = x2;
        this.yrel = y2;
        bmON = on;
        bmOFF = off;
        bmDisabled = disabled;
        w = bmON.getWidth() / 2;
        h = bmON.getHeight() / 2;
        if (controlAreaRect != null) {
            recalcXY();
        }

    }

    public LocoButton(float x2, float y2, Bitmap on) {
        this.xrel = x2;
        this.yrel = y2;
        bmON = on;
        w = bmON.getWidth() / 2;
        h = bmON.getHeight() / 2;
        if (controlAreaRect != null) {
            recalcXY();
        }

    }

    public LocoButton(float x2, float y2) {
        this.xrel = x2;
        this.yrel = y2;
        bmON = bitmaps.get("button100");
        bmOFF = null;
        w = bmON.getWidth() / 2;
        h = bmON.getHeight() / 2;
        if (controlAreaRect != null) {
            recalcXY();
        }

    }


    public boolean isTouched(float xt, float yt) {
        if ((xt > x) && (xt < (x + w + w)) && (yt > y) && (yt < (y + h + h))) {
            if (DEBUG) Log.d(TAG, this.toString() + " was touched.");
            return true;
        } else {
            if (DEBUG) Log.d(TAG, this.toString() + " was not touched.");
            return false;
        }
    }

    public void recalcXY() {
        if (bmON == null) {
            w = (controlAreaRect.right - controlAreaRect.left) / 30;
            h = (controlAreaRect.bottom - controlAreaRect.top) / 3;
        }
        x = controlAreaRect.left + xrel * (controlAreaRect.right - controlAreaRect.left) - w;  // position where bitmap is drawn
        y = controlAreaRect.top + yrel * (controlAreaRect.bottom - controlAreaRect.top) - h;
        if (DEBUG)
            Log.d(TAG, this.toString() + "btn recalc, x=" + x + " y=" + y + " w=" + w + " h=" + h);
    }

    // for 2 states
    public void doDraw(Canvas c, boolean state) {
        if (state == true) {
            c.drawBitmap(bmON, x, y, null);
        } else {
            c.drawBitmap(bmOFF, x, y, null);
        }
    }

    // 2 states plus "disabled" state
    public void doDraw(Canvas c, boolean state, boolean enabled) {
        if (enabled == false) {
            c.drawBitmap(bmDisabled, x, y, null);
        } else {
            if (state == true) {
                c.drawBitmap(bmON, x, y, null);
            } else {
                c.drawBitmap(bmOFF, x, y, null);
            }
        }
    }

    // draw a single bitmap at (x,y)
    public void doDraw(Canvas c) {
        c.drawBitmap(bmON, x, y, null);
    }

    public void doDraw(Canvas c, int value, Paint p) {
        // (x,y) drawing position for text is DIFFERENT than for bitmaps.(upper left)
        // (x,y) = lower left start of text.
        c.drawBitmap(bmON, x, y, null);
        //c.drawRect(x+3,y+3,x+w+w-3,y+h+h-3, bg);
        c.drawText("" + value, x + w * 0.6f, y + h * 1.42f, p);

    }

}
