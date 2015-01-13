package com.bjorkcomputing.camerakitkat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter.Blur;
//import android.graphics.Camera;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;
import org.opencv.android.Utils;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String TAG = "PREVIEW";
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private byte[] mBuffer;
    
	public Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public void init(){
		mHolder = getHolder();
	    mHolder.addCallback(this);
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	   
	}
	
	public Bitmap getPic(int x, int y, int width, int height){
		System.gc();
        Bitmap b = null;
        Camera.Size s = mParameters.getPreviewSize();

        YuvImage yuvimage = new YuvImage(mBuffer, ImageFormat.NV21, s.width, s.height, null);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(x, y, width, height), 100, outStream); // make JPG
        b = BitmapFactory.decodeByteArray(outStream.toByteArray(), 0, outStream.size());
        if (b != null) {
            //Log.i(TAG, "getPic() WxH:" + b.getWidth() + "x" + b.getHeight());
        } else {
            //Log.i(TAG, "getPic(): Bitmap is null..");
        }
        yuvimage = null;
        outStream = null;
        System.gc();
        
        Mat ImageMat = new Mat( b.getHeight(), b.getWidth(), CvType.CV_8U, new Scalar(1));
        Utils.bitmapToMat(b, ImageMat);
        Bitmap be = createEdgeDetectionImage(ImageMat);
        return be;
	}
	
	private Bitmap createEdgeDetectionImage(Mat src){
		Bitmap b = null;
		Mat dst = new Mat();
		Size sz = new Size(3.0,3.0);
		
		//Blur bl = new Blur(src,src,sz);
		
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
		Mat matEdges = new Mat();
		Imgproc.Canny( src, matEdges , 10, 100, 3 ,true);
		Bitmap resultBitmap = Bitmap.createBitmap(matEdges.cols(),  matEdges.rows(),Bitmap.Config.ARGB_8888);
	
		Utils.matToBitmap(matEdges, resultBitmap);
		return resultBitmap;
	}
	
	private void updateBufferSize(){
		mBuffer = null;
        System.gc();
        // prepare a buffer for copying preview data to
        int h = mCamera.getParameters().getPreviewSize().height;
        int w = mCamera.getParameters().getPreviewSize().width;
        int bitsPerPixel =
                ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat());
        mBuffer = new byte[w * h * bitsPerPixel / 8];
	}
	
	public void setCameraFocus(Camera.AutoFocusCallback autoFocus){
        if (mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_AUTO) ||
                mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_MACRO)){
            mCamera.autoFocus(autoFocus);
        }
    }
	
	

    public void setFlash(boolean flash){
        Toast.makeText(Preview.this.getContext(), "Flash is: "+mParameters.getFlashMode(), Toast.LENGTH_LONG).show();
        if (flash){
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(mParameters);
        }
        else{
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
        }
    }
    
//    public void takePicutureNow(){
//    	Log.i(TAG,"takePicutureNow");
//    	mCamera.takePicture(shutterCB, picCB, picCB);
//    	
//    }
//	
//    private ShutterCallback shutterCB = new ShutterCallback() {
//		@Override
//		public void onShutter() {
//			// TODO Auto-generated method stub
//			Log.i(TAG,"onShutter");
//		}
//	};
//	
//	private PictureCallback picCB = new PictureCallback() {
//		@Override
//		public void onPictureTaken(byte[] data, Camera camera) {
//			// TODO Auto-generated method stub
//			Log.i(TAG,"onPictureTaken");
//			
//		}
//	};
	
	
	

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
            mCamera = Camera.open(); // WARNING: without permission in Manifest.xml, crashes
        }
        catch (RuntimeException exception) {
            //Log.i(TAG, "Exception on Camera.open(): " + exception.toString());
            Toast.makeText(getContext(), "Camera broken, quitting :(", Toast.LENGTH_LONG).show();
            // TODO: exit program
        }

        try {
            mCamera.setPreviewDisplay(holder);
            //updateBufferSize();
            updateBufferSize();
            mCamera.addCallbackBuffer(mBuffer); // where we'll store the image data
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                public synchronized void onPreviewFrame(byte[] data, Camera c) {

                    if (mCamera != null) { // there was a race condition when onStop() was called..
                        mCamera.addCallbackBuffer(mBuffer); // it was consumed by the call, add it back
                    }
                }
            });
        } catch (Exception exception) {
            //Log.e(TAG, "Exception trying to set preview");
            if (mCamera != null){
                mCamera.release();
                mCamera = null;
            }
            // TODO: add more exception handling logic here
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
            mParameters = mCamera.getParameters();
            mParameters.set("orientation","landscape");
           // mParameters.set("rotation", 90);
            for (Integer i : mParameters.getSupportedPreviewFormats()) {
                //Log.i(TAG, "supported preview format: " + i);
            }
            List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
            for (Camera.Size size : sizes) {
                //Log.i(TAG, "supported preview size: " + size.width + "x" + size.height);
            }
            mCamera.setParameters(mParameters); // apply the changes
        } catch (Exception e) {
            // older phone - doesn't support these calls
        }
        //updateBufferSize(); // then use them to calculate
		updateBufferSize();
        Camera.Size p = mCamera.getParameters().getPreviewSize();
        //Log.i(TAG, "Preview: checking it was set: " + p.width + "x" + p.height); // DEBUG
        
        mCamera.startPreview();
	}
	
	public Camera.Parameters getCameraParameters(){
		if(mCamera != null){
			return mCamera.getParameters(); 
		}else{
			return null;
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
	    mCamera.release();
	    mCamera = null;
	}

}
