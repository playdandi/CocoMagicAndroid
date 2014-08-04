package com.playDANDi.CocoMagic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.playDANDi.CocoMagic.util.IabHelper;
import com.playDANDi.CocoMagic.util.IabResult;
import com.playDANDi.CocoMagic.util.Inventory;
import com.playDANDi.CocoMagic.util.Purchase;

public class InAppBilling extends Activity {
	
	IInAppBillingService mService;
	static IabHelper mHelper;
	static Context mContext;
	CocoMagic parentActivity;
	
	static boolean isBackBtnOk;
	
	static int type; // 0 : 앱 초기실행 시  ,  1 : 구매를 시도할 경우(일반구매) , 2 : 구매를 시도할 경우(선물하기)
	static int topazId;
	static String kakaoId;
	static String friendKakaoId;
	static String productId;
	static String payload;
	static String base64EncodedPublicKey;
	
	static ArrayList<Purchase> purchaseForConsume;
	static int consumedCnt;
	
	public native String VerifyGoogle(int purchaseType, int topazId, String kakaoId, String friendKakaoId, String purchaseData, String dataSignature, int consumeIdx);
	public native String Deobfuscated(String response);
	public native void sendResultToCocos2dx(String response, int size, int consumeIdx, String friendKakaoId);
	public native static void startGame();
	public native static void setErrorFlag(boolean flag);
	 
	ServiceConnection mServiceConn = null;
	
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Log.e("method", "onCreate start");
		
		mContext = this;
		parentActivity = (CocoMagic)getParent();
		
		isBackBtnOk = true;
		
		Intent intent = getIntent();
		type = intent.getIntExtra("type", 1);
		topazId = intent.getIntExtra("topazId", -1);
		kakaoId = intent.getStringExtra("kakaoId");
		friendKakaoId = intent.getStringExtra("friendKakaoId");
		productId = intent.getStringExtra("productId");
		payload = intent.getStringExtra("payload");
		base64EncodedPublicKey = intent.getStringExtra("gcmPublicKey");
		
		
		// Binding to IInAppBillingService (to establish a connection with IAB service on GooglePlay)
	    mServiceConn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = IInAppBillingService.Stub.asInterface(service);
				
