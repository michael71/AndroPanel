package de.blankedv.andropanel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * main application
 * holds all globally used constants and variables
 * <p>
 * handles interfacing with sx-net-client, which is run on different thread
 * <p>
 * main contains a 50msec timer which is used for LocoSpeed mass simulation
 * <p>
 * multiple turnouts can be controlled (all from panel...xml)
 * and a single Loco (name "selectedLoco", selected from loco...xml)
 * and sensor states are displayed (all from panel...xml)
 *
 * @author mblank
 */

public class AndroPanelApplication extends Application {

    public static final boolean DEBUG = false;  // enable or disable debugging with file

    public static final String TAG = "AndroPanelActivity";

    public static ArrayList<PanelElement> panelElements = new ArrayList<>();

    public static String panelName = "panel_1";
    public static ArrayList<Route> routes = new ArrayList<>();


    public static ArrayList<Loco> locolist = new ArrayList<>();
    public static Loco selectedLoco = null;
    public static String locolistName ="?";


    public static boolean demoFlag = false;

    public static boolean restartCommFlag = false;

    public static int counter = 0;
    // preferences
    public static final String KEY_IP = "ipPref";
    public static final String KEY_LOCO_ADR = "locoAdrPref";
    public static final String KEY_LOCO_MASS = "locoMassPref";
    public static final String KEY_LOCO_NAME = "locoNamePref";
    public static final String KEY_SHOW_SX = "showSXPref";
    public static final String KEY_SHOW_XY_VALUES = "showXYPref";
    public static final String KEY_ENABLE_ZOOM = "enableZoomPref";
    public static final String KEY_ENABLE_EDIT = "enableEditPref";
    public static final String KEY_ENABLE_DEMO = "enableDemoPref";
    public static final String KEY_XOFF = "xoffPref";
    public static final String KEY_YOFF = "yoffPref";
    public static final String KEY_SCALE = "scalePref";
    public static final String KEY_CONFIG_FILE = "configFilenamePref";
    public static final String KEY_LOCOS_FILE = "locosFilenamePref";
    public static final String KEY_STYLE_PREF = "selectStylePref";

    public static final int SXNET_PORT = 4104;
    public static final int SX_FEEDBACK_MESSAGE = 1;
    public static final int LAHNBAHN_MESSAGE = 2;
    public static final int SX3PC_IP_MESSAGE = 3;
    public static final int ERROR_MESSAGE = 4;

    public static final int INVALID_INT = -9999;

    private static int[] sxData = new int[128];   // contains all selectrix channel data

    public static boolean drawSXAddresses;
    public static boolean drawXYValues;
    public static String selectedStyle="DE";

    public static IncomingHandler handler;   //

    // connection state

    public static long mLastMessage = 0;

    // in the sendQ all messages are queued and later sent via SXnetThread to SX_System
    public static final BlockingQueue<String> sendQ = new ArrayBlockingQueue<>(50);

    public static String connString = "";

    public static final String DIRECTORY = "andropanel/";     // with trailing slash
    // panel config and loco config file (see preferences) are loaded from this directory
    // names must match "panel....." and "loco.....", resp.

    public static final String DEMO_FILE = "panel-demo.xml";    // demo data in raw assets dir.
    public static final String DEMO_LOCOS_FILE = "locos-demo.xml";

    public static String locoConfigFilename = "locos.xml";
    public static String configFilename = "panel.xml";


    public static boolean configHasChanged = false;   // store info whether config has changed
    // if true, then a new config file is written at the end of the Activity
    public static boolean locoConfigHasChanged = false;   // store info whether config has changed
    // if true, then a new config file is written at the end of the Activity

    public static final ArrayList<Integer> adrList = new ArrayList<>();  // contains all needed SX channels
    public static final Hashtable<String, Bitmap> bitmaps = new Hashtable<>();

    public static boolean zoomEnabled;
    public static float scale = 1.0f;  // user selectable scaling of panel area


    // all Paints and track and turnout x/y-s are scaled by 2 before drawing to bitmap
    public static float xoff = 20;
    public static float yoff = 100;
    public static int peOffsetX = 0;  // this number will be added to the X-values of the Panel Elements
    public static int peOffsetY = 0;  // this number will be added to the Y-values of the Panel Elements


    // define Paints
    public static Paint linePaint, linePaint2, rasterPaint, circlePaint, signalLine, yellowPaint, yellowSignal, greenPaint, greenSignal, redSignal;
    public static Paint greyPaint, whitePaint, btn0Paint, btn1Paint, addressPaint, addressBGPaint;
    public static Paint redPaint, linePaintRedDash, linePaintGrayDash, tachoPaint, tachoOutsideLine;
    public static Paint rimCirclePaint, rimShadowPaint, rimPaint, majorTick, minorTick, tachoSpeedPaint, tachoShadowPaint;
    public static int BG_COLOR = Color.DKGRAY;  // panel background color
    public static Paint bgPaint, sxAddressBGPaint;
    public static TextPaint sxAddressPaint, xyPaint, panelNamePaint;  // used for displaying SX address on panel and for panel Name

    public static boolean reinitPaints = false;

    public static final int RASTER = 40;   // raster points with xx pixels
    public static final int TURNOUT_LENGTH = 10;
    public static final int TURNOUT_LENGTH_LONG = (int) (TURNOUT_LENGTH * 1.4f);

    public static Bitmap myBitmap = Bitmap.createBitmap(4000, 1600, Bitmap.Config.ARGB_4444);
    public static Canvas myCanvas = new Canvas(myBitmap);

    public static boolean enableEdit = false;

