package com.playDANDi.CocoMagic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
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

import android.R.id;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.playDANDi.CocoMagic.util.IabHelper;
import com.playDANDi.CocoMagic.util.IabResult;

public class InAppBilling extends Activity {
	
	IInAppBillingService mService;
	IabHelper mHelper;
	Context mContext;
	
	 static final String SKU_GAS = "gas";
	 
	 
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
		
		mContext = this;
		Log.e("method", "onCreate start");
		
		// perform the binding (after that, we can use mService ref. to communicate with the Google Play service)
		bindService (new Intent("com.android.vending.billing.InAppBillingService.BIND"),
				mServiceConn, Context.BIND_AUTO_CREATE);
		
		// IAB helper (To set up synchronous communication with Google Play)
		// compute your public key and store it in base64EncodedPublicKey
		//String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsHSzHv+vN6mLeMDIWGTCvs+FBIBBsftTQf0obqWhKr6rnCcXcb5p6Q7ECP86FARW2uPpb0JJRuijTgM8c2wrFb8qiuxCo4gqEyUeriNfYsDD6SNagmbdIqlcq+zkKnKRW5REqOMDnpuUdT06QtCUV3D4pArhRwzqMp4XdKn/oA/jx7tuELNMaiyw+0UGBhuXTrhovNwJOyBWVebX9nFbzjfqrMe4FKWChQUsDMIoP9X7FXalHsASz7mUJm4yNOrV5kCuHR8vzroQ1XXuYkwxY6FPxaJkR81pQzUhNScky5lRCLNHM0YxyLpIWmZu3Qy/hLyT5I6I7jRLx+dLO0LqTwIDAQAB";
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp/wCUpIlBnW7HvDklSPEDBrD5U9Ubh92+oocpgigRDEBcUdvFdCL63ctaF1F45kYwXOC1OYGqp184LHNNKcO7S+qMd4jAeortnQgGcIzTCTxBSeu5xWpFiz6nM3IO+X51LHW57ou8pLhGbJ17HXP8SGWUUpV25KL1/4c7wunTUYcW7MYwIvd2GZSsdWxBuB9a2AgJwbWs5FfgJrxPeTq1wlAMoQABl5j8r/zYqqy/7edASjWcQELBXDf9jhWgEwvqcK/USqJFYBCA2gkFle3SwK+1Doy5/D5RzR+kpk8Tpx4z09UprXnzHjnFCZJQNVayABPZGpej4XIAC5nFM5TKwIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		//mHelper.enableDebugLogging(true);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					Log.d("IABHelper", "Problem setting up In-app Billing: " + result);
					// Toast�� AlertDialog�����.
					//Toast toast = Toast.makeText(mContext, "IABHelper ���� ����.", Toast.LENGTH_SHORT);
		            //toast.show();
		            return;
				}
				
				 // �� ������ mHelper�� �ҰŵǾ��ٸ� (��Ƽ��Ƽ �����) �ٷ� �����մϴ�.
		        if (mHelper == null)
		        	return;
		 
		        // IAB �¾��� �Ϸ�Ǿ����ϴ�.
		        Log.d("IAB", "Setup successful. Querying inventory.");
		        // mHelper.queryInventoryAsync(mGotInventoryListener);
	            
				// AlreadyPurchaseItems(); �޼���� ���Ÿ���� �ʱ�ȭ�ϴ� �޼����Դϴ�.
				// v3���� �Ѿ���鼭 ���ű���� ��� ���� �Ǵµ� �籸�� ������ ��ǰ( ���ӿ����� ���ΰ���������) ������ �������־�� �մϴ�.
				// �� �޼���� ��ǰ ������ Ȥ�� �Ŀ� �ݵ�� ȣ���ؾ��մϴ�. ( �籸�Ű� �Ұ����� 1ȸ�� �������ǰ�� ȣ���ϸ� �ȵ˴ϴ� )
				// AlreadyPurchasedItems();
		        
		        String purchaseToken = "inapp:"+getPackageName()+":android.test.purchased";
		        try {
					int res = mService.consumePurchase(3, getPackageName(),purchaseToken);
					Log.e("consume", res+"");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        Buy();
			}
		});
    }	
    
    /*
    // ���� ��û ���� ���� callback �κ�
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			
			if (result.isFailure()) {
				// ���� ���� �̺�Ʈ ó��
				finish();
				return;
			}
			
			// ���� ����!
			
			// ���⼭ ������ �߰� ���ֽø� �˴ϴ�.
			// ���� ������ ������ üũ�Ŀ� ������ �߰��Ѵٸ�,
			// ������ purchase.getOriginalJson(), purchase.getSignature() 2�� �����ø� �˴ϴ�.
			
			finish();
		}
	};
	*/
    
	/*
    // query inventory callback �κ�
 	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
 	    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
 	        Log.d("callback", "Query inventory finished.");
 	 
 	        // mHelper�� �ҰŵǾ��ٸ� ����
 	        if (mHelper == null)
 	        	return;
 	 
 	        // getPurchases()�� �����Ͽ��ٸ� ����
 	        if (result.isFailure()) {
 	            //complain("Failed to query inventory: " + result);
 	            return;
 	        }
 	        
 	        


 	        Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
 	        if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
 	            Log.d(TAG, "We have gas. Consuming it.");
 	            mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
 	            return;
 	        }
 	 
 	        //updateUi();
 	        //setWaitScreen(false);
 	        //Log.d(TAG, "Initial inventory query finished; enabling main UI.");
 	    }
 	};
    */
    /*
    // Querying for Items Available for Purchase
    // ���� ������ ��� ��ǰ ����� get�ϴ� �Լ�.
    public void GetAvailablePurchaseItems()
    {
    	ArrayList<String> skuList = new ArrayList<String> ();
    	skuList.add("premiumUpgrade");
    	skuList.add("gas");
    	Bundle querySkus = new Bundle();
    	querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
    	
    	try {
			Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
			int response = skuDetails.getInt("RESPONSE_CODE");
			if (response == 0) {
			   ArrayList<String> responseList
			      = skuDetails.getStringArrayList("DETAILS_LIST");
			   
			   for (String thisResponse : responseList) {
			      JSONObject object = new JSONObject(thisResponse);
			      String sku = object.getString("productId");
			      String price = object.getString("price");
			      if (sku.equals("premiumUpgrade")) mPremiumUpgradePrice = price;
			      else if (sku.equals("gas")) mGasPrice = price;
			   }
			}
		}
    	catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    */
 	
 	/*
 	 * 
 final ArrayList<String> skus = new ArrayList<String>();
   skus.add("10");
   skus.add("30");
   skus.add("50");
   skus.add("75");
   skus.add("100");
   skus.add("500");
   skus.add("1000");
   skus.add("1500");
   skus.add("2000");
   mHelper.queryInventoryAsync(true, skus, this);
 	 */
	
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
	
	
	//public void Buy(String sku) {
	public void Buy() {
		try {
			Log.e("method", "Buy start");
			String sku = "topaz10";
			// API version (3) 
			Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),	sku, "inapp", "developerpayload");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

			if (pendingIntent != null) {
				Log.e("content", "Buy Try");
				startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
				//mHelper.launchPurchaseFlow(this, getPackageName(), 1001,  mPurchaseFinishedListener, "developerpayload");
				// ���� ���� ����ȣ���� 2������ �ִµ� �������� ����ϸ� ����� onActivityResult �޼���� ����, 
				// �ؿ����� ����ϸ� OnIabPurchaseFinishedListener �޼���� ���ϴ�.  (�����ϼ���!)
			} else {
				Log.e("content", "���� ����...");
				// ������ �����ٸ�
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
	   if (requestCode == 1001) {           
		   
		  Log.d("onActivityResult", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
	      int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
	      final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
	      final String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
	      
	      Log.e("haha", "haha + " + dataSignature + "haha + ");
	        
	      if (resultCode == RESULT_OK && responseCode == 0) {
	         try {
	        	Log.e("onActivityResult", "���� ����");
	            JSONObject jo = new JSONObject(purchaseData);
	            String sku = jo.getString("productId");
	            int state = jo.getInt("purchaseState");
	            String token = jo.getString("purchaseToken");
	            mService.consumePurchase(3, getPackageName(), token);
	            Log.d("purchase state", state+"");
	            Log.d("onActivityResult", purchaseData);
	  	      	Log.d("onActivityResult", dataSignature + "");
	            //if (state == 0)
	            //{
	            
	  	    
	            Thread thread = new Thread() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        String urlString = "http://14.63.225.203/cogma/game/verify_google.php?";
                        try {
                            URI url = new URI(urlString);

                            HttpPost httpPost = new HttpPost();
                            httpPost.setURI(url);

                            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                            nameValuePairs.add(new BasicNameValuePair("id", "userId"));
                            nameValuePairs.add(new BasicNameValuePair("data", purchaseData));
                            nameValuePairs.add(new BasicNameValuePair("sign", dataSignature));

                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                            HttpResponse response = httpClient.execute(httpPost);
                            String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                            Log.d("", responseString);

                        } catch (URISyntaxException e) {
                            Log.e("", e.getLocalizedMessage());
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            Log.e("", e.getLocalizedMessage());
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.e("", e.getLocalizedMessage());
                            e.printStackTrace();
                        }

                    }
                };

                thread.start();
               
	            Toast toast = Toast.makeText(this, sku + " �� �����Ͽ����ϴ�!", Toast.LENGTH_SHORT);
	            toast.show();
	          }
	          catch (JSONException e) {
	        	  Log.e("onActivityResult", "���� ����");
	        	  Toast toast = Toast.makeText(this, "���� ���� ...", Toast.LENGTH_SHORT);
	        	  toast.show();
	        	  e.printStackTrace();
	          } catch (RemoteException e) {
	        	  Log.e("onActivityResult", "consume ���� ");
	        	  e.printStackTrace();
			}
	      }
	      else {
	    	  Log.e("onActivityResult", "���� ����");
	      }
	   }
	   
	   finish();
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
