package com.bjorkcomputing.camerakitkat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class TouchView extends View {
	
	private Drawable mLeftTopIcon;
    private Drawable mRightTopIcon;
    private Drawable mLeftBottomIcon;
    private Drawable mRightBottomIcon;

    private boolean mLeftTopBool = false;
    private boolean mRightTopBool = false;
    private boolean mLeftBottomBool = false;
    private boolean mRightBottomBool = false;

    // Starting positions of the bounding box

    private float mLeftTopPosX = 30;
    private float mLeftTopPosY = 120;

    private float mRightTopPosX = 150;
    private float mRightTopPosY = 120;

    private float mLeftBottomPosX = 30;
    private float mLeftBottomPosY = 200;

    private float mRightBottomPosX = 150;
    private float mRightBottomPosY = 200;
    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;

    private Paint topLine;
    private Paint bottomLine;
    private Paint leftLine;
    private Paint rightLine;

    private Rect buttonRec;

    private int mCenter;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    // you can ignore this for this code
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    
    
    public TouchView(Context context) {
        super(context);
    }

    public TouchView(Context context, AttributeSet attrs){
        super (context,attrs);
        init(context);
    }

    public TouchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

        // I need to create lines for the bouding box to connect

        topLine = new Paint();
        bottomLine = new Paint();
        leftLine = new Paint();
        rightLine = new Paint();

        setLineParameters(Color.WHITE,2);

        // Here I grab the image that will work as the corners of the bounding
        // box and set their positions.

        mLeftTopIcon = context.getResources().getDrawable(R.drawable.corners);

        mCenter = mLeftTopIcon.getMinimumHeight()/2;
        mLeftTopIcon.setBounds((int)mLeftTopPosX, (int)mLeftTopPosY,
                mLeftTopIcon.getIntrinsicWidth()+(int)mLeftTopPosX,
                mLeftTopIcon.getIntrinsicHeight()+(int)mLeftTopPosY);

        mRightTopIcon = context.getResources().getDrawable(R.drawable.corners);
        mRightTopIcon.setBounds((int)mRightTopPosX, (int)mRightTopPosY,
                mRightTopIcon.getIntrinsicWidth()+(int)mRightTopPosX,
                mRightTopIcon.getIntrinsicHeight()+(int)mRightTopPosY);

        mLeftBottomIcon = context.getResources().getDrawable(R.drawable.corners);
        mLeftBottomIcon.setBounds((int)mLeftBottomPosX, (int)mLeftBottomPosY,
                mLeftBottomIcon.getIntrinsicWidth()+(int)mLeftBottomPosX,
                mLeftBottomIcon.getIntrinsicHeight()+(int)mLeftBottomPosY);

        mRightBottomIcon = context.getResources().getDrawable(R.drawable.corners);
        mRightBottomIcon.setBounds((int)mRightBottomPosX, (int)mRightBottomPosY,
                mRightBottomIcon.getIntrinsicWidth()+(int)mRightBottomPosX,
                mRightBottomIcon.getIntrinsicHeight()+(int)mRightBottomPosY);
        
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return false;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });

    }

    private void setLineParameters(int color, float width){

        topLine.setColor(color);
        topLine.setStrokeWidth(width);

        bottomLine.setColor(color);
        bottomLine.setStrokeWidth(width);

        leftLine.setColor(color);
        leftLine.setStrokeWidth(width);

        rightLine.setColor(color);
        rightLine.setStrokeWidth(width);

    }

    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        boolean intercept = true;

        switch (action) {

            case MotionEvent.ACTION_DOWN: {

                final float x = ev.getX();
                final float y = ev.getY();

                // in CameraPreview we have Rect rec. This is passed here to return
                // a false when the camera button is pressed so that this view ignores
                // the touch event.
                if ((x >= buttonRec.left) && (x <=buttonRec.right) && (y>=buttonRec.top) && (y<=buttonRec.bottom)){
                    intercept = false;
                    break;
                }

                // is explained below, when we get to this method.
                manhattanDistance(x,y);

                // Remember where we started
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX();
                final float y = ev.getY();
                //Log.i(TAG,"x: "+x);
                //Log.i(TAG,"y: "+y);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                // but we ignore here because we are not using ScaleGestureDetector.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                // Calculate the distance moved
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;


                // Move the object
                if (mPosX >= 0 && mPosX <=800){
                    mPosX += dx;
                }
                if (mPosY >=0 && mPosY <= 480){
                    mPosY += dy;
                }

                // while its being pressed n it does not overlap the bottom line or right line
                if (mLeftTopBool && ((y+mCenter*2) < mLeftBottomPosY) && ((x+mCenter*2) < mRightTopPosX)){
                    if (dy != 0){
                        mRightTopPosY = y;
                    }
                    if (dx != 0){
                        mLeftBottomPosX = x;
                    }
                    mLeftTopPosX = x;//mPosX;
                    mLeftTopPosY = y;//mPosY;
                }
                if (mRightTopBool && ((y+mCenter*2) < mRightBottomPosY) && (x > (mLeftTopPosX+mCenter*2))){
                    if (dy != 0){
                        mLeftTopPosY = y;
                    }
                    if (dx != 0){
                        mRightBottomPosX = x;
                    }
                    mRightTopPosX = x;//mPosX;
                    mRightTopPosY = y;//mPosY;
                }
                if (mLeftBottomBool && (y > (mLeftTopPosY+mCenter*2)) && ((x +mCenter*2) < mRightBottomPosX)){
                    if (dx != 0){
                        mLeftTopPosX = x;
                    }
                    if (dy != 0){
                        mRightBottomPosY = y;
                    }
                    mLeftBottomPosX = x;
                    mLeftBottomPosY = y;
                }
                if (mRightBottomBool && (y > (mLeftTopPosY+mCenter*2)) && (x > (mLeftBottomPosX+mCenter*2) )){
                    if (dx != 0){
                        mRightTopPosX = x;
                    }
                    if (dy != 0){
                        mLeftBottomPosY = y;
                    }
                    mRightBottomPosX = x;
                    mRightBottomPosY = y;
                }

                // Remember this touch position for the next move event
                mLastTouchX = x;
                mLastTouchY = y;

                // Invalidate to request a redraw
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                // when one of these is true, that means it can move when onDraw is called
                mLeftTopBool = false;
                mRightTopBool = false;
                mLeftBottomBool = false;
                mRightBottomBool = false;
                //mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return intercept;
    }
    private void manhattanDistance(float x, float y) {

        double leftTopMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mLeftTopPosX)),2)
                + Math.pow((Math.abs((double)y-(double)mLeftTopPosY)),2));

        double rightTopMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mRightTopPosX)),2)
                + Math.pow((Math.abs((double)y-(double)mRightTopPosY)),2));

        double leftBottomMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mLeftBottomPosX)),2)
                + Math.pow((Math.abs((double)y-(double)mLeftBottomPosY)),2));

        double rightBottomMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mRightBottomPosX)),2)
                + Math.pow((Math.abs((double)y-(double)mRightBottomPosY)),2));

        //Log.i(TAG,"leftTopMan: "+leftTopMan);
        //Log.i(TAG,"RightTopMan: "+rightTopMan);

        if (leftTopMan < 50){
            mLeftTopBool = true;
            mRightTopBool = false;
            mLeftBottomBool = false;
            mRightBottomBool = false;
        }
        else if (rightTopMan < 50){
            mLeftTopBool = false;
            mRightTopBool = true;
            mLeftBottomBool = false;
            mRightBottomBool = false;
        }
        else if (leftBottomMan < 50){
            mLeftTopBool = false;
            mRightTopBool = false;
            mLeftBottomBool = true;
            mRightBottomBool = false;
        }
        else if (rightBottomMan < 50){
            mLeftTopBool = false;
            mRightTopBool = false;
            mLeftBottomBool = false;
            mRightBottomBool = true;
        }

    }

    public float getmLeftTopPosX(){
        return mLeftTopPosX;
    }
    public float getmLeftTopPosY(){
        return mLeftTopPosY;
    }
    public float getmRightTopPosX(){
        return mRightTopPosX;
    }
    public float getmRightTopPosY(){
        return mRightTopPosY;
    }
    public float getmLeftBottomPosX() {
        return mLeftBottomPosX;
    }
    public float getmLeftBottomPosY() {
        return mLeftBottomPosY;
    }
    public float getmRightBottomPosY() {
        return mRightBottomPosY;
    }
    public float getmRightBottomPosX() {
        return mRightBottomPosX;
    }
    public void setRec(Rect rec) {
        this.buttonRec = rec;
    }

    // calls the onDraw method, I used it in my app Translanguage OCR
// because I have a thread that needs to invalidate, or redraw
// you cannot call onDraw from a thread not the UI thread.
    public void setInvalidate() {
        invalidate();

    }
}
