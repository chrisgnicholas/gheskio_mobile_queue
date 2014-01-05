package org.gheskio.queue;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class SimpleAuth extends Authenticator {

	public SimpleAuth() {
		// TODO Auto-generated constructor stub
	}
	
	 // Called when password authorization is needed
	protected PasswordAuthentication getPasswordAuthentication() {
	
		String userVal = MainActivity.sharedPref.getString("USERVAL", "GHESKIO");
		String passwdVal = MainActivity.sharedPref.getString("UPLOAD_PW", "stop_HIV_now");
		
		return new PasswordAuthentication(userVal, passwdVal.toCharArray());
	}

}