				//mHelper.enableDebugLogging(true);
				mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					public void onIabSetupFinished(IabResult result) {
						if (!result.isSuccess()) {
							//Log.d("IABHelper", "Problem setting up In-app Billing: " + result);
				            return;
						}
						
						 // 이 시점에 mHelper가 소거되었다면 (엑티비티 종료등) 바로 종료합니다.
				        if (mHelper == null)
				        	return;
				 
				        // IAB 셋업이 완료되었습니다.
				        //Log.d("IAB", "Setup successful. (type = " + type + ")");
				        
				        /*
				        String purchaseToken = "inapp:"+getPackageName()+":android.test.purchased";
				        try {
							mService.consumePurchase(3, getPackageName(),purchaseToken);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						*/
				        
				        if (type == 0) { // 앱을 처음 실행한 경우 (소진되지 않은 상품 확인용)
				        	purchaseForConsume = new ArrayList<Purchase>();
				        	consumedCnt = 0;
				        	mHelper.queryInventoryAsync(mGotInventoryListener);
				        }
				        else if (type == 1 || type == 2) // 구매 시도하는 경우 (일반구매, 선물하기)
				        {			        
				        	Buy();
				        }
					}
				});
			}
		};	
		
		// perform the binding (after that, we can use mService ref. to communicate with the Google Play service)
		bindService (new Intent("com.android.vending.billing.InAppBillingService.BIND"),
				mServiceConn, Context.BIND_AUTO_CREATE);
		
		// IAB helper (To set up synchronous communication with Google Play)
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		
    }	
    
    // 앱을 처음 실행할 때 IAB setup 완료 직후에 소진되지 않은 상품이 있는지 확인하는 부분의 callback.
    // 소진되지 않은 상품이 있다면 서버를 통해 verify를 한 후 소진하도록 하자. 
 	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
 	    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
 	        //Log.d("callback", "Query inventory finished.");
 	 
 	        // mHelper가 소거되었다면 종료
 	        if (mHelper == null) {
 	        	finish();
 	        	return;
 	        }
 	 
 	        // getPurchases()가 실패하였다면 종료
 	        if (result.isFailure()) {
 	        	finish();
 	            return;
 	        }
 	        
 	        ArrayList<String> skuList = new ArrayList<String>(); // 숫자 바꾸기
 	        skuList.add("topaz20");
 	        skuList.add("topaz55");
 	        skuList.add("topaz120");
 	        skuList.add("topaz390");
 	        skuList.add("topaz900");
 	        skuList.add("topaz20_p");
	        skuList.add("topaz55_p");
	        skuList.add("topaz120_p");
	        skuList.add("topaz390_p");
	        skuList.add("topaz900_p");
 	        
 	       	// 각 sku마다 검사 : 소진되지 않은 상품을 다시 verify해서 소진하자.
 	       	boolean flag = false;
 	       	for (int i = 0 ; i < skuList.size(); i++) {
 	       		Purchase p = inventory.getPurchase(skuList.get(i));
 	       		if (p != null) {
 	       			//Log.e("Query Inventory", skuList.get(i) + "소진 시도");
 	       			flag = true;
 	       			purchaseForConsume.add(p);
 	       			
 	       			int curTopazId = i+1;
 	       			if (curTopazId > 5)
 	       				curTopazId -= 5;
 	       			int purchaseType = (i < 5) ? 1 : 2; // 일반구매(1), 선물하기(2)
 	       			VerifyToServer(p, curTopazId, purchaseForConsume.size()-1, purchaseType);
 	       		}
 	       	}
 	       	if (!flag) {
 	       		//Log.e("Query Inventory", "소진할 상품이 없음");
 	       		finish();
 	       		startGame();
 	       	}
 	    }
 	};
	
	// 제품 구매 구글 결제 팝업창을 띄우는 함수
	public void Buy() {
		try {
			//Log.e("method", "Buy start (productId = " + productId + "), (kakaoId = " + kakaoId + "), (topazId = " + topazId + ")");
			
			// test
			//String sku = "android.test.purchased";
			//mHelper.launchPurchaseFlow(this, sku, 1001, mPurchaseFinishedListener, payload);
			
			mHelper.launchPurchaseFlow(this, productId, 1001, mPurchaseFinishedListener, payload);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
	    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
	        //Log.e("Purchase Result ", "result: " + result + ", purchase: " + purchase);

	        if (result.isFailure()) {
	        	//Log.e("Purchase Result ", "구매 실패...");
	        	finish();
	            return;
	        }
	        // mHelper 객체가 소거되었다면 종료
	        if (mHelper == null) {
	        	finish();
	        	return;
	        }
	        
	        VerifyToServer(purchase, topazId, -1, type);
	    }
	};	
	
	// 서버 검증
	// consumeIdx는 앱 처음 구동 시 consume할 때만 사용된다. 
	public void VerifyToServer(Purchase purchase, final int curTopazId, final int consumeIdx, final int purchaseType)
	{
		final String purchasedData = purchase.getOriginalJson();
        final String dataSignature = purchase.getSignature().replace("+",  "-");
        
        //Log.d("data", purchasedData);
        //Log.d("sign", dataSignature);
        //Log.d("topaz id", curTopazId+"");
        //Log.d("kakao id", kakaoId);
        //Log.d("friend kakao id", friendKakaoId);
        
        purchased = purchase; // 전역 변수 임시 저장

        String params = "";
        if (purchaseType == 1) // 일반구매 
    		params = VerifyGoogle(purchaseType, curTopazId, String.valueOf(kakaoId), "", purchasedData, dataSignature, consumeIdx);
    	else if (purchaseType == 2) 
    		params = VerifyGoogle(purchaseType, curTopazId, String.valueOf(kakaoId), friendKakaoId, purchasedData, dataSignature, consumeIdx);
        
        String[] p = params.split("#@!!@#");
        final String ps = p[0];
        final String a = p[1];
        final String urlString = p[2];
        
        //Log.e("PS", p[0]);
        //Log.e("a", p[1]);
        //Log.e("url", p[2]);
        
        Thread thread = new Thread() {
            @Override
            public void run() {
            	// http request & response
                HttpClient httpClient = new DefaultHttpClient();
            	try {
                    URI url = new URI(urlString);
                    HttpPost httpPost = new HttpPost();
                    httpPost.setURI(url);
                    
                    List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("PS", ps));
                    nameValuePairs.add(new BasicNameValuePair("a", a));
                    
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);
                    String res = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                    
                    int size = res.length();
                    //Log.e("response", res);
                    //Log.e("response", "size = " + size);
                    
                    // 여기서 response 복호화해야 한다!!!
                    String responseString = Deobfuscated(res);
                    //Log.e("response", responseString);
                    //Log.e("response", "size = " + responseString.length());
                    
                    // code가 0이 아니면 액티비티 종료. (그리고 cocos2d-x 에서 재부팅 팝업창 띄움)
                    int code = Integer.parseInt( responseString.split("<code>")[1].split("</code>")[0].trim() );
                    if (code != 0) {
                    	//Log.e("code error", "failed code = " + code + " , 결제 액티비티 종료함.");
                    	((Activity)mContext).finish();
                    }
                    else {
                    	// cocos2d-x에 response를 보내서 클라이언트 상에 상품이 지급되도록 하자.
                    	sendResultToCocos2dx(responseString, size, consumeIdx, friendKakaoId);
                    }
                    
            	} catch (URISyntaxException e) {
                    //Log.e("http thread", e.getLocalizedMessage());
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (ClientProtocolException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
            }
        };
        thread.start();

        return;
	}
	
	
	// 검증 결과의 code가 0이 아니면, 검증 단계에서 문제가 생겼으므로 재부팅 
	public static void PurchaseError()
	{
		//Log.e("purchase error", "결제 액티비티 종료함.");
    	((Activity)mContext).finish();
	}
	
	
	static Purchase purchased;
	
	// 검증이 성공적으로 되었다면, 소진한다.
	public static void Consume(final int consumeIdx)
	{
		//Log.e("CONSUME", "Type = " + type + " , consumeIdx = " + consumeIdx);
    	((Activity)mContext).runOnUiThread(new Runnable() {
    		public void run() {
    			if (type == 0)
    				mHelper.consumeAsync(purchaseForConsume.get(consumeIdx), mConsumeFinishedListener);
    			else
    				mHelper.consumeAsync(purchased, mConsumeFinishedListener);
    		}
    	});
	}
	
	static IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
	    public void onConsumeFinished(Purchase purchase, IabResult result) {
	        //Log.d("Consumption finished", "Purchase: " + purchase + ", result: " + result);
	 
	        // mHelper가 소거되었다면 종료
	        if (mHelper == null) {
	        	((Activity)mContext).finish();
	        	return;
	        }
	        
	        if (result.isSuccess()) {
	            //Log.e("성공", "Consumption successful. Provisioning.");
	            
	            int topazCount = -1;
				try {
					JSONObject jo = new JSONObject(purchase.getOriginalJson());
					topazCount = Integer.parseInt( jo.getString("productId").replace("topaz", "").replace("_p", "") );
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// cocos2d-x에서 에러 팝업창을 띄우지 않도록 한다. (정상처리 되었으니까)
				setErrorFlag(false);
				
	            Toast toast = Toast.makeText(mContext, "토파즈 " + topazCount + "개를 성공적으로 구매하였습니다!", Toast.LENGTH_SHORT);
		    	toast.show();
	        }
	        else {
	            //Log.e("실패", "소진 실패");
	        }
	        
	        // 최종 : 결제 액티비티를 종료하고 cocos2d-x 게임 화면으로 돌아간다.
	        if (type == 0) { // (Splash 화면에서 소진 끝나고 게임을 시작하는 경우)
	        	consumedCnt++;
	        	//Log.e("consumedCnt", consumedCnt + " , " + purchaseForConsume.size());
	        	if (consumedCnt >= purchaseForConsume.size()) {
	        		((Activity)mContext).finish();
	        		startGame();
	        	}
	        }
	        else
	        {
	        	((Activity)mContext).finish();
	        }
	    }
	};
	
	// 구매 '확인' 후, 결과를 받는 부분
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		//Log.d("onActivityResult", requestCode + "," + resultCode + "," + data);
		
		//Log.e("isBackBtnOk", isBackBtnOk+"");
		if (!isBackBtnOk)
			return;
	    
		if (mHelper == null) {
	    	finish();
	    	return;
	    }
	 
	    if (requestCode == 1001) {
	    	int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
		    //Log.d("onActivityResult", responseCode + "");
		    
		    if (responseCode != 0) {
		    	if (isBackBtnOk)
		    	{
		    		Toast toast = Toast.makeText(mContext, "구매가 취소되었습니다.", Toast.LENGTH_SHORT);
		    		toast.show();
		    		setErrorFlag(false);
		    		finish();
		    	}
		    }
		    else {
		    	//Log.e("결제", "이제 결제를 처리합니다. isBackBtnOk = false 로 바뀝니다.");
		    	isBackBtnOk = false;
		    	// 결과를 mHelper를 통해 처리합니다.
		    	if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
		    		// 처리할 결과물이 아닐경우 이곳으로 빠져 기본처리를 하도록 합니다.
		    		//Log.e("에러", "정상적 상황이 아닌 경우");
		    		setErrorFlag(false);
		    		finish();
		    		//super.onActivityResult(requestCode, resultCode, data);
		    	}
		    	else {
		    		//Log.d("onActivityResult", "onActivityResult handled by IABUtil.");
		    	}
		    }
	    }
	    else {
	    	Toast toast = Toast.makeText(mContext, "구매가 취소되었습니다.", Toast.LENGTH_SHORT);
	    	toast.show();
	    	setErrorFlag(false);
	    	finish();
	    }
	}

    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	//Log.e("method", "onDestory");
    	
    	if (mHelper != null)
    		mHelper.dispose();
    	mHelper = null;
    	
    	if (mService != null) // or mServiceConn?
    		unbindService(mServiceConn);
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
