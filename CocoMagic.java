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

import com.google.android.gcm.GCMRegistrar;
import com.playDANDi.CocoMagic.util.IabHelper;
import com.playDANDi.CocoMagic.util.IabResult;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CocoMagic extends Cocos2dxActivity{
	
	static Variables v;
	static String regId;
	public static Cocos2dxActivity activity;
	
    protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);	
		
		activity = this;
		v = ((Variables)getApplicationContext());
		
		// GCM push 관련 registration  
		registerGCM();
	}

    public void registerGCM()
    {
    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this);
    	
    	regId = GCMRegistrar.getRegistrationId(this);

    	if (regId.equals(""))
    	{
    		GCMRegistrar.register(this, getResources().getString(R.string.project_id));
    	}
    	else
    	{
    		Log.e("id", regId);
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
    	//Log.e("JNITest", "Success Java Func Call ! ");
    	return v.getRegistrationId();
    }
    
    public void setRegistrationId(String id)
    {
    	regId = id;
    }
    
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
    
    /*public void verifyPurchase(String data, String signature)
    {
    	Log.e("verifyPurchase", "hihi");
    	verifyPayloadAndProvideItem(data, signature);
    }*/

    static {
        System.loadLibrary("cocos2dcpp");
    }
    
    //private native void verifyPayloadAndProvideItem(String data, String signature);
}
