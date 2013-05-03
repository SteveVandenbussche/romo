package romo;

public interface ConnectedListener {
	
	public void onDisconnect();
	public void onReceive(byte[] buffer, int length);

}
