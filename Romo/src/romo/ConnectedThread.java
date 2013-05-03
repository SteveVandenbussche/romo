package romo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions
 * @author SteveVdb
 */
public class ConnectedThread extends Thread{
	
	// Debugging
	private static final String TAG = "ConnectedThread";
	
	private final BluetoothSocket oSocket;
	private final InputStream oInStream;
	private final OutputStream oOutStream;
	private ConnectedListener oListener;
	private volatile boolean running;
	
	
	/**
	 * 
	 * @param socket  The BluetoothSocket on which the connection was made
	 */
	public ConnectedThread(BluetoothSocket socket){
		
		oSocket = socket;
		oListener = null;
		running = true;
		
		// Use temporary in- and output streams because oInStream and oOutStream are final
		InputStream tempIn = null;
		OutputStream tempOut = null;
		
		// Get BluetoothSocket in- and output streams
		try{
			tempIn = oSocket.getInputStream();
			tempOut = oSocket.getOutputStream();
		}catch(IOException e){
			Log.e(TAG, "unable to get in- and output streams", e);
		}
		
		oInStream = tempIn;
		oOutStream = tempOut;
	}
	
	/**
	 * Register a ConnectedListener
	 * @param listener The ConnectedListener to register
	 */
	public void register(ConnectedListener listener){
		oListener = listener;
	}
	
	/**
	 * Unregister the ConnectedListener
	 */
	public void unRegister(){
		oListener = null;
	}
		
	/**
	 * Read data from InputStream while connected
	 */
	@Override
	public void run() {
		
		Log.d(TAG, "begin ConnectedThread job");
		
		// Receive buffer
		byte[] buffer = new byte[256];
		// Amount of bytes read from InputStream
		int length = 0;
		
		while(running){
			
			try{
				
				// Read available bytes from InputStream
				length = oInStream.read(buffer);
						
				// Report the obtained bytes to the ConnectedListener
				if(oListener != null){
					oListener.onReceive(buffer, length);
				}
			}catch(IOException e){
				
				Log.e(TAG, "disconnected", e);
				
				// Stop job
				running = false;
				
				// Report this connection lose to the ConnectedListener 
				if(oListener != null){
					oListener.onDisconnect();
				}
			}
		}
	}
	
	/**
	 * Write to the connected OutputStream
	 * @param buffer
	 */
	public void write(byte[] buffer){
		
		try{
			oOutStream.write(buffer);
		}catch(IOException e){
			Log.e(TAG, "exception during write", e);
		}
	}
	
	/**
	 * Stop thread in a safe manner
	 */
	public void cancel(){
		
		try{			
			// Stop the runnable job
			running = false;
			join(1000);
			
			// Close socket
			oSocket.close();
			
		}catch(InterruptedException e1){
			Log.e(TAG, "terminating tread failed", e1);
		}catch (IOException e2) {
			Log.e(TAG, "cancel(), closing socket failed", e2);
		}	
	}
}
