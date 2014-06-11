package com.playDANDi.CocoMagic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.PowerManager;
import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService {
 
private static void generateNotification(Context context, String message)
{
	int icon = R.drawable.icon;
	long when = System.currentTimeMillis();
 
	NotificationManager notificationManager = (NotificationManager) context
			.getSystemService(Context.NOTIFICATION_SERVICE);
 
	Notification notification = new Notification(icon, message, when);
 
	String title = context.getString(R.string.app_name);
 
	Intent notificationIntent = new Intent(context, CocoMagic.class);
 
	// set intent so it doesn't start a new activity
	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP 
        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	PendingIntent intent = PendingIntent.getActivity(context, 0,
			notificationIntent, 0);
 
	notification.setLatestEventInfo(context, title, message, intent);
 
	notification.flags |= Notification.FLAG_AUTO_CANCEL;
	
	//notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
	
	// Play default notification sound
	notification.defaults |= Notification.DEFAULT_SOUND;
		
	// Vibrate if vibrate is enabled
	notification.defaults |= Notification.DEFAULT_VIBRATE;
 
	notificationManager.notify(0, notification);
}
 
	@Override
	protected void onError(Context arg0, String arg1) {
		
	}
 
	@Override
	protected void onMessage(Context context, Intent intent) {
		String msg = intent.getStringExtra("msg");
		//Log.e("getmessage", "getmessage:" + msg);
		
		displayMessage(context, msg);
		
		generateNotification(context, msg);
	}
 
	@Override
	protected void onRegistered(Context context, String reg_id) {
		Log.e("키를 등록합니다.(GCM INTENTSERVICE)", reg_id);
		Variables v = ((Variables)getApplicationContext());
		v.setRegistrationId(reg_id);
	}
 
	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.e("키를 제거합니다.(GCM INTENTSERVICE)","제거되었습니다.");
	}
	
	/**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
	static final String DISPLAY_MESSAGE_ACTION = "com.playDANDi.CocoMagic.DISPLAY_MESSAGE";
	static final String EXTRA_MESSAGE = "message";
	    
    static void displayMessage(Context context, String message) {
    	
    	if (!isScreenOn(context)) {
    		PushWakeLock.acquireCpuWakeLock(context);
    		
    		// 팝업으로 사용할 액티비티를 호출할 인텐트를 작성한다.
    		Intent popupIntent = new Intent(context, PushPopup.class)
               .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
    		Bundle b = new Bundle();
    		b.putString("title", "코코가 그리는 마법");
    		b.putString("msg", message);
    		popupIntent.putExtras(b);
    		context.startActivity(popupIntent);
    	}
    }
    
    static boolean isScreenOn(Context context) {
    	return ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }
}
 