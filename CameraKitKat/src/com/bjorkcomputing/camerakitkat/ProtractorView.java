package com.bjorkcomputing.camerakitkat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.graphics.PorterDuff;

import android.graphics.PorterDuffXfermode;

public class ProtractorView extends View {
	
	private final String TAG = "ProtractorView";
	
	private CameraPreview hostActivity;
	private DisplayMetrics mDisplay;
	private double horizFOV; // Horizontal field of view
	private double vertFOV; // Vertical field of view
	private int mDisplayWidth;
	private int mDisplayHeight;
	
	public ProtractorView(Context context) {
		super(context);
	}
	
	public ProtractorView(Context context,double thetaH,double thetaV) {
		super(context);
		// TODO Auto-generated constructor stub
		
		horizFOV = thetaH;
		vertFOV = thetaV;
		
		hostActivity = (CameraPreview)context;
		mDisplay = new DisplayMetrics();
		hostActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplay);
		
		mDisplayWidth = mDisplay.widthPixels;
		mDisplayHeight = mDisplay.heightPixels;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		Parameters p = hostActivity.getCameraParameters();
		
		if(p == null)return;
		
//		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//		Paint transparentPaint = new Paint();
//		transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
//		transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//		transparentPaint.setAntiAlias(true);
		
		int cameraPitchAngle = hostActivity.getCameraPitch();
		
		Log.i(TAG,"pitch="+cameraPitchAngle);
		
		horizFOV = p.getHorizontalViewAngle();
		vertFOV = p.getVerticalViewAngle();
		
		Paint linepaint = new Paint();
		linepaint.setARGB(255, 255, 128, 0);
		linepaint.setStrokeWidth(5);
		
		Paint textpaint = new Paint();
		textpaint.setARGB(255, 255, 255, 0);
		textpaint.setTextSize(30);
		
		double dxda = mDisplayWidth/horizFOV; // pixels per degree angle
		double dyda = mDisplayHeight/vertFOV; // pixels per degree angle
		
		double focal_length = (mDisplayWidth/2)/Math.tan(Math.PI*horizFOV/360);
		
		int startAngle = -30;
		int endAngle = 30;
		
		int angle = startAngle;
		double stretch = 0.0;
		int da = 5; // tics a 5 degree increments
		double dy = 0;
		double dx = 0;
		double y = 0;
		double x = 0;
		
		
		
		while(angle < endAngle){
			//dy = da * focal_length/Math.pow(Math.cos(angle*Math.PI/180),2);
			y = -focal_length * Math.tan(angle*Math.PI/180) + mDisplayHeight/2;
			int yi = (int)y;
			canvas.drawLine(165, yi, 185, yi, linepaint);
			int angleHoriz = angle - cameraPitchAngle;
			canvas.drawText(String.valueOf(angleHoriz), 200, yi-10, textpaint);
			canvas.drawLine(mDisplayWidth-165, yi, mDisplayWidth-185, yi, linepaint);
			canvas.drawText(String.valueOf(angleHoriz),  mDisplayWidth-200 , yi-10, textpaint);
			angle += 5;
		}
		
		angle = startAngle;
		
		while(angle < endAngle){
			//stretch = 1/Math.pow(Math.cos(angle*Math.PI/180),2);
			//int x = (int)(stretch*Math.round(angle*dxda)) + mDisplayWidth/2;
			//dx = da * focal_length/Math.pow(Math.cos(angle*Math.PI/180),2);
			x = focal_length * Math.tan(angle*Math.PI/180) + mDisplayWidth/2;;
			int xi = (int)x;
			int angleHoriz = angle;// + cameraPitchAngle;
			canvas.drawLine(xi, 10, xi, 20, linepaint);
			canvas.drawText(String.valueOf(angleHoriz), xi-10, 50, textpaint);
			canvas.drawLine(xi, mDisplayHeight-10, xi, mDisplayHeight-20, linepaint);
			canvas.drawText(String.valueOf(angleHoriz), xi-10, mDisplayHeight-50, textpaint);
			angle += 5;
		}
		
	}
	
	

}
