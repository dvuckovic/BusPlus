package com.dvuckovic.busplus;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

/** Custom map view class in order to intercept some events **/
public class MyMapView extends MapView {

	private Context context;
	private GestureDetector gestureDetector;
	private int oldZoomLevel = -1;
	private OnZoomListener mListener;

	public MyMapView(Context aContext, AttributeSet attrs) {
		super(aContext, attrs);
		context = aContext;

		// Detect gestures
		gestureDetector = new GestureDetector((OnGestureListener) context);
		gestureDetector.setOnDoubleTapListener((OnDoubleTapListener) context);
	}
	
	public MyMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	public MyMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// Override the onTouchEvent() method to intercept events and pass them
	// to the GestureDetector. If the GestureDetector doesn't handle the event,
	// propagate it up to the MapView.
	public boolean onTouchEvent(MotionEvent ev) {
		if (this.gestureDetector.onTouchEvent(ev))
			return true;
		else
			return super.onTouchEvent(ev);
	}

	/** Define on zoom listener **/
	public interface OnZoomListener {
		public void onZoom();
	}

	/** Define on zoom listener setter **/
	public void setOnZoomListener(OnZoomListener listener) {
		mListener = listener;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		// Call on zoom listener on zoom
		if (getZoomLevel() != oldZoomLevel) {
			mListener.onZoom();
			oldZoomLevel = getZoomLevel();
		}
	}

}