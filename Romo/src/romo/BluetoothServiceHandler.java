package romo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BluetoothServiceHandler extends Handler {
	
	// Debugging
	private static final String TAG = "BluetoothServiceHandler";
	
	// Application context
	private final Context oContext;
	
	public BluetoothServiceHandler(Context context){
		oContext = context;
	}
	
	@Override
	public void handleMessage(Message msg) {
		
		switch (msg.what) {
		
		case BluetoothService.MESSAGE_DATA:
			
			//byte[] readBuf = (byte[])msg.obj;
			Log.i("HANDLER", "!data received " + msg.arg1);
			
			break;
		
		case BluetoothService.MESSAGE_STATE_CHANGED:
			
			Bundle bundle = msg.getData();
			
			String deviceName = bundle.getString(BluetoothService.KEY_DEVICE_NAME);
			int currentState = bundle.getInt(BluetoothService.KEY_CURRENT_STATE);
			int nextState = bundle.getInt(BluetoothService.KEY_NEXT_STATE);
			
			
			if((currentState == BluetoothService.STATE_NONE) && (nextState == BluetoothService.STATE_CONNECTING)){
				
				Toast.makeText(oContext, "Connection with device " + deviceName + ".", Toast.LENGTH_LONG).show();
				
			}else if((currentState == BluetoothService.STATE_CONNECTING) && (nextState == BluetoothService.STATE_NONE)) {
				
				Toast.makeText(oContext, "Connection with device " + deviceName + " failed.", Toast.LENGTH_LONG).show();
				
			}else if((currentState == BluetoothService.STATE_CONNECTING) && (nextState == BluetoothService.STATE_CONNECTED)){
				
				Toast.makeText(oContext, "Connection established with: " + deviceName, Toast.LENGTH_LONG).show();
				
			}else if((currentState == BluetoothService.STATE_CONNECTED) && (nextState == BluetoothService.STATE_NONE)){
			
				Toast.makeText(oContext, "Connection with " + deviceName + " closed", Toast.LENGTH_LONG).show();
			}else if(currentState != nextState){
				Log.w(TAG, "Invalid transition: " + currentState + "-" + nextState);
			}
			
		default:
			break;
		}
		
	}
}
