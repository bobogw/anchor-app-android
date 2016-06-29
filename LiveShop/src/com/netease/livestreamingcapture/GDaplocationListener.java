
package com.netease.livestreamingcapture;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;

/**
 * @author liupeng
 * 实现AMapLocationListener接口
 * 记录历史定位信息
 */
public class GDaplocationListener extends Service implements AMapLocationListener {
	//保存历史定位信息
	public ArrayList<HashMap<String, Object>> locationList = new ArrayList<HashMap<String, Object>>();

	private Handler mHandler;
	
	//传递地址信息给netinfo信息框
	private Intent mNetInfoIntent = new Intent("com.netease.netInfo");
	
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO 自动生成的方法存根
		if (amapLocation != null) {
			//通知定位结果代码
			mNetInfoIntent.putExtra("locationCode", amapLocation.getErrorCode());
	        if (amapLocation.getErrorCode() == 0) {
	        	HashMap<String, Object> locationMsg = new HashMap<String, Object>();
		        //定位成功回调信息，设置相关消息
		        locationMsg.put("type", amapLocation.getLocationType());//获取当前定位结果来源，如网络定位结果，详见定位类型表
		        locationMsg.put("latitude", amapLocation.getLatitude());//获取纬度
		        locationMsg.put("longitude", amapLocation.getLongitude());//获取经度
		        locationMsg.put("accuracy", amapLocation.getAccuracy());//获取精度信息
		        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        Date date = new Date(amapLocation.getTime());
		        locationMsg.put("datetime", df.format(date));//定位时间
		        locationMsg.put("address", amapLocation.getAddress());//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
		        locationList.add(locationMsg);
		        mNetInfoIntent.putExtra("latitude", amapLocation.getLatitude());
		        mNetInfoIntent.putExtra("longitude", amapLocation.getLongitude());
		        mNetInfoIntent.putExtra("address", amapLocation.getAddress());
		        sendBroadcast(mNetInfoIntent);
	        } else {
		        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
/*		        Log.e("AmapError","location Error, ErrCode:"
		            + amapLocation.getErrorCode() + ", errInfo:"
		            + amapLocation.getErrorInfo());*/
	        }
		}
	}
	
	public HashMap getLastLocation(){
		if(locationList.size()>0){
			return locationList.get(locationList.size()-1);
		}else{
			return null;
		}
		 
	}
	
	public ArrayList getAllLocation(){
		return locationList;
	}
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO 自动生成的方法存根
		return null;
	}

}
