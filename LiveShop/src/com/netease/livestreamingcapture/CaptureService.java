package com.netease.livestreamingcapture;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CaptureService extends Service {
	
	private static final String TAG = "CaptureService";
	//退出按钮
	private ImageButton closeBtn;
	//截图显示
	private ImageView imgView;
	//截图保存路径
	private String imgFilePath;
	//说明输入
	private EditText introTxt;
	//提交按钮
	private Button subBtn;
	//删除（取消）按钮
	private Button delBtn;
	//提交地址
	private String postURL="";
	
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
		imgFilePath = bundle.getString("imgfile");
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
		File imgFile = new File(imgFilePath);
		if(imgFile.exists()){
			Bitmap bm=BitmapFactory.decodeFile(imgFilePath);
			imgView.setImageBitmap(bm);
		}
		
		introTxt=(EditText)view.findViewById(R.id.introTxt);
		
		//发送按钮，按下后POST方式发送图片和说明文本到服务器
		subBtn=(Button)view.findViewById(R.id.submitButton);
		subBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				postData();
			}
		});
		
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
	
	
	private String postData(){
		String returnStr="";
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postRequest = new HttpPost(postURL);
		MultipartEntity reqEntity =new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		File imgFile = new File(imgFilePath);
		ContentBody imgBody = new FileBody(imgFile,"image/jpeg");
		StringBody introBody=null;
		try {
			introBody =  new StringBody(introTxt.getText().toString().trim());
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		reqEntity.addPart("introTXT", introBody);
		reqEntity.addPart("image", imgBody);
		
		postRequest.setEntity(reqEntity);
		
		try {
			HttpResponse response = httpClient.execute(postRequest);
			if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
				Log.i(TAG,"upload succed");
				returnStr="succed";
			}
		} catch (ClientProtocolException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			returnStr="error";
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			returnStr="error";
		}
		
		return returnStr;
	}

}
