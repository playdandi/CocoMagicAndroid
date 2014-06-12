package com.playDANDi.CocoMagic;

import java.util.ArrayList;

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
	
	static int type; // 0 : 앱 초기실행 시  ,  1 : 구매를 시도할 경우
	static String productId;
	static String payload;	 
	
	public native void verifyPayloadAndProvideItem(String data, String signature, int topazCount);
	 
	// Binding to IInAppBillingService (to establish a connection with IAB service on GooglePlay)
    ServiceConnection mServiceConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
		}
	};	
	
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.e("method", "onCreate start");
		
		mContext = this;
		parentActivity = (CocoMagic)getParent();
		
		Intent intent = getIntent();
		type = intent.getIntExtra("type", 1);
		productId = intent.getStringExtra("productId");
		payload = intent.getStringExtra("payload");
		
		
		// perform the binding (after that, we can use mService ref. to communicate with the Google Play service)
		bindService (new Intent("com.android.vending.billing.InAppBillingService.BIND"),
				mServiceConn, Context.BIND_AUTO_CREATE);
		
		// IAB helper (To set up synchronous communication with Google Play)
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp/wCUpIlBnW7HvDklSPEDBrD5U9Ubh92+oocpgigRDEBcUdvFdCL63ctaF1F45kYwXOC1OYGqp184LHNNKcO7S+qMd4jAeortnQgGcIzTCTxBSeu5xWpFiz6nM3IO+X51LHW57ou8pLhGbJ17HXP8SGWUUpV25KL1/4c7wunTUYcW7MYwIvd2GZSsdWxBuB9a2AgJwbWs5FfgJrxPeTq1wlAMoQABl5j8r/zYqqy/7edASjWcQELBXDf9jhWgEwvqcK/USqJFYBCA2gkFle3SwK+1Doy5/D5RzR+kpk8Tpx4z09UprXnzHjnFCZJQNVayABPZGpej4XIAC5nFM5TKwIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		//mHelper.enableDebugLogging(true);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					Log.d("IABHelper", "Problem setting up In-app Billing: " + result);
		            return;
				}
				
				 // 이 시점에 mHelper가 소거되었다면 (엑티비티 종료등) 바로 종료합니다.
		        if (mHelper == null)
		        	return;
		 
		        // IAB 셋업이 완료되었습니다.
		        //Log.d("IAB", "Setup successful. Querying inventory.");
		        
		        if (type == 0) // 앱을 처음 실행한 경우 (소진되지 않은 상품 확인용)
		        	mHelper.queryInventoryAsync(mGotInventoryListener);
		        else if (type == 1) // 구매 시도하는 경우
		        {
		        	String purchaseToken = "inapp:"+getPackageName()+":android.test.purchased";
			        try {
						mService.consumePurchase(3, getPackageName(),purchaseToken);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	Buy();
		        }
			}
		});
    }	
    
    // 앱을 처음 실행할 때 IAB setup 완료 직후에 소진되지 않은 상품이 있는지 확인하는 부분의 callback.
    // 소진되지 않은 상품이 있다면 서버를 통해 verify를 한 후 소진하도록 하자. 
 	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
 	    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
 	        Log.d("callback", "Query inventory finished.");
 	 
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
 	        
 	        ArrayList<String> skuList = new ArrayList<String>(6); // 숫자 바꾸기
 	        skuList.add("topaz20");
 	        skuList.add("topaz55");
 	        skuList.add("topaz120");
 	        skuList.add("topaz390");
 	       	skuList.add("topaz900");
 	       	skuList.add("topaz10"); // test
 	        
 	       	// 각 sku마다 검사 : 소진되지 않은 상품을 다시 verify해서 소진하자.
 	       	boolean flag = false;
 	       	for (int i = 0 ; i < skuList.size(); i++) {
 	       		Purchase p = inventory.getPurchase(skuList.get(i));
 	       		if (p != null) {
 	       			flag = true;
 	       			VerifyToServer(p);
 	       		}
 	       	}
 	       	if (!flag)
 	       		finish();
 	    }
 	};
	
    /*
	public void AlreadyPurchasedItems() {
		try {
			Log.e("method", "AlreadyPurchaseItems start");
			Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
			int response = ownedItems.getInt("RESPONSE_CODE");
			if (response == 0) {
				ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
				String[] tokens = new String[purchaseDataList.size()];
				for (int i = 0; i < purchaseDataList.size(); ++i) {
					String purchaseData = (String) purchaseDataList.get(i);
					JSONObject jo = new JSONObject(purchaseData);
					tokens[i] = jo.getString("purchaseToken");
					// 여기서 tokens를 모두 컨슘 해주기
					mService.consumePurchase(3, getPackageName(), tokens[i]);
				}
			}

			// 토큰을 모두 컨슘했으니 구매 메서드 처리
			Buy();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	// 제품 구매 구글 결제 팝업창을 띄우는 함수
	public void Buy() {
		try {
			Log.e("method", "Buy start (product id = " + productId);
			
			//String sku = "android.test.purchased";
			//mHelper.launchPurchaseFlow(this, sku, 1001, mPurchaseFinishedListener, payload);
			
			mHelper.launchPurchaseFlow(this, productId, 1001, mPurchaseFinishedListener, payload);
			
			/*
			// 제품(id: productId)을 사기 위해 구글 API 호출 
			Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),	sku, "inapp", "developerpayload");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

			if (pendingIntent != null) {
				Log.e("content", "Buy Try");
				// 제품(id: productId)을 구매하겠냐는 구글 결제 팝업창을 띄운다.
				//startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
				mHelper.launchPurchaseFlow(this, getPackageName(), 1001,  mPurchaseFinishedListener, payload);				
			} else {
				Log.e("content", "결제 막힘...");
				// 결제가 막혔다면
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
	    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
	        Log.e("Purchase Result ", "result: " + result + ", purchase: " + purchase);
	        
	        VerifyToServer(purchase);
	    }
	};	
	
	
	public void VerifyToServer(Purchase purchase)
	{
		String purchasedData = purchase.getOriginalJson();
        String dataSignature = purchase.getSignature();
        dataSignature = dataSignature.replace("+", "-");
        
        Log.d("data", purchasedData);
        Log.d("sign", dataSignature);
        
        int topazCount = -1;
		try {
			JSONObject jo = new JSONObject(purchasedData);
			String productId = jo.getString("productId");
			//productId = "topaz390";
			productId = productId.replace("topaz", "");
			topazCount = Integer.parseInt(productId);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
        purchased = purchase;
        
		// cocos2d-x에서 구매 검증 프로토콜을 서버로 보낸다.
        verifyPayloadAndProvideItem(purchasedData, dataSignature, topazCount);
 
        /*
        if (result.isFailure()) {
        	Log.e("Purchase Result ", "구매 실패...");
        	finish();
            return;
        }
        // mHelper 객체가 소거되었다면 종료
        if (mHelper == null) {
        	finish();
        	return;
        }
        */
	}
	
	
	static Purchase purchased;
	
	// 검증이 성공적으로 되었다면, 소진한다.
	public static void Consume()
	{
		Log.e("CONSUME", "CONSUME");
		mHelper.consumeAsync(purchased, mConsumeFinishedListener);
	}
	
	static IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
	    public void onConsumeFinished(Purchase purchase, IabResult result) {
	        Log.d("Consumption finished", "Purchase: " + purchase + ", result: " + result);
	 
	        // mHelper가 소거되었다면 종료
	        if (mHelper == null) {
	        	((Activity)mContext).finish();
	        	return;
	        }
	        
	        if (result.isSuccess()) {
	            Log.e("성공", "Consumption successful. Provisioning.");
	        }
	        else {
	            Log.e("실패", "소진 실패");
	        }
	        
	        ((Activity)mContext).finish();
	    }
	};
	
	// 구매 '확인' 후, 결과를 받는 부분
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		Log.d("onActivityResult", requestCode + "," + resultCode + "," + data);
	    if (mHelper == null) {
	    	finish();
	    	return;
	    }
	 
	    if (requestCode == 1001) {
	    	// 결과를 mHelper를 통해 처리합니다.
	    	if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
	    		// 처리할 결과물이 아닐경우 이곳으로 빠져 기본처리를 하도록 합니다.
	    		super.onActivityResult(requestCode, resultCode, data);
	    	}
	    	else {
	    		Log.d("onActivityResult", "onActivityResult handled by IABUtil.");
	    	}
	    }
	}

    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	Log.e("method", "onDestory");
    	
    	if (mHelper != null)
    		mHelper.dispose();
    	mHelper = null;
    	
    	if (mService != null) // or mServiceConn?
    		unbindService(mServiceConn);
    	
    }
}
