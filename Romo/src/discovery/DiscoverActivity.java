package discovery;


import java.util.Set;

import ui.ViewHolder;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.example.romo.R;


public class DiscoverActivity extends Activity {
	
	// Debugging
	public static final String TAG = "DiscoverRomoActivity";
	
	// Return Intent extra
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";
	
	// Member fields
	private BluetoothAdapter radio;
	private CustomArrayAdapter pdArrayAdapter;
	private CustomArrayAdapter ndArrayAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setup window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_discover);
				
		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);
		
		// Discover button OnClickListener
		Button btn = (Button)findViewById(R.id.btn_discover);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Log.d(TAG, "start bluetooth discovery");
						
				// Indicate scanning in title
				setProgressBarIndeterminateVisibility(true);
				
				// If we're already discovering, then stop it
				if(radio.isDiscovering()){
					radio.cancelDiscovery();
				}
				
				// Request bluetooth discovery
				radio.startDiscovery();
				
				// Disable this button 
				v.setEnabled(false);
			}
		});
		
		
		// Initialize BtDeviceAdapters one for paired devices and one for new devices 
		pdArrayAdapter = new CustomArrayAdapter(this, R.layout.listitem);
		ndArrayAdapter = new CustomArrayAdapter(this, R.layout.listitem);
		
		// Find and setup ListView for paired devices
        ListView pdListview = (ListView)findViewById(R.id.lv_paired_devices);
        pdListview.setAdapter(pdArrayAdapter);
        pdListview.setOnItemClickListener(itemClickListener);
        
        
     	// Find and setup Listview for new devices
        ListView ndListView = (ListView)findViewById(R.id.lv_new_devices);
        ndListView.setAdapter(ndArrayAdapter);
        ndListView.setOnItemClickListener(itemClickListener);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        
        // Register for broadcast when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        
        // Get the local bluetooth adapter
        radio = BluetoothAdapter.getDefaultAdapter();
        
        // Get a set of currently paired devices
        Set<BluetoothDevice> pd = radio.getBondedDevices();
                
        // If there are paired devices, add each one to the pdArrayAdapter
        if(pd.size() > 0){
        	
        	// Show title and listview for paired devices
        	findViewById(R.id.root_paired_devices).setVisibility(View.VISIBLE);
        	
        	for(BluetoothDevice device : pd){
        		pdArrayAdapter.add(new BtListItem(device.getName(), device.getAddress(), icon(device.getBluetoothClass())));
        	}	
        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// Make sure we're not doing discovery anymore
		if(radio != null){
			radio.cancelDiscovery();
		}
		
		// Unregister broadcast listeners
		unregisterReceiver(receiver);
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
			
			Log.d(TAG, "onItemClick called");
			
			// Cancel bluetooth discovery
			radio.cancelDiscovery();
			
			// Get tag of the clicked view
			ViewHolder holder = (ViewHolder)view.getTag();
			
			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, holder.getListItem().getSubtitle());
			
			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};
	
	private final BroadcastReceiver receiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.d(TAG, "onReceive called");
			
			String action = intent.getAction();
			
			// When a new devices is discovered...
			if(action.equals(BluetoothDevice.ACTION_FOUND)){
				
				// Get the BluetoothDevice object from the intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				// If it's already paired, skip it, because it's been listed already
				if(device.getBondState() != BluetoothDevice.BOND_BONDED){
					ndArrayAdapter.add(new BtListItem(device.getName(), device.getAddress(), icon(device.getBluetoothClass())));
				}
				
			// When discovery has finished...
			}else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
			
				// Indicate stop scanning in title
				setProgressBarIndeterminateVisibility(false);
				
				// Enable discovery button
				findViewById(R.id.btn_discover).setEnabled(false);
			}
		}
	};
	
	private int icon(BluetoothClass bluetoothClass){
		
		int major = bluetoothClass.getMajorDeviceClass();
		
		if(major == BluetoothClass.Device.Major.COMPUTER){
			return R.drawable.hwd_computer;
			
		}else if(major == BluetoothClass.Device.Major.PHONE){
			return R.drawable.hwd_phone;
			
		}else if(major == BluetoothClass.Device.Major.TOY){
			return R.drawable.hwd_toy;
			
		}else{
			return 0;
		}
	}
}