    public static Context appContext;



    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "onCreate AndroPanelApplication");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        drawSXAddresses = prefs.getBoolean(KEY_SHOW_SX, false);
        drawXYValues = prefs.getBoolean(KEY_SHOW_XY_VALUES, false);

        selectedStyle = prefs.getString(KEY_STYLE_PREF, "US");
        LinePaints.init(this);

        // do some initializations
        for (int i = 0; i < sxData.length; i++) {
            sxData[i] = 0;
        }
        AndroBitmaps.init(getResources());

        handler = new IncomingHandler(this);

        Log.d(TAG, "device name=" + getDeviceName());


        // initialize Lanbahn
        // NOT USED  lbClient = new LanbahnThread(getApplicationContext());
        //  lbClient.start();

    }

    // this construct to avoid leaks see the postings
    // https://groups.google.com/forum/?fromgroups=#!msg/android-developers/1aPZXZG6kWk/lIYDavGYn5UJ
    // http://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
    static class IncomingHandler extends Handler {
        private final WeakReference<AndroPanelApplication> mApp;

        IncomingHandler(AndroPanelApplication app) {
            mApp = new WeakReference<AndroPanelApplication>(app);
        }

        @Override
        public void handleMessage(Message msg) {
            AndroPanelApplication app = mApp.get();
            if (app != null) {
                app.handleMessage(msg);
            }
            tachoPaint.setStrokeCap(Paint.Cap.SQUARE);
        }
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "AndroPanelApp - terminating.");

    }

    public void handleMessage(Message msg) {
        int what = msg.what;
        switch (what) {
            case SX_FEEDBACK_MESSAGE:
                int chan = msg.arg1;
                int data = msg.arg2;
                sxData[chan] = data;
                mLastMessage = System.currentTimeMillis();
                for (PanelElement pe : panelElements) {
                    if (pe.getSxAdr() == chan) {
                        pe.update();
                    }
                }
                break;
            case LAHNBAHN_MESSAGE:
                String content = (String)msg.obj;
                System.out.println("object.toString()="+content);
                break;

            case ERROR_MESSAGE:
                String error = (String)msg.obj;
                Toast toast = Toast.makeText(appContext, error, Toast.LENGTH_LONG);
                toast.show();
                break;

        }

    }

    /**
     * the addresses of all active elements are stored in "adrList", to
     * be able to act properly if data in these channels is changing
     *
     * @param panelElements
     */
    public static void initSXaddresses(ArrayList<PanelElement> panelElements) {
        adrList.clear();
        for (PanelElement e : panelElements) {
            if (e instanceof SXPanelElement) {
                // add its address to list of interesting SX addresses
                // only needed for active elements, not for tracks
                int a = e.getSxAdr();
                if (!adrList.contains(a) && (a != INVALID_INT)) {
                    adrList.add(a);
                }
            }
        }
        if (!adrList.contains(127))
            adrList.add(127);  // always interested in (virtual) Power Channel
        if (!adrList.contains(selectedLoco.adr)) adrList.add(selectedLoco.adr);

        if (DEBUG) {
            StringBuilder adrL = new StringBuilder();
            for (int a : adrList) {
                adrL.append(" ").append(a);
            }
            Log.d(TAG, "adrlist=" + adrL);
        }
        requestSXdata();
    }

    public static void requestSXdata() {
        for (int a : adrList)
            sendQ.add("R " + a);  // request dara for these addresses from SX central station
    }

    public static boolean isPowerOn() {
        if ((sxData[127] & 0x80) != 0)
            return true;
        else
            return false;
    }

    public static int getSxData(int chan) {
        if (validSXAddress(chan)) {
            if (!adrList.contains(chan)) {
                // add to list of addresses which needs to be listened to
                adrList.add(chan);
                sendQ.add("R " + chan);   // request data
            }

            return sxData[chan]; // sxData is private and read only for other
            // programs
        } else {
            return 0;
        }
    }

    public static int getSxBit(int chan, int bit) {
        if (validSXAddress(chan) && (bit >= 1) && (bit <= 8)) {
            if ((sxData[chan] & (1 << (bit - 1))) == 0) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }


    public static boolean validSXAddress(int chan) {
        if ((chan >= 0) && (chan < 128)) {
            return true;
        } else {
            return false;
        }
    }

    public void saveZoomEtc() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_XOFF, "" + xoff);
        editor.putString(KEY_YOFF, "" + yoff);
        editor.putString(KEY_SCALE, "" + scale);
        editor.putString(KEY_LOCO_ADR, "" + selectedLoco.adr);
        editor.putString(KEY_LOCO_MASS, "" + selectedLoco.mass);
        editor.putString(KEY_LOCO_NAME, "" + selectedLoco.name);
        editor.putString(KEY_STYLE_PREF, selectedStyle);
        // Commit the edits!
        editor.commit();
    }

    public void loadZoomEtc() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        zoomEnabled = prefs.getBoolean(KEY_ENABLE_ZOOM, false);
        enableEdit = prefs.getBoolean(KEY_ENABLE_EDIT, false);
        demoFlag = prefs.getBoolean(KEY_ENABLE_DEMO, false);
        xoff = Float.parseFloat(prefs.getString(KEY_XOFF, "20"));
        yoff = Float.parseFloat(prefs.getString(KEY_YOFF, "50"));
        scale = Float.parseFloat(prefs.getString(KEY_SCALE, "1.0"));
        selectedStyle = prefs.getString(KEY_STYLE_PREF, "US");
        // loco data loaded before

    }


    public static boolean connectionIsAlive() {
        if ((System.currentTimeMillis() - mLastMessage) < 30 * 1000) {
            return true;
        } else {
            //if (DEBUG) Log.d(TAG,"connection no longer alive.");
            return false;
        }
    }


    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /*public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("NsdChat")){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    } */

}
