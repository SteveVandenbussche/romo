package media;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MediaService implements SurfaceHolder.Callback, OnPreparedListener, 
		OnVideoSizeChangedListener, OnErrorListener, OnCompletionListener {
	
	// Debugging
	private static final String TAG = "MediaService";
	
	// Message types send from the MediaService Handler
	public static final int MESSAGE_SURFACE = 0;
	public static final int MESSAGE_PLAYBACK = 1;
	
	// Key names received from the MediaService Handler
	public static final String KEY_SURFACE_STATE = "surface_state";

	// Member fields
	private MediaPlayer oPlayer;
	private Handler oHandler;
	private SurfaceView oVideoView;
	private SurfaceHolder oVideoHolder;
	private boolean surfaceCreated;		
	private boolean videoReadyToPlay;	
	private boolean videoSizeKnown;

	public MediaService(SurfaceView view, Handler oHandler){
		
		oVideoView = view;
		oVideoHolder = oVideoView.getHolder();
		oVideoHolder.addCallback(this);
		
		surfaceCreated = false;
		videoReadyToPlay = false;
		videoSizeKnown = false;
	}
	
	/*public boolean getSurfaceCreated(){
		return surfaceCreated;
	}*/
	
	public void play(String source){
		
		reset();
		
		if(surfaceCreated){
			
			try{
				
				oPlayer = new MediaPlayer();
				oPlayer.setDataSource(source);
				oPlayer.setDisplay(oVideoHolder);
				
				oPlayer.setOnPreparedListener(this);
				oPlayer.setOnVideoSizeChangedListener(this);
				oPlayer.setOnCompletionListener(this);
				oPlayer.setOnErrorListener(this);
				
				oPlayer.prepareAsync();
			}catch(Exception e){
				
				Log.e(TAG, "play media failed", e);
			}
			
		}else{
			
			Message msg = oHandler.obtainMessage(MESSAGE_SURFACE);
			Bundle bundle = new Bundle();
			bundle.putBoolean(KEY_SURFACE_STATE, surfaceCreated);
			msg.setData(bundle);
			msg.sendToTarget();
		}
	}
	
	public void reset(){
		
		if(oPlayer != null){
			oPlayer.release();
			oPlayer = null;
		}
		
		videoReadyToPlay = false;
		videoSizeKnown = false;
	}
	
	private void startVideoPlayback(){
		
		// Start video playback if the video size is known and 
		// the media player is the prepared state
		if(videoReadyToPlay && videoSizeKnown){
			
			Log.d(TAG, "video playback started");
			
			oPlayer.start();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged called");	
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated called");
		
		surfaceCreated = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed called");
		
		surfaceCreated = false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
	
		Log.d(TAG, "onCompletion called");
		
		oHandler.obtainMessage(MESSAGE_PLAYBACK).sendToTarget();
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		
		Log.d(TAG, "onPrepared called");
		
		videoReadyToPlay = true;
		startVideoPlayback();	
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		
		Log.d(TAG, "onVideoSizeChanged called");
		
		if((width == 0) || (height == 0)){
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
			return;
		}
		
		// Force streching video, it's better to use the real width and height
		oVideoHolder.setFixedSize(720, 1280);				
		//videoViewHolder.setFixedSize(width, height);
		videoSizeKnown = true;
		startVideoPlayback();
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		
		Log.e(TAG, "onError called, type: " + what + ", extra code: " + extra);
		return true;
	}
}
