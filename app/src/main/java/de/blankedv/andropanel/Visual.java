package de.blankedv.andropanel;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 
 * @author Sonja Schittenhelm
 *
 */
public class Visual {

	/**
	 * Gets or sets the canvas.
	 */
	private Canvas canvas;
	
	/**
	 * Gets or sets the bitmap.
	 */
	private Bitmap bitmap;
	
	/**
	 * Gets or sets the context.
	 */
	private Context context;
	
	/**
	 * Gets or sets the layout drawing area.
	 */
	private RectF window; 
	
	/**
	 * Gets or sets the layout dimension.
	 */
	private RectF layout; 
	
	/**
	 * Gets or sets the layout/screen ratio.
	 */
	private PointF ratio;
	
	private float scale;
	private static final boolean DEBUG = true;
	private static String myTag = AndroPanelApplication.TAG; // For debugging purposes only
	
	/**
	 * Android screen informations.
	 */
	public DisplayMetrics screen; 
	
	/**
	 * Gets or sets the layout offset.
	 */
	public PointF offset;
	
	/**
	 * Initializes a new instance of the Visual class.
	 */
	public Visual() 
	{
		// Use Sonshi tag for logcat debugging only.
		if (Visual.DEBUG) myTag = "Sonshi";
		
		if (Visual.DEBUG) Log.v(myTag, "Initialize Visual");
		
		// Initialize variables.
		this.createBitmap();
		this.screen = new DisplayMetrics();
		this.getScreen();
		this.window = new RectF();
		this.layout = new RectF();
		this.ratio = new PointF();
		this.offset = new PointF();
		this.scale = 1.0f;
	}
	
	/**
	 * Gets the screen informations.
	 */
	public void getScreen()
	{
		// Avoid null exception
		if (this.context == null)
		{
			Log.w(myTag, "Activity not yet started!");
			return;
		}
		
		// Ok, there is a context. Let's get work.
		if (Visual.DEBUG) Log.v(myTag, "Get screen info.");
		WindowManager wm = (WindowManager)this.context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(this.screen);
		
		Log.d(myTag, String.format("Screen (%d, %d)", this.screen.widthPixels, this.screen.heightPixels));
		Log.d(myTag, String.format("Prescale (%f)", this.getPreScale()));
	}
	
	/**
	 * Sets the context.
	 * @param context Application context.
	 */
	public void setContext(Context context)
	{
		this.context = context;
	}
	
	/**
	 * Gets the context.
	 * @return Returns the application context.
	 */
	public Context getContext()
	{
		return this.context;
	}
	
	/**
	 * Gets the canvas.
	 * @return Returns a canvas.
	 */
	public Canvas getCanvas()
	{
		return new Canvas(this.bitmap);
	}
	
	/**
	 * Gets the bitmap.
	 * @return Returans a bitmap.
	 */
	public Bitmap getBitmap()
	{
		return this.bitmap;
	}
	
	/**
	 * Sets the current window size.
	 * @param width Window width.
	 * @param height Window height.
	 */
	public void setWindow(int width, int height)
	{
		//Log.i(TAG,"surface changed - format="+format+" w="+width+" h="+height);
		
		this.window.right = (float)width;
		this.window.bottom = (float)height;
		Log.d(myTag, String.format("Window (%.1f, %.1f)", this.window.right, this.window.bottom));
	}
	
	/**
	 * Gets the size of the control area.
	 * @return Returns the control area.
	 */
	public RectF getControlArea()
	{
		RectF rc = new RectF(this.window);
		rc.bottom = this.window.bottom / 8f;
		return rc;
	}

	/**
	 * Gets the size of the layout area.
	 * @return Returns the layout area.
	 */
	public RectF getLayoutArea()
	{
		RectF rc = new RectF(this.window);
		rc.top = this.window.bottom / 8f;
		return rc;
	}
	
	/**
	 * Gets the pre scale factor.
	 * @return Returns the pre scale factor.
	 */
	public float getPreScale()
	{
		//float prescale = (float)this.screen.widthPixels / 200f;

		// Assure a minimum pre scale of 1
		//if (prescale < 1f) prescale = 1f;
		
		return (float)this.screen.density;
	}
	
	/**
	 * Gets the width/height of the grid. 
	 */
	public float getGrid()
	{
		return 20f * this.getPreScale();
	}
	
	/**
	 * Gets the scale factor.
	 * @return Returns the current scale factor;
	 */
	public float getScale()
	{
		return this.scale;
	}
	
	/**
	 * Sets the scale factor.
	 * @param scale The new scale factor.
	 */
	public void setScale(float scale)
	{
		Log.d(myTag, String.format("Scale changed (%f)", this.scale));
		this.scale = scale;
	}
	
	/**
	 * Fits the layout to the screen.
	 */
	public void fit()
	{
    	Log.v(myTag, "Fit layout...");
		this.scale = this.getFitScale();
	}
	
	/**
	 * Center the layout on the screen.
	 */
	public void center()
	{
    	Log.v(myTag, "Center layout...");
    	this.calculateOffset();
	}
	
