package com.netease.livestreamingcapture;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class NetInfoService extends Service {
	Dialog mDialog;
	WindowManager.LayoutParams wmParams;
	LayoutInflater inflater;

    public static Boolean isShown = false;   
    private TextView videoFrameRateTV, videoBitRateTV, audioBitRateTV, totalRealBitRateTV, ResolutionTV;
    private TextView latitudeView, longitudeView, addressView;
    private MsgReceiver msgReceiver;
    
    private int mVideoFrameRate = 0;
    private int mVideoBitrate = 0;
    private int mAudioBitrate = 0;
    private int mTotalRealBitrate = 0;
    
    private double latitude=0;
    private double longitude=0;
    private String address="";
    
    
    private int mResolution = 0;
    
    private Intent mIntent = new Intent("com.netease.livestreamingcapture");  
    
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
        msgReceiver = new MsgReceiver();  
        IntentFilter intentFilter = new IntentFilter();  
        intentFilter.addAction("com.netease.netInfo");  
        registerReceiver(msgReceiver, intentFilter);  

		initWindow();
		return START_NOT_STICKY;//super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mDialog != null){
			mDialog.dismiss();
		}
		unregisterReceiver(msgReceiver);
	}
	
	
	/**
	 * 初始化
	 */
	
	private void initWindow() {
		mDialog = new Dialog(NetInfoService.this);
		mDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
			
		inflater = LayoutInflater.from(getApplication());
		View view = inflater.inflate(R.layout.net_info_layout, null);

		videoFrameRateTV = (TextView)view.findViewById(R.id.VideoFrameRateTV); 
		videoBitRateTV = (TextView)view.findViewById(R.id.VideoBitRateTV); 
		audioBitRateTV = (TextView)view.findViewById(R.id.AudioBitRateTV); 
		totalRealBitRateTV = (TextView)view.findViewById(R.id.TotalRealBitRateTV); 
		ResolutionTV = (TextView)view.findViewById(R.id.ResolutionTV); 
		
		videoFrameRateTV.setText(String.valueOf(mVideoFrameRate) + " fps");
        videoBitRateTV.setText(String.valueOf(mVideoBitrate) + " kbps");
		audioBitRateTV.setText(String.valueOf(mAudioBitrate) + " kbps"); 
		totalRealBitRateTV.setText(String.valueOf(mTotalRealBitrate) + " kbps"); 
		
        if(mResolution == 1)
        {
            ResolutionTV.setText("高清");
        }
        else if(mResolution == 2)
        {
        	ResolutionTV.setText("标清");
        }
        else if(mResolution == 3)
        {
        	ResolutionTV.setText("流畅");
        }
        
        //定位信息显示
		latitudeView=(TextView)view.findViewById(R.id.Latitude);
		longitudeView=(TextView)view.findViewById(R.id.Longitude);
		addressView=(TextView)view.findViewById(R.id.Address);
		
		latitudeView.setText(String.valueOf(latitude));
		longitudeView.setText(String.valueOf(longitude));
		addressView.setText(address);
		
		mDialog.setContentView(view);
		mDialog.setTitle("网络信息");
		
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.show();
	}
	
    /** 
     * 广播接收器 
     * @author len 
     * 
     */  
    public class MsgReceiver extends BroadcastReceiver{  
  
        @Override  
        public void onReceive(Context context, Intent intent) {  
 
            int videoFrameRate = intent.getIntExtra("videoFrameRate", 0);  
            int videoBitRate = intent.getIntExtra("videoBitRate", 0);  
            int audioBitRate = intent.getIntExtra("audioBitRate", 0);  
            int totalRealBitRate = intent.getIntExtra("totalRealBitrate", 0);  
            int resolution = intent.getIntExtra("resolution", 0); 
            //获取纬度、经度、地址信息
            double latitudeMsg= intent.getDoubleExtra("latitude", 0);
            double longitudeMsg= intent.getDoubleExtra("longitude", 0);
            String addressMsg= intent.getStringExtra("address");
            
            if(videoFrameRate != 0){
            	mVideoFrameRate = videoFrameRate;
            	videoFrameRateTV.setText(String.valueOf(videoFrameRate) + " fps");
            }
            if(videoBitRate !=0){
                mVideoBitrate = videoBitRate;
                videoBitRateTV.setText(String.valueOf(videoBitRate) + " kbps");
            }
            if(audioBitRate != 0){
                mAudioBitrate = audioBitRate;
                audioBitRateTV.setText(String.valueOf(audioBitRate) + " kbps");
            }
            if(totalRealBitRate != 0){
                mTotalRealBitrate = totalRealBitRate;
                totalRealBitRateTV.setText(String.valueOf(totalRealBitRate) + " kbps");
            }
            if(resolution != 0){
                mResolution = resolution;

            }
            //显示定位信息
            if(intent.getIntExtra("locationCode", -1) == 0){
	            
	            if(latitudeMsg != 0){
	            	latitude = latitudeMsg;
	                latitudeView.setText(String.valueOf(latitudeMsg));
	            }
	            if(longitudeMsg != 0){
	            	longitude = longitudeMsg;
	            	longitudeView.setText(String.valueOf(longitudeMsg));
	            }
	            if(addressMsg != null){
	            	address=addressMsg;
	            	addressView.setText(addressMsg);
	            }
            }else if(intent.getIntExtra("locationCode", -1) == -1){
            	addressView.setText("没有定位信息");
            }else{
            	addressView.setText("定位出错："+ intent.getIntExtra("locationCode", -1));
            }
            
            if(mResolution == 1)
            {
                ResolutionTV.setText("高清");
            }
            else if(mResolution == 2)
            {
            	ResolutionTV.setText("标清");
            }
            else if(mResolution == 3)
            {
            	ResolutionTV.setText("流畅");
            }
        }  
    }

}
