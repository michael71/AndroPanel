package de.blankedv.andropanel;

import static de.blankedv.andropanel.AndroPanelApplication.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import android.content.Context;
import android.os.Message;
import android.util.Log;

/**
 * communicates with the SX3-PC server program (usually on port 4104)
 * 
 * runs on own thread, using a BlockingQueue for queing the commands
 * can be shutdown by calling the shutdown method.
 * 
 * @author mblank
 *
 */
public class SXnetClientThread extends Thread {
	// threading und BlockingQueue siehe http://www.javamex.com/tutorials/blockingqueue_example.shtml

	private volatile boolean shuttingDown, clientTerminated;

	private Context context;   
	private int count_no_response = 0;

	private static final int ERROR = 9999;
	
	private long timeElapsed;

	private boolean shutdownFlag;

	private String ip;
	private int port;
	private Socket socket;
	private PrintWriter out = null;
	private BufferedReader in = null; 


	public SXnetClientThread(Context context, String ip, int port) {
		if (DEBUG) Log.d(TAG,"SXnetClientThread constructor.");
		this.context=context;
		this.ip = ip;
		this.port = port;
		shuttingDown=false;
		clientTerminated=false;
		shutdownFlag=false;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isConnected() {
		if (socket != null) {
			return socket.isConnected();
		} else {
			return false;
		}
	}
	
	public void shutdown() {
		shutdownFlag=true;
	}

	public void run(){
		if (DEBUG) Log.d(TAG,"SXnetClientThread run.");
        shutdownFlag = false;
        clientTerminated = false;
        connect();
   

		while ((shutdownFlag == false) && (!Thread.currentThread().isInterrupted())) {   
			try {
				if ((in != null) && (in.ready())) {
					String in1 = in.readLine();
					if (DEBUG) Log.d(TAG,"msgFromServer: " + in1);
					handleMsgFromServer(in1.toUpperCase());
					count_no_response = 0; // reset timeout counter.
				}
			} catch (IOException e) {
				Log.e(TAG,"ERROR: reading from socket - "+e.getMessage());
			}

			// check send queue
			if (!sendQ.isEmpty()) {
	
				String comm="";
				try {
					comm = sendQ.take();
					if (comm.length()>0) immediateSend(comm);
				} catch (InterruptedException e) {
					Log.e(TAG,"could not take command from sendQ");
				}

			}

			// send a command at least every 10 secs
			if ((System.currentTimeMillis() - timeElapsed) > 10*1000) {
				if (socket.isConnected()) { 
					readChannel(127); //read power channel
					count_no_response++;
				}
				timeElapsed = System.currentTimeMillis();  // reset
				if (count_no_response > 10) {
				 	Log.e(TAG,"SXnetClientThread - connection lost? ");
					count_no_response = 0;
				}
			}
		}

        clientTerminated = true;
        if (socket != null) {
        	try {
				socket.close();
				Log.e(TAG,"SXnetClientThread - socket closed");
			} catch (IOException e) {
				Log.e(TAG,"SXnetClientThread - "+e.getMessage());
			}
        }
		if (DEBUG) Log.d(TAG,"SXnetClientThread stopped.");			
	}


	private void connect() {
		if (DEBUG) Log.d(TAG,"trying conn to - "+ip+":"+port);
		try {
			SocketAddress socketAddress = new InetSocketAddress(ip, port);

			// create a socket
			socket = new Socket();
			socket.connect(socketAddress, 2000);
			//socket.setSoTimeout(2000);  // set read timeout to 2000 msec   

			socket.setSoLinger(true, 0);  // force close, dont wait.

			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			connString = in.readLine();

			if (DEBUG) Log.d(TAG,"connected to: "+connString);

		} catch (Exception e) {
			Log.e(TAG,"SXnetClientThread.connect - Exception: "+e.getMessage());
		} 
 	}

	public void disconnectContext() {
		this.context = null;
		Log.d(TAG,"lost context, stopping thread");
		shutdown();
	}

	public void readChannel(int adr) {

		if (DEBUG) Log.d(TAG,"readChannel a="+adr+" shutd.="+shuttingDown+" clientTerm="+clientTerminated);
		if ( shutdownFlag || clientTerminated || (adr == INVALID_INT)) return;
		String command = "R "+adr;
		Boolean success = sendQ.offer(command);
		if ((success == false) && (DEBUG)) Log.d(TAG,"readChannel failed, queue full")	;
	}

	/** adds a command to the sendQueue "sendQ"
	 * 
	 * in case the demoFlag is true, an echo will be sent back to UI Thread via a message handler
	 * 
	 * @param adr
	 * @param data
	 */
	public void sendCommand(int adr, int data) {
		
		if (DEBUG) Log.d(TAG,"sendCommand a="+adr+" d="+data+" shutd.="+shuttingDown+" clientTerm="+clientTerminated);
		if (shutdownFlag || clientTerminated || (adr == INVALID_INT)) return;
		if (demoFlag) {
			Message m = Message.obtain();
			m.arg1 = adr;
			m.arg2 = data;
			handler.sendMessage(m);  // send SX data to UI Thread via Message
			return;
		}

		String command = "S "+adr+" "+data;
		Boolean success = sendQ.offer(command);
		if ((success == false) && (DEBUG)) Log.d(TAG,"sendCommand failed, queue full")	;
	}

	private void immediateSend(String command) {
		if (shutdownFlag || clientTerminated ) return;
		if (out == null) {
			if (DEBUG) Log.d(TAG,"out=null, could not send: "+command);
		} else {
			try {	
				out.println(command);
				out.flush();
				if (DEBUG) Log.d(TAG,"sent: "+command);
			}
			catch (Exception e) {
				if (DEBUG) Log.d(TAG,"could not send: "+command);
				Log.e(TAG, e.getClass().getName() + " "+ e.getMessage()); 
			}
		}
	}


	/**
	 * SX Net Protocol (all msg terminated with CR)
	 * 
	 * client sends                           |  SXnetServer Response  
	 * ---------------------------------------|-------------------
	 * R cc    = Read channel cc (0..127)     |  "X" cc dd
	 * B cc b  = SetBit Ch. cc Bit b (1..8)   |  "OK" (and later, when changed in CS: X cc dd )
	 * C cc b  = Clear Ch cc Bit b (1..8)     |  "OK" (and later, when changed in CS: X cc dd )
	 * S cc dd = set channel cc Data dd (<256)|  "OK" (and later, when changed in CS: X cc dd )
	 * DSDF 89sf  (i.e. garbage)              |  "ERROR" 
	 * 
	 * channel 127 bit 8 == Track Power
	 * 
	 * for a list of channels (which the client has set or read in the past) all changes are 
	 *                    transmitted back to the client
	 */
 
	private void handleMsgFromServer(String msg) {
		// check whether there is an application to send info to -
		// to avoid crash if application has stopped but thread is still running
		if (context == null) return; 
		
		String[] info = null;
		msg=msg.toUpperCase();

		int adr = ERROR;
		int data;

		if( (msg.length() != 0) && 
				(!msg.contains("ERROR")) && 
				(!msg.contains("OK"))
				)  { // message should contain valid data
			info = msg.split("\\s+");  // one or more whitespace

			if ( (info.length >= 3) && info[0].equals("X") ) {
				adr = getChannelFromString(info[1]);
				data = getDataFromString(info[2]);
				if (( adr != ERROR) && (data != ERROR) ) {
					Message m = Message.obtain();
					m.arg1 = adr;
					m.arg2 = data;
					handler.sendMessage(m);  // send SX data to UI Thread via Message
				}
			}
		}
	}

	private int getDataFromString(String s) {
		// converts String to integer between 0 and 255 (=SX Data)
		Integer data=ERROR;
		try {
			data = Integer.parseInt(s);
			if ( (data < 0 ) || (data >255)) {
				data = ERROR;
			} 
		} catch (Exception e) {
			data = ERROR;
		}
		return data;
	}

	int getChannelFromString(String s) {
		Integer channel=ERROR;
		try {
			channel = Integer.parseInt(s);
			if ( (channel >= 0 ) && (channel <= 127)) {
				return channel;
			} else {
				channel = ERROR;
			}
		} catch (Exception e) {

		}
		return channel;
	}



}
