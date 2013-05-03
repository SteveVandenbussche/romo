package media;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.example.romo.R;

public class MediaActivity extends Activity implements 
		SurfaceHolder.Callback, OnPreparedListener, OnVideoSizeChangedListener, 
		OnErrorListener, OnCompletionListener{

	// Debug only
	private static final String TAG = "MainActivity";
	
	// Intent bundle keys
	public static final String MEDIA = "media";
	
	// Member data
	private MediaPlayer mediaPlayer;
	private SurfaceView videoView;
	private SurfaceHolder videoViewHolder;
	private Bundle extras;
	
	private boolean videoSizeKnow = false;
	private boolean videoReadyToPlay = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setup window
		setContentView(R.layout.activity_media);
		getActionBar().hide();
		
		// Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
		
        // Prepare SurfaceView
		videoView = (SurfaceView)findViewById(R.id.videoView);
		videoViewHolder = videoView.getHolder();
		videoViewHolder.addCallback(this);
		
		// Get data from intent that started this activity
		extras = getIntent().getExtras();
	}
	
	public void playVideo(String path){
		
		try{
			mediaPlayer = new MediaPlayer();			// create new player
			mediaPlayer.setDataSource(path);			// Set data source to play
			mediaPlayer.setDisplay(videoViewHolder);	// Set SurfaceHolder to use for displaying the video 
			//mediaPlayer.setLooping(true);
			
			// Set listeners
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnVideoSizeChangedListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnErrorListener(this);
			
			mediaPlayer.prepareAsync();					// Prepare player for playback (asynchronously)
			
		}catch(Exception e){
			Log.e(TAG, e.getMessage());
		}
	}
		
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		Log.d(TAG, "onStop called");
		
		// Reset video playback conditions
		cleanUp();
		
		// Release media player
		releaseMediaPlayer();
		
	}
	
	private void startVideoPlayback(){
		
		// Start video playback if the video size is know and 
		// the media player is the prepared state
		if(videoReadyToPlay && videoSizeKnow){
			
			Log.d(TAG, "video playback started");
			
			mediaPlayer.start();
		}
	}
	
	private void releaseMediaPlayer(){
		if(mediaPlayer != null){
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	private void cleanUp()
	{
		videoSizeKnow = false;
		videoReadyToPlay = false;	
	}
	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
		Log.d(TAG, "surfaceChanged called");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		Log.d(TAG, "surfaceCreated called");
		playVideo(extras.getString(MEDIA));
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
		Log.d(TAG, "surfaceDestroyed called");	
	}

	
	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		
		Log.d(TAG, "onVideoSizeChanged called");
		
		if((width == 0) || (height == 0)){
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
			return;
		}
		
		videoSizeKnow = true;
		videoViewHolder.setFixedSize(720, 1280);
		//videoViewHolder.setFixedSize(width, height);
		
		//Toast.makeText(this, "width " + width + ", height " + height , Toast.LENGTH_LONG).show();
		startVideoPlayback();
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		
		Log.d(TAG, "onPrepared called");
		
		videoReadyToPlay = true;
		startVideoPlayback();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion called");
		
		// Set result and finish this Activity
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		
		Log.e(TAG, "MediaPlayer error, type: " + what + ", extra code: " + extra);
		
		return true;
	}
}

//The SurfaceHolder providing access and control over the SurfaceView's underlying surface.
