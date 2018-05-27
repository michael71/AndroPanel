package de.blankedv.andropanel;

import android.graphics.Canvas;
import android.util.Log;

import static android.R.attr.type;
import static android.R.attr.x;
import static android.R.attr.y;
import static de.blankedv.andropanel.AndroPanelApplication.DEBUG;
import static de.blankedv.andropanel.AndroPanelApplication.INVALID_INT;
import static de.blankedv.andropanel.AndroPanelApplication.TAG;
import static de.blankedv.andropanel.AndroPanelApplication.bgPaint;
import static de.blankedv.andropanel.AndroPanelApplication.client;
import static de.blankedv.andropanel.AndroPanelApplication.drawSXAddresses;
import static de.blankedv.andropanel.AndroPanelApplication.enableEdit;
import static de.blankedv.andropanel.AndroPanelApplication.greenPaint;
import static de.blankedv.andropanel.AndroPanelApplication.linePaint;
import static de.blankedv.andropanel.AndroPanelApplication.linePaint2;
import static de.blankedv.andropanel.AndroPanelApplication.redPaint;
import static de.blankedv.andropanel.SXPanelElement.STATE_CLOSED;
import static de.blankedv.andropanel.SXPanelElement.STATE_THROWN;
import static de.blankedv.andropanel.SXPanelElement.STATE_UNKNOWN;


public class DoubleslipElement extends PanelElement {


	public DoubleslipElement(int x, int y) {
 		this.x = x;
        this.y = y;
        this.type = "doubleslip";
    }

    public DoubleslipElement() {

    }

    public void doDraw(Canvas canvas) {
        // nothing is drawn
    }


 }
