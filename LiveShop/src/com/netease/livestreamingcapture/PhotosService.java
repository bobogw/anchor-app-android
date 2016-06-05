package com.netease.livestreamingcapture;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

public class PhotosService extends Service {
	
	private static final String TAG = "PhotosService";

	//退出按钮
	private ImageButton closeBtn;
	
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

    private WebView mAlertWebView;
    
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
		Log.i(TAG, "ophotosservice start");
/*		Bundle bundle = intent.getExtras();
		String alertString = bundle.getString("alert");
		*/
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
		Log.i(TAG, "PhotosService initWindow");
//		boolean bShowAlert = false;
		mDialog = new Dialog(PhotosService.this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

		//得到容器，通过这个inflater来获得悬浮窗控件
		inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		View view = inflater.inflate(R.layout.alert_photos, null);

		mAlertWebView = (WebView)view.findViewById(R.id.webViewPhotos); 
		mAlertWebView.loadUrl("http://m.liepin.com");
		mAlertWebView.setWebViewClient(new WebViewClient(){
        	@Override
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                   //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                 view.loadUrl(url);
                return true;
            }
        });
		
		//退出按钮事件
		closeBtn = (ImageButton)view.findViewById(R.id.closeBtn);
		closeBtn.setOnClickListener(new OnClickListener(){
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
