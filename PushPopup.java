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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class PushPopup extends Activity {
	
    protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		  // 화면이 잠겨있을 때 보여주기
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
	        // 키잠금 해제하기
	        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
	        // 화면 켜기
	        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	    
        Bundle b = getIntent().getExtras();
        String title = b.getString("title");
        String msg = b.getString("msg");
         
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PushPopup.this);
        alertDialog.setIcon(R.drawable.icon);
         
        alertDialog.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PushWakeLock.releaseCpuLock();
                PushPopup.this.finish();
            }
        });
         
        alertDialog.setNegativeButton("보러가기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent().setClassName(getPackageName(), getPackageName()+".CocoMagic"));
                PushWakeLock.releaseCpuLock();
                PushPopup.this.finish();
            }
        });
         
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.show();
	}
    

}
