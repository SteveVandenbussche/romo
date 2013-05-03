package romo;

import media.MediaActivity;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.romo.R;

import discovery.DiscoverActivity;

public class RomoActivity extends Activity {
	
	// Debugging
	public static final String TAG = "RomoActivity";

	// Intent request codes
	private static final int REQUEST_ENABLE_BT   = 0;
	private static final int REQUEST_DISCOVER_BT = 1;
	private static final int REQUEST_MEDIAPLAYER = 2;
	
	// Local Bluetooth adapter
	private BluetoothAdapter oAdapter;
	
	// The BluetoothService
	private BluetoothService oBluetootService;
	
	// Detects various gestures and touch events 
	private GestureDetectorCompat oDetector;
	
	// Client for camera service, which manages the actual camera hardware
	private Camera oCamera;
	
	// SurfaceView that can display the live image data coming from the camera
	private CameraPreview oPreview;
			
	/**
	 * Activity initialisation
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate called");
		
		// Setup window
		setContentView(R.layout.activity_romo);
		
		// Get local Bluetooth adapter
		oAdapter = BluetoothAdapter.getDefaultAdapter();
				
		// If the adpater is null, then Bleutooth is not supported
		if(oAdapter == null){
			Toast.makeText(this, R.string.bluetooth_availability, Toast.LENGTH_LONG).show();
			finish();
		}
		
		oBluetootService = new BluetoothService(this, oHandler);
		oDetector = new GestureDetectorCompat(this, GestureListener);
		
		// Get acces to front camera
		oCamera = getFrontCamera();
		
		// Setup camera preview and face detection listener
		if(oCamera != null){
						
			oPreview = new CameraPreview(this, oCamera);
			FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
			preview.addView(oPreview);
			oCamera.setFaceDetectionListener(oFaceDetectionListener);
			oCamera.stopPreview();		
			
		}else{
			Toast.makeText(this, R.string.camera_availability, Toast.LENGTH_LONG).show();
		}
		
		getActionBar().hide();
	}
	
	
	/**
	 * Start intent to enable Bluetooth if it's not on
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
		Log.d(TAG, "onStart called");
		
		if(!oAdapter.isEnabled()){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		oBluetootService.stop();
	}
	
	
	/**
	 * Receive the result from a previous launched activity
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.d(TAG, "onActivityResult called " + resultCode);
		
		switch (requestCode) {
		
		// When the Bluetooth enable activity returns
		case REQUEST_ENABLE_BT:
			
			// Bluetooth is enabled now
			if(resultCode == Activity.RESULT_OK){
				Log.d(TAG, "Bluetooth enabled");
				
			// or something went wrong
			}else{
				Toast.makeText(this, R.string.bluetooth_not_enabled, Toast.LENGTH_LONG).show();
				finish();
			}
			break;

		// When the Discover activity returns
		case REQUEST_DISCOVER_BT:
			
			// Attempt to connect to the device
			if(resultCode == Activity.RESULT_OK){
				
				String address = data.getStringExtra(DiscoverActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = oAdapter.getRemoteDevice(address);
				oBluetootService.connect(device);
			}
			
			break;
		
		// When the Media activity returns
		case REQUEST_MEDIAPLAYER:
			
			if(resultCode == Activity.RESULT_OK){
				
				oCamera.stopFaceDetection();
				oCamera.stopPreview();
			}
			
		default:
			break;
		}		
	}
	
	/**
	 * Get front camera
	 * @return
	 */
	public Camera getFrontCamera(){
		
		Log.d(TAG, "acces front camera");
		
		Camera c = null;
		CameraInfo cInfo = new CameraInfo();
		
		// Get the rotation of the screen from its "natural" orientation
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		
		// Convert rotation ID to actual degrees
		switch (rotation) {
					
		case Surface.ROTATION_0: degrees = 0; break;
		case Surface.ROTATION_90: degrees = 90; break;
		case Surface.ROTATION_180: degrees = 180; break;
		case Surface.ROTATION_270: degrees = 270; break;
		}
		
		
		// Try to access the front camera
		try{
			
			int numCams = Camera.getNumberOfCameras();
			
			for(int i=0; i<numCams; i++){
				
				Camera.getCameraInfo(i, cInfo);
				
				if(cInfo.facing == CameraInfo.CAMERA_FACING_FRONT){
					c = Camera.open(i);
					
					// Set camera orientation identical to the display orientation
				    int result = (cInfo.orientation + degrees) % 360;
				    result = (360 - result) % 360;  
				    c.setDisplayOrientation(result);
					
					break;
				}
			}
			
		}catch(Exception e){
			
			Log.e(TAG, "acces camera failed",e);
		}
		
		return c;
	}
		
