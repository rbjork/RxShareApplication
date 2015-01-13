package com.bjorkcomputing.camerakitkat;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

//import android.widget.LinearLayout.*;
import android.widget.RelativeLayout.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public class CameraPreview extends Activity implements SensorEventListener {
	private static final String TAG = "CAMERA_PREVIEW";
	private Preview mPreview;
    private ImageView mTakePicture;
    private ImageView mToggleFlash;
    private TouchView mView;

    private boolean mAutoFocus = true;

    private boolean mFlashBoolean = false;

    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mGravity;
    private boolean mInitialized = false;
    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    
    private float gLastX = 1;
    private float gLastY = 1;
    private float gLastZ = 1;
    
    private Rect rec = new Rect();

    private int mScreenHeight;
    private int mScreenWidth;

    private boolean mInvalidate = false;
    
    private LevelControlsFragment levelControlFragment;

    private File mLocation = new File(Environment.
            getExternalStorageDirectory(),"test.jpg");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera_preview);
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      //  mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        // get the window width and height to display buttons
        // according to device screen size
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        mScreenWidth = displaymetrics.widthPixels;
        // I need to get the dimensions of this drawable to set margins
        // for the ImageView that is used to take pictures
        Drawable mButtonDrawable = this.getResources().
                getDrawable(R.drawable.camera);

        
        
        

        // setting where I will draw the ImageView for taking pictures

//        LayoutParams lp = new LayoutParams(mToggleFlash.getLayoutParams());
//
//        lp.setMargins((int)((double)mScreenWidth*.85),
//                (int)((double)mScreenHeight*.70) ,
//                (int)((double)mScreenWidth*.85)+mButtonDrawable.
//                        getMinimumWidth(),
//                (int)((double)mScreenHeight*.70)+mButtonDrawable.
//                        getMinimumHeight());
      //  mToggleFlash.setLayoutParams(lp);
        // rec is used for onInterceptTouchEvent. I pass this from the
        // highest to lowest layer so that when this area of the screen
        // is pressed, it ignores the TouchView events and passes it to
        // this activity so that the button can be pressed.
        rec.set((int)((double)mScreenWidth*.85),
                (int)((double)mScreenHeight*.10) ,
                (int)((double)mScreenWidth*.85)+mButtonDrawable.getMinimumWidth(),
                (int)((double)mScreenHeight*.70)+mButtonDrawable.getMinimumHeight());
       // mButtonDrawable = null;
        
       
        
        // get our Views from the XML layout
        
        
        mPreview = (Preview) findViewById(R.id.preview);
        mView = (TouchView) findViewById(R.id.left_top_view);
        mView.setRec(rec);
        edgeImage = (ImageView)findViewById(R.id.edgepicture);
        
        FragmentManager fragmentManager = getFragmentManager();
        levelControlFragment = new LevelControlsFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.fragment_container, levelControlFragment);
		fragmentTransaction.commit();
		
		
        
    }
    
    private ImageView edgeImage;
    
    protected OnClickListener shutterListener = new OnClickListener(){
    	 @Override
         public void onClick(View v) {
             //mPreview.takePicutureNow();
    		 DisplayMetrics displaymetrics = new DisplayMetrics();
    	     getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    	     mScreenHeight = displaymetrics.heightPixels;
    	     mScreenWidth = displaymetrics.widthPixels;
             Bitmap picture = mPreview.getPic(mScreenWidth/10,mScreenHeight/10,9*mScreenWidth/10,9*mScreenHeight/10);
             edgeImage.setImageAlpha(255);
             edgeImage.setImageBitmap(picture);
             Log.i(TAG,String.valueOf(picture.getWidth()));
    	 }
    };
    
    protected OnClickListener flashListener = new OnClickListener(){

        @Override
        public void onClick(View v) {
            if (mFlashBoolean){
                mPreview.setFlash(false);
            }
            else{
                mPreview.setFlash(true);
            }
            mFlashBoolean = !mFlashBoolean;
        }

    };
    
    protected OnClickListener previewBtnListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			edgeImage.setImageAlpha(0);
		}
	};
    
    // with this I get the ratio between screen size and pixels
    // of the image so I can capture only the rectangular area of the
    // image and save it.
    public Double[] getRatio(){
        Camera.Size s = mPreview.getCameraParameters().getPreviewSize();
        double heightRatio = (double)s.height/(double)mScreenHeight;
        double widthRatio = (double)s.width/(double)mScreenWidth;
        Double[] ratio = {heightRatio,widthRatio};
        return ratio;
    }
    
    public Parameters getCameraParameters(){
    	return mPreview.getCameraParameters();
    }
    
    public int getCameraPitch(){
    	return (int)(180*Math.atan(gLastZ/gLastX)/Math.PI);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){

        public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
            //Wait.oneSec();
            mAutoFocus = true;
        }
    };
    
    
    


	@Override
	public void onSensorChanged(SensorEvent event) {
		
		float x = event.values[0];
	    float y = event.values[1];
	    float z = event.values[2];
	    
		if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
			// Use to control level
			gLastX = x;
			gLastY = y;
			gLastZ = z;
			Log.i(TAG,"TYPE_GRAVITY: x="+String.valueOf(x)+" y="+String.valueOf(y)+" z="+String.valueOf(z));
			levelControlFragment.updateProtractor();
		}else{
		//	Log.i(TAG,"TYPE_ACCEL:  x="+String.valueOf(x)+" y="+String.valueOf(y)+" z="+String.valueOf(z));
			if (mInvalidate == true){
	            mView.invalidate();
	            mInvalidate = false;
	        }
	       
	        if (!mInitialized){
	            mLastX = x;
	            mLastY = y;
	            mLastZ = z;
	            mInitialized = true;
	        }
	        float deltaX  = Math.abs(mLastX - x);
	        float deltaY = Math.abs(mLastY - y);
	        float deltaZ = Math.abs(mLastZ - z);
	
	        if (deltaX > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing)
	            mAutoFocus = false;
	            mPreview.setCameraFocus(myAutoFocusCallback);
	        }
	        if (deltaY > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing)
	            mAutoFocus = false;
	            mPreview.setCameraFocus(myAutoFocusCallback);
	        }
	        if (deltaZ > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing) */
	            mAutoFocus = false;
	            mPreview.setCameraFocus(myAutoFocusCallback);
	        }
	
	        mLastX = x;
	        mLastY = y;
	        mLastZ = z;
		}
	}
	
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        //Log.i(TAG, "onPause()");
        mSensorManager.unregisterListener(this);
    }
	
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                   // mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        
       // mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_UI);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        //Log.i(TAG, "onResume()");
    }
}
