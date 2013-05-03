package romo;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * This thread runs while attempting to make an outgoing conection with a device,
 * the connection either succeeds or fails
 * @author SteveVdb
 */
public class ConnectThread extends Thread {
	
	// Debugging
	private static final String TAG = "ConnectThread";
	
	//UUID Bluetooth service used by SDP lookup
	private static final String spp_uuid = "00001101-0000-1000-8000-00805f9b34fb";
	
	private final BluetoothSocket oSocket;
	private final BluetoothDevice oDevice;
	private ConnectListener oListener;
	
	/**
	 * Initialize ConnectThread
	 * @param device  The BluetoothDevice to connect
	 */
	public ConnectThread(BluetoothDevice device){
		
		oDevice = device;
		oListener = null;
		
		// Use temporary BluetoothSocket object because _socket is final
		BluetoothSocket temp = null;
		
		// Get a RFCOMM BluetoothSocket ready to start a secure 
		// outgoing connection to this remote device using SDP lookup of UUID
		try{
			
			temp = oDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(spp_uuid));
			
		}catch(IOException e){
			
			Log.e(TAG, "create RFCOMM socket failed", e);
		}
		
		oSocket = temp;
	}
	
	/**
	 * Register a ConnectListener
	 * @param listener The ConnectListener to register
	 */
	public void register(ConnectListener listener){
		oListener = listener;
	}
	
	/**
	 * Unregister the ConnectListener
	 */
	public void unRegister(){
		oListener = null;
	}
	
	/**
	 * Indicate that the connection attempt failed and notify the UI-Activity
	 */
	/*private void connectionFailed(){
		
		// Data to send to the receiver
		String data = "Unable to connect with device: " + oDevice.getName();
				
		// Obtain new message from the global message pool and send it to the specified target
		oHandler.obtainMessage(BluetoothService.MESSAGE_TOAST, data).sendToTarget();
	}*/
	
	/**
	 * Attempt to make a outgoing connection with a remote device
	 */
	@Override
	public void run() {
		
		Log.d(TAG, "begin ConnectThread job");
			
		try{
			
			// Attempt to connect to a remote device, this is a blocking call
			// and will only return on a succesful connection or an exception
			oSocket.connect();
			
		}catch(IOException e1){
			
			// Close socket
			try{
				oSocket.close();
			}catch(IOException e2){
				Log.e(TAG, "unable to close socket", e2);
			}
			
			// Report UI-activity that the connection attempt failed
			// connectionFailed();
			
			// Report to the ConnectListener that this connection attempt failed
			if(oListener != null){
				oListener.onConnect(ConnectListener.CONNECT_FAILED, oDevice, null);
			}
			
			return;
		}
		
		// Report this connection to the ConnectListener because its now save
		// to start the thread for transferring data 
		if(oListener != null){
			oListener.onConnect(ConnectListener.CONNECT_SUCCEED, oDevice, oSocket);
		}
	}
	
	/**
	 * Stop thread, this will cancel an in-progress connection
	 */
	public void cancel(){
		
		try{			
			// Close socket
			oSocket.close();
		}catch (IOException e) {
			Log.e(TAG, "cancel(), closing socket failed", e);
		}	
	}	
}