	/**
	 * Evaluate the size of the specified layout.
	 * @param items List of panel elements.
	 */
	public void getLayoutSize(ArrayList<PanelElement> items)
	{
		Log.v(myTag, String.format("Evaluate layout size (%d items) ...", items.size()));
		
		for (PanelElement item : items)
		{
			// if (Visual.DEBUG) Log.d(myTag, String.format("Item (%s): (%d, %d); (%d, %d); (%d, %d)", item.name, item.x, item.y, item.x2, item.y2, item.xt, item.yt));
			
			// Evaluate the x.min (Left) value.
			if (this.layout.left > item.x && item.x != AndroPanelApplication.INVALID_INT) this.layout.left = item.x;
			if (this.layout.left > item.x2 && item.x2 != AndroPanelApplication.INVALID_INT) this.layout.left = item.x2;
			if (this.layout.left > item.xt && item.xt != AndroPanelApplication.INVALID_INT) this.layout.left = item.xt;
			
			// Evaluate the y.min (Top) value.
			if (this.layout.top > item.y && item.y != AndroPanelApplication.INVALID_INT) this.layout.top = item.y;
			if (this.layout.top > item.y2 && item.y2 != AndroPanelApplication.INVALID_INT) this.layout.top = item.y2;
			if (this.layout.top > item.yt && item.yt != AndroPanelApplication.INVALID_INT) this.layout.top = item.yt;
			
			// Evaluate the x.max (Right) value.
			if (this.layout.right < item.x && item.x != AndroPanelApplication.INVALID_INT) this.layout.right = item.x;
			if (this.layout.right < item.x2 && item.x2 != AndroPanelApplication.INVALID_INT) this.layout.right = item.x2;
			if (this.layout.right < item.xt && item.xt != AndroPanelApplication.INVALID_INT) this.layout.right = item.xt;
			
			// Evaluate the y.max (Bottom) value.
			if (this.layout.bottom < item.y && item.y != AndroPanelApplication.INVALID_INT) this.layout.bottom = item.y;
			if (this.layout.bottom < item.y2 && item.y2 != AndroPanelApplication.INVALID_INT) this.layout.bottom = item.y2;
			if (this.layout.bottom < item.yt && item.yt != AndroPanelApplication.INVALID_INT) this.layout.bottom = item.yt;
		}
		
		Log.d(myTag, String.format("Layout Size: (%.0f, %.0f); (%.0f, %.0f)", this.layout.left, this.layout.top, this.layout.right, this.layout.bottom));
	}

	/**
	 * Gets the scale factor to fit the layout in the draw area.
	 * @return Returns the fit scale factor.
	 */
	private float getFitScale()
	{
		this.calculateRatio();
		
		if (this.ratio.x < this.ratio.y)
		{
			Log.d(myTag, String.format("Fit scale (%f)", this.ratio.x));
			return this.ratio.x;
		}
		else
		{
			Log.d(myTag, String.format("Fit scale (%f)", this.ratio.y));
			return this.ratio.y;
		}
	}
	
	/**
	 * Calculates the layout/screen ratio.
	 */
	private void calculateRatio()
	{
		if (Visual.DEBUG) Log.v(myTag, "Calculate ratio...");
		if (Visual.DEBUG) Log.v(myTag, String.format("Screen (%d, %d)", this.screen.widthPixels, this.screen.heightPixels));
		if (Visual.DEBUG) Log.v(myTag, String.format("Area (%.1f, %.1f)", this.window.width(), this.window.height()));
		if (Visual.DEBUG) Log.v(myTag, String.format("Layout (%.1f, %.1f)", this.layout.width(), this.layout.height()));
		
		this.ratio.x = this.window.width() / this.layout.width() / this.getPreScale();
		this.ratio.y = this.window.height() / this.layout.height() / this.getPreScale();
		
		Log.d(myTag, String.format("Ratio (%.1f, %.1f)", this.ratio.x, this.ratio.y));
	}
	
	/**
	 * Calculates the x/y offset to center the layout in the draw area.
	 */
	private void calculateOffset()
	{
		if (Visual.DEBUG) Log.v(myTag, "Calculate offset...");
		if (Visual.DEBUG) Log.v(myTag, String.format("Center panel: (%.1f, %.1f)", this.window.centerX(), this.window.centerY()));
		if (Visual.DEBUG) Log.v(myTag, String.format("Center layout: (%.1f, %.1f)", this.layout.centerX(), this.layout.centerY()));
		
		this.offset.x = (this.window.centerX() / this.getPreScale() - this.layout.centerX() * scale) * this.getPreScale();
		this.offset.y = (this.window.centerY() / this.getPreScale() - this.layout.centerY() * scale) * this.getPreScale();
    	
    	Log.d(myTag, String.format("Offset (%.1f, %.1f)", this.offset.x, this.offset.y));
	}
	
	/**
	 * !!! NOT YET IMPLEMENTED !!! - Dynamically resizes the bitmap and canvas.
	 */
	private void resizeBitmap()
	{
		/*
		Point size = new Point();
		size.x = (int)FloatMath.floor((float)this.layoutSize.width() * scale *1.5f);
		size.y = (int)FloatMath.floor((float)this.layoutSize.height() * scale *2f);
		
		Log.d(myTag, String.format("Resize Bitmap (%d, %d)", size.x, size.y));
    	
		myBitmap = Bitmap.createBitmap(size.x, size.y , Bitmap.Config.ARGB_4444);
		myCanvas = new Canvas(myBitmap);
		*/
	}

	/**
	 * Creates a new default (4000, 1600) bitmap. 
	 */
	private void createBitmap()
	{
		//public static Bitmap myBitmap = Bitmap.createBitmap(4000,1600, Bitmap.Config.ARGB_4444);
		this.bitmap = Bitmap.createBitmap(4000, 1600, Bitmap.Config.ARGB_8888);
	}
}
