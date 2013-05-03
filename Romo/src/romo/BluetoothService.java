package romo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothService implements ConnectListener, ConnectedListener {
	
	// Debugging
	private static final String TAG = "BluetoothService";
	
	// Message types send from the BluetoothService Handler
	public static final int MESSAGE_STATE_CHANGED = 0;			// Post connection state changed (synchronize UI-Activity)
	public static final int MESSAGE_DATA  = 1;					// Post data received
	
	// Key names received from the BluetoothChatService Handler
	public static final String KEY_DEVICE_NAME = "device_name";
	public static final String KEY_CURRENT_STATE = "current_state";
	public static final String KEY_NEXT_STATE = "next_state";
	
	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;						// we're doing nothing
	public static final int STATE_CONNECTING = 1;					// now initiating an outgoing connection
	public static final int STATE_CONNECTED = 2;					// now connected to remote device
	
	// Member fields
	private final BluetoothAdapter oAdapter;						
	private final Handler oHandler;
	private ConnectThread oConnectThread;
	private ConnectedThread oConnectedThread;
	private String deviceName;
	private int iState;
	
	/**
	 * Initialize BluetoothService
	 * @param context
	 * @param handler
	 */
	public BluetoothService(Context context, Handler handler){
		
		oAdapter = BluetoothAdapter.getDefaultAdapter();
		oHandler = handler;
		
		oConnectThread = null;
		oConnectedThread = null;
		
		deviceName = "n.a.v.";
		iState = STATE_NONE;
	}
	
	/**
	 * Return the current state of the BluetoothService
	 */
	public synchronized int getState(){
		return iState;
	}
	
	/**
	 * Set the current state of the BluetoothService and report
	 * this state change to the UI-Activity
	 * @param state An integer defining the the current connection state
	 */
	private synchronized void setState(int nextState){
		
		Log.d(TAG, "next state: " + nextState);
		
		// Obtain new message from the global message pool
		Message msg = oHandler.obtainMessage(MESSAGE_STATE_CHANGED);
		
		// Set bundle of data
		Bundle bundle = new Bundle();
		bundle.putString(KEY_DEVICE_NAME, deviceName);
		bundle.putInt(KEY_CURRENT_STATE, iState);
		bundle.putInt(KEY_NEXT_STATE, nextState);
		msg.setData(bundle);
		
		// It's know save to change the state
		iState = nextState;
		
		// Send message the specified target
		msg.sendToTarget();
	}
	
	/**
	 * Start the ConnectThread to initiate a connection to a remote device
	 * @param device The Bluetooth device to connect
	 */
	public synchronized void connect(BluetoothDevice device){
		
		Log.d(TAG, "connect() called");
		
		// Clean start stop all threads currently running
		stop();
		
		// Always cancel discovery because it will slow down a connection
		oAdapter.cancelDiscovery();
		
		// Save device name to connect
		deviceName = device.getName();
		
		// Start the thread to connect with the given device
		oConnectThread = new ConnectThread(device);
		oConnectThread.register(this);
		oConnectThread.start();
		
		// Transit from STATE_NONE to STATE_CONNECTING
		setState(STATE_CONNECTING);
	}
	
	private synchronized void connected(BluetoothSocket socket){
		
		Log.d(TAG, "connected() called");
		
			
		// Start the thread to manage the connection and peform transmissions
		oConnectedThread = new ConnectedThread(socket);
		oConnectedThread.register(this);
		oConnectedThread.start();
		
		
		// Transit from STATE_CONNECTING to STATE_CONNECTED
		setState(STATE_CONNECTED);
	}
	
	/**
	 * Stop all running threads
	 */
	public synchronized void stop(){
		
		Log.d(TAG, "stop threads");
		
		if(oConnectThread != null){
			oConnectThread.cancel();
			oConnectThread = null;
		}
		
		if(oConnectedThread != null){
			oConnectedThread.cancel();
			oConnectedThread = null;
		}
		
		setState(STATE_NONE);
	}
	

	@Override
	public void onConnect(int flag, BluetoothDevice device, BluetoothSocket socket) {
		
		Log.d(TAG, "onConnect() called, " + flag);
		
		// Reset the ConnectThread because it's done
		oConnectedThread = null;
		
		// If connection succeed then start ConnectedThread
		if(flag == ConnectListener.CONNECT_SUCCEED){
			connected(socket);
		}else{
			// Connection failed : transit from STATE_CONNECTING to STATE_NONE
			setState(STATE_NONE);
		}
	}

	
	@Override
	public void onReceive(byte[] buffer, int length) {
		
		Log.d(TAG, "onReceive() called, " + length);
	
		// Send obtained bytes to the UI-Activity
		oHandler.obtainMessage(MESSAGE_DATA, length, 0, buffer).sendToTarget();
	}
	
	@Override
	public void onDisconnect() {
		
		Log.d(TAG, "onDisconnect() called");

		// Reset ConnectedThread because it's done
		oConnectedThread = null;
		
		// Transit from STATE_CONNECTED to STATE_NONE
		setState(STATE_NONE);
	}
}
