package de.blankedv.andropanel;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class ViewThread extends Thread {

	    private Panel mPanel;
	    private SurfaceHolder mHolder;
	    private boolean mRun = false;
	 
	    public ViewThread(Panel panel) {
	        mPanel = panel;
	        mHolder = mPanel.getHolder();
	    }
	 
	    public void setRunning(boolean run) {
	        mRun = run;
	    }
	 
	    @Override
	    public void run() {
	        Canvas canvas;
	        while (mRun) {
	            canvas = mHolder.lockCanvas();
	            if (canvas != null) {
	                mPanel.doDraw(canvas);
	                mHolder.unlockCanvasAndPost(canvas);
	            }
	        }
	    }

}
