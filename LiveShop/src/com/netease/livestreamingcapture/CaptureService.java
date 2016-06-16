package com.netease.livestreamingcapture;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.newname.http.HttpHost;
import com.newname.http.HttpResponse;
import com.newname.http.HttpStatus;
import com.newname.http.client.*;
import com.newname.http.client.methods.HttpPost;
import com.newname.http.entity.*;
import com.newname.http.entity.mime.HttpMultipartMode;
import com.newname.http.entity.mime.MultipartEntity;
import com.newname.http.entity.mime.content.ContentBody;
import com.newname.http.entity.mime.content.FileBody;
import com.newname.http.entity.mime.content.StringBody;
import com.newname.http.impl.client.DefaultHttpClient;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CaptureService extends Service {
	
	private static final String TAG = "CaptureService";
	//退出按钮
	private ImageButton closeBtn;
	//截图显示
	private ImageView imgView;
	
	//标题
	private TextView titleTxt;
	
	//截图保存路径
	private String imgFilePath;
	//说明输入
	private EditText introTxt;
	//提交按钮
	private Button subBtn;
	//删除（取消）按钮
	private Button delBtn;
	//提交地址
	private String postURL="http://www.baidu.com/s";
	
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
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));

		//得到容器，通过这个inflater来获得悬浮窗控件
		inflater = LayoutInflater.from(getApplication());
		// 获取浮动窗口视图所在布局
		View view = inflater.inflate(R.layout.alert_capture, null);

		titleTxt=(TextView)view.findViewById(R.id.AlertTitle);
		
		
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
		introTxt.setText(imgFilePath);
		
		//发送按钮，按下后POST方式发送图片和说明文本到服务器
		subBtn=(Button)view.findViewById(R.id.submitButton);
		subBtn.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				//不能在主线程里做网络操作，因此另起线程
				subBtn.setEnabled(false);
				titleTxt.setText("上传中");
				new Thread(networkTask).start(); 
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
	
	/**
	 * 执行上传操作
	 * @return 上传结果。"succed"=成功；"error"=失败
	 */
	private String postData(){
		String returnStr="";
		HttpClient httpClient = new DefaultHttpClient();
		Log.i(TAG, "生成httppost");
		HttpPost postRequest = new HttpPost(postURL);
		Log.i(TAG, "生成MultipartEntity");
		MultipartEntity reqEntity =new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
/*		File imgFile = new File(imgFilePath);
		ContentBody imgBody = new FileBody(imgFile,"image/jpeg");
*/
		Log.i(TAG, "生成要提交的消息体");
		StringBody introBody=null;
		try {
			introBody =  new StringBody(introTxt.getText().toString().trim());
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		Log.i(TAG, "加入要提交的键值对");
		reqEntity.addPart("introTXT", introBody);
//		reqEntity.addPart("image", imgBody);
		Log.i(TAG, "封装好药提交的内容，添加进提交请求");
		postRequest.setEntity(reqEntity);
		Log.i(TAG, "执行信息发送");
		HttpResponse response=null;
		try {
			response = httpClient.execute(postRequest);
			if(response.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
				Log.i(TAG,"upload succed");
				returnStr="succed";
			}else{
				Log.i(TAG, "response statuscode=" + response.getStatusLine().getStatusCode());
				returnStr="error";
			}

		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			Log.i(TAG, response.toString());
			//e.printStackTrace();
			returnStr="error";
		}
		
		return returnStr;
	}
	
	Runnable networkTask = new Runnable(){
		public void run(){
	        Message msg = new Message();  
	        Bundle data = new Bundle();  
	        data.putString("result", postData());  
	        msg.setData(data);  
	        handler.sendMessage(msg); 
		}
	};
	
	/**
	 * 返回结果处理
	 */
	Handler handler = new Handler() {  
	    @Override  
	    public void handleMessage(Message msg) {  
	        super.handleMessage(msg);  
	        Bundle data = msg.getData();  
	        String val = data.getString("result");  
	        Log.i(TAG, "请求结果为-->" + val);  
	        if(val.equals("succed")){
	        	titleTxt.setText("上传成功");
	        	delBtn.setText("返回");
	        }else if(val.equals("error")){
	        	titleTxt.setText("上传失败");
	        	subBtn.setEnabled(true);
	        	subBtn.setText("重试");
	        }
	    }
	};  
	
	
}
