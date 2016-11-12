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
import static de.blankedv.andropanel.AndroPanelApplication.linePaint2;
import static de.blankedv.andropanel.AndroPanelApplication.prescale;
import static de.blankedv.andropanel.AndroPanelApplication.redPaint;
import static de.blankedv.andropanel.SXPanelElement.STATE_CLOSED;
import static de.blankedv.andropanel.SXPanelElement.STATE_THROWN;
import static de.blankedv.andropanel.SXPanelElement.STATE_UNKNOWN;


public class Doubleslip {

	// for doubleslips which can be interactivly set from panel
	// the doubleslip is equivalent to 2 (coupled) turnouts
	private TurnoutElement t1;
	private TurnoutElement t2;

	public Doubleslip(TurnoutElement turnout1, TurnoutElement turnout2) {
 		t1 = turnout1;
		t2 = turnout2;
    }

    TurnoutElement getT1() {
        return t1;
    }

    TurnoutElement getT2() {
        return t2;
    }
 }
