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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Term extends Activity {
	
	private Activity act;
	private WebView mWebView;
	private String type;
 	
    @SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.term);
		
		act = this;
		mWebView = (WebView)findViewById(R.id.webview);
		
		mWebView.clearCache(true);
		
		
		Intent intent = getIntent();
		type = intent.getStringExtra("type");
		Log.e("type", type);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.setInitialScale(getScale());
		mWebView.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

		mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
            	mWebView.loadUrl("javascript:window.HtmlViewer.showHTML" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

		String url = "";
		if (type.compareTo("service") == 0)
			url = "http://14.63.200.217/cogma/game/update/rule_service.html";
		else 
			url = "http://14.63.200.217/cogma/game/update/rule_private.html";
		mWebView.loadUrl(url);

	}
    
    public int getScale()
    {
        Display display = ((WindowManager)act.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        int PIC_WIDTH = mWebView.getRight() - mWebView.getLeft();
        Double val = new Double(PIC_WIDTH) / new Double(width);
        val = val * 100d;
        return val.intValue();
    }
    
    public String DownloadHtml(final String addr) {
		StringBuilder html = new StringBuilder(); 
		try {
			URL url = new URL(addr);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			if (conn != null) {
				conn.setConnectTimeout(10000);
				conn.setUseCaches(false);
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					for (;;) {
						String line = br.readLine();
						if (line == null) break;
						html.append(line + '\n'); 
					}
					br.close();
				}
				conn.disconnect();
			}
			return html.toString();
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
		return "";
}
    
    class MyJavaScriptInterface {

    	private Context ctx;

    	MyJavaScriptInterface(Context ctx) {
    		this.ctx = ctx;
    	}

    	public void showHTML(String html) {
    		//new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html)
    		//        .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
    	}
    }
 
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) { 
            mWebView.goBack(); 
            return true;
        } 
        return super.onKeyDown(keyCode, event);
    }
}
