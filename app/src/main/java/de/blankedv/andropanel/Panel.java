package de.blankedv.andropanel;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import static de.blankedv.andropanel.AndroPanelApplication.*;
import static de.blankedv.andropanel.ParseConfig.*;

/**
 * the main panel of the application is comprised of two parts: a (small height)
 * LOCO CONTROL area at the top and the larger part with the main SWITCH PANEL
 * at the bottom, handles all touch events
 * 
 * the switch panel is scaled by: matrix.postScale(scale,scale); and translated
 * by: matrix.postTranslate(xoff, yoff);
 */
public class Panel extends SurfaceView implements SurfaceHolder.Callback {
	public static Rect controlAreaRect;

	private ViewThread mThread;
	private int mX, mY;
	private float mPosX, mPosY;

	private float mLastTouchX, mLastTouchY;
	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	private static Paint paintControlAreaBG;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	private long scalingTime = 0L;

	public static int mWidth, mHeight;
	private static float oldAl=0;
	//private static final long SCALING_WAIT = 1000L;
	
	private static final boolean DEBUG_PANEL = false;  // =DEBUG

	LocoControlArea locoControlArea;

	/** main panel for application
	 * starts and stops rendering thread
	 * @param context
	 */
	@TargetApi(8)
	public Panel(Context context) {
		super(context);

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

		paintControlAreaBG = new Paint();
		paintControlAreaBG.setColor(0xff224422);
		panelNamePaint = new TextPaint();

		locoControlArea = new LocoControlArea(context);

		AndroPanelApplication.initSXaddresses(panelElements);
		getHolder().addCallback(this);

		mThread = new ViewThread(this);

	}

	// start and stop the dedicated rendering thread
	// implementing SurfaceHolder Callback for your SurfaceView.
	// This should be enough to
	// limit drawing only when the SurfaceView is visible.

	public void surfaceCreated(SurfaceHolder holder) {
		if (!mThread.isAlive()) {
			mThread = new ViewThread(this);
			mThread.setRunning(true);
			mThread.start();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mThread.isAlive()) {
			mThread.setRunning(false);
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(event);

		final int action = event.getAction();

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			final float x = event.getX();
			final float y = event.getY();

			mLastTouchX = x;
			mLastTouchY = y;

			mX = (int) (x); // /scale);
			mY = (int) (y); // /scale);

			mPosX = 0;
			mPosY = 0;
			// if (DEBUGPANEL) Log.d(TAG,"ACTION_DOWN - (scaled) mX="+mX+"  mY"+mY);
			// if (DEBUGPANEL) Log.d(TAG,"ACTION_DOWN - (abs) x="+x+"  y"+y);
			mActivePointerId = event.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = event.findPointerIndex(mActivePointerId);
			final float x = event.getX(pointerIndex);
			final float y = event.getY(pointerIndex);

			// Only move if the ScaleGestureDetector isn't processing a gesture.
			if (!mScaleDetector.isInProgress()) {
				final float dx = x - mLastTouchX;
				final float dy = y - mLastTouchY;

				mPosX += dx;
				mPosY += dy;
				if ((zoomEnabled) && (mX > 300) && (mY > 200)) {
					xoff += dx;
					yoff += dy;
					// avoid control of SX elements during pan-move
					scalingTime = System.currentTimeMillis(); 
				}
				// invalidate();
				// if (DEBUGPANEL) Log.d(TAG,"mPosX="+mPosX+" mPosY="+mPosY);
				locoControlArea.checkSpeedMove(x, y);
			}

			mLastTouchX = x;
			mLastTouchY = y;

			if (enableEdit) {
				// selSxAddress.dismiss();
			}

			break;
		}

		case MotionEvent.ACTION_UP: {
			if (DEBUG_PANEL)
				Log.d(TAG, "ACTION_UP");
			mActivePointerId = INVALID_POINTER_ID;

			// do SX control only when NOT scaling (and wait 1 sec after
			// scaling)
			long deltaT = System.currentTimeMillis() - scalingTime;
			if (!mScaleDetector.isInProgress()) { // && (deltaT > SCALING_WAIT))
													// {
				// assuming control area is always at the top !!
				if (mLastTouchY < controlAreaRect.bottom) {
					Log.d(TAG,
							"ACTION_UP _Checking Loco Control  at: mlastTouchX="
									+ mLastTouchX + "  mLastTouchY"
									+ mLastTouchY);
					locoControlArea.checkTouch(mLastTouchX, mLastTouchY);
				} else {
					if (disp_selected == DISP_PANEL) {
					Log.d(TAG,
							"ACTION_UP _Checking SX panel elements at: mlastTouchX="
									+ mLastTouchX + "  mLastTouchY"
									+ mLastTouchY);
					float xs = (mLastTouchX - xoff) / scale;
					float ys = (mLastTouchY - yoff) / scale;
					Log.d(TAG, "ACTION_UP _Checking SX panel elements at: xs="
							+ xs + "  ys" + ys);
					for (PanelElement e : panelElements) {
						if (e.isSelected(xs, ys)) { // mLastTouchX,
													// mLastTouchY)) {
							if (enableEdit) {
								Dialogs.selectSXAddressDialog(e); //
							} else {
								e.toggle();
							}
							break; // only 1 can be selected with one touch
						}
					}
					} else {
						// control tacho etc
						Log.d(TAG,"ACTION_UP speed mlastTouchX="
								+ mLastTouchX + "  mLastTouchY"
								+ mLastTouchY);
						calcSpeedTouched(mLastTouchX,mLastTouchY);
					}
				}

			} else {
				if (DEBUG_PANEL)
					Log.d(TAG, "scaling wait - delta-t=" + deltaT);
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
			if (DEBUG_PANEL)
				Log.d(TAG, "ACTION_CANCEL - mPosX=" + mPosX + " mPosY=" + mPosY);
			mActivePointerId = INVALID_POINTER_ID;

			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {
			final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = event.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastTouchX = event.getX(newPointerIndex);
				mLastTouchY = event.getY(newPointerIndex);
				mActivePointerId = event.getPointerId(newPointerIndex);

			}
			break;
		}
		default:
			if (DEBUG_PANEL)
				Log.d(TAG, "unknown motion event = " + event.toString());
		} // end switch

		return true; // super.onTouchEvent(event);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG, "surface changed - format=" + format + " w=" + width + " h="
				+ height);
		mWidth = width;
		mHeight = height;
		controlAreaRect = new Rect(0, 0, mWidth, mHeight / 8);
		locoControlArea.recalcGeometry();

