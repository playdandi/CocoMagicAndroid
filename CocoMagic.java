/****************************************************************************
Copyright (c) 2010-2011 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package com.playDANDi.CocoMagic;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.gcm.GCMRegistrar;
import com.kakao.cocos2dx.plugin.KakaoAndroid;
import com.kakao.cocos2dx.plugin.KakaoAndroidInterface;

public class CocoMagic extends Cocos2dxActivity implements KakaoAndroidInterface {
 	
	static Variables v;
	static String regId = "";
	public static Cocos2dxActivity activity;
	static int version = -1;
	static PackageManager pm;
	
    protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		pm = getPackageManager();
		
		activity = this;
		v = ((Variables)getApplicationContext());
		
		// GCM push 관련 registration  
		registerGCM();
		
		// for kakao
		KakaoAndroid.plugin = this;
		KakaoAndroid.uri = getIntent().getData();
		initJNIBridge();
		
		// version name 저장해 두기.
		try {
			version = Integer.parseInt( getPackageManager().getPackageInfo(getPackageName(), 0).versionName );
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public void registerGCM()
    {
    	//Log.e("registerGCM", "registerGCM");
    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this);
    	
    	regId = GCMRegistrar.getRegistrationId(this);
    	//Log.e("registerGCM", regId);

    	if (regId.equals(""))
    	{
    		GCMRegistrar.register(this, getResources().getString(R.string.project_id));
    		//Log.e("registerGCM", "registrar...");
    	}
    	else
    	{
    		//Log.e("id", regId);
    		v.setRegistrationId(regId);
    	}
    }
    
    public Cocos2dxGLSurfaceView onCreateView() {
    	Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
    	// CocoMagic should create stencil buffer
    	glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);
    	
    	return glSurfaceView;
    }
    
    public static String GetRegistrationId()
    {
    	return v.getRegistrationId();
    }
    
    public void setRegistrationId(String id)
    {
    	regId = id;
    }
    
    // 루팅 폰 감지 함수
    public static boolean IsRootingPhone()
    {
    	boolean result = false;
    	try {
			Runtime.getRuntime().exec("su");
			// 루팅 폰!
			result = true;
			//android.os.Process.killProcess(android.os.Process.myPid());
		}
		catch (Exception e) {
			// 루팅이 안 되어 있을 때 exception (정상임)
			result = false;
		}

    	return result;
    }
    
    // 악성 앱 감지 함수
    public static int CheckFuckingApp(String packageName)
    {
    	int result = 0;
    	try {
    		ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
    		//Log.e("악성어플 이름", ai.packageName);
    		result = 1;
    		//android.os.Process.killProcess(android.os.Process.myPid());
    	}
    	catch (NameNotFoundException e) {
    		//Log.e("악성어플 없음", fuckingApps[i]);
    		result = 0;
    	}

    	return result;
    }
    
    // URL open 함수
    public static void openURL(String type)
    { 
    	Intent intent = new Intent(activity, Term.class);
		intent.putExtra("type", type);
		activity.startActivity(intent);
    }
    
    public static void OpenNoticeURL(String url)
    {
    	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    	activity.startActivity(intent);
    }
    
    // 결제 시작 함수
    public static void StartIAB(int type, int topazId, String kakaoId, String friendKakaoId, String productId, String payload, String gcmPublicKey)
    {
    	// 팝업으로 사용할 액티비티를 호출할 인텐트를 작성한다.
		Intent intent = new Intent(activity, InAppBilling.class);
		intent.putExtra("type", type);
		intent.putExtra("topazId", topazId);
		intent.putExtra("kakaoId", kakaoId);
		intent.putExtra("friendKakaoId", friendKakaoId);
		intent.putExtra("productId", productId);
		intent.putExtra("payload", payload);
		intent.putExtra("gcmPublicKey", gcmPublicKey);
		activity.startActivity(intent);
    }
    
    // 플레이스토어로 가는 함수
    public static void GoToPlayStore()
    {
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse("market://details?id=com.playDANDi.CocoMagic"));
    	activity.startActivity(intent); 
    }
    
    public static int GetBinaryVersion()
    {
        return version;
    }
    
    
    static {
        System.loadLibrary("cocos2dcpp");
    }
    
    private native void initJNIBridge();
 	private native void sendMessageBridge(String target, String method, String params);
    
    
 	/*
    @Override
    protected void onResume() {
    	super.onResume();
    	KakaoAndroid.getInstance().resume(this);
    }
    */

    // for kakao
 	@Override
 	public void sendMessage(final String target, final String method, final String params)
 	{
 		runOnGLThread(new Runnable() {
 			@Override
 			public void run() {
 				sendMessageBridge(target, method, params);
 			}
 		});
 	}
 	
 	@Override
 	public void kakaoCocos2dxExtension(String params)
 	{
 		//Logger.getInstance().i("kakaoCocos2dxExtension params: " + params);
 		try {
 			KakaoAndroid.getInstance().execute(CocoMagic.this, params);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		KakaoAndroid.getInstance().activityResult(this, requestCode, resultCode, data);
 	}
 	
 	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:  //버튼 반응없음(막기)
        	//Log.e("KEY DOWN", "안드로이드 백버튼 호출");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
