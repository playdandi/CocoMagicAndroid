package com.playDANDi.CocoMagic;

import android.app.Application;

public class Variables extends Application {
	private String regId;
	
	public String getRegistrationId() {
		return regId;
	}
	
	public void setRegistrationId(String id) {
		regId = id;
	}
}