		panelNamePaint.setColor(Color.LTGRAY);
		panelNamePaint.setTextSize((8.0f * prescale * width) / 1024);
		panelNamePaint.setStyle(Style.FILL);
	}

	public void doDraw(Canvas canvas) {

		canvas.drawColor(BG_COLOR);
		
		// draw Panel and scale with zoom
		// if (USS == true)
		int topLeft = mHeight / 8;
		myBitmap.eraseColor(Color.TRANSPARENT); // Color.DKGRAY);
		if (disp_selected == DISP_PANEL) {
			// label with panel name and display green "unlock", if zoom enabled
			
			if (zoomEnabled) {
				canvas.drawBitmap(bitmaps.get("unlock"), 5, topLeft, null);
			} else {
				canvas.drawBitmap(bitmaps.get("lock"), 5, topLeft, null);
			}
			canvas.drawText(panelName, 50, topLeft + 24, panelNamePaint);

			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			matrix.postTranslate(xoff, yoff);

			for (PanelElement e : panelElements) {
				e.doDraw(myCanvas);
			}
			drawRaster(myCanvas, RASTER);

			canvas.drawBitmap(myBitmap, matrix, null);
		} else {
	
			drawThrottle(canvas);
		}

		canvas.drawRect(controlAreaRect, paintControlAreaBG);

		locoControlArea.draw(canvas); // not scaled with zoom

	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if ((zoomEnabled) && (disp_selected == DISP_PANEL)) {
				mScaleFactor *= detector.getScaleFactor();
				Log.d(TAG, "mScaleFactor=" + mScaleFactor);
				// Don't let the object get too small or too large.
				mScaleFactor = Math.max(0.4f, Math.min(mScaleFactor, 3.0f));
				Log.d(TAG, "mScaleFactor (lim)=" + mScaleFactor);
				scale = mScaleFactor;
				invalidate();
				scalingTime = System.currentTimeMillis();
			}
			return true;
		}
	}

	private void drawRaster(Canvas canvas, int step) {
		String txt="";
		for (int x = 0; x <= Math.min(canvas.getWidth(),panelXmax*prescale); x += step) {
			for (int y = 0; y <= Math.min(canvas.getHeight(),panelYmax*prescale); y += step) {
				// (RASTER*5)
				if ((drawXYValues) && (x%200 == RASTER) && (y%160 == RASTER) &&
						(x <= panelXmax*prescale) && (y <= panelYmax*prescale)) {
					txt=x/prescale+"/"+y/prescale;
					float dx=xyPaint.measureText(txt);
					canvas.drawText(txt, x-dx/2, y+4, xyPaint);
				} else {
					canvas.drawPoint(x, y, rasterPaint);
				}
			}
		}
	}
	
    private void drawThrottle(Canvas canvas) {
		//canvas.drawBitmap(bitmaps.get("fahrpult_rw"), mWidth/5, topLeft+50, null);
		float xc = mWidth/2;
		float yc = mHeight/2+mHeight/16;
		float rad = mHeight/2.7f;
		canvas.drawCircle(xc, yc, rad, tachoPaint);
		canvas.drawCircle(xc, yc, rad*1.01f, tachoOutsideLine);
		canvas.drawCircle(xc, yc, rad/40, minorTick);
		//canvas.drawCircle(xc, yc, rad/1.04f, rimShadowPaint);
		float al,phi,x1,x2,y1,y2;
		int vmax = locolist.selectedLoco.vmax;
		for (int i= 0; i<= vmax; i+=5) {
			al = (float) (-135f+(i*270f/vmax));
			phi = (float) (al*Math.PI/180.0f);
			x1 = (float) (xc +rad*0.9*FloatMath.sin(phi));
			x2 = (float) (xc +rad*FloatMath.sin(phi));
			y1 = (float) (yc -rad*0.9*FloatMath.cos(phi));
			y2 = (float) (yc -rad*FloatMath.cos(phi));
			canvas.drawLine(x1,y1,x2,y2,minorTick);
			//Log.d(TAG,"al="+al+" phi="+phi+"  x1,x2,y1,y2="+x1+x2+y1+y2);
		}
		for (int i= 0; i<= vmax; i+=20) {
			al = (float) (-135f+(i*270f/vmax));
			phi = (float) (al*Math.PI/180.0f);
			x1 = (float) (xc +rad*0.8*FloatMath.sin(phi));
			x2 = (float) (xc +rad*FloatMath.sin(phi));
			y1 = (float) (yc -rad*0.8*FloatMath.cos(phi));
			y2 = (float) (yc -rad*FloatMath.cos(phi));
			canvas.drawLine(x1,y1,x2,y2,majorTick);
			//Log.d(TAG,"al="+al+" phi="+phi+"  x1,x2,y1,y2="+x1+x2+y1+y2);
			String txt=""+i;
			Rect tb = new Rect();
		    tachoSpeedPaint.getTextBounds(txt, 0, txt.length(), tb);
		    //Log.d(TAG,"Bounds i="+i+" tb.left="+tb.left+" tb.right="+tb.right+" tb.top="+tb.top+" tb.bottom="+tb.bottom);
		    float offX=(tb.right)/2.0f;
		    float offY=(-tb.top)/2.0f;

			canvas.drawText(txt, (float) (xc +rad*0.64*FloatMath.sin(phi)-offX), 
					(float) (yc -rad*0.64*FloatMath.cos(phi)+offY),tachoSpeedPaint);
		}

		// speed zeiger
		int s = locolist.selectedLoco.speed_from_sx;
		al = (float) (-135f+(Math.abs(s)*270f/31));
		al = (float) (0.1*al+0.9*oldAl); // low pass filter
		oldAl=al;
		phi = (float) (al*Math.PI/180.0f);
		x1 = (float) (xc +rad*0.02*FloatMath.sin(phi));
		x2 = (float) (xc +rad*0.85*FloatMath.sin(phi));
		y1 = (float) (yc -rad*0.02*FloatMath.cos(phi));
		y2 = (float) (yc -rad*0.85*FloatMath.cos(phi));
	//	canvas.drawLine(x1+2,y1,x2+2,y2,tachoShadowPaint);
		canvas.drawLine(x1,y1,x2,y2,minorTick);
    }

    private int calcSpeedTouched(float mLastTouchX2, float mLastTouchY2) {
    	float xc = mWidth/2;
		float yc = mHeight/2+mHeight/16;
		float rad = mHeight/2.7f;
		
		float rx, ry, r; // relative to Zero
		rx = mLastTouchX2 - xc;
		ry = -mLastTouchY2 + yc;
		r = FloatMath.sqrt(rx*rx+ry*ry);
		Log.d(TAG,"speed rx="+rx+" ry="+ry+" r="+r+" rad="+rad);;
		if ((r > rad*0.8f) && (r < rad *1.1f)) {
			float angle = (float) ((Math.atan2(ry,rx)/Math.PI)*180f);
			Log.d(TAG,"speed radius o.k.  rx="+rx+" ry="+ry+ " atan2 angle="+angle);
			if ((angle >0) && (angle <=180)) {
				angle = 225-angle;
			} else if ((angle >= -180) && (angle <= -130)) {
				angle = 45-(180+angle);
			} else if ((angle <=0) && (angle >= -50) ) {
			    angle =-angle+225;
			} else {
				angle = 999f;
			}
			if (angle != 999f) {
				angle = Math.min(angle, 270f);
				angle = Math.max(0, angle);

				float speed = 160f*angle/270f;
				Log.d(TAG,"speed radius o.k.  atan2 angleCorr="+angle+" speed="+speed+" km/h");
				int s = Math.round(31*speed/160f);
                if (locolist.selectedLoco.isForward()) {
                    locolist.selectedLoco.setSpeed(s);
                } else {
                    locolist.selectedLoco.setSpeed(-s);
                }
			} else {
				Log.d(TAG,"speed radius o.k.  invalid angle");
			}
		}
		return 0;
    }
}
