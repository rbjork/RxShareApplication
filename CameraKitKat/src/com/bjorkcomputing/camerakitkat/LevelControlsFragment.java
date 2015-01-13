package com.bjorkcomputing.camerakitkat;

import android.app.Fragment;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LevelControlsFragment extends Fragment {

	private ProtractorView pv;
	private Button mTakePicture;
	private Button mPreviewBtn;
	private ImageView mToggleFlash;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		RelativeLayout v = (RelativeLayout)inflater.inflate(R.layout.fragment_levelcontrols, container,false);
		
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		CameraPreview cpv = (CameraPreview)getActivity();
		
		mTakePicture = (Button)cpv.findViewById(R.id.takepictureBtn);
		mToggleFlash = (ImageView)cpv.findViewById(R.id.flashtogglebutton);
		mPreviewBtn = (Button)cpv.findViewById(R.id.previewBtn);
	    mToggleFlash.setClickable(true);
	    mToggleFlash.setOnClickListener(cpv.flashListener);
	    mTakePicture.setClickable(true);
	    mTakePicture.setOnClickListener(cpv.shutterListener);
	    mPreviewBtn.setOnClickListener(cpv.previewBtnListener);
		
		Camera.Parameters p = cpv.getCameraParameters();
		RelativeLayout rv = (RelativeLayout)getView();;
		if(p == null){
			pv = new ProtractorView(getActivity(),40,30);
			
		}else{
			double thetaV = Math.toRadians(p.getVerticalViewAngle());
			double thetaH = Math.toRadians(p.getHorizontalViewAngle());
			pv = new ProtractorView(getActivity(),thetaV,thetaH);
		}
		rv.addView(pv);
	}
	
	public void updateProtractor(){
		if(pv != null){
			pv.invalidate();
		}
	}
	
}
