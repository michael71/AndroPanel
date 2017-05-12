package de.blankedv.andropanel;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;
import static de.blankedv.andropanel.AndroPanelApplication.bitmaps;
import static de.blankedv.andropanel.AndroPanelApplication.DEBUG;
import static de.blankedv.andropanel.AndroPanelApplication.TAG;

/** all bitmap resources, which are needed during program lifetime, are
 *  loaded from resources into the Hashtable<String, Bitmap> bitmaps 
 *  They can be retrieved by statements like   bitmaps.get("reddot")
 *  
 * @author mblank
 *
 */
public class AndroBitmaps {
	
	public static void init(Resources resources)  {
		
		bitmaps.put("commok", BitmapFactory.decodeResource(resources, R.drawable.commok));
		bitmaps.put("nocomm", BitmapFactory.decodeResource(resources, R.drawable.nocomm));

		bitmaps.put("sensor_on", BitmapFactory.decodeResource(resources, R.drawable.led_red_2));
		bitmaps.put("sensor_off",BitmapFactory.decodeResource(resources, R.drawable.led_off_2));
		bitmaps.put("sensor_on_act",BitmapFactory.decodeResource(resources, R.drawable.sens_on_act));
		bitmaps.put("sensor_off_act",BitmapFactory.decodeResource(resources, R.drawable.sens_off_act));
		bitmaps.put("incr",BitmapFactory.decodeResource(resources, R.drawable.incr));
		bitmaps.put("decr",BitmapFactory.decodeResource(resources, R.drawable.decr));
		
		bitmaps.put("greendot", BitmapFactory.decodeResource(resources, R.drawable.greendot));
		bitmaps.put("reddot", BitmapFactory.decodeResource(resources, R.drawable.reddot));
		bitmaps.put("greydot", BitmapFactory.decodeResource(resources, R.drawable.greydot));

		bitmaps.put("genloco_s", BitmapFactory.decodeResource(resources, R.drawable.genloco_s));
		
		bitmaps.put("loco1", BitmapFactory.decodeResource(resources, R.drawable.f7_2_s));
		bitmaps.put("loco0", BitmapFactory.decodeResource(resources, R.drawable.f7_2_l));
		
		bitmaps.put("button120", BitmapFactory.decodeResource(resources, R.drawable.button120));
		bitmaps.put("button100", BitmapFactory.decodeResource(resources, R.drawable.button100));
		
		bitmaps.put("stop_s_on", BitmapFactory.decodeResource(resources, R.drawable.stop_s_on));
		bitmaps.put("stop_s_off", BitmapFactory.decodeResource(resources, R.drawable.stop_s_off));

		
		bitmaps.put("lamp1", BitmapFactory.decodeResource(resources, R.drawable.lamp1btn));
		bitmaps.put("lamp0", BitmapFactory.decodeResource(resources, R.drawable.lamp0btn));
	
		if (DEBUG) Log.d(TAG,"BM h="+bitmaps.get("lamp1").getHeight());
		bitmaps.put("func1", BitmapFactory.decodeResource(resources, R.drawable.func1));
		bitmaps.put("func0", BitmapFactory.decodeResource(resources, R.drawable.func0));
		
		bitmaps.put("bump", BitmapFactory.decodeResource(resources, R.drawable.bump));
		
		bitmaps.put("slider", BitmapFactory.decodeResource(resources, R.drawable.slider));
		bitmaps.put("slider_grey", BitmapFactory.decodeResource(resources, R.drawable.slider_grey));
		
		bitmaps.put("lock", BitmapFactory.decodeResource(resources, R.drawable.lock_red_s));
		bitmaps.put("unlock", BitmapFactory.decodeResource(resources, R.drawable.unlock_s));
	}

	
}
