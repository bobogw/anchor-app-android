package com.netease.livestreamingcapture;


import com.netease.livestreamingcapture.MediaPreviewActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {
	private OnClickListener OnClickEvent;
	private RadioButton NormalModeBtn;
	//private RadioButton BeautyModeBtn;
    private Button mBtnStartBtn;
    private EditText mMediaEditURL;
    private MsgReceiver msgReceiver;
    private String mVideoResolution = "SD";
    private String mFilterMode = "NormalMode";

    //private RadioButton mRadioHD;  
    private RadioButton mRadioSD;  
    private RadioButton mRadioFluency;  
    
    private String mUrlMedia; 

    //滤镜（涉及硬件编码）的相关操作
    Blacklist[] g_blacklist = { //可扩展，需修改
    		new Blacklist("L39h", 19),
    		new Blacklist("N1", 22)
    };
    
    public class Blacklist {
    	public Blacklist(String model, int api){
    		mModel = model;
    		mApi = api;
    	}
    	public String getModel() {
    		return mModel;
    	}
    	public int getApi() {
    		return mApi;
    	}
    	private String mModel;
    	private int mApi;
    }
    
    public boolean checkCurrentDeviceInBlacklist(){ 
    	boolean bInBlacklist = false;
    	String model = Build.MODEL;
    	int api = Build.VERSION.SDK_INT;
    	
    	int listsize = g_blacklist.length;
    	
    	for(int i = 0; i < listsize; i++)
    	{
    		if(model.equals(g_blacklist[i].getModel()) && api == g_blacklist[i].getApi())
    			bInBlacklist = true;
    	}    	
    	
    	return bInBlacklist;
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_main2);

        //动态注册广播接收器，接收Service的消息  
        msgReceiver = new MsgReceiver();  
        IntentFilter intentFilter = new IntentFilter();  
        intentFilter.addAction("LiveStreamingStopFinished");  
        registerReceiver(msgReceiver, intentFilter);  
        
        
        //根据ID找到RadioButton实例
        //mRadioHD=(RadioButton)findViewById(R.id.radioHD);  
        mRadioSD=(RadioButton)findViewById(R.id.radioSD);  
        mRadioFluency=(RadioButton)findViewById(R.id.radioFluency);  
        
    	//mRadioHD.setButtonDrawable(R.drawable.radio_button_unused);
    	mRadioSD.setButtonDrawable(R.drawable.radio_button_used);
    	mRadioFluency.setButtonDrawable(R.drawable.radio_button_unused);

    	NormalModeBtn=(RadioButton)findViewById(R.id.NormalModeBtn);  
    	//BeautyModeBtn=(RadioButton)findViewById(R.id.BeautyModeBtn);  
    	
		NormalModeBtn.setButtonDrawable(R.drawable.radio_button_used);
		//BeautyModeBtn.setButtonDrawable(R.drawable.radio_button_unused);

        mBtnStartBtn = (Button)findViewById(R.id.StartAVBtn);
        mBtnStartBtn.setEnabled(true);
    	
		mMediaEditURL = (EditText)findViewById(R.id.RTMP_URL);
        
		OnClickEvent = new OnClickListener(){	
	
		 public void onClick(View v) {
			 Intent   intent;
			        switch (v.getId()) {
			        /*
			            case R.id.radioHD:
			            	mVideoResolution = "HD"; 
			            	mRadioHD.setButtonDrawable(R.drawable.radio_button_used);
			            	mRadioSD.setButtonDrawable(R.drawable.radio_button_unused);
			            	mRadioFluency.setButtonDrawable(R.drawable.radio_button_unused);
			            	mBtnStartBtn.setEnabled(true);
			                break;
			         */
			            case R.id.radioSD:
			            	mVideoResolution = "SD"; 
			            	//mRadioHD.setButtonDrawable(R.drawable.radio_button_unused);
			            	mRadioSD.setButtonDrawable(R.drawable.radio_button_used);
			            	mRadioFluency.setButtonDrawable(R.drawable.radio_button_unused);
			            	mBtnStartBtn.setEnabled(true);
			                break;
			            case R.id.radioFluency:
			            	mVideoResolution = "Fluency";
			            	//mRadioHD.setButtonDrawable(R.drawable.radio_button_unused);
			            	mRadioSD.setButtonDrawable(R.drawable.radio_button_unused);
			            	mRadioFluency.setButtonDrawable(R.drawable.radio_button_used);
			            	mBtnStartBtn.setEnabled(true);
			                break;
			            case R.id.NormalModeBtn:
			            	mFilterMode = "NormalMode";
			            	NormalModeBtn.setButtonDrawable(R.drawable.radio_button_used);
			            	//BeautyModeBtn.setButtonDrawable(R.drawable.radio_button_unused);
			            	mBtnStartBtn.setEnabled(true);
			                break;
			            /*
			            case R.id.BeautyModeBtn:
			            	mFilterMode = "BeautyMode";
			            	NormalModeBtn.setButtonDrawable(R.drawable.radio_button_unused);
			            	BeautyModeBtn.setButtonDrawable(R.drawable.radio_button_used);
			            	mBtnStartBtn.setEnabled(true);
			                break;
			            */
			            case R.id.StartAVBtn:
			            	mUrlMedia = mMediaEditURL.getText().toString();
			            	if(mFilterMode.equals("NormalMode"))
			            	{
			            		intent = new Intent(MainActivity.this, MediaPreviewActivity.class);
				                intent.putExtra("mediaPath", mUrlMedia);
				                intent.putExtra("videoResolution", mVideoResolution);
				                intent.putExtra("filter", false);
				                intent.putExtra("alert", false);
				                startActivity(intent);
			            	}
			            	else if(mFilterMode.equals("BeautyMode"))
			            	{
			            		//Android SDK版本低于4.4（19）或者不支持硬件编码时，提示用户不能开启滤镜功能
			            		if(android.os.Build.VERSION.SDK_INT < 19)
			            		{			            			
				            		intent = new Intent(MainActivity.this, MediaPreviewActivity.class);
					                intent.putExtra("mediaPath", mUrlMedia);
					                intent.putExtra("videoResolution", mVideoResolution);
					                intent.putExtra("filter", true);
					                intent.putExtra("alert1", true);
					                intent.putExtra("alert2", false);
					                startActivity(intent);
			            		}
			            		else if(checkCurrentDeviceInBlacklist())
			            		{			            			
				            		intent = new Intent(MainActivity.this, MediaPreviewActivity.class);
					                intent.putExtra("mediaPath", mUrlMedia);
					                intent.putExtra("videoResolution", mVideoResolution);
					                intent.putExtra("filter", true);
					                intent.putExtra("alert1", false);
					                intent.putExtra("alert2", true);
					                startActivity(intent);
			            		}
			            		else
			            		{
			            		    intent = new Intent(MainActivity.this, MediaPreviewActivity.class);
				                    intent.putExtra("mediaPath", mUrlMedia);
				                    intent.putExtra("videoResolution", mVideoResolution);
				                    intent.putExtra("filter", true);
				                    intent.putExtra("alert1", false);
					                intent.putExtra("alert2", false);
				                    startActivity(intent);
			            		}
				                break;
			            	}
			            default:
			                break;
			        }
			    }
		
	    };
	    //mRadioHD.setOnClickListener(OnClickEvent);
	    mRadioSD.setOnClickListener(OnClickEvent);
	    mRadioFluency.setOnClickListener(OnClickEvent);
		NormalModeBtn.setOnClickListener(OnClickEvent);
		//BeautyModeBtn.setOnClickListener(OnClickEvent);
	    mBtnStartBtn.setOnClickListener(OnClickEvent);
	}
	
	//用于接收Service发送的消息
    public class MsgReceiver extends BroadcastReceiver{  
  
        @Override  
        public void onReceive(Context context, Intent intent) {  
 
            int value = intent.getIntExtra("LiveStreamingStopFinished", 0);   
            if(value == 1)//finished
            {
            	mBtnStartBtn.setEnabled(true);
            	mBtnStartBtn.setText("进入直播");
            }
            else//not yet finished
            {
            	mBtnStartBtn.setEnabled(false);
            	mBtnStartBtn.setText("直播停止中...");
            }
        }          
    } 
}

