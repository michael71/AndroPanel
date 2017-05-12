package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.*;
import static de.blankedv.andropanel.Panel.controlAreaRect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas; 
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * handles the display of the top 20% of the display, the "LOCO CONTROL AREA"
 * one loco can be controlled (out of the ones defined in "locos-xyz.xml" Files
 * 
 * @author mblank
 *
 */
public final class LocoControlArea {

	private static Paint paintLocoAdrTxt, paintLocoSpeedTxt, paintLocoSpeed, green, white, editPaint, demoPaint;
	private static int sliderXoff, sliderYoff;

	private static final float X_LOCO_MID=0.5f;
	private static final float X_LOCO_RANGE=0.25f;

	private static float ySpeed = 50f, xSpeedAct=0, xSpeedToBe=0;
	private static final float ZERO_LINE_HALFLENGTH = 15f;  // 
	private static float canvasWidth = 100f;

	private static int sxmin=0, sxmax=0;

	private LocoButton stopBtn, lampBtn, adrBtn, incrSpeedBtn, decrSpeedBtn, commBtn, powerBtn, functionBtn, nameBtn;
	private static long lastSpeedCheckMove=0L;
	private static float lastXt=X_LOCO_MID;

    Context ctx;

	public LocoControlArea(Context context) {

        ctx = context;

		sliderXoff =  bitmaps.get("slider").getWidth()/2;
		sliderYoff =  bitmaps.get("slider").getHeight()/2;
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		// define some paints for the loco controls and texts
		paintLocoSpeed  = new Paint();
		paintLocoSpeed.setColor(0xff22ff22);

		paintLocoAdrTxt  = new Paint();
		paintLocoAdrTxt.setColor(Color.BLACK);
		paintLocoAdrTxt.setTextSize(calcTextSize(width));

		editPaint  = new Paint();
		editPaint.setColor(Color.RED);
		editPaint.setTextSize(calcTextSize(width));
		editPaint.setTypeface(Typeface.DEFAULT_BOLD);
		
		demoPaint  = new Paint();
		demoPaint.setColor(Color.CYAN);
		demoPaint.setTextSize(calcTextSize(width));	
		demoPaint.setTypeface(Typeface.DEFAULT_BOLD);

		paintLocoSpeedTxt = new Paint();
		paintLocoSpeedTxt.setColor(Color.WHITE);
		paintLocoSpeedTxt.setTextSize(calcTextSize(width)*0.66f);

		green = new Paint();
		green.setColor(Color.GREEN);
		white = new Paint();
		white.setColor(Color.WHITE);

		stopBtn = new LocoButton(0.20f,0.5f,
				bitmaps.get("stop_s_on"),
				bitmaps.get("stop_s_off"));
		lampBtn = new LocoButton(0.848f,0.5f,
				bitmaps.get("lamp1"),
				bitmaps.get("lamp0"));
		functionBtn = new LocoButton(0.796f,0.5f,
			 	bitmaps.get("func1"),
			 	bitmaps.get("func0"));
		adrBtn = new LocoButton(0.91f,0.5f);  // only text.
		//adrBtn = new LocoButton(0.91f,0.5f,bitmaps.get("genloco_s"));

		incrSpeedBtn = new LocoButton(0.97f,0.52f,bitmaps.get("incr"));
		decrSpeedBtn = new LocoButton(0.03f,0.52f,bitmaps.get("decr"));
	
		commBtn = new LocoButton(0.09f,0.5f,bitmaps.get("commok"),bitmaps.get("nocomm"));
        powerBtn = new LocoButton(0.13f, 0.5f, bitmaps.get("greendot"), bitmaps.get("reddot"), bitmaps.get("greydot"));
    }

	public void draw(Canvas canvas) {
			 
		selectedLoco.updateLocoFromSX();  // to be able to display actual states of this loco
		
		// draw "buttons" and states
		
		adrBtn.doDraw(canvas,selectedLoco.adr, paintLocoAdrTxt);		
		lampBtn.doDraw(canvas,selectedLoco.lamp_to_be);
	 	functionBtn.doDraw(canvas,selectedLoco.function_to_be);
		stopBtn.doDraw(canvas,(selectedLoco.speed_act != 0));
		incrSpeedBtn.doDraw(canvas);
		decrSpeedBtn.doDraw(canvas);
		commBtn.doDraw(canvas,AndroPanelApplication.connectionIsAlive());
        // disable power indicator when no connection
        powerBtn.doDraw(canvas, AndroPanelApplication.isPowerOn(), AndroPanelApplication.connectionIsAlive());


        // draw slider for speed selection
		sxmin = (int)((canvas.getWidth()*(X_LOCO_MID-X_LOCO_RANGE)));
		sxmax = (int)((canvas.getWidth()*(X_LOCO_MID+X_LOCO_RANGE)));

		Rect speedLine = new Rect(sxmin,(int)(ySpeed-1),sxmax,(int)ySpeed+1);       
		canvas.drawRect(speedLine, white);

		Rect zeroLine = new Rect((sxmin+sxmax)/2-1,(int)(ySpeed-ZERO_LINE_HALFLENGTH),
				(sxmin+sxmax)/2+1,(int)(ySpeed+ZERO_LINE_HALFLENGTH));   
		canvas.drawRect(zeroLine, white);

		canvasWidth = canvas.getWidth();	

		xSpeedAct = canvasWidth*sxSpeed();   // grey slider on bottom
		xSpeedToBe = canvasWidth*speedToBe();  // orange slider on top

		canvas.drawBitmap(bitmaps.get("slider_grey"), xSpeedAct-sliderXoff, ySpeed-sliderYoff,  null);
		canvas.drawBitmap(bitmaps.get("slider"), xSpeedToBe-sliderXoff, ySpeed-sliderYoff,  null);

		int xtext =  (int)(canvasWidth*(X_LOCO_MID+X_LOCO_RANGE*0.9f));
		canvas.drawText(locoSpeed(),xtext, ySpeed+32, paintLocoSpeedTxt);
		xtext =  (int)(canvasWidth*(X_LOCO_MID-X_LOCO_RANGE*0.9f));
		canvas.drawText(selectedLoco.longString(),xtext, ySpeed+32, paintLocoSpeedTxt);
		
		if (enableEdit) canvas.drawText("Edit", (int)(canvas.getWidth()*0.36f), ySpeed*0.8f, editPaint);
		if (demoFlag) canvas.drawText("Demo", (int)(canvas.getWidth()*0.28f), ySpeed*0.8f, demoPaint);
		//canvas.drawText(""+counter, xtext, ySpeed-20, paintLocoSpeedTxt);
	
	}

	private float sxSpeed() {
		int s = selectedLoco.speed_from_sx;
		float speed = (X_LOCO_RANGE*(float)s / 31.0f)+X_LOCO_MID;  // sx=31 ==> ~0.25f (+0.6f offset)
		                            
		return speed;
	}
	
	private float speedToBe() {
		int s = selectedLoco.speed_to_be;
		float speed = (X_LOCO_RANGE*(float)s / 31.0f)+X_LOCO_MID;  // sx=31 ==> ~0.25f (+0.6f offset)
		return speed;
	}

	private  String locoSpeed() {		
		int s = selectedLoco.speed_from_sx;
		return ""+s;
	}



	public void checkSpeedMove(float xt, float yt) {
		// check slider
		//if (DEBUG) Log.d(TAG,"check slider touch xt="+xt+"  yt="+yt+" xSpeed="+xSpeed+" sliderXoff="+sliderXoff+" ySpeed="+ySpeed+" sliderYoff="+sliderYoff);

		if (   (xt > (X_LOCO_MID-X_LOCO_RANGE)*canvasWidth) 
				&& (xt < (X_LOCO_MID+X_LOCO_RANGE)*canvasWidth) 
				&& (yt > (ySpeed-sliderYoff)) 
				&& (yt < (ySpeed+sliderYoff))) {
			lastSpeedCheckMove = System.currentTimeMillis();
			lastXt = xt;
			int s = Math.round((((31.0f/X_LOCO_RANGE)*(xt - X_LOCO_MID*canvasWidth))/canvasWidth));
			if (DEBUG) Log.d(TAG,"slider, speed set to be = "+s);
			selectedLoco.setSpeed(s);  // will be sent only when different to currently known speed.
			//AndroPanelApplication.sendSpeed(0,true);
		}
	}

	/**
	 * check, if the control area was touched at the Button positions or at the speed slider
	 * 
	 * @param x
	 * @param y
	 */
	public void checkTouch(float x, float y) {
		if (stopBtn.isTouched(x, y)) {
			selectedLoco.stopLoco();
		} else if (lampBtn.isTouched(x,y)) {
			selectedLoco.toggleLocoLamp();
		} else if (incrSpeedBtn.isTouched(x,y)) {
			selectedLoco.incrLocoSpeed();
		} else if (decrSpeedBtn.isTouched(x,y)) {
			selectedLoco.decrLocoSpeed();
		} else if (functionBtn.isTouched(x,y)) {
			selectedLoco.toggleFunc();
        } else if (commBtn.isTouched(x, y)) {
            restartCommFlag = true;
        } else if (adrBtn.isTouched(x,y)) {
			Dialogs.selectLocoDialog();
			
		}

	}

	public void recalcGeometry() {
		stopBtn.recalcXY();
		lampBtn.recalcXY();
		adrBtn.recalcXY();	
		decrSpeedBtn.recalcXY();
		incrSpeedBtn.recalcXY();
		functionBtn.recalcXY();
		powerBtn.recalcXY();
		commBtn.recalcXY();
		ySpeed=(controlAreaRect.bottom - controlAreaRect.top)/2;  // mid of control area range.
	}

	private float calcTextSize(int w) {
		// text size = 30 for width=1024
		return (30.0f*w/1024);
	}
	
}
