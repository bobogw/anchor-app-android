package com.netease.livestreamingcapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

import com.netease.LSMediaCapture.lsSurfaceView;

public class LiveSurfaceView extends lsSurfaceView {

    private int previewWidth;
    private int previewHeight;
    private float ratio;
    private SurfaceHolder holder;
//添加截图
    private Bitmap capturePic;
    
    public LiveSurfaceView(Context context) {
        super(context);
    }

    public LiveSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	public void setPreviewSize(int width, int height) {
		Log.i("lSurfaceView", "setPreviewSize begin. width="+width+"  height="+height);
	    int screenW = getResources().getDisplayMetrics().widthPixels;
	    int screenH = getResources().getDisplayMetrics().heightPixels;
	    if (screenW < screenH) {
		    previewWidth = width < height ? width : height;
		    previewHeight = width >= height ? width : height;
	    }
	    else {
		    previewWidth = width > height ? width : height;
		    previewHeight = width <= height ? width : height;
	    }
	    ratio = previewHeight / (float) previewWidth;
	    //Log.i("lSurfaceView", "previewWidth="+previewWidth+"  previewHeight="+previewHeight+"  ratio="+ratio);
	    previewWidth = screenW;
	    previewHeight = screenH;
		//Log.i("lSurfaceView", "setPreviewSize end, call requestLayout.");
	    requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	Log.i("lSurfaceView", "override onMeasure begin");
	    int previewW     = MeasureSpec.getSize(widthMeasureSpec);
	    int previewWMode = MeasureSpec.getMode(widthMeasureSpec);
	    int previewH     = MeasureSpec.getSize(heightMeasureSpec);
	    int previewHMode = MeasureSpec.getMode(heightMeasureSpec);
	
	    int measuredWidth  = 0;
	    int measuredHeight = 0;
	
	    int defineWidth  = 0;
	    int defineHeight = 0;

	    Log.i("lSurfaceView", "previewW="+previewW+"  previewH="+previewH);
	    if(previewWMode== MeasureSpec.UNSPECIFIED){
	    	Log.i("lSurfaceView", "previewWMode=UNSPECIFIED");
	    }else if(previewWMode== MeasureSpec.EXACTLY){
	    	Log.i("lSurfaceView", "previewWMode=EXACTLY");
	    }else{
	    	Log.i("lSurfaceView", "previewWMode=AT_MOST");
	    }
	    
	    Log.i("lSurfaceView", "previewWidth="+previewWidth+"  previewHeight="+previewHeight);
	    
	    if (previewWidth > 0 && previewHeight > 0) {
		    measuredWidth = defineWidth(previewW, previewWMode);
		    
		    measuredHeight = (int) (measuredWidth * ratio);
		    if (previewHMode != MeasureSpec.UNSPECIFIED && measuredHeight > previewH) {
			    measuredWidth = (int) (previewH / ratio);
			    measuredHeight = previewH;
		    }
		
		    defineWidth = defineWidth(previewW, previewWMode);
		    defineHeight = defineHeight(previewH, previewHMode);

		    if(defineHeight*1.0/defineWidth > ratio)
		    {
			    measuredHeight = defineHeight(previewH, previewHMode);
			    measuredWidth = (int) (measuredHeight/ratio);
		    }
		    else
		    {
			    measuredWidth = defineWidth(previewW, previewWMode);
			    measuredHeight = (int) (measuredWidth*ratio);
		    }

		    Log.i("lSurfaceView", "width and height = " + measuredWidth + "*" + measuredHeight);
		    setMeasuredDimension(measuredWidth, measuredHeight);
	    }
	    else {
		    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    }
    }

    protected int defineWidth(int previewW, int previewWMode) {
	    int measuredWidth;
	    if (previewWMode == MeasureSpec.UNSPECIFIED) {
		    measuredWidth = previewWidth;
	    } 
	    else if (previewWMode == MeasureSpec.EXACTLY) {
		    measuredWidth = previewW;
	    } 
	    else {
		    measuredWidth = Math.min(previewW, previewWidth);
	    }
	    return measuredWidth;
    }

    protected int defineHeight(int previewH, int previewHMode) {
	    int measuredHeight;
	    if (previewHMode == MeasureSpec.UNSPECIFIED) {
		    measuredHeight = previewHeight;
	    } 
	    else if (previewHMode == MeasureSpec.EXACTLY) {
		    measuredHeight = previewH;
	    } 
	    else {
		    measuredHeight = Math.min(previewH, previewHeight);
	    }
	    return measuredHeight;
    }
    
    
}
