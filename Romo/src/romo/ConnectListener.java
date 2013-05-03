package romo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public interface ConnectListener {
	
	public static final int CONNECT_SUCCEED =  1;
	public static final int CONNECT_FAILED  = -1;
		
	public void onConnect(int flag, BluetoothDevice device, BluetoothSocket oSocket);
}
