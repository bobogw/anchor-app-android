package com.netease.livestreamingcapture;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CaptureService extends Service {
	
	//退出按钮
	private ImageButton closeBtn;
	private ImageView imgView;
	private EditText introTxt;
	private Button subBtn;
	private Button delBtn;
	
	/**
	 * 定义浮动窗口布局
	 */
	Dialog mDialog;
	/**
	 * 悬浮窗的布局
	 */
	WindowManager.LayoutParams wmParams;
	LayoutInflater inflater;

    public static Boolean isShown = false;

    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO 自动生成的方法存根
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO 自动生成的方法存根
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle bundle = intent.getExtras();
		String alertString = bundle.getString("alert");
		
		initWindow();
		return START_NOT_STICKY;//super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mDialog != null){
			mDialog.dismiss();
		}
	}

	
	/**
	 * 初始化
	 */
	private void initWindow() {
//		boolean bShowAlert = false;
		mDialog = new Dialog(CaptureService.this);
		mDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

		//得到容器，通过这个inflater来获得悬浮窗控件
		inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		View view = inflater.inflate(R.layout.alert_capture, null);

		
		//退出按钮事件
		closeBtn = (ImageButton)view.findViewById(R.id.closeBtn);
		closeBtn.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		mDialog.dismiss();
        	}
        });
		
		imgView=(ImageView)view.findViewById(R.id.imageView1);
		introTxt=(EditText)view.findViewById(R.id.introTxt);
		subBtn=(Button)view.findViewById(R.id.submitButton);
		
		delBtn=(Button)view.findViewById(R.id.delButton);
		delBtn.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		mDialog.dismiss();
        	}
        });
		
		// 添加悬浮窗的视图
//		if(bShowAlert) {
		    mDialog.setContentView(view);
//		    mDialog.setTitle("警告");
		
		    mDialog.setCanceledOnTouchOutside(true);
		    mDialog.show();
//		}
	}

}
