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

public class LinePaints {
    private static int width;
    private static int height;


    public static void init(Context context) {

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        Log.d(TAG, "LinePaint init - w=" + width + " h=" + height);

        reinitPaints = false;

        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(9f);
        linePaint.setAntiAlias(true);
        linePaint.setDither(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.SQUARE);
        //linePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        signalLine = new Paint();
        signalLine.setColor(Color.WHITE);
        signalLine.setStrokeWidth(4.0f);
        signalLine.setAntiAlias(true);
        signalLine.setDither(true);
        signalLine.setStyle(Paint.Style.STROKE);
        signalLine.setStrokeCap(Paint.Cap.SQUARE);

        tachoPaint = new Paint();
        tachoPaint.setColor(Color.WHITE);
        tachoPaint.setStrokeWidth(9f);
        tachoPaint.setAntiAlias(true);
        tachoPaint.setDither(true);
        tachoPaint.setStyle(Paint.Style.FILL);


        linePaintRedDash = new Paint();
        linePaintRedDash.setColor(Color.RED);
        linePaintRedDash.setStrokeWidth(7f);
        linePaintRedDash.setAntiAlias(true);
        linePaintRedDash.setDither(true);
        linePaintRedDash.setStyle(Paint.Style.STROKE);
        linePaintRedDash.setStrokeCap(Paint.Cap.SQUARE);
        linePaintRedDash.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        linePaintGrayDash = new Paint(linePaintRedDash);
        linePaintGrayDash.setColor(0xffaaaaaa);

        linePaint2 = new Paint();
        linePaint2.setColor(Color.WHITE);
        linePaint2.setStrokeWidth(9f);
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
        greenPaint.setStrokeWidth(9f);
        greenPaint.setDither(true);
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setStrokeCap(Paint.Cap.ROUND);

        greenSignal = new Paint(greenPaint);
        greenSignal.setStyle(Paint.Style.FILL);

        redPaint = new Paint();
        redPaint.setColor(0xccff0000);
        redPaint.setStrokeWidth(9f);
        redPaint.setAntiAlias(true);
        redPaint.setDither(true);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeCap(Paint.Cap.ROUND);

        bgPaint = new Paint();
        bgPaint.setColor(BG_COLOR);
        bgPaint.setAntiAlias(true);
        bgPaint.setStrokeWidth(7.6f);
        bgPaint.setDither(true);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.BUTT);

        sxAddressPaint = new TextPaint();
        sxAddressPaint.setColor(Color.YELLOW);
        sxAddressPaint.setTextSize(14);
        sxAddressPaint.setStyle(Style.FILL);

        xyPaint = new TextPaint();
        xyPaint.setColor(Color.GRAY);
        xyPaint.setTextSize(12);

        rasterPaint = new Paint();
        rasterPaint.setColor(Color.LTGRAY);
        rasterPaint.setAntiAlias(true);
        rasterPaint.setDither(true);

        sxAddressBGPaint = new Paint();
        sxAddressBGPaint.setColor(Color.DKGRAY);
        sxAddressBGPaint.setAlpha(175);

        /*rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setShader(new LinearGradient(0.0f, 0.0f, 500f, 400f,
                Color.rgb(0xf0, 0xf5, 0xf0),
                Color.rgb(0x30, 0x31, 0x30),
                Shader.TileMode.CLAMP)); */

        /* majorTick = new Paint();
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
        minorTick.setStyle(Paint.Style.STROKE); */

        rasterPaint = new Paint();
        rasterPaint.setColor(Color.LTGRAY);
        rasterPaint.setAntiAlias(true);
        rasterPaint.setDither(true);

        circlePaint = new Paint();
        circlePaint.setColor(0x88ff2222);
        circlePaint.setAntiAlias(true);
        circlePaint.setDither(true);
        /*tachoOutsideLine = new Paint();
        tachoOutsideLine.setColor(Color.BLACK);
        tachoOutsideLine.setAntiAlias(true);
        tachoOutsideLine.setStrokeWidth(scaled(0.6f));
        tachoOutsideLine.setDither(true);
        tachoOutsideLine.setStyle(Paint.Style.STROKE); */

        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
        rimCirclePaint.setStrokeWidth(0.005f);

        /*rimShadowPaint = new Paint();
        rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, 400,
                new int[]{0x00000000, 0x00000500, 0x50000500},
                new float[]{0.96f, 0.96f, 0.99f},
                Shader.TileMode.MIRROR));
        rimShadowPaint.setStyle(Paint.Style.FILL); */

        /* tachoSpeedPaint = new TextPaint();
        tachoSpeedPaint.setColor(Color.BLACK);
        tachoSpeedPaint.setTextSize(scaled(15.0f));
        tachoSpeedPaint.setStyle(Style.FILL);

        tachoShadowPaint = new TextPaint();
        tachoShadowPaint.setColor(Color.LTGRAY);
        tachoShadowPaint.setTextSize(scaled(15.0f));
        tachoShadowPaint.setStyle(Style.FILL); */

