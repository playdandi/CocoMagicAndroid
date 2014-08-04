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

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log; 
 
public class PushWakeLock {     
    private static PowerManager.WakeLock sCpuWakeLock;    
    private static KeyguardManager.KeyguardLock mKeyguardLock;    
    private static boolean isScreenLock;     
     
    static void acquireCpuWakeLock(Context context) {        
        //Log.e("PushWakeLock", "Acquiring cpu wake lock");        
        //Log.e("PushWakeLock", "wake sCpuWakeLock = " + sCpuWakeLock);        
         
        if (sCpuWakeLock != null) {            
            return;        
        }         
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);         
        sCpuWakeLock = pm.newWakeLock(                
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |                
                PowerManager.ACQUIRE_CAUSES_WAKEUP |                
                PowerManager.ON_AFTER_RELEASE, "hello");        
         
        sCpuWakeLock.acquire();        
    }
     
    static void releaseCpuLock() {        
        //Log.e("PushWakeLock", "Releasing cpu wake lock");
        //Log.e("PushWakeLock", "relase sCpuWakeLock = " + sCpuWakeLock);
         
        if (sCpuWakeLock != null) {            
            sCpuWakeLock.release();            
            sCpuWakeLock = null;        
        }    
    }
}