/*  (C) 2011-2015, Michael Blank
 *
 *  This file is part of Lanbahn Throttle.

    Lanbahn Throttle is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Lanbahn Throttle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with LanbahnThrottle.  If not, see <http://www.gnu.org/licenses/>. */


package de.blankedv.andropanel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.util.Log;

import static de.blankedv.andropanel.AndroPanelApplication.*;

/**
 *
 * NOT USED !!!
 *
 *
 * network threads for sending and receiving multicast messages over the network at
 * the LANBAHN port 27027 and the LANBAHN_GROUP 239.200.201.250
 *
 * SENDING: (own thread)
 * 2 types of commands are sent, exapmles:
 * LOCO 798 50 1 0 0 0 0 1 (String set by calling "updateLocoCommand")
 * POWER 0   (String set by calling "updatePowerCommand")
 * data is sent when changed at a rate of every 100ms, the LocoCommand is repeated
 * every 5 seconds if nothing has changed
 *
 * RECEIVING
 * all command are received, currently only the "power 0" and "power 1" commands
 * are processed (=> change of global track power state
 *
 */
public class LanbahnThread extends Thread {

    private static final int LANBAHN_PORT = 27027;
    private static final String LANBAHN_GROUP = "239.200.201.250";
    private final boolean DEBUG_COMM = false;
    private final long LOCO_REFRESH_INTERVAL = 5 * 1000L;  // = 5 seconds
    // all sending is stopped if throttle activity is no longer running after 30 secs
    private final long THROTTLE_DEAD = 30 * 1000L;  // = 30 seconds
    protected InetAddress mgroup;
    protected MulticastSocket multicastsocket;
    protected long lastLocoUpdate; // time of last sent message with LOCO command
    private Timer sendTimer;
    // this LOCO command is regularly sent as UDP message
    // every 5 secs if not changed, every 100 msecs if changed
    private String locoCommand = "";
    private String lastLocoCommand = "";
    private long locoRefreshTime = 0L;
    // power command is only sent when changed
    private String powerCommand = "";
    private String lastPowerCommand = "";
    private Context mContext;

    /**
     * initialize LanbahnClientTread and start a thred for sending
     * messages to the lanbahn multicast group
     *
     * @param context
     */
    public LanbahnThread(Context context) {
        if (DEBUG)
            Log.d(TAG, "LanbahnClientThread constructor.");
        mContext = context;


    }

    public void run() {
        if (DEBUG)
            Log.d(TAG, "LanbahnClientThread run.");
        connect();

        // create sendTimer for periodically checking if there
        // are new messages to send
        //sendTimer = new Timer();
        //sendTimer.schedule(new mySendTimer(), 100);

        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length, mgroup,
                LANBAHN_PORT);

        try {
            while (true) {
                // receiving loop for main program (receive(packet) is blocking
                multicastsocket.receive(packet);
                String message = new String(packet.getData(), 0,
                        packet.getLength(), "UTF-8");

                // replace multiple spaces by one only
                message = message.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ENGLISH);

                if (DEBUG_COMM) Log.d(TAG, "received:" + message + ".");
                interpretMessage(message);   // send to UI
            }
        } catch (IOException e) {
            Log.e(TAG, "ERROR in ..Client..run(): " + e.getMessage());
        }
        multicastsocket.close();
        Log.e(TAG, "ERROR: LanbahnClientThread stopping.");
    }


    private void interpretMessage(String cmd) {
        //if (DEBUG) Log.d(TAG, "received  cmd=" + cmd);
        cmd = cmd.toLowerCase();
        if (cmd.charAt(0) == 'a') {
            String info[] = cmd.split(" ");
            if ((info.length >= 3)  && info[1].contains("sx3pc")) {
                // send message containing sx3pc to UI
                if (DEBUG) Log.d(TAG, "SX3PC ip=" + info[2]);
                Message m = Message.obtain();
                m.what = SX3PC_IP_MESSAGE;
                m.obj = info[2];
                handler.sendMessage(m);  // send lanbahn data to UI Thread via Message
            }


        }
    }

    private void connect() {
        if (DEBUG) Log.d(TAG, "trying conn to Lanbahn UDP");
        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface
                    .getNetworkInterfaces(); list.hasMoreElements(); ) {
                NetworkInterface i = list.nextElement();
                //Log.d(TAG, "network_interface: " + i.getDisplayName());
            }
        } catch (SocketException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            long lastLocoUpdate;
        }

        try {
            multicastsocket = new MulticastSocket(LANBAHN_PORT);
            multicastsocket.setLoopbackMode(true); // == DISABLE loopback
            mgroup = InetAddress.getByName(LANBAHN_GROUP);
            multicastsocket.joinGroup(mgroup);
            if (DEBUG)
                Log.d(TAG, "connected to lanbahn  " + LANBAHN_GROUP + ":" + LANBAHN_PORT);

        } catch (Exception e) {
            Log.e(TAG,
                    "LanbahnClientThread.connect - Exception: "
                            + e.getMessage());
        }
    }

    /** separate Thread for sending to the network
     * send: max every 100msec
     */
    /*
    public class mySendTimer extends TimerTask {

        mySendTimer() {
            lastLocoUpdate = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if (DEBUG) Log.d(TAG, "mySendTimer.run");
            String lastCommand = "";
            // check send queue for messages to send toLanbahn multicast group
            while (true) {    // will only be killed by operation system

                // send messages only when throttle was active recently
                if ((System.currentTimeMillis() - lastThrottleActiveTime) < THROTTLE_DEAD) {
                    if (!locoCommand.equalsIgnoreCase(lastLocoCommand)) {
                        if (immediateSend(locoCommand.toUpperCase())) {
                            // store only if sent succeeded
                            lastLocoCommand = locoCommand.toUpperCase();
                            locoRefreshTime = System.currentTimeMillis();
                        }
                    } else if ((System.currentTimeMillis() - locoRefreshTime) > LOCO_REFRESH_INTERVAL) {
                        immediateSend(lastLocoCommand.toUpperCase());
                        locoRefreshTime = System.currentTimeMillis();
                    }
                    if ((!powerCommand.equalsIgnoreCase(lastPowerCommand)) &&
                            (!powerCommand.isEmpty())) {
                        if (immediateSend(powerCommand.toUpperCase())) {
                            lastPowerCommand = powerCommand.toUpperCase();
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }

            }
        }


        // Sends a Lanbahn UDP command via wifi
        // @param command the message to send (multicast). Null or empty string => no send.
        // @return true, if sending was successful

        private boolean immediateSend(String command) {

            if (mContext == null) return false;

            if ((command == null) || (command.length() == 0)) {
                //Log.e(TAG, "imm.Send: message == null or has zero lenght.)");
                return false;
            }

            ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi == null || !mWifi.isConnected()) {
                Log.e(TAG, "imm.Send: no wifi.)");
                return false;
            }

            if (!connected) {
                Log.e(TAG, "imm.Send: not connected.)");
                return false;
            }

            try {
                byte[] buf = command.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, mgroup,
                        LANBAHN_PORT);
                if (multicastsocket != null) {
                    multicastsocket.send(packet);
                    if (DEBUG_COMM) Log.d(TAG, "sent: " + command);
                } else {
                    if (DEBUG_COMM) Log.d(TAG, "could NOT send: " + command);
                }

            } catch (IOException ex) {
                System.out.println("imm.Send: ERROR when sending to lanbahn "
                        + ex.getMessage());
                return false;
            }
            return true;
        }

    } */

}
