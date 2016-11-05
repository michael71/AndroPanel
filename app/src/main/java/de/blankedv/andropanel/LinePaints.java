package de.blankedv.andropanel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;

import static de.blankedv.andropanel.AndroPanelApplication.*;

public class LinePaints
{
	private static int width;
	private static int height;
	public static void init(Context context) {
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		width = metrics.widthPixels;
		height = metrics.heightPixels;
		Log.d(TAG,"LinePaint init - w="+width+" h="+height);
		
	linePaint = new Paint();
	linePaint.setColor(Color.WHITE);
	linePaint.setStrokeWidth(4.5f*prescale);
	linePaint.setAntiAlias(true);
	linePaint.setDither(true);
	linePaint.setStyle(Paint.Style.STROKE);
	linePaint.setStrokeCap(Paint.Cap.SQUARE);
	//linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
	
	tachoPaint = new Paint();
	tachoPaint.setColor(Color.WHITE);
	tachoPaint.setStrokeWidth(4.5f*prescale);
	tachoPaint.setAntiAlias(true);
	tachoPaint.setDither(true);
	tachoPaint.setStyle(Paint.Style.FILL);

	
	linePaintRedDash = new Paint();
	linePaintRedDash.setColor(Color.RED);
	linePaintRedDash.setStrokeWidth(3.5f*prescale);
	linePaintRedDash.setAntiAlias(true);
	linePaintRedDash.setDither(true);
	linePaintRedDash.setStyle(Paint.Style.STROKE);
	linePaintRedDash.setStrokeCap(Paint.Cap.SQUARE);
	linePaintRedDash.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
	
	linePaintGrayDash = new Paint(linePaintRedDash);
	linePaintGrayDash.setColor(0xffaaaaaa); 
	
	linePaint2 = new Paint();
	linePaint2.setColor(Color.WHITE);
	linePaint2.setStrokeWidth(4.5f*prescale);
	linePaint2.setAntiAlias(true);
	linePaint2.setDither(true);
	linePaint2.setStyle(Paint.Style.STROKE);
	linePaint2.setStrokeCap(Paint.Cap.ROUND);
	
	rasterPaint = new Paint();
	rasterPaint.setColor(Color.LTGRAY);
	rasterPaint.setAntiAlias(true);
	rasterPaint.setDither(true);
	
	
	circlePaint = new Paint();
	circlePaint.setColor(0x88ff2222);
	circlePaint.setAntiAlias(true);
	circlePaint.setDither(true);
	
	greenPaint = new Paint();
	greenPaint.setColor(0xcc00ff00);
	greenPaint.setAntiAlias(true);
	greenPaint.setStrokeWidth(4.5f*prescale);
	greenPaint.setDither(true);
	greenPaint.setStyle(Paint.Style.STROKE);
	greenPaint.setStrokeCap(Paint.Cap.ROUND);
	
	redPaint = new Paint();
	redPaint.setColor(0xccff0000);
	redPaint.setStrokeWidth(4.5f*prescale);
	redPaint.setAntiAlias(true);
	redPaint.setDither(true);
	redPaint.setStyle(Paint.Style.STROKE);
	redPaint.setStrokeCap(Paint.Cap.ROUND);
	
	bgPaint = new Paint();
	bgPaint.setColor(BG_COLOR);
	bgPaint.setAntiAlias(true);
	bgPaint.setStrokeWidth(3.8f*prescale);
	bgPaint.setDither(true);
	bgPaint.setStyle(Paint.Style.STROKE);
	bgPaint.setStrokeCap(Paint.Cap.BUTT);
	
	sxAddressPaint  = new TextPaint();
	sxAddressPaint.setColor(Color.YELLOW);
	sxAddressPaint.setTextSize(7*prescale);
	sxAddressPaint.setStyle(Style.FILL);
	
	xyPaint  = new TextPaint();
	xyPaint.setColor(Color.GRAY);
	xyPaint.setTextSize(6*prescale);


	sxAddressBGPaint = new Paint();
	sxAddressBGPaint.setColor(Color.DKGRAY);
	sxAddressBGPaint.setAlpha(175);
	
	rimPaint = new Paint();
	rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	rimPaint.setShader(new LinearGradient(0.0f, 0.0f, 500f, 400f, 
									   Color.rgb(0xf0, 0xf5, 0xf0),
									   Color.rgb(0x30, 0x31, 0x30),
									   Shader.TileMode.CLAMP));		
	
	majorTick = new Paint();
	majorTick.setColor(Color.BLACK);
	majorTick.setAntiAlias(true);
	majorTick.setStrokeWidth(scaled(4.5f));
	majorTick.setDither(true);
	majorTick.setStyle(Paint.Style.STROKE);
	
	minorTick = new Paint();
	minorTick.setColor(Color.BLACK);
	minorTick.setAntiAlias(true);
	minorTick.setStrokeWidth(scaled(1.5f));
	minorTick.setDither(true);
	minorTick.setStyle(Paint.Style.STROKE);

	tachoOutsideLine = new Paint();
	tachoOutsideLine.setColor(Color.BLACK);
	tachoOutsideLine.setAntiAlias(true);
	tachoOutsideLine.setStrokeWidth(scaled(0.6f));
	tachoOutsideLine.setDither(true);
	tachoOutsideLine.setStyle(Paint.Style.STROKE);
	
	rimCirclePaint = new Paint();
	rimCirclePaint.setAntiAlias(true);
	rimCirclePaint.setStyle(Paint.Style.STROKE);
	rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
	rimCirclePaint.setStrokeWidth(0.005f);
	
	rimShadowPaint = new Paint();
	rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, 400, 
			   new int[] { 0x00000000, 0x00000500, 0x50000500 },
			   new float[] { 0.96f, 0.96f, 0.99f },
			   Shader.TileMode.MIRROR));
	rimShadowPaint.setStyle(Paint.Style.FILL);
	
	tachoSpeedPaint = new TextPaint();
	tachoSpeedPaint.setColor(Color.BLACK);
	tachoSpeedPaint.setTextSize(scaled(15.0f));
	tachoSpeedPaint.setStyle(Style.FILL);

	tachoShadowPaint = new TextPaint();
	tachoShadowPaint.setColor(Color.LTGRAY);
	tachoShadowPaint.setTextSize(scaled(15.0f));
	tachoShadowPaint.setStyle(Style.FILL);



	}

	private static float scaled(float w) {
		return (float)(w*prescale*width/1280);
	}
}