	/**
	 * Inflate and configure menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.romo, menu);
		
		// Configure switch 
		Switch serviceSwitch = (Switch)menu.findItem(R.id.action_service).getActionView();
		
		serviceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				Log.d(TAG, "onCheckedChanged called " + isChecked);
				
				if(isChecked){
					
					// Attemp to start the BluetoothService by doing a Bluetooth discovery
					Intent discoverIntent = new Intent(getApplicationContext(), DiscoverActivity.class);
					startActivityForResult(discoverIntent, REQUEST_DISCOVER_BT);
					
				}else{
					// Stop BluetoothService
					oBluetootService.stop();
				}
			}
		});
		
		return true;
	}

	
	/**
	 * Analayse the given MotionEvent and triggers the appropriate callbacks
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		oDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	
    /**
     * The gesture listener, used for handling simple gestures such as double touches, scrolls, and flings.
     */
	private final SimpleOnGestureListener GestureListener = new SimpleOnGestureListener(){
		
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown called");
			return true;
		};
		
		public boolean onDoubleTap(MotionEvent e) {
			
			Log.d(TAG, "onDoubleTap called");
			//ActionBar actionBar = getActionBar();
			
			/*if(actionBar.isShowing()){
				actionBar.hide();
			}else{
				actionBar.show();
			}*/
			
			
			oCamera.startPreview();
			oCamera.startFaceDetection();

			return true;
		};
	};

	
	/**
	 * Handle messages from the BluetoothService
	 */
	private Handler oHandler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			
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
					
					Toast.makeText(getApplicationContext(), "Connection with device " + deviceName + ".", Toast.LENGTH_SHORT).show();
					
				}else if((currentState == BluetoothService.STATE_CONNECTING) && (nextState == BluetoothService.STATE_NONE)) {
					
					Toast.makeText(getApplicationContext(), "Connection with device " + deviceName + " failed.", Toast.LENGTH_LONG).show();
					
					// Uncheck service switch, this will also stop the BluetoothService
					CompoundButton serviceSwitch = (CompoundButton)findViewById(R.id.action_service);
					serviceSwitch.setChecked(false);
					
				}else if((currentState == BluetoothService.STATE_CONNECTING) && (nextState == BluetoothService.STATE_CONNECTED)){
					
					Toast.makeText(getApplicationContext(), "Connection established with: " + deviceName, Toast.LENGTH_SHORT).show();
					
				}else if((currentState == BluetoothService.STATE_CONNECTED) && (nextState == BluetoothService.STATE_NONE)){
				
					Toast.makeText(getApplicationContext(), "Connection with " + deviceName + " closed", Toast.LENGTH_LONG).show();
					
					// Uncheck service switch, this will also stop the BluetoothService
					CompoundButton serviceSwitch = (CompoundButton)findViewById(R.id.action_service);
					serviceSwitch.setChecked(false);
						
				}else if(currentState != nextState){
					
					// This state will normally never occure
					Log.w(TAG, "Invalid transition: " + currentState + "-" + nextState);
				}
				
			default:
				break;
			}
		};
	};
	
	private FaceDetectionListener oFaceDetectionListener = new FaceDetectionListener() {
		
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			
			if(faces.length > 0){
				
				// Stop face detection
				camera.stopFaceDetection();
				
				Intent intent = new Intent(getApplicationContext(), MediaActivity.class);
				
				if(faces.length == 1){
					intent.putExtra(MediaActivity.MEDIA, "sdcard/video/romo/Romo_Knipoog_High.mp4");
				}else{
					intent.putExtra(MediaActivity.MEDIA, "sdcard/video/romo/Romo_Vrolijk.mp4");
				}
				
				startActivityForResult(intent, REQUEST_MEDIAPLAYER);
			}
		}
	};
}