        yellowPaint = new Paint();
        yellowPaint.setColor(0xccffff00);
        yellowPaint.setAntiAlias(true);
        yellowPaint.setStrokeWidth(9f);
        yellowPaint.setDither(true);
        yellowPaint.setStyle(Paint.Style.STROKE);
        yellowPaint.setStrokeCap(Paint.Cap.ROUND);

        yellowSignal = new Paint(yellowPaint);
        yellowSignal.setStyle(Paint.Style.FILL);


        redPaint = new Paint();
        redPaint.setColor(0xccff0000);
        redPaint.setStrokeWidth(9f);
        redPaint.setAntiAlias(true);
        redPaint.setDither(true);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setStrokeCap(Paint.Cap.ROUND);

        redSignal = new Paint(redPaint);
        redSignal.setStyle(Paint.Style.FILL);

        greyPaint = new Paint();
        greyPaint.setColor(Color.GRAY);
        greyPaint.setStrokeWidth(9f);
        greyPaint.setAntiAlias(true);
        greyPaint.setDither(true);
        greyPaint.setStyle(Paint.Style.STROKE);
        greyPaint.setStrokeCap(Paint.Cap.ROUND);

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(9f);
        whitePaint.setAntiAlias(true);
        whitePaint.setDither(true);
        whitePaint.setStyle(Paint.Style.STROKE);
        whitePaint.setStrokeCap(Paint.Cap.ROUND);

        btn0Paint = new Paint();
        btn0Paint.setColor(Color.GRAY);
        btn0Paint.setStrokeWidth(12f);
        btn0Paint.setAntiAlias(true);
        btn0Paint.setDither(true);
        btn0Paint.setStyle(Paint.Style.STROKE);
        btn0Paint.setStrokeCap(Paint.Cap.ROUND);

        btn1Paint = new Paint();
        btn1Paint.setColor(Color.WHITE);
        btn1Paint.setStrokeWidth(12f);
        btn1Paint.setAntiAlias(true);
        btn1Paint.setDither(true);
        btn1Paint.setStyle(Paint.Style.STROKE);
        btn1Paint.setStrokeCap(Paint.Cap.ROUND);

        addressPaint = new TextPaint();
        addressPaint.setColor(Color.YELLOW);
        addressPaint.setTextSize(14f);
        addressPaint.setStyle(Style.FILL);

        addressBGPaint = new Paint();
        addressBGPaint.setColor(Color.DKGRAY);
        addressBGPaint.setAlpha(175);

        panelNamePaint = new TextPaint();
        panelNamePaint.setColor(Color.LTGRAY);
        panelNamePaint.setTextSize(24f);
        panelNamePaint.setStyle(Style.FILL);


        bgPaint = new Paint();
        bgPaint.setColor(BG_COLOR);
        bgPaint.setAntiAlias(true);
        bgPaint.setStrokeWidth(7.6f);
        bgPaint.setDither(true);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.BUTT);

        if (selectedStyle.equals("DE")) {
            BG_COLOR = Color.LTGRAY;
            linePaint.setColor(Color.BLACK);
            signalLine.setColor(Color.BLACK);
            linePaint2.setColor(Color.BLACK);
            rasterPaint.setColor(Color.LTGRAY);
            rasterPaint.setAntiAlias(true);
            rasterPaint.setDither(true);
            greyPaint.setColor(Color.GRAY);
            whitePaint.setColor(Color.BLACK);
            bgPaint.setColor(BG_COLOR);
            addressPaint.setColor(Color.YELLOW);
            addressBGPaint.setColor(Color.DKGRAY);
            panelNamePaint.setColor(Color.BLACK);
        } else if (selectedStyle.equals("UK")) {
            BG_COLOR = 0xff306630;
            linePaint.setColor(Color.BLACK);
            signalLine.setColor(Color.BLACK);
            linePaint2.setColor(Color.BLACK);
            rasterPaint.setColor(Color.LTGRAY);
            rasterPaint.setAntiAlias(true);
            rasterPaint.setDither(true);
            greyPaint.setColor(Color.GRAY);
            whitePaint.setColor(Color.BLACK);

            bgPaint.setColor(BG_COLOR);
            addressPaint.setColor(Color.YELLOW);
            addressBGPaint.setColor(Color.DKGRAY);
            panelNamePaint.setColor(Color.BLACK);
        } else  {
            BG_COLOR = Color.BLACK;
            linePaint.setColor(Color.WHITE);
            signalLine.setColor(Color.WHITE);
            linePaint2.setColor(Color.WHITE);
            rasterPaint.setColor(Color.LTGRAY);
            rasterPaint.setAntiAlias(true);
            rasterPaint.setDither(true);
            greyPaint.setColor(Color.GRAY);
            whitePaint.setColor(Color.WHITE);
            bgPaint.setColor(BG_COLOR);
            addressPaint.setColor(Color.YELLOW);
            addressBGPaint.setColor(Color.DKGRAY);
            panelNamePaint.setColor(Color.WHITE);
        }
    }

    private static float scaled(float w) {
        return (float) (w * 2 * width / 1280);
    }
}
