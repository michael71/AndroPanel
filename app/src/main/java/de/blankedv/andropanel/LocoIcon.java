/*  (c) 2011, Michael Blank
 * 
 *  This file is part of SRCP Client.

    SRCP Client is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SRCP Client is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SCRPclient.  If not, see <http://www.gnu.org/licenses/>.

*/

package de.blankedv.andropanel;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;


public class LocoIcon {
	
	private Bitmap mIcon;
	
	public static final int BITMAP_HEIGHT = 100;
	public static final int BITMAP_WIDTH = 230;	
	
	public LocoIcon(Bitmap bm) {
		// create new loco Icon from Input Stream
        
		mIcon = Bitmap.createBitmap(bm);
		// should always be landscape
		if (mIcon.getHeight() > mIcon.getWidth()) {
			// Setting post rotate to 90
			Matrix mtx = new Matrix();
			mtx.postRotate(90);
			// Rotating Bitmap
			mIcon = Bitmap.createBitmap(mIcon, 0, 0, mIcon.getWidth(), mIcon.getHeight(), mtx, true);
		}
		scale();
		roundCorners();
	}
	
	public static int calc_scale(int w, int h) {
		int scale = 1;
		if ((w > BITMAP_WIDTH) && (h > BITMAP_HEIGHT)) {
			// scale should be a power of 2 !!
			scale = (int) Math.pow(2, (int) (Math.round(Math.log(BITMAP_WIDTH / (double) w)) / Math.log(0.5)));
		}
		return scale;
	}
		
	private void roundCorners() {
		int w = mIcon.getWidth();
		int h = mIcon.getHeight();
		//if (DEBUG) Log.d(TAG,"rounded corners in: "+w+" * "+h);
		assert (w == BITMAP_WIDTH);
		int y = (h - BITMAP_HEIGHT)/2;
		if (y < 0) y=0;
		Bitmap crop = Bitmap.createBitmap(mIcon, 0, y, BITMAP_WIDTH, BITMAP_HEIGHT);  //230x100 cropped

		// make nice round corners
		Bitmap output = Bitmap.createBitmap(crop.getWidth(), crop.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output); 
		
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, crop.getWidth(), crop.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = 12;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(crop, rect, rect, paint);
		mIcon = output;
	}


	public Bitmap getBitmap() {
	
		return mIcon;
	}

	public void scale() {
		// scale bitmap
		float ratio = ((float)mIcon.getHeight()) / mIcon.getWidth();
		int newheight = (int)(BITMAP_WIDTH * ratio);
		if (newheight < BITMAP_HEIGHT) newheight = BITMAP_HEIGHT;
		mIcon = Bitmap.createScaledBitmap(mIcon,BITMAP_WIDTH, newheight, true);
	}
}
