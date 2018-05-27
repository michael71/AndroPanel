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
		panelNamePaint.setTextSize((16.0f * width) / 1024);
		panelNamePaint.setStyle(Style.FILL);
	}

	public void doDraw(Canvas canvas) {

		canvas.drawColor(BG_COLOR);
		
		// draw Panel and scale with zoom
		// if (USS == true)
		int topLeft = mHeight / 8;
		myBitmap.eraseColor(Color.TRANSPARENT); // Color.DKGRAY);

			// label with panel name and display green "unlock", if zoom enabled
			
			if (zoomEnabled) {
				canvas.drawBitmap(bitmaps.get("unlock"), 5, topLeft, null);
			} else {
				canvas.drawBitmap(bitmaps.get("lock"), 5, topLeft, null);
			}
			canvas.drawText(panelName, 50, topLeft + 24, panelNamePaint);

			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			matrix.postTranslate(xoff, yoff );

			for (PanelElement e : panelElements) {
				e.doDraw(myCanvas);
			}
			drawRaster(myCanvas, RASTER);

			canvas.drawBitmap(myBitmap, matrix, null);


		canvas.drawRect(controlAreaRect, paintControlAreaBG);

		locoControlArea.draw(canvas); // not scaled with zoom

	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (zoomEnabled) {
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
		String txt;
		for (int x = 0; x <= Math.min(canvas.getWidth(),panelXmax*2); x += step) {
			for (int y = 0; y <= Math.min(canvas.getHeight(),panelYmax*2); y += step) {
				// (RASTER*5)
				if ((drawXYValues) && (x%200 == RASTER) && (y%160 == RASTER) &&
						(x <= panelXmax*2) && (y <= panelYmax*2)) {
					txt=x/2+"/"+y/2;
					float dx=xyPaint.measureText(txt);
					canvas.drawText(txt, x-dx/2, y+4, xyPaint);
				} else {
					canvas.drawPoint(x, y, rasterPaint);
				}
			}
		}
	}
	
}
