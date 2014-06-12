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
	
	static int type; // 0 : �� �ʱ���� ��  ,  1 : ���Ÿ� �õ��� ���
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
				
				 // �� ������ mHelper�� �ҰŵǾ��ٸ� (��Ƽ��Ƽ �����) �ٷ� �����մϴ�.
		        if (mHelper == null)
		        	return;
		 
		        // IAB �¾��� �Ϸ�Ǿ����ϴ�.
		        //Log.d("IAB", "Setup successful. Querying inventory.");
		        
		        if (type == 0) // ���� ó�� ������ ��� (�������� ���� ��ǰ Ȯ�ο�)
		        	mHelper.queryInventoryAsync(mGotInventoryListener);
		        else if (type == 1) // ���� �õ��ϴ� ���
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
    
    // ���� ó�� ������ �� IAB setup �Ϸ� ���Ŀ� �������� ���� ��ǰ�� �ִ��� Ȯ���ϴ� �κ��� callback.
    // �������� ���� ��ǰ�� �ִٸ� ������ ���� verify�� �� �� �����ϵ��� ����. 
 	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
 	    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
 	        Log.d("callback", "Query inventory finished.");
 	 
 	        // mHelper�� �ҰŵǾ��ٸ� ����
 	        if (mHelper == null) {
 	        	finish();
 	        	return;
 	        }
 	 
 	        // getPurchases()�� �����Ͽ��ٸ� ����
 	        if (result.isFailure()) {
 	        	finish();
 	            return;
 	        }
 	        
 	        ArrayList<String> skuList = new ArrayList<String>(6); // ���� �ٲٱ�
 	        skuList.add("topaz20");
 	        skuList.add("topaz55");
 	        skuList.add("topaz120");
 	        skuList.add("topaz390");
 	       	skuList.add("topaz900");
 	       	skuList.add("topaz10"); // test
 	        
 	       	// �� sku���� �˻� : �������� ���� ��ǰ�� �ٽ� verify�ؼ� ��������.
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
					// ���⼭ tokens�� ��� ���� ���ֱ�
					mService.consumePurchase(3, getPackageName(), tokens[i]);
				}
			}

			// ��ū�� ��� ���������� ���� �޼��� ó��
			Buy();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	// ��ǰ ���� ���� ���� �˾�â�� ���� �Լ�
	public void Buy() {
		try {
			Log.e("method", "Buy start (product id = " + productId);
			
			//String sku = "android.test.purchased";
			//mHelper.launchPurchaseFlow(this, sku, 1001, mPurchaseFinishedListener, payload);
			
			mHelper.launchPurchaseFlow(this, productId, 1001, mPurchaseFinishedListener, payload);
			
			/*
			// ��ǰ(id: productId)�� ��� ���� ���� API ȣ�� 
			Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),	sku, "inapp", "developerpayload");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

			if (pendingIntent != null) {
				Log.e("content", "Buy Try");
				// ��ǰ(id: productId)�� �����ϰڳĴ� ���� ���� �˾�â�� ����.
				//startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
				mHelper.launchPurchaseFlow(this, getPackageName(), 1001,  mPurchaseFinishedListener, payload);				
			} else {
				Log.e("content", "���� ����...");
				// ������ �����ٸ�
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
        
		// cocos2d-x���� ���� ���� ���������� ������ ������.
        verifyPayloadAndProvideItem(purchasedData, dataSignature, topazCount);
 
        /*
        if (result.isFailure()) {
        	Log.e("Purchase Result ", "���� ����...");
        	finish();
            return;
        }
        // mHelper ��ü�� �ҰŵǾ��ٸ� ����
        if (mHelper == null) {
        	finish();
        	return;
        }
        */
	}
	
	
	static Purchase purchased;
	
	// ������ ���������� �Ǿ��ٸ�, �����Ѵ�.
	public static void Consume()
	{
		Log.e("CONSUME", "CONSUME");
		mHelper.consumeAsync(purchased, mConsumeFinishedListener);
	}
	
	static IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
	    public void onConsumeFinished(Purchase purchase, IabResult result) {
	        Log.d("Consumption finished", "Purchase: " + purchase + ", result: " + result);
	 
	        // mHelper�� �ҰŵǾ��ٸ� ����
	        if (mHelper == null) {
	        	((Activity)mContext).finish();
	        	return;
	        }
	        
	        if (result.isSuccess()) {
	            Log.e("����", "Consumption successful. Provisioning.");
	        }
	        else {
	            Log.e("����", "���� ����");
	        }
	        
	        ((Activity)mContext).finish();
	    }
	};
	
	// ���� 'Ȯ��' ��, ����� �޴� �κ�
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		Log.d("onActivityResult", requestCode + "," + resultCode + "," + data);
	    if (mHelper == null) {
	    	finish();
	    	return;
	    }
	 
	    if (requestCode == 1001) {
	    	// ����� mHelper�� ���� ó���մϴ�.
	    	if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
	    		// ó���� ������� �ƴҰ�� �̰����� ���� �⺻ó���� �ϵ��� �մϴ�.
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
