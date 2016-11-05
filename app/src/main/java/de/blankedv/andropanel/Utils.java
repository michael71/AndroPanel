package de.blankedv.andropanel;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.format.DateFormat;

/** Utils - utility functions
 * 
 * @author Michael Blank
 *
 */
public class Utils {
	/** scale a bitmap both in x and y direction
	 * 
	 * @author Michael Blank
	 * @param bm bitmap to resize
	 * @param scale scaling factor (both in x and y direction)
	 * @return re-scaled Bitmap
	 *
	 */
	
	public static Bitmap getResizedBitmap(Bitmap bm, float scale) {

		int width = bm.getWidth();

		int height = bm.getHeight();


		// create a matrix for the manipulation

		Matrix matrix = new Matrix();
		// resize the bit map

		matrix.postScale(scale, scale);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

	public static String getDateTime()
	{
	    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss");
	    //df.setTimeZone(TimeZone.getTimeZone("PST"));
	    return df.format(new Date());
	}
	// svn test comment
}
